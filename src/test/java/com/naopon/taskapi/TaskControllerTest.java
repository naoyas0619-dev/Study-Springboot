package com.naopon.taskapi;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.naopon.taskapi.auth.LoginRequest;
import com.naopon.taskapi.auth.RefreshTokenRequest;
import com.naopon.taskapi.dto.AdminUserCreateRequest;
import com.naopon.taskapi.dto.AdminUserUpdateRequest;
import com.naopon.taskapi.dto.TaskRequest;
import com.naopon.taskapi.model.AppRole;
import com.naopon.taskapi.model.AppUser;
import com.naopon.taskapi.model.Task;
import com.naopon.taskapi.repository.AppUserRepository;
import com.naopon.taskapi.repository.RefreshTokenRepository;
import com.naopon.taskapi.repository.TaskRepository;
import com.naopon.taskapi.security.LoginRateLimitService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class TaskControllerTest {

    private static final String TEST_USERNAME = "test-user";
    private static final String TEST_PASSWORD = "test-password-123";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TaskRepository repository;

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private LoginRateLimitService loginRateLimitService;

    @BeforeEach
    void setUp() {
        repository.deleteAll();
        refreshTokenRepository.deleteAll();
        loginRateLimitService.clearAll();
        appUserRepository.findAll().stream()
                .filter(user -> !TEST_USERNAME.equals(user.getUsername()))
                .forEach(appUserRepository::delete);
        AppUser user = appUserRepository.findByUsername(TEST_USERNAME)
                .orElseThrow();
        user.setEnabled(true);
        user.setRole(AppRole.ADMIN);
        user.setTokenVersion(0);
        appUserRepository.save(user);
    }

    @Test
    void loginReturnsJwtToken() throws Exception {
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                .content(loginRequestBody(TEST_USERNAME, TEST_PASSWORD)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isString())
                .andExpect(jsonPath("$.refreshToken").isString())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.accessExpiresInSeconds").value(900))
                .andExpect(jsonPath("$.refreshExpiresInSeconds").value(604800))
                .andExpect(jsonPath("$.role").value("ADMIN"));
    }

    @Test
    void loginWithInvalidCredentialsReturnsUnauthorized() throws Exception {
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequestBody(TEST_USERNAME, "wrong-password")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.message").value("Invalid username or password"))
                .andExpect(jsonPath("$.path").value("/auth/login"));
    }

    @Test
    void loginRateLimitReturnsTooManyRequestsAfterRepeatedFailures() throws Exception {
        for (int i = 0; i < 5; i++) {
            mockMvc.perform(post("/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(loginRequestBody(TEST_USERNAME, "wrong-password")))
                    .andExpect(status().isUnauthorized());
        }

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequestBody(TEST_USERNAME, "wrong-password")))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.status").value(429))
                .andExpect(jsonPath("$.error").value("Too Many Requests"))
                .andExpect(jsonPath("$.message").value("Too many authentication attempts. Try again later."))
                .andExpect(jsonPath("$.path").value("/auth/login"));
    }

    @Test
    void getAllWithoutJwtReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/tasks"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.message").value("Authentication required"))
                .andExpect(jsonPath("$.path").value("/tasks"));
    }

    @Test
    void getAllWithInvalidJwtReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/tasks")
                        .header("Authorization", "Bearer invalid-token"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.message").value("Invalid or expired token"))
                .andExpect(jsonPath("$.path").value("/tasks"));
    }

    @Test
    void healthEndpointIsPublic() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    void studyGuideRedirectEndpointIsPublic() throws Exception {
        mockMvc.perform(get("/study-guide"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/study-guide.html"));
    }

    @Test
    void studyGuideStaticHtmlIsPublic() throws Exception {
        mockMvc.perform(get("/study-guide.html"))
                .andExpect(status().isOk());
    }

    @Test
    void w04GuideStaticHtmlIsPublic() throws Exception {
        mockMvc.perform(get("/w04-jwt-auth-guide.html"))
                .andExpect(status().isOk());
    }

    @Test
    void refreshReturnsNewTokenPair() throws Exception {
        String refreshToken = refreshToken();

        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(refreshRequestBody(refreshToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isString())
                .andExpect(jsonPath("$.refreshToken").isString())
                .andExpect(jsonPath("$.role").value("ADMIN"));
    }

    @Test
    void usedRefreshTokenCannotBeReused() throws Exception {
        String refreshToken = refreshToken();

        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(refreshRequestBody(refreshToken)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(refreshRequestBody(refreshToken)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.message").value("Invalid or expired refresh token"))
                .andExpect(jsonPath("$.path").value("/auth/refresh"));
    }

    @Test
    void logoutRevokesCurrentAccessAndRefreshTokens() throws Exception {
        String accessToken = bearerToken();
        String refreshToken = refreshTokenFromLogin();

        mockMvc.perform(post("/auth/logout")
                        .header("Authorization", accessToken))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/tasks")
                        .header("Authorization", accessToken))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid or expired token"));

        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(refreshRequestBody(refreshToken)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid or expired refresh token"));
    }

    @Test
    void disabledUserCannotUseExistingAccessToken() throws Exception {
        String accessToken = bearerToken();
        disableTestUser();

        mockMvc.perform(get("/tasks")
                        .header("Authorization", accessToken))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid or expired token"));
    }

    @Test
    void disabledUserCannotRefreshToken() throws Exception {
        String refreshToken = refreshToken();
        disableTestUser();

        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(refreshRequestBody(refreshToken)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid or expired refresh token"));
    }

    @Test
    void adminCanCreateUserAndUserCanLogin() throws Exception {
        mockMvc.perform(post("/admin/users")
                        .header("Authorization", bearerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(adminUserCreateRequestBody("member-user", "member-password", AppRole.USER)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("member-user"))
                .andExpect(jsonPath("$.role").value("USER"))
                .andExpect(jsonPath("$.enabled").value(true));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequestBody("member-user", "member-password")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("USER"));
    }

    @Test
    void duplicateUsernameReturnsConflict() throws Exception {
        mockMvc.perform(post("/admin/users")
                        .header("Authorization", bearerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(adminUserCreateRequestBody(TEST_USERNAME, "another-password", AppRole.USER)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value("username already exists"))
                .andExpect(jsonPath("$.path").value("/admin/users"));
    }

    @Test
    void nonAdminCannotAccessAdminUserManagementApi() throws Exception {
        createUserAsAdmin("member-user", "member-password", AppRole.USER);
        String userToken = bearerToken("member-user", "member-password");

        mockMvc.perform(get("/admin/users")
                        .header("Authorization", userToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("Forbidden"))
                .andExpect(jsonPath("$.message").value("Access denied"))
                .andExpect(jsonPath("$.path").value("/admin/users"));
    }

    @Test
    void adminCanDisableUserAndRevokeTokens() throws Exception {
        createUserAsAdmin("member-user", "member-password", AppRole.USER);
        String userAccessToken = bearerToken("member-user", "member-password");
        String userRefreshToken = refreshToken("member-user", "member-password");
        Long userId = appUserRepository.findByUsername("member-user").orElseThrow().getId();

        mockMvc.perform(patch("/admin/users/{id}", userId)
                        .header("Authorization", bearerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(adminUserUpdateRequestBody(null, false, null)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.enabled").value(false));

        mockMvc.perform(get("/tasks")
                        .header("Authorization", userAccessToken))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid or expired token"));

        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(refreshRequestBody(userRefreshToken)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid or expired refresh token"));
    }

    @Test
    void createReturnsCreatedTask() throws Exception {
        TaskRequest request = new TaskRequest();
        request.setTitle("write tests");

        MvcResult result = mockMvc.perform(post("/tasks")
                        .header("Authorization", bearerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.title").value("write tests"))
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        Long id = objectMapper.readTree(responseBody).get("id").asLong();
        String location = result.getResponse().getHeader("Location");

        Assertions.assertNotNull(location);
        Assertions.assertTrue(location.endsWith("/tasks/" + id));
    }

    @Test
    void createWithBlankTitleReturnsBadRequest() throws Exception {
        TaskRequest request = new TaskRequest();
        request.setTitle(" ");

        mockMvc.perform(post("/tasks")
                        .header("Authorization", bearerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("title must not be blank"))
                .andExpect(jsonPath("$.path").value("/tasks"));
    }

    @Test
    void createWithMalformedJsonReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/tasks")
                        .header("Authorization", bearerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Malformed JSON request"))
                .andExpect(jsonPath("$.path").value("/tasks"));
    }

    @Test
    void getByIdReturnsTask() throws Exception {
        Task task = saveTask("read docs");

        mockMvc.perform(get("/tasks/{id}", task.getId())
                        .header("Authorization", bearerToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(task.getId()))
                .andExpect(jsonPath("$.title").value("read docs"));
    }

    @Test
    void getByIdWithMissingIdReturnsNotFound() throws Exception {
        mockMvc.perform(get("/tasks/{id}", 9999L)
                        .header("Authorization", bearerToken()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("task not found"))
                .andExpect(jsonPath("$.path").value("/tasks/9999"));
    }

    @Test
    void getByIdWithInvalidIdReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/tasks/{id}", "abc")
                        .header("Authorization", bearerToken()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Invalid value for parameter 'id'"))
                .andExpect(jsonPath("$.path").value("/tasks/abc"));
    }

    @Test
    void getAllReturnsPagedTasks() throws Exception {
        saveTask("task 1");
        saveTask("task 2");
        saveTask("task 3");

        mockMvc.perform(get("/tasks")
                        .header("Authorization", bearerToken())
                        .param("page", "0")
                        .param("size", "2")
                        .param("sort", "id,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.totalPages").value(2));
    }

    @Test
    void searchReturnsMatchingTasks() throws Exception {
        saveTask("alpha");
        saveTask("alphabet");
        saveTask("beta");

        mockMvc.perform(get("/tasks")
                        .header("Authorization", bearerToken())
                        .param("title", "alpha")
                        .param("sort", "id,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.content[0].title").value("alpha"))
                .andExpect(jsonPath("$.content[1].title").value("alphabet"));
    }

    @Test
    void updateReturnsUpdatedTask() throws Exception {
        Task task = saveTask("before");
        TaskRequest request = new TaskRequest();
        request.setTitle("after");

        mockMvc.perform(put("/tasks/{id}", task.getId())
                        .header("Authorization", bearerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(task.getId()))
                .andExpect(jsonPath("$.title").value("after"));
    }

    @Test
    void updateWithBlankTitleReturnsBadRequest() throws Exception {
        Task task = saveTask("before");
        TaskRequest request = new TaskRequest();
        request.setTitle("");

        mockMvc.perform(put("/tasks/{id}", task.getId())
                        .header("Authorization", bearerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("title must not be blank"))
                .andExpect(jsonPath("$.path").value("/tasks/" + task.getId()));
    }

    @Test
    void updateWithMissingIdReturnsNotFound() throws Exception {
        TaskRequest request = new TaskRequest();
        request.setTitle("after");

        mockMvc.perform(put("/tasks/{id}", 9999L)
                        .header("Authorization", bearerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("task not found"))
                .andExpect(jsonPath("$.path").value("/tasks/9999"));
    }

    @Test
    void updateWithMalformedJsonReturnsBadRequest() throws Exception {
        Task task = saveTask("before");

        mockMvc.perform(put("/tasks/{id}", task.getId())
                        .header("Authorization", bearerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Malformed JSON request"))
                .andExpect(jsonPath("$.path").value("/tasks/" + task.getId()));
    }

    @Test
    void deleteReturnsNoContent() throws Exception {
        Task task = saveTask("cleanup");

        mockMvc.perform(delete("/tasks/{id}", task.getId())
                        .header("Authorization", bearerToken()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/tasks/{id}", task.getId())
                        .header("Authorization", bearerToken()))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteWithMissingIdReturnsNotFound() throws Exception {
        mockMvc.perform(delete("/tasks/{id}", 9999L)
                        .header("Authorization", bearerToken()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("task not found"))
                .andExpect(jsonPath("$.path").value("/tasks/9999"));
    }

    private Task saveTask(String title) {
        return repository.save(new Task(null, title));
    }

    private String bearerToken() throws Exception {
        return "Bearer " + loginResponseBody().get("accessToken").asText();
    }

    private String bearerToken(String username, String password) throws Exception {
        return "Bearer " + loginResponseBody(username, password).get("accessToken").asText();
    }

    private String loginRequestBody(String username, String password) throws Exception {
        LoginRequest request = new LoginRequest();
        request.setUsername(username);
        request.setPassword(password);
        return objectMapper.writeValueAsString(request);
    }

    private String refreshToken() throws Exception {
        return loginResponseBody().get("refreshToken").asText();
    }

    private String refreshToken(String username, String password) throws Exception {
        return loginResponseBody(username, password).get("refreshToken").asText();
    }

    private String refreshTokenFromLogin() throws Exception {
        return refreshToken();
    }

    private String refreshRequestBody(String refreshToken) throws Exception {
        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken(refreshToken);
        return objectMapper.writeValueAsString(request);
    }

    private com.fasterxml.jackson.databind.JsonNode loginResponseBody() throws Exception {
        return loginResponseBody(TEST_USERNAME, TEST_PASSWORD);
    }

    private com.fasterxml.jackson.databind.JsonNode loginResponseBody(String username, String password) throws Exception {
        String responseBody = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequestBody(username, password)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readTree(responseBody);
    }

    private String adminUserCreateRequestBody(String username, String password, AppRole role) throws Exception {
        AdminUserCreateRequest request = new AdminUserCreateRequest();
        request.setUsername(username);
        request.setPassword(password);
        request.setRole(role);
        return objectMapper.writeValueAsString(request);
    }

    private String adminUserUpdateRequestBody(AppRole role, Boolean enabled, String password) throws Exception {
        AdminUserUpdateRequest request = new AdminUserUpdateRequest();
        request.setRole(role);
        request.setEnabled(enabled);
        request.setPassword(password);
        return objectMapper.writeValueAsString(request);
    }

    private void disableTestUser() {
        AppUser user = appUserRepository.findByUsername(TEST_USERNAME)
                .orElseThrow();
        user.setEnabled(false);
        appUserRepository.save(user);
    }

    private void createUserAsAdmin(String username, String password, AppRole role) throws Exception {
        mockMvc.perform(post("/admin/users")
                        .header("Authorization", bearerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(adminUserCreateRequestBody(username, password, role)))
                .andExpect(status().isCreated());
    }
}

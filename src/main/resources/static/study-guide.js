document.addEventListener("DOMContentLoaded", () => {
    const tabs = Array.from(document.querySelectorAll(".api-tab"));
    const panels = Array.from(document.querySelectorAll(".api-panel"));

    const activatePanel = (target) => {
        tabs.forEach((tab) => {
            const active = tab.dataset.apiTarget === target;
            tab.classList.toggle("is-active", active);
            tab.setAttribute("aria-pressed", String(active));
        });

        panels.forEach((panel) => {
            const active = panel.dataset.apiPanel === target;
            panel.classList.toggle("is-active", active);
        });
    };

    tabs.forEach((tab) => {
        tab.addEventListener("click", () => activatePanel(tab.dataset.apiTarget));
    });
});

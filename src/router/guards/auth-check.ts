import type { Router } from "vue-router";

export function setupAuthCheckGuard(router: Router) {
  router.beforeEach((to, from, next) => {
    if (to.name === "Setup" || to.name === "Login") {
      next();
      return;
    }
    if (localStorage.getItem("logged_in") !== "true") {
      next({ name: "Login" });
      return;
    }
    next();
  });
}

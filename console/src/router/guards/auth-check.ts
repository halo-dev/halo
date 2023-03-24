import { useUserStore } from "@/stores/user";
import type { Router } from "vue-router";

export function setupAuthCheckGuard(router: Router) {
  router.beforeEach((to, from, next) => {
    if (to.name === "Setup" || to.name === "Login") {
      next();
      return;
    }

    const userStore = useUserStore();

    if (userStore.isAnonymous) {
      next({ name: "Login" });
      return;
    }
    next();
  });
}

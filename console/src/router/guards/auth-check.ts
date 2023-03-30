import { useUserStore } from "@/stores/user";
import type { Router } from "vue-router";

const whiteList = ["Setup", "Login", "Binding"];

export function setupAuthCheckGuard(router: Router) {
  router.beforeEach((to, from, next) => {
    if (whiteList.includes(to.name as string)) {
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

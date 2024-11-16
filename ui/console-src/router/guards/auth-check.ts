import { useUserStore } from "@/stores/user";
import type { Router } from "vue-router";

const whiteList = ["Setup"];

export function setupAuthCheckGuard(router: Router) {
  router.beforeEach((to, _, next) => {
    const userStore = useUserStore();

    if (userStore.isAnonymous) {
      if (whiteList.includes(to.name as string)) {
        next();
        return;
      }
      window.location.href = `/login?redirect_uri=${encodeURIComponent(
        window.location.href
      )}`;
      return;
    }

    next();
  });
}

import { stores } from "@halo-dev/console-shared";
import type { Router } from "vue-router";

const whiteList = ["ResetPassword"];

export function setupAuthCheckGuard(router: Router) {
  router.beforeEach((to, _from, next) => {
    if (whiteList.includes(to.name as string)) {
      next();
      return;
    }

    const currentUserStore = stores.currentUser();

    if (currentUserStore.isAnonymous) {
      window.location.href = `/login?redirect_uri=${encodeURIComponent(
        window.location.href
      )}`;
      return;
    }

    next();
  });
}

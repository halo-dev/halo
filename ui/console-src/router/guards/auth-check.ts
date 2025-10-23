import { stores } from "@halo-dev/console-shared";
import type { Router } from "vue-router";

const whiteList = ["Setup"];

export function setupAuthCheckGuard(router: Router) {
  router.beforeEach((to, _, next) => {
    const currentUserStore = stores.currentUser();

    if (currentUserStore.isAnonymous) {
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

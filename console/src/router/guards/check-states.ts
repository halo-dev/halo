import { useGlobalInfoStore } from "@/stores/global-info";
import { useUserStore } from "@/stores/user";
import type { Router } from "vue-router";

export function setupCheckStatesGuard(router: Router) {
  router.beforeEach(async (to, from, next) => {
    const userStore = useUserStore();
    const { globalInfo } = useGlobalInfoStore();
    const { userInitialized, dataInitialized } = globalInfo || {};

    if (to.name === "Setup" && userInitialized) {
      next({ name: "Dashboard" });
      return;
    }

    if (to.name === "SetupInitialData" && dataInitialized) {
      next({ name: "Dashboard" });
      return;
    }

    if (userInitialized === false && to.name !== "Setup") {
      next({ name: "Setup" });
      return;
    }

    if (
      dataInitialized === false &&
      !userStore.isAnonymous &&
      to.name !== "SetupInitialData"
    ) {
      next({ name: "SetupInitialData" });
      return;
    }

    next();
  });
}

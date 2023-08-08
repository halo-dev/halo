import { useGlobalInfoStore } from "@/stores/global-info";
import type { Router } from "vue-router";

const whiteList = ["Setup", "SetupInitialData"];

export function setupCheckStatesGuard(router: Router) {
  router.beforeEach(async (to, from, next) => {
    const { globalInfo } = useGlobalInfoStore();
    if (
      (to.name === "Setup" && globalInfo?.userInitialized) ||
      (to.name === "SetupInitialData" && globalInfo?.dataInitialized)
    ) {
      next({ name: "Dashboard" });
      return;
    }

    if (whiteList.includes(to.name as string)) {
      next();
      return;
    }

    if (globalInfo && globalInfo.userInitialized === false) {
      next({ name: "Setup" });
      return;
    }

    if (globalInfo && globalInfo.dataInitialized === false) {
      next({ name: "SetupInitialData" });
      return;
    }

    next();
  });
}

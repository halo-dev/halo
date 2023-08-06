import { useGlobalInfoStore } from "@/stores/global-info";
import type { Router } from "vue-router";

const whiteList = ["Setup"];

export function setupCheckStatesGuard(router: Router) {
  router.beforeEach(async (to, from, next) => {
    if (whiteList.includes(to.name as string)) {
      next();
      return;
    }

    const globalInfoStore = useGlobalInfoStore();

    if (!globalInfoStore.globalInfo?.initialized) {
      next({ name: "Setup" });
      return;
    }

    next();
  });
}

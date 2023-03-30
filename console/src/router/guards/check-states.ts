import { useSystemStatesStore } from "@/stores/system-states";
import type { Router } from "vue-router";

const whiteList = ["Setup", "Login", "Binding"];

export function setupCheckStatesGuard(router: Router) {
  router.beforeEach(async (to, from, next) => {
    if (whiteList.includes(to.name as string)) {
      next();
      return;
    }

    const systemStateStore = useSystemStatesStore();

    if (!systemStateStore.states.isSetup) {
      next({ name: "Setup" });
      return;
    }

    next();
  });
}

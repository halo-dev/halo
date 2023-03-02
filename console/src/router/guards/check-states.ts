import { useSystemStatesStore } from "@/stores/system-states";
import type { Router } from "vue-router";

export function setupCheckStatesGuard(router: Router) {
  router.beforeEach(async (to, from, next) => {
    if (to.name === "Setup" || to.name === "Login") {
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

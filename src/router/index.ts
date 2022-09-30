import { createRouter, createWebHashHistory } from "vue-router";
import routesConfig from "@/router/routes.config";
import { setupPermissionGuard } from "./guards/permission";
import { setupCheckStatesGuard } from "./guards/check-states";

const router = createRouter({
  history: createWebHashHistory(import.meta.env.BASE_URL),
  routes: routesConfig,
  scrollBehavior: () => ({ left: 0, top: 0 }),
});

setupPermissionGuard(router);
setupCheckStatesGuard(router);

export default router;

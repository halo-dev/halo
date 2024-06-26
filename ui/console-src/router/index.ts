import routesConfig from "@console/router/routes.config";
import {
  createRouter,
  createWebHistory,
  type RouteLocationNormalized,
  type RouteLocationNormalizedLoaded,
} from "vue-router";
import { setupAuthCheckGuard } from "./guards/auth-check";
import { setupCheckStatesGuard } from "./guards/check-states";
import { setupPermissionGuard } from "./guards/permission";

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: routesConfig,
  scrollBehavior: (
    to: RouteLocationNormalized,
    from: RouteLocationNormalizedLoaded
  ) => {
    if (to.name !== from.name) {
      return { left: 0, top: 0 };
    }
  },
});

setupCheckStatesGuard(router);
setupAuthCheckGuard(router);
setupPermissionGuard(router);

export default router;

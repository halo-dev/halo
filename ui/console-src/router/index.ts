import routesConfig from "@console/router/routes.config";
import {
  createRouter,
  createWebHistory,
  type RouteLocationNormalized,
  type RouteLocationNormalizedLoaded,
} from "vue-router";
import { setupStopImplicitSubmission } from "@/formkit/plugins/stop-implicit-submission";
import { setupProcessBarGuard } from "@/router/process-bar";
import { setupAuthCheckGuard } from "./guards/auth-check";
import { setupPermissionGuard } from "./guards/permission";

const router = createRouter({
  history: createWebHistory("/console/"),
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

setupAuthCheckGuard(router);
setupPermissionGuard(router);
setupProcessBarGuard(router);
setupStopImplicitSubmission(router);

export default router;

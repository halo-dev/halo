import {
  createRouter,
  createWebHistory,
  type RouteLocationNormalized,
  type RouteLocationNormalizedLoaded,
} from "vue-router";
import routesConfig from "@uc/router/routes.config";

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

export default router;

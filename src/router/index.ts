import { createRouter, createWebHistory } from "vue-router";
import routesConfig from "@/router/routes.config";

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: routesConfig,
  scrollBehavior: () => ({ left: 0, top: 0 }),
});

export default router;

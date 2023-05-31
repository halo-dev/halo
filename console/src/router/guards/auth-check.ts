import { useUserStore } from "@/stores/user";
import type { RouteLocationNormalized, Router } from "vue-router";

const whiteList = ["Setup", "Login", "Binding"];

export function setupAuthCheckGuard(router: Router) {
  router.beforeEach((to, from, next) => {
    const userStore = useUserStore();

    if (userStore.isAnonymous) {
      if (whiteList.includes(to.name as string)) {
        next();
        return;
      }

      next({
        name: "Login",
        query: {
          redirect_uri: window.location.href,
        },
      });
      return;
    } else {
      if (to.name === "Login") {
        if (allowRedirect(to)) {
          next({
            name: "Redirect",
            query: {
              redirect_uri: to.query.redirect_uri,
            },
          });
        } else {
          next({
            name: "Dashboard",
          });
          return;
        }
      }
    }

    next();
  });
}

function allowRedirect(route: RouteLocationNormalized) {
  const redirect_uri = route.query.redirect_uri as string;

  if (!redirect_uri || redirect_uri === window.location.href) {
    return false;
  }

  if (redirect_uri.startsWith("/")) {
    return true;
  }

  if (
    redirect_uri.startsWith("https://") ||
    redirect_uri.startsWith("http://")
  ) {
    const url = new URL(redirect_uri);
    if (url.origin === window.location.origin) {
      return true;
    }
  }

  return false;
}

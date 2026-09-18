import nprogress from "nprogress";
import "nprogress/nprogress.css";
import type { Router } from "vue-router";

nprogress.configure({
  showSpinner: false,
});

let progressTimer: ReturnType<typeof setTimeout> | null = null;

export function setupProcessBarGuard(router: Router) {
  router.beforeEach(() => {
    progressTimer = setTimeout(() => {
      nprogress.start();
      progressTimer = null;
    }, 200);
  });
  router.afterEach(() => {
    if (progressTimer) {
      clearTimeout(progressTimer);
      progressTimer = null;
    } else {
      nprogress.done();
    }
  });
}

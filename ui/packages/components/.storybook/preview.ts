import { setup, type Preview } from "@storybook/vue3-vite";
import { provide } from "vue";
import {
  createMemoryHistory,
  createRouter,
  routeLocationKey,
  routerKey,
  START_LOCATION,
} from "vue-router";
import "overlayscrollbars/overlayscrollbars.css";
import "../src/styles/tailwind.css";

const router = createRouter({
  history: createMemoryHistory(),
  routes: [
    {
      path: "/:pathMatch(.*)*",
      component: { template: "<div />" },
    },
  ],
});

setup((app) => {
  app.use(router);
});

const preview: Preview = {
  decorators: [
    (story) => ({
      components: { story },
      setup() {
        provide(routerKey, router);
        provide(routeLocationKey, START_LOCATION);
      },
      template: "<story />",
    }),
  ],
  parameters: {
    actions: { argTypesRegex: "^on[A-Z].*" },
    controls: {
      matchers: {
        color: /(background|color)$/i,
        date: /Date$/,
      },
    },
    layout: "padded",
  },
};

export default preview;

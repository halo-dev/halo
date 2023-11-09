import { createApp } from "vue";
import App from "./App.vue";
import { setupVueQuery } from "@/setup/setupVueQuery";
import { setupComponents } from "@/setup/setupComponents";
import { setupI18n } from "@/locales";
import { createPinia } from "pinia";

const app = createApp(App);

setupComponents(app);
setupI18n(app);
setupVueQuery(app);

app.use(createPinia());

(async function () {
  await initApp();
})();

async function initApp() {
  app.mount("#app");
}

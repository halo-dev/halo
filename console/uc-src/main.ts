import { createApp } from "vue";
import App from "./App.vue";
import { setupVueQuery } from "@console/setup/setupVueQuery";

const app = createApp(App);
setupVueQuery(app);

app.mount("#app");

import { createApp } from "vue";
import { createPinia } from "pinia";
import App from "./App.vue";
import router from "./router";

const app = createApp(App);

// setup
import "./setup/setupStyles";
import { setupComponents } from "./setup/setupComponents";

setupComponents(app);

app.use(createPinia());
app.use(router);

app.mount("#app");

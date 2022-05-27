import { createApp } from "vue";
import { createPinia } from "pinia";

import App from "./App.vue";
import router from "./router";
import "@/styles/tailwind.css";
import { Dropdown, Menu, Tooltip, VClosePopper, VTooltip } from "floating-vue";
import "floating-vue/dist/style.css";
// @ts-ignore
import VueGridLayout from "vue-grid-layout";
import Widgets from "@/views/dashboard/widgets";

const app = createApp(App);

app.use(createPinia());
app.use(router);
app.use(VueGridLayout);
app.use(Widgets);

app.directive("tooltip", VTooltip);
app.directive("close-popper", VClosePopper);

app.component("FloatingDropdown", Dropdown);
app.component("FloatingTooltip", Tooltip);
app.component("FloatingMenu", Menu);

app.mount("#app");

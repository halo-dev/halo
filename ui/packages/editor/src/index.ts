import "floating-vue/dist/style.css";
import "github-markdown-css/github-markdown-light.css";
import type { App, Plugin } from "vue";
import { RichTextEditor } from "./components";
import "./styles/index.scss";
import "./styles/tailwind.css";

const plugin: Plugin = {
  install(app: App) {
    app.component("RichTextEditor", RichTextEditor);
  },
};

export default plugin;

export * from "./components";
export * from "./extensions";
export * from "./tiptap";
export * from "./utils";
// TODO: export * from "./types";

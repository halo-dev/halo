import { defineConfig } from "vitest/config";
import { fileURLToPath, URL } from "url";

import { sharedPlugins } from "./src/vite/config-builder";

export default defineConfig({
  plugins: [sharedPlugins],
  resolve: {
    alias: {
      "@": fileURLToPath(new URL("./src", import.meta.url)),
      "@console": fileURLToPath(new URL("./console-src", import.meta.url)),
    },
  },
  test: {
    dir: "./src",
    transformMode: {
      web: [/\.[jt]sx$/],
    },
  },
});

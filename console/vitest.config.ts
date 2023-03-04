import { defineConfig } from "vitest/config";
import { fileURLToPath, URL } from "url";

import { sharedPlugins } from "./vite.config";

export default defineConfig({
  plugins: [sharedPlugins],
  resolve: {
    alias: {
      "@": fileURLToPath(new URL("./src", import.meta.url)),
    },
  },
  test: {
    dir: "./src",
    transformMode: {
      web: [/\.[jt]sx$/],
    },
  },
});

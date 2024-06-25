
import path from "path";
import { defineConfig } from "vite";
import Dts from "vite-plugin-dts";

export default defineConfig({
  plugins: [
    Dts({
      tsconfigPath: "./tsconfig.json",
    }),
  ],
  build: {
    lib: {
      entry: path.resolve(__dirname, "entry/index.ts"),
      name: "HaloApiClient",
      formats: ["es", "iife"],
      fileName: (format) => `halo-api-client.${format}.js`,
    },
    rollupOptions: {
      external: ["axios"],
      output: {
        globals: {
          axios: "axios",
        },
        exports: "named",
      },
    },
    sourcemap: true,
  },
});

import { defineConfig } from "vite-plus";
export default defineConfig({
  pack: {
    entry: ["./src/index.ts", "./src/vite.ts", "./src/rsbuild.ts"],
    format: ["esm"],
    dts: {
      tsgo: true,
    },
    exports: true,
  },
});

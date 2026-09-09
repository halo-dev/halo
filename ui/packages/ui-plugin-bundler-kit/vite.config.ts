import { defineConfig } from "vite-plus";
export default defineConfig({
  pack: {
    deps: { resolveDepSubpath: true },
    entry: ["./src/vite.ts", "./src/rsbuild.ts"],
    format: ["esm"],
    dts: {
      generator: "tsgo",
    },
    exports: true,
  },
});

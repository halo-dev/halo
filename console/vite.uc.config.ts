import path from "path";
import { createViteConfig } from "./src/vite/config-builder";

export default ({ mode }: { mode: string }) => {
  return createViteConfig({
    base: "/uc/",
    entryFile: "/uc-src/main.ts",
    port: 4000,
    outDir: path.resolve("../application/src/main/resources/uc"),
    mode,
  });
};

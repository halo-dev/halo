import { describe, expect, it } from "vite-plus/test";
import * as bundlerKit from "../index";

describe("public API", () => {
  it("exports only supported bundler helpers", () => {
    expect(Object.keys(bundlerKit).sort()).toEqual([
      "HaloUIPluginBundlerKit",
      "rsbuildConfig",
      "viteConfig",
    ]);
  });
});

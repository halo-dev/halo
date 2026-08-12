import { describe, expect, it } from "vite-plus/test";
import packageJson from "../../package.json";
import * as bundlerKit from "../index";
import * as rsbuildEntry from "../rsbuild";
import * as viteEntry from "../vite";

describe("public API", () => {
  it("exports only supported bundler helpers", () => {
    expect(Object.keys(bundlerKit).sort()).toEqual([
      "HaloUIPluginBundlerKit",
      "rsbuildConfig",
      "viteConfig",
    ]);
  });

  it("provides isolated bundler entry points", () => {
    expect(Object.keys(viteEntry)).toEqual(["viteConfig"]);
    expect(Object.keys(rsbuildEntry)).toEqual(["rsbuildConfig"]);
    expect(bundlerKit.viteConfig).toBe(viteEntry.viteConfig);
    expect(bundlerKit.rsbuildConfig).toBe(rsbuildEntry.rsbuildConfig);
    expect(packageJson.exports["./vite"]).toBe("./dist/vite.mjs");
    expect(packageJson.exports["./rsbuild"]).toBe("./dist/rsbuild.mjs");
  });
});

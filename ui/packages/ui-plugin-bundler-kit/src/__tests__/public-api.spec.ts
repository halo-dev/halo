import { describe, expect, it } from "vite-plus/test";
import packageJson from "../../package.json";
import * as rsbuildEntry from "../rsbuild";
import * as viteEntry from "../vite";

describe("public API", () => {
  it("provides only isolated bundler entry points", () => {
    expect(Object.keys(viteEntry)).toEqual(["viteConfig"]);
    expect(Object.keys(rsbuildEntry)).toEqual(["rsbuildConfig"]);
    expect(packageJson.exports).toEqual({
      "./vite": "./dist/vite.mjs",
      "./rsbuild": "./dist/rsbuild.mjs",
      "./package.json": "./package.json",
    });
    expect(packageJson).not.toHaveProperty("types");
  });

  it("makes both toolchains optional peer dependencies", () => {
    for (const dependency of Object.keys(packageJson.peerDependencies)) {
      expect(packageJson.peerDependenciesMeta).toHaveProperty(dependency, {
        optional: true,
      });
    }
  });
});

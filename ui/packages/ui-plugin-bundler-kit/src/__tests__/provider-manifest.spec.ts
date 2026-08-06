import { describe, expect, it } from "vite-plus/test";
import {
  normalizeProviderResourcePath,
  validateEsmProviderManifest,
} from "../provider-manifest";

describe("ESM provider manifest", () => {
  it("accepts only the minimal manifest and normalizes resource paths", () => {
    expect(
      validateEsmProviderManifest({
        format: "esm",
        entry: "chunks/../main.js",
        styles: ["./styles/main.css", "styles/extra.css"],
      })
    ).toEqual({
      format: "esm",
      entry: "./main.js",
      styles: ["./styles/main.css", "./styles/extra.css"],
    });
  });

  it.each([
    "/main.js",
    "../main.js",
    "https://example.com/main.js",
    "//example.com/main.js",
    "main.js?v=1",
    "main.js#entry",
  ])("rejects unsafe resource path %s", (resourcePath) => {
    expect(() => normalizeProviderResourcePath(resourcePath)).toThrow();
  });

  it("rejects unsupported fields and formats", () => {
    expect(() =>
      validateEsmProviderManifest({
        schemaVersion: 1,
        format: "esm",
        entry: "./main.js",
        styles: [],
      })
    ).toThrow("must contain only format, entry, and styles");
    expect(() =>
      validateEsmProviderManifest({
        format: "iife",
        entry: "./main.js",
        styles: [],
      })
    ).toThrow();
  });
});

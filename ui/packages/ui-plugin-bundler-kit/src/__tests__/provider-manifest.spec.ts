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
        style: "styles/main.css",
      })
    ).toEqual({
      format: "esm",
      entry: "./main.js",
      style: "./styles/main.css",
    });
  });

  it("allows a provider without a startup stylesheet", () => {
    expect(
      validateEsmProviderManifest({ format: "esm", entry: "./main.js" })
    ).toEqual({ format: "esm", entry: "./main.js" });
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
      })
    ).toThrow("must contain format, entry, and optional style only");
    expect(() =>
      validateEsmProviderManifest({
        format: "iife",
        entry: "./main.js",
      })
    ).toThrow();
  });
});

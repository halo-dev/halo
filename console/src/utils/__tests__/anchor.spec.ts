import { describe, it, expect } from "vitest";
import { generateAnchor } from "../anchor";

describe("generateAnchor", () => {
  it("should handle basic text", () => {
    expect(generateAnchor("Hello World")).toBe("hello-world");
  });

  it("should trim whitespace", () => {
    expect(generateAnchor("  Hello World  ")).toBe("hello-world");
  });

  it("should replace multiple spaces with a single dash", () => {
    expect(generateAnchor("Hello    World")).toBe("hello-world");
  });

  it("should handle Chinese characters", () => {
    expect(generateAnchor("你好")).toBe("%E4%BD%A0%E5%A5%BD");
  });

  it("should handle special characters", () => {
    expect(generateAnchor("Hello@#World$")).toBe("hello%40%23world%24");
  });

  it("should handle empty string", () => {
    expect(generateAnchor("")).toBe("");
  });
});

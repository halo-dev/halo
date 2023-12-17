import { describe, expect, it } from "vitest";
import { matchMediaType, matchMediaTypes } from "../media-type";

describe("matchMediaType", () => {
  it('should match all image types for "image/*"', () => {
    expect(matchMediaType("image/png", "image/*")).toBe(true);
    expect(matchMediaType("image/jpeg", "image/*")).toBe(true);
    expect(matchMediaType("image/gif", "image/*")).toBe(true);
    expect(matchMediaType("image/webp", "image/*")).toBe(true);
  });

  it('should only match image/png for "image/png"', () => {
    expect(matchMediaType("image/png", "image/png")).toBe(true);
    expect(matchMediaType("image/jpeg", "image/png")).toBe(false);
    expect(matchMediaType("image/gif", "image/png")).toBe(false);
    expect(matchMediaType("image/webp", "image/png")).toBe(false);
  });

  it('should match any type for "*/*"', () => {
    expect(matchMediaType("image/png", "*/*")).toBe(true);
    expect(matchMediaType("application/json", "*/*")).toBe(true);
    expect(matchMediaType("video/mp4", "*/*")).toBe(true);
  });

  it("should not match if type does not match accept", () => {
    expect(matchMediaType("image/png", "video/*")).toBe(false);
    expect(matchMediaType("video/mp4", "image/*")).toBe(false);
  });

  it("should match with case-insensitive comparison", () => {
    expect(matchMediaType("image/png", "IMAGE/*")).toBe(true);
    expect(matchMediaType("application/json", "APPLICATION/*")).toBe(true);
  });
});

describe("matchMediaTypes", () => {
  it("multi accepts", () => {
    expect(matchMediaTypes("image/png", ["image/*", "video/*"])).toBe(true);
    expect(matchMediaTypes("image/jpg", ["image/jpg", "video/*"])).toBe(true);
    expect(matchMediaTypes("image/png", ["video/mp4", "application/*"])).toBe(
      false
    );
  });
});

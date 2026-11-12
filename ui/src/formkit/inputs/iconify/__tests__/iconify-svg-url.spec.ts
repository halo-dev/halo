import { describe, expect, it } from "vite-plus/test";
import { buildIconifySvgUrl, ICONIFY_BASE_URL } from "../api";

describe("buildIconifySvgUrl", () => {
  it("requests /{prefix}/{name}.svg instead of a colon path", () => {
    const url = new URL(
      buildIconifySvgUrl("mdi:home", { width: "24", color: "#000" })
    );

    expect(url.origin).toBe(ICONIFY_BASE_URL);
    expect(url.pathname).toBe("/mdi/home.svg");
    expect(url.pathname).not.toContain(":");
    expect(url.searchParams.get("width")).toBe("24");
    expect(url.searchParams.get("color")).toBe("#000");
  });

  it("keeps hyphenated prefixes and encodes the color query", () => {
    expect(
      buildIconifySvgUrl("fa6-solid:house-chimney", {
        width: "1.2em",
        color: "rgb(0, 0, 0)",
      })
    ).toBe(
      `${ICONIFY_BASE_URL}/fa6-solid/house-chimney.svg?width=1.2em&color=${encodeURIComponent("rgb(0, 0, 0)")}`
    );
  });

  it("replaces only the prefix separator", () => {
    const url = new URL(
      buildIconifySvgUrl("mdi:home:extra", { width: "", color: "" })
    );

    expect(url.pathname).toBe("/mdi/home:extra.svg");
    expect(url.search).toBe("?width=&color=");
  });

  it("leaves names without a prefix unchanged", () => {
    const url = new URL(buildIconifySvgUrl("home", { width: "48" }));

    expect(url.pathname).toBe("/home.svg");
    expect(url.searchParams.get("width")).toBe("48");
    expect(url.searchParams.get("color")).toBe("");
  });
});

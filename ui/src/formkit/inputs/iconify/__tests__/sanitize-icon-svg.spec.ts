import { describe, expect, it } from "vite-plus/test";
import { sanitizeIconSvg } from "../sanitize-icon-svg";

function parse(value: string) {
  const element = document.createElement("div");
  element.innerHTML = sanitizeIconSvg(value);
  return element;
}

describe("sanitizeIconSvg", () => {
  it("removes event handlers and scripts while preserving normal SVG", () => {
    const element = parse(
      '<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" onload="alert(1)"><script>alert(1)</script><path fill="currentColor" d="M0 0h24v24H0z" onclick="alert(1)"/></svg>'
    );
    expect(element.querySelector("script")).toBeNull();
    expect(element.querySelector("[onload], [onclick]")).toBeNull();
    expect(element.querySelector("svg")?.getAttribute("viewBox")).toBe(
      "0 0 24 24"
    );
    expect(element.querySelector("path")?.getAttribute("fill")).toBe(
      "currentColor"
    );
    expect(element.querySelector("path")?.getAttribute("d")).toBe(
      "M0 0h24v24H0z"
    );
  });

  it("removes foreign HTML and executable link protocols", () => {
    const element = parse(
      '<svg xmlns="http://www.w3.org/2000/svg"><foreignObject><img src="x" onerror="alert(1)"></foreignObject><a href="javascript:alert(1)"><text>icon</text></a><a xmlns:xlink="http://www.w3.org/1999/xlink" xlink:href="javascript:alert(1)"><path/></a></svg><img src="x" onerror="alert(1)">'
    );
    expect(element.querySelector("foreignObject, img")).toBeNull();
    expect(element.querySelector("[href], [onerror]")).toBeNull();
    expect(element.innerHTML).not.toContain("javascript:");
  });

  it("preserves gradients, local references, and filters", () => {
    const element = parse(
      '<svg viewBox="0 0 24 24"><defs><linearGradient id="gradient"><stop offset="0" stop-color="red"/></linearGradient><filter id="blur"><feGaussianBlur stdDeviation="1"/></filter></defs><path fill="url(#gradient)" filter="url(#blur)" d="M0 0h24v24H0z"/></svg>'
    );
    expect(element.querySelector("linearGradient")).not.toBeNull();
    expect(element.querySelector("feGaussianBlur")).not.toBeNull();
    expect(element.querySelector("path")?.getAttribute("fill")).toBe(
      "url(#gradient)"
    );
  });

  it("removes stylesheets, animation, and obfuscated executable links", () => {
    const element = parse(
      '<svg><style>body{display:none}</style><animate attributeName="href" values="javascript:alert(1)"/><set attributeName="onload" to="alert(1)"/><a href="java&#x09;script:alert(1)"><path style="fill: red"/></a></svg>'
    );
    expect(element.querySelector("style, animate, set, [href]")).toBeNull();
    expect(element.querySelector("path")?.getAttribute("style")).toBe(
      "fill: red"
    );
  });

  it("handles missing and repeated values", () => {
    expect(sanitizeIconSvg()).toBe("");
    expect(sanitizeIconSvg("")).toBe("");
    const clean = sanitizeIconSvg('<svg onload="alert(1)"><path/></svg>');
    expect(sanitizeIconSvg(clean)).toBe(clean);
  });
});

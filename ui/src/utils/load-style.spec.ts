import { afterEach, describe, expect, it } from "vite-plus/test";
import { loadStyle } from "./load-style";

describe("loadStyle", () => {
  afterEach(() => {
    document.head.innerHTML = "";
  });

  it("keeps insertion order independent from network settlement", async () => {
    const marker = document.createElement("meta");
    document.head.appendChild(marker);
    const first = loadStyle("/first.css", marker);
    const second = loadStyle("/second.css", marker);
    const links = [...document.head.querySelectorAll("link")];

    expect(links.map((link) => link.getAttribute("href"))).toEqual([
      "/first.css",
      "/second.css",
    ]);

    links[1].dispatchEvent(new Event("load"));
    links[0].dispatchEvent(new Event("load"));
    await expect(Promise.all([first, second])).resolves.toEqual(links);
  });
});

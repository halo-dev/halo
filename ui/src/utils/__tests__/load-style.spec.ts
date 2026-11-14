import { afterEach, describe, expect, it } from "vite-plus/test";
import { loadStyle, unloadStyle } from "../load-style";

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

  it("removes a newly inserted stylesheet when loading fails", async () => {
    const loading = loadStyle("/failed.css");
    const link = document.head.querySelector("link") as HTMLLinkElement;

    link.dispatchEvent(new Event("error"));

    await expect(loading).rejects.toBeInstanceOf(Event);
    expect(link.isConnected).toBe(false);
  });

  it("only unloads stylesheets inserted by loadStyle", async () => {
    const existing = document.createElement("link");
    existing.href = "/existing.css";
    existing.setAttribute("data-loaded", "true");
    document.head.appendChild(existing);

    const reused = await loadStyle("/existing.css");
    const loading = loadStyle("/owned.css");
    const owned = document.head.querySelector(
      'link[href="/owned.css"]'
    ) as HTMLLinkElement;
    owned.dispatchEvent(new Event("load"));
    await loading;

    unloadStyle(reused);
    unloadStyle(owned);

    expect(existing.isConnected).toBe(true);
    expect(owned.isConnected).toBe(false);
  });
});

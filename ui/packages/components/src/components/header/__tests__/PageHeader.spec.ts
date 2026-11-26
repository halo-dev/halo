import { enableAutoUnmount, mount } from "@vue/test-utils";
import { afterEach, beforeEach, describe, expect, it } from "vite-plus/test";
import { h, nextTick } from "vue";
import { setComponentsLocale } from "../../../locales";
import { VPageHeader } from "../index";

enableAutoUnmount(afterEach);

beforeEach(() => {
  setComponentsLocale("en");
});

afterEach(() => {
  setComponentsLocale("en");
  Object.defineProperty(window, "scrollY", {
    configurable: true,
    value: 0,
  });
});

describe("PageHeader", () => {
  it("renders the title as the top-level heading", () => {
    const wrapper = mount(VPageHeader, {
      props: { title: "Posts" },
    });

    expect(wrapper.element.tagName).toBe("HEADER");
    expect(wrapper.get("h1.page-header__title").text()).toBe("Posts");
    expect(wrapper.get("h1").attributes("title")).toBe("Posts");
  });

  it("renders the subtitle, icon and actions", () => {
    const wrapper = mount(VPageHeader, {
      props: { title: "Posts", subtitle: "128 items" },
      slots: {
        icon: () => h("span", { class: "my-icon" }),
        actions: () => h("button", { class: "my-action" }, "New"),
      },
    });

    expect(wrapper.get(".page-header__subtitle").text()).toBe("128 items");
    expect(wrapper.get(".page-header__icon").attributes("aria-hidden")).toBe(
      "true"
    );
    expect(wrapper.get(".my-icon").exists()).toBe(true);
    expect(wrapper.get(".page-header__actions").attributes("aria-label")).toBe(
      "Page actions"
    );
    expect(wrapper.get(".my-action").text()).toBe("New");
  });

  it("shows skeletons and disables the actions while loading", () => {
    const wrapper = mount(VPageHeader, {
      props: { title: "Posts", subtitle: "128 items", loading: true },
      slots: {
        actions: () => h("button", { class: "my-action" }, "New"),
      },
    });

    expect(wrapper.attributes("aria-busy")).toBe("true");
    expect(wrapper.findAll(".page-header__skeleton")).toHaveLength(2);
    expect(wrapper.find(".page-header__title").exists()).toBe(false);
    expect(
      wrapper.get(".page-header__actions").attributes("inert")
    ).toBeDefined();
  });

  it("implies a border when sticky and tracks the stuck state", async () => {
    const wrapper = mount(VPageHeader, {
      props: { title: "Posts", sticky: true },
    });

    expect(wrapper.classes()).toContain("page-header--sticky");
    expect(wrapper.classes()).toContain("page-header--bordered");

    // jsdom reports a zero top offset, so only the scroll position changes.
    Object.defineProperty(window, "scrollY", {
      configurable: true,
      value: 24,
    });
    window.dispatchEvent(new Event("scroll"));
    await nextTick();

    expect(wrapper.classes()).toContain("page-header--stuck");
  });

  it("allows the title to wrap when truncate is false", () => {
    const wrapper = mount(VPageHeader, {
      props: { title: "A very long title", truncate: false },
    });

    expect(wrapper.classes()).toContain("page-header--wrapped");
  });

  it("renders the title, subtitle and tags slots", () => {
    const wrapper = mount(VPageHeader, {
      slots: {
        title: () => h("span", { class: "custom-title" }, "Rich title"),
        subtitle: () =>
          h("span", { class: "custom-subtitle" }, "Rich subtitle"),
        tags: () => h("span", { class: "custom-tag" }, "Active"),
      },
    });

    expect(wrapper.get("h1 .custom-title").text()).toBe("Rich title");
    expect(wrapper.get(".custom-subtitle").text()).toBe("Rich subtitle");
    expect(wrapper.get(".page-header__tags .custom-tag").text()).toBe("Active");
    expect(wrapper.get("h1").attributes("title")).toBeUndefined();
  });

  it("keeps consumer classes on the root element", () => {
    const wrapper = mount(VPageHeader, {
      props: { title: "Posts" },
      attrs: { class: "border-b" },
    });

    expect(wrapper.classes()).toContain("page-header");
    expect(wrapper.classes()).toContain("border-b");
  });
});

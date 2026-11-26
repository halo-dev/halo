import { mount } from "@vue/test-utils";
import { describe, expect, it } from "vite-plus/test";
import { h } from "vue";
import { VCard } from "../index";

describe("Card", () => {
  it("should render", () => {
    expect(VCard).toBeDefined();
  });

  it("renders the title as a third-level heading", () => {
    const wrapper = mount(VCard, {
      props: { title: "站点概览" },
    });

    const title = wrapper.get("h3.card-header-title");
    expect(title.text()).toBe("站点概览");
  });

  it("renders the header when only actions are provided", () => {
    const wrapper = mount(VCard, {
      slots: { actions: () => h("button", "刷新") },
    });

    expect(wrapper.find(".card-header").exists()).toBe(true);
    expect(wrapper.get(".card-header-actions").text()).toBe("刷新");
  });

  it("lets the header slot fully replace the default header", () => {
    const wrapper = mount(VCard, {
      props: { title: "should be ignored" },
      slots: {
        header: () => h("div", { class: "custom-header" }, "自定义头部"),
      },
    });

    expect(wrapper.get(".custom-header").text()).toBe("自定义头部");
    expect(wrapper.find("h3.card-header-title").exists()).toBe(false);
  });

  it("keeps the actions slot when a custom title slot is used", () => {
    const wrapper = mount(VCard, {
      props: { title: "fallback" },
      slots: {
        title: () => h("span", { class: "custom-title" }, "自定义标题"),
        actions: () => h("button", "刷新"),
      },
    });

    expect(wrapper.get(".custom-title").text()).toBe("自定义标题");
    expect(wrapper.get(".card-header-actions").text()).toBe("刷新");
  });

  it("applies the flush, full height, scrollable and overflow modifiers", () => {
    const wrapper = mount(VCard, {
      props: {
        flush: true,
        fullHeight: true,
        scrollable: true,
        overflow: "visible",
      },
    });

    expect(wrapper.classes()).toContain("card-full-height");
    expect(wrapper.classes()).toContain("card-overflow-visible");
    expect(wrapper.get(".card-body").classes()).toEqual(
      expect.arrayContaining(["card-body-flush", "card-body-scrollable"])
    );
  });

  it("applies shadow and hoverable modifiers", () => {
    const wrapper = mount(VCard, {
      props: { shadow: "hover", hoverable: true },
    });

    expect(wrapper.classes()).toContain("card-shadow-hover");
    expect(wrapper.classes()).toContain("card-hoverable");
  });

  it("does not lift a card when shadow is never", () => {
    const wrapper = mount(VCard, {
      props: { shadow: "never", hoverable: true },
    });

    expect(wrapper.classes()).not.toContain("card-hoverable");
  });

  it("accepts a string body class and body style", () => {
    const wrapper = mount(VCard, {
      props: {
        bodyClass: "custom-body",
        bodyStyle: { paddingTop: "20px" },
      },
    });

    const body = wrapper.get(".card-body");
    expect(body.classes()).toContain("custom-body");
    expect(body.attributes("style")).toContain("padding-top: 20px");
  });

  it("renders the toolbar slot", () => {
    const wrapper = mount(VCard, {
      slots: { toolbar: () => h("span", "工具栏") },
    });

    expect(wrapper.get(".card-toolbar").text()).toBe("工具栏");
  });

  it("renders a custom root tag", () => {
    const wrapper = mount(VCard, {
      props: { as: "section" },
    });

    expect(wrapper.element.tagName).toBe("SECTION");
  });
});

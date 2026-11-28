import { enableAutoUnmount, mount } from "@vue/test-utils";
import { afterEach, describe, expect, it } from "vite-plus/test";
import { h } from "vue";
import { VPageHeader } from "../index";

enableAutoUnmount(afterEach);

describe("PageHeader", () => {
  it("renders the title, icon and actions", () => {
    const wrapper = mount(VPageHeader, {
      props: { title: "Posts" },
      slots: {
        icon: () => h("span", { class: "my-icon" }),
        actions: () => h("button", { class: "my-action" }, "New"),
      },
    });

    expect(wrapper.get("h2.page-header__title").text()).toBe("Posts");
    expect(wrapper.get(".my-icon").exists()).toBe(true);
    expect(wrapper.get(".my-action").text()).toBe("New");
  });

  it("does not render an empty actions row", () => {
    const wrapper = mount(VPageHeader, {
      props: { title: "Posts" },
    });

    expect(wrapper.find(".page-header__actions").exists()).toBe(false);
  });

  it("renders a center slot between the title and actions", () => {
    const wrapper = mount(VPageHeader, {
      props: { title: "Posts" },
      slots: {
        center: () => h("div", { class: "my-center" }, "Center"),
        actions: () => h("button", { class: "my-action" }, "New"),
      },
    });

    expect(wrapper.classes()).toContain("page-header--has-center");
    expect(wrapper.get(".page-header__center .my-center").text()).toBe(
      "Center"
    );
  });

  it("does not mark the header as centered without the center slot", () => {
    const wrapper = mount(VPageHeader, {
      props: { title: "Posts" },
    });

    expect(wrapper.classes()).not.toContain("page-header--has-center");
  });
});

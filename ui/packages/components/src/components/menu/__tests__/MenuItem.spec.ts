import { mount } from "@vue/test-utils";
import { describe, expect, it } from "vite-plus/test";
import { VMenuItem } from "../index";

describe("MenuItem", () => {
  it("selects from the keyboard", async () => {
    const wrapper = mount(VMenuItem, {
      props: { id: "/home", title: "Home" },
    });
    const item = wrapper.get(".menu-item");

    await item.trigger("keydown", { key: "Enter" });
    await item.trigger("keydown", { key: " " });

    expect(wrapper.emitted("select")).toEqual([["/home"], ["/home"]]);
  });

  it("exposes the submenu state", async () => {
    const wrapper = mount(VMenuItem, {
      props: { id: "/content", title: "Content" },
      slots: { default: "<li>Child</li>" },
    });
    const item = wrapper.get(".menu-item");

    expect(item.attributes("aria-expanded")).toBe("false");

    await item.trigger("click");

    expect(item.attributes("aria-expanded")).toBe("true");
  });
});

import { mount } from "@vue/test-utils";
import { describe, expect, it } from "vite-plus/test";
import SelectOptionItem from "../SelectOptionItem.vue";
import SelectSelector from "../SelectSelector.vue";

describe("select option rendering", () => {
  it("renders option icon and description in dropdown rows", () => {
    const wrapper = mount(SelectOptionItem, {
      props: {
        option: {
          description: "Manage installed plugins",
          icon: "/plugins/plugin-a/assets/icon.svg",
          label: "Plugin A",
          value: "plugin-a",
        },
      },
    });

    const image = wrapper.find("img");
    expect(image.exists()).toBe(true);
    expect(image.attributes("src")).toBe("/plugins/plugin-a/assets/icon.svg");
    expect(image.attributes("referrerpolicy")).toBe("no-referrer");
    expect(image.classes()).toContain("h-8");
    expect(image.classes()).toContain("w-8");
    expect(wrapper.text()).toContain("Plugin A");
    expect(wrapper.text()).toContain("Manage installed plugins");
  });

  it("renders a smaller option icon when description is absent", () => {
    const wrapper = mount(SelectOptionItem, {
      props: {
        option: {
          icon: "/plugins/plugin-a/assets/icon.svg",
          label: "Plugin A",
          value: "plugin-a",
        },
      },
    });

    const image = wrapper.find("img");
    expect(image.classes()).toContain("h-5");
    expect(image.classes()).toContain("w-5");
  });

  it("hides failed option icon images", async () => {
    const wrapper = mount(SelectOptionItem, {
      props: {
        option: {
          icon: "/missing.svg",
          label: "Missing icon",
          value: "missing",
        },
      },
    });

    const image = wrapper.find("img");
    await image.trigger("error");

    expect((image.element as HTMLImageElement).hidden).toBe(true);
    expect(wrapper.text()).toContain("Missing icon");
  });

  it("keeps selected display label-only", () => {
    const wrapper = mount(SelectSelector, {
      props: {
        isDropdownVisible: false,
        searchable: false,
        selectedOptions: [
          {
            description: "Manage installed plugins",
            icon: "/plugins/plugin-a/assets/icon.svg",
            label: "Plugin A",
            value: "plugin-a",
          },
        ],
      },
    });

    expect(wrapper.text()).toContain("Plugin A");
    expect(wrapper.text()).not.toContain("Manage installed plugins");
    expect(wrapper.find("img").exists()).toBe(false);
  });
});

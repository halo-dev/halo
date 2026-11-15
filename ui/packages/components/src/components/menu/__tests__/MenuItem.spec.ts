import { mount } from "@vue/test-utils";
import { describe, expect, it } from "vite-plus/test";
import { defineComponent } from "vue";
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

  it("does not select the parent item from a child item's keyboard event", async () => {
    const wrapper = mount(
      defineComponent({
        components: { VMenuItem },
        template: `
          <ul>
            <VMenuItem id="/content" title="Content">
              <VMenuItem id="/content/posts" title="Posts" />
            </VMenuItem>
          </ul>
        `,
      })
    );

    const parent = wrapper.findAllComponents(VMenuItem)[0];
    const child = wrapper.findAllComponents(VMenuItem)[1];

    await child.get(".menu-item").trigger("keydown", { key: "Enter" });
    await child.get(".menu-item").trigger("keydown", { key: " " });

    expect(child.emitted("select")).toEqual([
      ["/content/posts"],
      ["/content/posts"],
    ]);
    expect(parent.emitted("select")).toBeUndefined();
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

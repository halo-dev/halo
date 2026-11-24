/* eslint-disable vue/one-component-per-file -- Test-local host components keep the reactive menu state isolated. */
import { mount } from "@vue/test-utils";
import { describe, expect, it } from "vite-plus/test";
import { defineComponent, nextTick, ref } from "vue";
import { VMenu, VMenuItem } from "../index";

describe("Menu", () => {
  it("should render", () => {
    expect(VMenu).toBeDefined();
    expect(VMenuItem).toBeDefined();
    expect(mount(VMenu).html()).toMatchSnapshot();
    expect(
      mount(VMenuItem, { global: { stubs: { Transition: false } } }).html()
    ).toMatchSnapshot();
  });

  it("opens sub menus when openIds changes", async () => {
    const openIds = ref<string[]>([]);

    const wrapper = mount(
      defineComponent({
        setup() {
          return () => (
            <VMenu openIds={openIds.value}>
              <VMenuItem id="content" title="Content">
                <VMenuItem id="posts" title="Posts" />
              </VMenuItem>
            </VMenu>
          );
        },
      })
    );

    const parent = wrapper.findAllComponents(VMenuItem)[0];

    expect(parent.attributes("aria-expanded")).toBe("false");

    openIds.value = ["content"];
    await nextTick();

    expect(parent.attributes("aria-expanded")).toBe("true");
  });

  it("keeps the current page marker on the leaf item", () => {
    const wrapper = mount(
      defineComponent({
        setup() {
          return () => (
            <VMenu openIds={["content"]}>
              <VMenuItem id="content" title="Content" active>
                <VMenuItem id="posts" title="Posts" active />
              </VMenuItem>
            </VMenu>
          );
        },
      })
    );

    const [parent, child] = wrapper.findAllComponents(VMenuItem);

    expect(parent.attributes("aria-current")).toBeUndefined();
    expect(parent.html()).toContain("active-trail");
    expect(child.attributes("aria-current")).toBe("page");
  });

  it("names the navigation landmark", () => {
    const namedByProp = mount(VMenu, {
      props: { ariaLabel: "Main menu" },
    });
    expect(
      namedByProp.get('[role="navigation"]').attributes("aria-label")
    ).toBe("Main menu");

    const namedByAttr = mount(VMenu, {
      attrs: { "aria-label": "Home" },
    });
    expect(
      namedByAttr.get('[role="navigation"]').attributes("aria-label")
    ).toBe("Home");
  });

  it("should work with openIds prop", function () {
    const wrapper = mount(
      {
        setup() {
          return () => (
            <VMenu openIds={["3"]}>
              <VMenuItem id="1" title="Menu Item 1" />
              <VMenuItem id="2" title="Menu Item 2" />
              <VMenuItem id="3" title="Menu Item 3">
                <VMenuItem key="4" title="Menu Item 4" />
              </VMenuItem>
            </VMenu>
          );
        },
      },
      { global: { stubs: { Transition: false } } }
    );

    expect(wrapper.html()).toMatchSnapshot();

    expect(wrapper.find(".sub-menu-items .menu-title").text()).contain(
      "Menu Item 4"
    );
  });
});

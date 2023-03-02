import { describe, expect, it } from "vitest";
import { mount } from "@vue/test-utils";
import { VMenu, VMenuItem } from "../index";

describe("Menu", () => {
  it("should render", () => {
    expect(VMenu).toBeDefined();
    expect(VMenuItem).toBeDefined();
    expect(mount(VMenu).html()).toMatchSnapshot();
    expect(mount(VMenuItem).html()).toMatchSnapshot();
  });

  it("should work with sub menus", async () => {
    const wrapper = await mount({
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
    });
    expect(wrapper.html()).toMatchSnapshot();

    // toggling sub menu
    expect(
      wrapper.find(".has-submenus .sub-menu-items").attributes().style
    ).toBeUndefined(); // visible

    await wrapper.find(".has-submenus").trigger("click");
    expect(
      wrapper.find(".has-submenus .sub-menu-items").attributes().style
    ).toBe("display: none;");

    await wrapper.find(".has-submenus").trigger("click");
    expect(
      wrapper.find(".has-submenus .sub-menu-items").attributes().style
    ).toBe(""); // visible
  });

  it("should work with openIds prop", function () {
    const wrapper = mount({
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
    });

    expect(wrapper.html()).toMatchSnapshot();

    expect(wrapper.find(".sub-menu-items .menu-title").text()).contain(
      "Menu Item 4"
    );
  });

  it("should work with select emit", async () => {
    const wrapper = mount({
      setup() {
        return () => (
          <VMenu openIds={["3"]}>
            <VMenuItem id="1" title="Menu Item 1" />
            <VMenuItem id="2" title="Menu Item 2" />
            <VMenuItem id="3" title="Menu Item 3">
              <VMenuItem id="4" title="Menu Item 4" />
            </VMenuItem>
          </VMenu>
        );
      },
    });

    wrapper.findAllComponents(VMenuItem).forEach((item) => {
      // has not sub menu
      if (item.props().id === "1") {
        item.trigger("click");
        expect(item.emitted().select[0]).toEqual(["1"]);
      }
      // has sub menu
      if (item.props().id === "3") {
        item.trigger("click");
        expect(item.emitted().select).toBeUndefined();

        expect(item.vm.open).toBe(false);

        item.trigger("click");
        expect(item.vm.open).toBe(true);
      }
    });
  });
});

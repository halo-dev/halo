import { mount } from "@vue/test-utils";
import { describe, expect, it } from "vite-plus/test";
import { h } from "vue";
import { VEntityContainer } from "..";

describe("EntityContainer", () => {
  it("renders slotted rows inside the table body", () => {
    const wrapper = mount(VEntityContainer, {
      slots: {
        default: () =>
          h("tr", { id: "row-1" }, [h("td", { id: "cell-1" }, "Row")]),
      },
    });

    expect(wrapper.get("table").classes()).toContain("w-full");
    expect(wrapper.get("table > tbody > tr#row-1").exists()).toBe(true);
    expect(wrapper.get("tbody > tr#row-1 > td#cell-1").text()).toBe("Row");
  });

  it("scrolls wide rows horizontally without chaining the scroll", () => {
    const wrapper = mount(VEntityContainer);
    const root = wrapper.get("div");

    expect(root.classes()).toEqual(
      expect.arrayContaining([
        "w-full",
        "overflow-x-auto",
        "overscroll-x-contain",
      ])
    );
  });
});

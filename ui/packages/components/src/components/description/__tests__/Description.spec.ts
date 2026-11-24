import { mount } from "@vue/test-utils";
import { describe, expect, it } from "vite-plus/test";
import type { VNodeChild } from "vue";
import { h } from "vue";
import { VDescription, VDescriptionItem } from "../index";

function mountDescription(
  descriptionProps: Record<string, unknown> = {},
  itemProps: Record<string, unknown> = { label: "Name", content: "Halo" },
  itemSlots: Record<string, () => VNodeChild> = {}
) {
  return mount(VDescription, {
    props: descriptionProps,
    slots: {
      default: () => h(VDescriptionItem, itemProps, itemSlots),
    },
  });
}

describe("Description", () => {
  it("keeps the current default layout", () => {
    const wrapper = mountDescription();
    const description = wrapper.get(".description-wrapper");
    const item = wrapper.get(".description-item-wrapper");

    expect(description.classes()).toContain("description-wrapper--divided");
    expect(description.classes()).not.toContain("description-wrapper--grid");
    expect(item.classes()).toContain("description-item--md");
    expect(item.classes()).toContain("description-layout-horizontal");
    expect(item.classes()).toContain("description-item--hoverable");
    expect(wrapper.get(".description-item__label").text()).toBe("Name");
    expect(wrapper.get(".description-item__content").text()).toBe("Halo");
  });

  it("renders label and content slots", () => {
    const wrapper = mountDescription(
      {},
      {},
      {
        label: () => h("strong", "Slotted label"),
        default: () => "Slotted content",
      }
    );

    expect(wrapper.get(".description-item__label").text()).toBe(
      "Slotted label"
    );
    expect(wrapper.get(".description-item__content").text()).toBe(
      "Slotted content"
    );
  });

  it("renders the empty text only when content is missing", () => {
    const withContent = mountDescription({ emptyText: "None" });
    expect(withContent.get(".description-item__content").text()).toBe("Halo");

    const withoutContent = mountDescription(
      { emptyText: "None" },
      { label: "Name" }
    );
    expect(withoutContent.get(".description-item__content").text()).toBe(
      "None"
    );

    const withoutFallback = mountDescription({}, { label: "Name" });
    expect(withoutFallback.get(".description-item__content").text()).toBe("");
  });

  it("keeps a numeric zero as content", () => {
    const wrapper = mountDescription({}, { label: "Count", content: 0 });
    expect(wrapper.get(".description-item__content").text()).toBe("0");
  });

  it("supports a grid layout with columns", () => {
    const wrapper = mountDescription({ columns: 2 });
    const description = wrapper.get(".description-wrapper");
    const item = wrapper.get(".description-item-wrapper");

    expect(description.classes()).toContain("description-wrapper--grid");
    expect(description.classes()).toContain("description-wrapper--columns-2");
    expect(description.classes()).not.toContain("description-wrapper--divided");
    expect(item.classes()).toContain("description-item--grid");
    expect(item.classes()).toContain("description-item--span-1");
    expect(item.classes()).toContain("description-item--divided");
  });

  it("clamps the item span to the column count", () => {
    const wrapper = mountDescription(
      { columns: 3 },
      { label: "Name", span: 5 }
    );
    expect(wrapper.get(".description-item-wrapper").classes()).toContain(
      "description-item--span-3"
    );
  });

  it("applies size, layout and border options", () => {
    const wrapper = mountDescription({
      size: "sm",
      layout: "vertical",
      bordered: true,
    });
    const description = wrapper.get(".description-wrapper");
    const item = wrapper.get(".description-item-wrapper");

    expect(description.classes()).toContain("description-wrapper--bordered");
    expect(item.classes()).toContain("description-item--sm");
    expect(item.classes()).toContain("description-layout-vertical");
    expect(item.classes()).toContain("description-item--bordered");
    expect(item.classes()).not.toContain("description-item--hoverable");
    expect(item.classes()).not.toContain("description-item--divided");
  });

  it("renders an inline separator", () => {
    const wrapper = mountDescription({ layout: "inline", separator: "：" });
    expect(
      wrapper.get(".description-item__label").attributes("data-separator")
    ).toBe("：");
  });

  it("supports a per-item label width and alignment", () => {
    const wrapper = mountDescription(
      {},
      { label: "Name", content: "Halo", labelWidth: 120, align: "right" }
    );
    const item = wrapper.get(".description-item-wrapper");
    const content = wrapper.get(".description-item__content");

    expect(item.attributes("style")).toContain("grid-template-columns");
    expect(content.attributes("style")).toContain("grid-column: 2");
    expect(content.attributes("style")).toContain("text-align: right");
  });

  it("renders a title and extra slot", () => {
    const wrapper = mount(VDescription, {
      props: {
        title: "Site profile",
      },
      slots: {
        default: () => h(VDescriptionItem, { label: "Name", content: "Halo" }),
        extra: () => h("span", "Extra"),
      },
    });

    expect(wrapper.get(".description-title").text()).toBe("Site profile");
    expect(wrapper.get(".description-extra").text()).toBe("Extra");
  });

  it("works without a parent description", () => {
    const wrapper = mount(VDescriptionItem, {
      props: { label: "Name", content: "Halo" },
    });
    expect(wrapper.get(".description-item-wrapper").classes()).toContain(
      "description-item--md"
    );
  });
});

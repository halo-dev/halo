import { mount } from "@vue/test-utils";
import { describe, expect, it, vi } from "vite-plus/test";
import { defineComponent, h, nextTick } from "vue";
import { VEntity } from "..";

const VDropdownStub = defineComponent({
  name: "VDropdown",
  props: {
    shown: Boolean,
  },
  emits: ["update:shown"],
  setup(_, { slots }) {
    return () => h("div", [slots.default?.()]);
  },
});

describe("Entity", () => {
  it("should render", () => {
    expect(mount(VEntity)).toBeDefined();
  });

  it("reflects the selected state", () => {
    const selected = mount(VEntity, {
      props: { isSelected: true },
    });
    const selectedRow = selected.find("tr");

    expect(selectedRow.classes()).toContain("entity-selected");
    expect(selectedRow.find(".entity-selected-indicator").exists()).toBe(true);

    const unselected = mount(VEntity);
    const unselectedRow = unselected.find("tr");

    expect(unselectedRow.classes()).not.toContain("entity-selected");
    expect(unselectedRow.find(".entity-selected-indicator").exists()).toBe(
      false
    );
  });

  it("forwards attributes and listeners to the main row", async () => {
    const onClick = vi.fn();
    const wrapper = mount(VEntity, {
      attrs: {
        id: "post-row",
        onClick,
      },
    });
    const row = wrapper.find("tr");

    expect(row.attributes("id")).toBe("post-row");

    await row.trigger("click");

    expect(onClick).toHaveBeenCalledOnce();
  });

  it("keeps row attributes on the main row when a footer is rendered", () => {
    const wrapper = mount(VEntity, {
      attrs: { id: "post-row" },
      slots: {
        start: () => "Post",
        footer: () => "Footer",
      },
    });

    const rows = wrapper.findAll("tr");

    expect(rows).toHaveLength(2);
    expect(rows[0].attributes("id")).toBe("post-row");
    expect(rows[1].attributes("id")).toBeUndefined();
  });

  it("renders an accessible dropdown trigger only when items are provided", async () => {
    const withoutItems = mount(VEntity);

    expect(withoutItems.find(".entity-dropdown").exists()).toBe(false);

    const onRowClick = vi.fn();
    const wrapper = mount(VEntity, {
      attrs: { onClick: onRowClick },
      global: {
        stubs: {
          VDropdown: VDropdownStub,
        },
      },
      slots: {
        dropdownItems: () => h("button", "Edit"),
      },
    });

    const trigger = wrapper.get(".entity-dropdown-trigger");

    expect(trigger.element.tagName).toBe("BUTTON");
    expect(trigger.attributes("type")).toBe("button");
    expect(trigger.attributes("aria-haspopup")).toBe("menu");
    expect(trigger.attributes("aria-label")).toBe("More actions");
    expect(trigger.attributes("aria-expanded")).toBe("false");

    await trigger.trigger("click");

    expect(onRowClick).not.toHaveBeenCalled();

    wrapper.findComponent({ name: "VDropdown" }).vm.$emit("update:shown", true);
    await nextTick();

    expect(
      wrapper.get(".entity-dropdown-trigger").attributes("aria-expanded")
    ).toBe("true");
  });
});

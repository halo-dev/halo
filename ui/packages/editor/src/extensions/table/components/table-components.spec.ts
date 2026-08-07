// @vitest-environment jsdom

import { mount } from "@vue/test-utils";
import { afterEach, describe, expect, it, vi } from "vitest";
import TableInsertGrid from "./TableInsertGrid.vue";
import TableMenuButton from "./TableMenuButton.vue";
import TableMenuSegmentedControl from "./TableMenuSegmentedControl.vue";

describe("table interaction components", () => {
  afterEach(() => {
    document.body.replaceChildren();
  });

  it("exposes an 8-by-8 insertion grid and selects pointer dimensions", async () => {
    const onSelect = vi.fn();
    const wrapper = mount(TableInsertGrid, { props: { onSelect } });
    const cells = wrapper.findAll('[role="gridcell"]');
    const grid = wrapper.get('[role="grid"]');

    expect(cells).toHaveLength(64);
    expect(grid.classes()).toEqual(
      expect.arrayContaining(["w-60", "rounded-lg", "p-3"])
    );
    await cells[26].trigger("mouseenter");
    expect(grid.attributes("aria-label")).toContain("4 × 3");
    await cells[26].trigger("click");
    expect(onSelect).toHaveBeenCalledWith({ rows: 4, columns: 3 });
  });

  it("supports bounded keyboard navigation, confirmation, and cancellation", async () => {
    const onSelect = vi.fn();
    const onCancel = vi.fn();
    const wrapper = mount(TableInsertGrid, {
      props: { rows: 2, columns: 2, onSelect, onCancel },
    });
    const grid = wrapper.get('[role="grid"]');

    await grid.trigger("keydown", { key: "ArrowLeft" });
    await grid.trigger("keydown", { key: "ArrowUp" });
    await grid.trigger("keydown", { key: "ArrowRight" });
    await grid.trigger("keydown", { key: "ArrowDown" });
    await grid.trigger("keydown", { key: "Enter" });
    await grid.trigger("keydown", { key: "Escape" });

    expect(onSelect).toHaveBeenCalledWith({ rows: 2, columns: 2 });
    expect(onCancel).toHaveBeenCalledTimes(1);
    expect(grid.attributes("aria-activedescendant")).toMatch(/cell-2-2$/);
  });

  it("publishes active, popup, disabled, and activation states on menu buttons", async () => {
    const onActivate = vi.fn();
    const wrapper = mount(TableMenuButton, {
      props: {
        label: "Center",
        active: true,
        hasPopup: true,
        expanded: true,
        variant: "segment",
        onActivate,
      },
    });
    const button = wrapper.get("button");

    expect(button.attributes("aria-label")).toBe("Center");
    expect(button.attributes("aria-pressed")).toBe("true");
    expect(button.attributes("aria-expanded")).toBe("true");
    expect(button.attributes("data-state")).toBe("on");
    expect(button.classes()).toEqual(
      expect.arrayContaining([
        "inline-flex",
        "!min-h-[1.875rem]",
        "gap-[0.4375rem]",
        "text-[0.8125rem]",
        "leading-[1.125rem]",
        "grow",
        "shrink",
        "border",
        "border-transparent",
      ])
    );
    await button.trigger("click");
    expect(onActivate).toHaveBeenCalledTimes(1);

    await wrapper.setProps({ disabled: true });
    await button.trigger("click");
    expect(onActivate).toHaveBeenCalledTimes(1);
  });

  it("groups segmented options under their visible label", () => {
    const wrapper = mount(TableMenuSegmentedControl, {
      props: { label: "Vertical alignment" },
      slots: { default: "<button>Top</button><button>Bottom</button>" },
    });

    expect(wrapper.text()).toContain("Vertical alignment");
    expect(wrapper.get('[role="group"]').attributes("aria-label")).toBe(
      "Vertical alignment"
    );
    expect(wrapper.get("section").classes()).toEqual(
      expect.arrayContaining([
        "box-border",
        "w-full",
        "min-w-0",
        "max-w-full",
        "gap-[0.4375rem]",
        "border-b",
      ])
    );
    expect(wrapper.get('[role="group"]').classes()).toEqual(
      expect.arrayContaining([
        "flex",
        "w-full",
        "rounded-lg",
        "gap-[0.1875rem]",
        "p-[0.1875rem]",
      ])
    );
    expect(wrapper.findAll("button")).toHaveLength(2);
  });
});

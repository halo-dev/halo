import { mount } from "@vue/test-utils";
import { describe, expect, it, vi } from "vite-plus/test";
import { VTag } from "../index";

describe("Tag", () => {
  it("should render with defaults", () => {
    const tag = mount(VTag, { slots: { default: "草稿" } });

    expect(tag.classes()).toContain("tag-default");
    expect(tag.classes()).toContain("tag-sm");
    expect(tag.classes()).not.toContain("tag-soft");
    expect(tag.text()).toBe("草稿");
  });

  it("should work with theme prop", () => {
    (
      [
        "default",
        "primary",
        "secondary",
        "danger",
        "success",
        "warning",
        "info",
      ] as const
    ).forEach((theme) => {
      const tag = mount(VTag, { props: { theme } });
      expect(tag.classes()).toContain(`tag-${theme}`);
      tag.unmount();
    });
  });

  it("should work with size prop", () => {
    (["sm", "md", "lg"] as const).forEach((size) => {
      const tag = mount(VTag, { props: { size } });
      expect(tag.classes()).toContain(`tag-${size}`);
      tag.unmount();
    });
  });

  it("should work with variant and rounded props", () => {
    const tag = mount(VTag, { props: { variant: "soft", rounded: true } });

    expect(tag.classes()).toContain("tag-soft");
    expect(tag.classes()).toContain("tag-rounded");
  });

  it("should ignore the soft variant for custom colors", () => {
    const tag = mount(VTag, {
      props: { variant: "soft", color: "#4CCBA0" },
    });

    expect(tag.classes()).not.toContain("tag-soft");
    expect(tag.classes()).toContain("tag-custom");
  });

  it("should render icon slots", () => {
    const tag = mount(VTag, {
      slots: {
        default: "标签",
        leftIcon: '<i class="left-icon" />',
        rightIcon: '<i class="right-icon" />',
      },
    });

    expect(tag.find(".tag-left-icon .left-icon").exists()).toBe(true);
    expect(tag.find(".tag-right-icon .right-icon").exists()).toBe(true);
    expect(tag.find(".tag-left-icon").attributes("aria-hidden")).toBe("true");
  });

  it("should apply maxWidth and styles", () => {
    const tag = mount(VTag, {
      props: { maxWidth: "240px", styles: { opacity: "0.8" } },
    });

    expect(tag.element.style.getPropertyValue("--tag-max-width")).toBe("240px");
    expect(tag.element.style.opacity).toBe("0.8");
  });

  it("should derive a readable foreground for custom colors", () => {
    const light = mount(VTag, { props: { color: "#FDE68A" } });
    expect(light.element.style.getPropertyValue("--tag-custom-bg")).toBe(
      "#FDE68A"
    );
    expect(light.element.style.getPropertyValue("--tag-custom-fg")).toBe(
      "#0e1731"
    );

    const dark = mount(VTag, { props: { color: "#312E81" } });
    expect(dark.element.style.getPropertyValue("--tag-custom-fg")).toBe(
      "#ffffff"
    );
  });

  it("should defer to contrast-color for non-hex colors", () => {
    const tag = mount(VTag, { props: { color: "rgb(0, 0, 0)" } });

    expect(tag.element.style.getPropertyValue("--tag-custom-fg")).toBe("");
  });

  it("should emit close from the close button", async () => {
    const onClick = vi.fn();
    const tag = mount(VTag, {
      props: { closable: true },
      attrs: { onClick },
    });

    expect(tag.find(".tag-close").attributes("aria-label")).toBe("Close");

    await tag.find(".tag-close").trigger("click");

    expect(tag.emitted("close")).toHaveLength(1);
    expect(onClick).not.toHaveBeenCalled();
  });

  it("should only be interactive when requested", async () => {
    const plain = mount(VTag);
    expect(plain.attributes("role")).toBeUndefined();
    expect(plain.attributes("tabindex")).toBeUndefined();
    expect(plain.classes()).not.toContain("tag-interactive");

    const clickable = mount(VTag, { props: { clickable: true } });
    expect(clickable.attributes("role")).toBe("button");
    expect(clickable.attributes("tabindex")).toBe("0");
    expect(clickable.classes()).toContain("tag-interactive");

    await clickable.trigger("click");
    expect(clickable.emitted("click")).toHaveLength(1);

    await clickable.trigger("keydown", { key: "Enter" });
    expect(clickable.emitted("click")).toHaveLength(2);
  });

  it("should ignore interactions when disabled", async () => {
    const onClick = vi.fn();
    const tag = mount(VTag, {
      props: { clickable: true, disabled: true },
      attrs: { onClick },
    });

    expect(tag.attributes("aria-disabled")).toBe("true");
    expect(tag.attributes("tabindex")).toBeUndefined();
    expect(tag.classes()).toContain("tag-disabled");

    await tag.trigger("click");

    expect(tag.emitted("click")).toBeUndefined();
    expect(onClick).not.toHaveBeenCalled();
  });

  it("should toggle checkable tags", async () => {
    const tag = mount(VTag, { props: { checkable: true } });

    expect(tag.attributes("aria-pressed")).toBe("false");

    await tag.trigger("click");

    expect(tag.emitted("update:checked")).toEqual([[true]]);
    expect(tag.emitted("change")).toEqual([[true]]);
    expect(tag.classes()).toContain("tag-checked");
    expect(tag.attributes("aria-pressed")).toBe("true");

    await tag.trigger("click");

    expect(tag.emitted("update:checked")).toEqual([[true], [false]]);
    expect(tag.classes()).not.toContain("tag-checked");
  });

  it("should support a controlled checked state", async () => {
    const tag = mount(VTag, {
      props: { checkable: true, checked: false },
    });

    await tag.trigger("click");

    expect(tag.emitted("update:checked")).toEqual([[true]]);
    expect(tag.classes()).not.toContain("tag-checked");
  });

  it("should respect defaultChecked", () => {
    const tag = mount(VTag, {
      props: { checkable: true, defaultChecked: true },
    });

    expect(tag.classes()).toContain("tag-checked");
    expect(tag.attributes("aria-pressed")).toBe("true");
  });

  it("should use the tooltip directive instead of a native title", () => {
    const tag = mount(VTag, { props: { title: "完整名称" } });

    expect(tag.attributes("title")).toBeUndefined();
    expect(tag.classes()).toContain("v-popper--has-tooltip");
  });

  it("should not attach a tooltip without a title or truncation", () => {
    const tag = mount(VTag, { slots: { default: "草稿" } });

    expect(tag.classes()).not.toContain("v-popper--has-tooltip");
  });
});

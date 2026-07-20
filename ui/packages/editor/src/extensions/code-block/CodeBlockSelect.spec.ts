// @vitest-environment jsdom

import { mount } from "@vue/test-utils";
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import CodeBlockSelect from "./CodeBlockSelect.vue";

const options = [
  { label: "JavaScript", value: "javascript" },
  { label: "TypeScript", value: "typescript" },
];

describe("CodeBlockSelect", () => {
  beforeEach(() => {
    Object.defineProperty(Element.prototype, "scrollIntoView", {
      configurable: true,
      value: vi.fn(),
    });
  });

  afterEach(() => {
    vi.useRealTimers();
  });

  it("ignores keyboard navigation when no options match", async () => {
    vi.useFakeTimers();
    const wrapper = mount(CodeBlockSelect, {
      props: { options },
      attachTo: document.body,
    });
    const input = wrapper.get("input");

    await input.setValue("does-not-exist");
    await wrapper
      .get(".relative.inline-block.w-full")
      .trigger("keydown", { key: "ArrowDown" });
    expect(() => vi.runAllTimers()).not.toThrow();
    await wrapper
      .get(".relative.inline-block.w-full")
      .trigger("keydown", { key: "ArrowUp" });
    await wrapper
      .get(".relative.inline-block.w-full")
      .trigger("keydown", { key: "Enter" });

    expect(wrapper.emitted("update:modelValue")).toBeUndefined();
    wrapper.unmount();
  });

  it("keeps the selected index within the filtered list", async () => {
    const wrapper = mount(CodeBlockSelect, {
      props: {
        options,
        modelValue: "typescript",
      },
      attachTo: document.body,
    });
    const input = wrapper.get("input");

    await input.setValue("java");
    const setupState = (
      wrapper.vm as unknown as {
        $: { setupState: { selectedIndex: number } };
      }
    ).$.setupState;
    expect(setupState.selectedIndex).toBe(0);
    wrapper.unmount();
  });

  it("displays an option whose value is empty", () => {
    const wrapper = mount(CodeBlockSelect, {
      props: {
        options: [{ label: "None", value: "" }, ...options],
        modelValue: "",
      },
    });

    expect(wrapper.get(".text-ellipsis.text-sm").text()).toBe("None");
  });
});

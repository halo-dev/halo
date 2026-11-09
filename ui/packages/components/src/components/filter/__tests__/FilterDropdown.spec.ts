import { flushPromises, mount } from "@vue/test-utils";
import { afterEach, describe, expect, it, vi } from "vite-plus/test";
import { defineComponent, h } from "vue";
import {
  VFilterDropdown,
  type FilterDropdownOption,
  type FilterDropdownVariant,
} from "..";

const VDropdownStub = defineComponent({
  name: "VDropdown",
  emits: ["show", "hide"],
  setup(_, { slots, expose }) {
    expose({
      show: vi.fn(),
      hide: vi.fn(),
    });

    return () => h("div", [slots.default?.(), slots.popper?.()]);
  },
});

function mountDropdown(
  props: {
    modelValue?: string | number | boolean;
    options?: FilterDropdownOption[];
    searchable?: boolean;
    remoteSearch?: boolean;
    hasMore?: boolean;
    loading?: boolean;
    showClearOption?: boolean;
    clearLabel?: string;
    panelWidth?: string;
    variant?: FilterDropdownVariant;
  } = {}
) {
  return mount(VFilterDropdown, {
    props: {
      label: "Author",
      options: [
        { label: "Alice", value: "alice" },
        { label: "Bob", value: "bob" },
      ],
      ...props,
    },
    global: {
      stubs: {
        VDropdown: VDropdownStub,
      },
    },
  });
}

afterEach(() => {
  vi.useRealTimers();
});

describe("FilterDropdown", () => {
  it("renders the selected option label on the trigger", () => {
    const wrapper = mountDropdown({ modelValue: "alice" });

    expect(wrapper.get("button").text()).toContain("Author：Alice");
  });

  it("uses selectedLabel when the selected option is not available", () => {
    const wrapper = mountDropdown({
      modelValue: "alice",
      options: [],
      selectedLabel: "Alice",
    });

    expect(wrapper.get("button").text()).toContain("Author：Alice");
  });

  it("renders the clear option and uses it as the trigger label", () => {
    const wrapper = mountDropdown({
      showClearOption: true,
      clearLabel: "All",
    });

    expect(wrapper.get("button").text()).toContain("Author：All");
    expect(wrapper.findAll("[data-option-id]")).toHaveLength(3);
    expect(
      wrapper.findAll('[role="option"]')[0].attributes("aria-selected")
    ).toBe("true");
  });

  it("applies a custom panel width", () => {
    const wrapper = mountDropdown({ panelWidth: "20rem" });
    const panel = wrapper.get<HTMLElement>('div[style*="width"]');

    expect(panel.attributes("style")).toContain("width: 20rem");
    expect(panel.classes()).toContain("max-w-[calc(100vw-2rem)]");
  });

  it("applies the default trigger variant", () => {
    const wrapper = mountDropdown();
    const button = wrapper.get("button");

    expect(button.classes()).toContain("hover:bg-gray-100");
    expect(button.classes()).not.toContain("border-gray-300");
  });

  it("applies the outline trigger variant", () => {
    const wrapper = mountDropdown({ variant: "outline" });
    const button = wrapper.get("button");

    expect(button.classes()).toContain("border");
    expect(button.classes()).toContain("bg-white");
    expect(button.classes()).toContain("border-gray-300");
  });

  it("highlights the trigger while the panel is open", async () => {
    const wrapper = mountDropdown();

    wrapper.getComponent({ name: "VDropdown" }).vm.$emit("show");
    await wrapper.vm.$nextTick();

    expect(wrapper.get("button").classes()).toContain("bg-gray-100");
    expect(wrapper.get("button").classes()).toContain("text-gray-900");
  });

  it("emits undefined when selecting the clear option", async () => {
    const wrapper = mountDropdown({
      modelValue: "alice",
      showClearOption: true,
      clearLabel: "All",
    });

    await wrapper.findAll("[data-option-id]")[0].trigger("click");

    expect(wrapper.emitted("update:modelValue")?.[0]).toEqual([undefined]);
    expect(wrapper.emitted("select")?.[0]).toEqual([undefined]);
  });

  it("emits the selected value and clears the selected value on click", async () => {
    const wrapper = mountDropdown({ modelValue: "alice" });

    await wrapper.findAll('[role="option"]')[1].trigger("click");
    expect(wrapper.emitted("update:modelValue")?.[0]).toEqual(["bob"]);

    await wrapper.findAll('[role="option"]')[0].trigger("click");
    expect(wrapper.emitted("update:modelValue")?.[1]).toEqual([undefined]);
    expect(wrapper.emitted("select")?.[1]).toEqual([undefined]);
  });

  it("filters options locally when searchable", async () => {
    const wrapper = mountDropdown({ searchable: true });

    await wrapper.get("input").setValue("bo");

    const options = wrapper.findAll('[role="option"]');
    expect(options).toHaveLength(1);
    expect(options[0].text()).toContain("Bob");
  });

  it("does not filter options locally in remote search mode", async () => {
    const wrapper = mountDropdown({ remoteSearch: true });

    await wrapper.get("input").setValue("zzz");

    expect(wrapper.findAll('[role="option"]')).toHaveLength(2);
  });

  it("renders groups", () => {
    const wrapper = mountDropdown({
      options: [
        {
          label: "Recent",
          options: [
            { label: "Alice", value: "alice" },
            { label: "Bob", value: "bob" },
          ],
        },
      ],
    });

    expect(wrapper.text()).toContain("Recent");
    expect(wrapper.findAll('[role="option"]')).toHaveLength(2);
  });

  it("collapses and expands tree nodes", async () => {
    const wrapper = mountDropdown({
      options: [
        {
          label: "Frontend",
          children: [{ label: "Vue", value: "vue" }],
        },
      ],
    });

    expect(wrapper.findAll("[data-option-id]")).toHaveLength(2);

    await wrapper.findAll("[data-option-id]")[0].trigger("click");
    expect(wrapper.findAll("[data-option-id]")).toHaveLength(1);

    await wrapper.findAll("[data-option-id]")[0].trigger("click");
    expect(wrapper.findAll("[data-option-id]")).toHaveLength(2);
  });

  it("supports keyboard navigation", async () => {
    const wrapper = mountDropdown({ searchable: true });
    const input = wrapper.get("input");

    await input.trigger("keydown", { key: "ArrowDown" });
    await input.trigger("keydown", { key: "Enter" });

    expect(wrapper.emitted("update:modelValue")?.[0]).toEqual(["alice"]);
  });

  it("debounces remote search", async () => {
    vi.useFakeTimers();

    const wrapper = mountDropdown({ remoteSearch: true });

    await wrapper.get("input").setValue("ali");
    expect(wrapper.emitted("search")).toBeUndefined();

    await vi.advanceTimersByTimeAsync(300);
    expect(wrapper.emitted("search")?.[0]).toEqual(["ali"]);
  });

  it("emits load-more when opening a short list with more pages", async () => {
    const wrapper = mountDropdown({ hasMore: true });

    wrapper.findComponent(VDropdownStub).vm.$emit("show");
    await flushPromises();

    expect(wrapper.emitted("load-more")).toHaveLength(1);
  });

  it("emits load-more when scrolled near the bottom", async () => {
    const wrapper = mountDropdown({ hasMore: true });
    const list = wrapper.get('[role="listbox"]');

    Object.defineProperties(list.element, {
      scrollHeight: { value: 200 },
      clientHeight: { value: 100 },
      scrollTop: { value: 60 },
    });

    await list.trigger("scroll");

    expect(wrapper.emitted("load-more")).toHaveLength(1);
  });
});

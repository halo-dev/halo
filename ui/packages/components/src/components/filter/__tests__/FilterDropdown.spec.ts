import { flushPromises, mount } from "@vue/test-utils";
import { afterEach, describe, expect, it, vi } from "vite-plus/test";
import { defineComponent, h, nextTick, reactive } from "vue";
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
    searchDelay?: number;
    variant?: FilterDropdownVariant;
  } = {},
  options: { attachTo?: HTMLElement } = {}
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
    ...(options.attachTo ? { attachTo: options.attachTo } : {}),
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

  it("matches option keywords in local search", async () => {
    const wrapper = mountDropdown({
      searchable: true,
      options: [
        { label: "Apple", value: "apple", keywords: ["tag-4fxvldaw"] },
        { label: "Banana", value: "banana" },
      ],
    });

    await wrapper.get("input").setValue("tag-4fxvldaw");

    const options = wrapper.findAll('[role="option"]');
    expect(options).toHaveLength(1);
    expect(options[0].text()).toContain("Apple");
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

  it("does not persist collapse state toggled while searching", async () => {
    const wrapper = mountDropdown({
      searchable: true,
      options: [
        {
          label: "Frontend",
          children: [{ label: "Vue", value: "vue" }],
        },
        {
          label: "Backend",
          children: [{ label: "Java", value: "java" }],
        },
      ],
    });
    const input = wrapper.get("input");

    await input.setValue("Backend");

    const backendRow = wrapper
      .findAll("[data-option-id]")
      .find((row) => row.text().includes("Backend"));
    await backendRow!.trigger("click");

    await input.setValue("");

    expect(wrapper.findAll("[data-option-id]")).toHaveLength(4);
  });

  it("supports keyboard navigation", async () => {
    const wrapper = mountDropdown({ searchable: true });
    const input = wrapper.get("input");

    await input.trigger("keydown", { key: "ArrowDown" });
    await input.trigger("keydown", { key: "Enter" });

    expect(wrapper.emitted("update:modelValue")?.[0]).toEqual(["alice"]);
  });

  it("expands and collapses tree nodes from the search input", async () => {
    const wrapper = mountDropdown({
      searchable: true,
      options: [
        {
          label: "Frontend",
          children: [{ label: "Vue", value: "vue" }],
        },
      ],
    });
    const input = wrapper.get("input");

    await input.trigger("keydown", { key: "ArrowDown" });
    expect(wrapper.findAll("[data-option-id]")).toHaveLength(2);

    await input.trigger("keydown", { key: "ArrowLeft" });
    expect(wrapper.findAll("[data-option-id]")).toHaveLength(1);

    await input.trigger("keydown", { key: "ArrowRight" });
    expect(wrapper.findAll("[data-option-id]")).toHaveLength(2);
  });

  it("keeps horizontal arrows for text editing while searching a tree", async () => {
    const wrapper = mountDropdown({
      searchable: true,
      options: [
        {
          label: "Frontend",
          children: [{ label: "Vue", value: "vue" }],
        },
      ],
    });
    const input = wrapper.get("input");

    await input.setValue("Frontend");

    const left = new KeyboardEvent("keydown", {
      key: "ArrowLeft",
      bubbles: true,
      cancelable: true,
    });
    input.element.dispatchEvent(left);

    expect(left.defaultPrevented).toBe(false);
    expect(wrapper.findAll("[data-option-id]")).toHaveLength(2);

    const right = new KeyboardEvent("keydown", {
      key: "ArrowRight",
      bubbles: true,
      cancelable: true,
    });
    input.element.dispatchEvent(right);

    expect(right.defaultPrevented).toBe(false);
    expect(wrapper.findAll("[data-option-id]")).toHaveLength(2);
  });

  it("exposes the popup type on the search input", () => {
    const searchable = mountDropdown({ searchable: true });
    expect(searchable.get("input").attributes("aria-haspopup")).toBe("listbox");

    const tree = mountDropdown({
      searchable: true,
      options: [
        {
          label: "Frontend",
          children: [{ label: "Vue", value: "vue" }],
        },
      ],
    });
    expect(tree.get("input").attributes("aria-haspopup")).toBe("tree");
  });

  it("focuses the search input after the popper is applied", async () => {
    const wrapper = mountDropdown(
      { searchable: true },
      { attachTo: document.body }
    );

    wrapper.findComponent(VDropdownStub).vm.$emit("apply-show");
    await nextTick();

    expect(document.activeElement).toBe(wrapper.get("input").element);
  });

  it("restores focus to the trigger when closed with Escape", async () => {
    const wrapper = mountDropdown(
      { searchable: true },
      { attachTo: document.body }
    );
    const input = wrapper.get("input");
    const trigger = wrapper.get("button");

    wrapper.findComponent(VDropdownStub).vm.$emit("apply-show");
    await nextTick();
    expect(document.activeElement).toBe(input.element);

    await input.trigger("keydown", { key: "Escape" });
    await nextTick();

    expect(document.activeElement).toBe(trigger.element);
  });

  it("stops the Escape keyup that dismissed the panel", async () => {
    const onKeyup = vi.fn();
    document.body.addEventListener("keyup", onKeyup);

    try {
      const wrapper = mountDropdown(
        { searchable: true },
        { attachTo: document.body }
      );

      wrapper.findComponent(VDropdownStub).vm.$emit("apply-show");
      await nextTick();

      await wrapper.get("input").trigger("keydown", { key: "Escape" });
      await nextTick();

      wrapper
        .get("button")
        .element.dispatchEvent(
          new KeyboardEvent("keyup", { key: "Escape", bubbles: true })
        );

      expect(onKeyup).not.toHaveBeenCalled();
    } finally {
      document.body.removeEventListener("keyup", onKeyup);
    }
  });

  it("lets Escape keyup bubble when the panel was not dismissed by Escape", async () => {
    const onKeyup = vi.fn();
    document.body.addEventListener("keyup", onKeyup);

    try {
      const wrapper = mountDropdown(
        { searchable: true },
        { attachTo: document.body }
      );

      wrapper
        .get("button")
        .element.dispatchEvent(
          new KeyboardEvent("keyup", { key: "Escape", bubbles: true })
        );

      expect(onKeyup).toHaveBeenCalledTimes(1);
    } finally {
      document.body.removeEventListener("keyup", onKeyup);
    }
  });

  it("lets Escape keyup bubble after a dismissed panel that never received a keyup", async () => {
    const onKeyup = vi.fn();
    document.body.addEventListener("keyup", onKeyup);

    try {
      const wrapper = mountDropdown(
        { searchable: true },
        { attachTo: document.body }
      );

      wrapper.findComponent(VDropdownStub).vm.$emit("apply-show");
      await nextTick();

      await wrapper.get("input").trigger("keydown", { key: "Escape" });
      await nextTick();

      await wrapper.get("button").trigger("keydown", { key: "Escape" });

      wrapper
        .get("button")
        .element.dispatchEvent(
          new KeyboardEvent("keyup", { key: "Escape", bubbles: true })
        );

      expect(onKeyup).toHaveBeenCalledTimes(1);
    } finally {
      document.body.removeEventListener("keyup", onKeyup);
    }
  });

  it("restores focus to the trigger after selecting an option", async () => {
    const wrapper = mountDropdown(
      { searchable: true },
      { attachTo: document.body }
    );
    const trigger = wrapper.get("button");

    wrapper.findComponent(VDropdownStub).vm.$emit("apply-show");
    await nextTick();

    await wrapper.findAll('[role="option"]')[0].trigger("click");
    await nextTick();

    expect(document.activeElement).toBe(trigger.element);
  });

  it("restores focus to the trigger when closed with Tab", async () => {
    const wrapper = mountDropdown(
      { searchable: true },
      { attachTo: document.body }
    );
    const input = wrapper.get("input");
    const trigger = wrapper.get("button");

    wrapper.findComponent(VDropdownStub).vm.$emit("apply-show");
    await nextTick();

    await input.trigger("keydown", { key: "Tab" });
    await nextTick();

    expect(document.activeElement).toBe(trigger.element);
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

    wrapper.findComponent(VDropdownStub).vm.$emit("apply-show");
    await flushPromises();

    expect(wrapper.emitted("load-more")).toHaveLength(1);
  });

  it("emits load-more again when nested options grow without filling the panel", async () => {
    const wrapper = mountDropdown({
      hasMore: true,
      options: [
        {
          label: "Recent",
          options: [{ label: "Alice", value: "alice" }],
        },
      ],
    });

    wrapper.findComponent(VDropdownStub).vm.$emit("show");
    await nextTick();
    wrapper.findComponent(VDropdownStub).vm.$emit("apply-show");
    await flushPromises();

    expect(wrapper.emitted("load-more")).toHaveLength(1);

    await wrapper.setProps({
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
    await flushPromises();

    expect(wrapper.emitted("load-more")).toHaveLength(2);
  });

  it("emits load-more when options are appended in place", async () => {
    const options = reactive<FilterDropdownOption[]>([
      {
        label: "Frontend",
        children: [{ label: "Vue", value: "vue" }],
      },
    ]);

    const wrapper = mountDropdown({ hasMore: true, options });

    wrapper.findComponent(VDropdownStub).vm.$emit("show");
    await nextTick();
    wrapper.findComponent(VDropdownStub).vm.$emit("apply-show");
    await flushPromises();

    expect(wrapper.emitted("load-more")).toHaveLength(1);

    options[0].children?.push({ label: "React", value: "react" });
    await nextTick();
    await flushPromises();

    expect(wrapper.emitted("load-more")).toHaveLength(2);
  });

  it("resets the autoload signature when the search keyword changes", async () => {
    const page = Array.from({ length: 8 }, (_, index) => ({
      label: `User ${index + 1}`,
      value: `user-${index + 1}`,
    }));

    const wrapper = mountDropdown({
      searchable: true,
      remoteSearch: true,
      hasMore: true,
      options: page,
      searchDelay: 10000,
    });

    wrapper.findComponent(VDropdownStub).vm.$emit("show");
    await nextTick();
    wrapper.findComponent(VDropdownStub).vm.$emit("apply-show");
    await flushPromises();

    expect(wrapper.emitted("load-more")).toHaveLength(1);

    await wrapper.get("input").setValue("zzz");
    await wrapper.setProps({
      options: [{ label: "Only", value: "only" }],
      hasMore: false,
    });
    await flushPromises();

    await wrapper.get("input").setValue("");
    await wrapper.setProps({ options: page, hasMore: true });
    await flushPromises();

    expect(wrapper.emitted("load-more")).toHaveLength(2);

    wrapper.unmount();
  });

  it("emits load-more after collapsing a tree branch", async () => {
    const children = (count: number, prefix: string) =>
      Array.from({ length: count }, (_, index) => ({
        label: `${prefix} ${index + 1}`,
        value: `${prefix.toLowerCase()}-${index + 1}`,
      }));

    const wrapper = mountDropdown({
      hasMore: true,
      options: [
        { label: "Frontend", children: children(6, "FE") },
        { label: "Backend", children: children(2, "BE") },
      ],
    });

    const list = wrapper.get('[role="tree"]');

    Object.defineProperty(list.element, "clientHeight", {
      configurable: true,
      get: () => 100,
    });
    Object.defineProperty(list.element, "scrollHeight", {
      configurable: true,
      get: () => wrapper.findAll("[data-option-id]").length * 20,
    });

    wrapper.findComponent(VDropdownStub).vm.$emit("show");
    await nextTick();
    wrapper.findComponent(VDropdownStub).vm.$emit("apply-show");
    await flushPromises();

    expect(wrapper.emitted("load-more")).toBeUndefined();

    const frontendRow = wrapper
      .findAll("[data-option-id]")
      .find((row) => row.text().includes("Frontend"));
    await frontendRow!.trigger("click");
    await flushPromises();

    expect(wrapper.findAll("[data-option-id]")).toHaveLength(4);
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

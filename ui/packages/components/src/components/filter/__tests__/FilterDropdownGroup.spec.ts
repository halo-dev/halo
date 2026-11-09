/* eslint-disable vue/one-component-per-file -- Test-local host and dropdown stubs keep this integration test isolated. */
import { mount } from "@vue/test-utils";
import { describe, expect, it, vi } from "vite-plus/test";
import { defineComponent, h, nextTick, ref } from "vue";
import { VFilterDropdown, VFilterDropdownGroup } from "..";

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

const clearButtonSelector = 'button[aria-label="Reset filters"]';

function mountGroup(initial: { author?: string; status?: string } = {}) {
  const author = ref<string | undefined>(initial.author);
  const status = ref<string | undefined>(initial.status);

  const Host = defineComponent({
    setup() {
      return () =>
        h(VFilterDropdownGroup, null, {
          default: () => [
            h(VFilterDropdown, {
              modelValue: author.value,
              "onUpdate:modelValue": (value: string | undefined) => {
                author.value = value;
              },
              label: "Author",
              options: [{ label: "Alice", value: "alice" }],
            }),
            h(VFilterDropdown, {
              modelValue: status.value,
              "onUpdate:modelValue": (value: string | undefined) => {
                status.value = value;
              },
              label: "Status",
              options: [{ label: "Published", value: "published" }],
            }),
          ],
        });
    },
  });

  const wrapper = mount(Host, {
    global: {
      stubs: {
        VDropdown: VDropdownStub,
      },
    },
  });

  return { wrapper, author, status };
}

describe("FilterDropdownGroup", () => {
  it("uses sm spacing by default and supports custom spacing", () => {
    const wrapper = mount(VFilterDropdownGroup);

    expect(wrapper.get(".filter-dropdown-group").attributes("style")).toContain(
      "gap: 4px"
    );

    const spaced = mount(VFilterDropdownGroup, {
      props: { spacing: "lg" },
    });

    expect(spaced.get(".filter-dropdown-group").attributes("style")).toContain(
      "gap: 12px"
    );
  });

  it("does not render the clear button when no dropdown has a value", () => {
    const { wrapper } = mountGroup();

    expect(wrapper.find(clearButtonSelector).exists()).toBe(false);
  });

  it("clears all dropdowns when the clear button is clicked", async () => {
    const { wrapper, author, status } = mountGroup({
      author: "alice",
      status: "published",
    });

    await nextTick();
    expect(wrapper.find(clearButtonSelector).exists()).toBe(true);

    await wrapper.get(clearButtonSelector).trigger("click");

    expect(author.value).toBeUndefined();
    expect(status.value).toBeUndefined();
    expect(
      wrapper.findComponent(VFilterDropdownGroup).emitted("clear")
    ).toHaveLength(1);

    await nextTick();
    expect(wrapper.find(clearButtonSelector).exists()).toBe(false);
  });

  it("renders a custom clear slot", async () => {
    const wrapper = mount(VFilterDropdownGroup, {
      slots: {
        clear: `<button data-testid="custom-clear">Clear</button>`,
      },
      global: {
        stubs: {
          VDropdown: VDropdownStub,
        },
      },
    });

    expect(wrapper.find('[data-testid="custom-clear"]').exists()).toBe(true);
    expect(wrapper.find(clearButtonSelector).exists()).toBe(false);
  });

  it("supports external filters and emits clear", async () => {
    const wrapper = mount(VFilterDropdownGroup, {
      props: {
        hasExternalFilters: true,
      },
    });

    expect(wrapper.find(clearButtonSelector).exists()).toBe(true);

    await wrapper.get(clearButtonSelector).trigger("click");

    expect(wrapper.emitted("clear")).toHaveLength(1);
  });

  it("emits clear when clearAll is called programmatically", () => {
    const wrapper = mount(VFilterDropdownGroup);

    (wrapper.vm as unknown as { clearAll: () => void }).clearAll();

    expect(wrapper.emitted("clear")).toHaveLength(1);
  });
});

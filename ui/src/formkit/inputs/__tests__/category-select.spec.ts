import { utils } from "@halo-dev/ui-shared";
import { flushPromises, mount } from "@vue/test-utils";
import { beforeEach, describe, expect, it, vi } from "vite-plus/test";
import { defineComponent, h, reactive } from "vue";
import { createI18n } from "vue-i18n";
import HasPermission from "@/components/permission/HasPermission.vue";
import CategorySelect from "../category-select/CategorySelect.vue";

vi.mock("@/locales", () => ({
  i18n: { global: { t: (key: string) => key } },
}));

vi.mock(
  "@console/modules/contents/posts/categories/composables/use-post-category",
  () => ({
    usePostCategory: vi.fn(() => ({
      categories: { value: [] },
      categoriesTree: { value: [] },
      handleFetchCategories: vi.fn(),
    })),
  })
);

const DropdownStub = defineComponent({
  name: "VDropdown",
  setup(_, { slots }) {
    return () => h("div", [slots.default?.(), slots.popper?.()]);
  },
});

const messages = {
  "core.formkit.category_select.creation_label": "Create {text}",
};

function mountCategorySelect() {
  const context = reactive({
    _value: undefined,
    attrs: {},
    classes: {
      "post-categories-wrapper": "wrapper",
      "post-categories": "categories",
      input: "input",
      "post-categories-button": "button",
      "dropdown-wrapper": "dropdown",
    },
    multiple: false,
    excludedNames: [],
    allowCreate: true,
    options: [],
    node: {
      input: vi.fn(),
      props: { disabled: false } as Record<string, unknown>,
      context: undefined,
    },
  });
  context.node.context = context;

  return mount(CategorySelect, {
    props: { context },
    global: {
      plugins: [
        createI18n({
          legacy: false,
          locale: "en",
          messages: { en: messages },
        }),
      ],
      components: { HasPermission },
      stubs: { VDropdown: DropdownStub },
    },
  });
}

describe("CategorySelect", () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it("offers to create a category only with category manage permission", async () => {
    utils.permission.setUserPermissions(["system:categories:view"]);

    const wrapper = mountCategorySelect();
    await wrapper.find("input").setValue("new category");
    await flushPromises();

    expect(wrapper.find("#category-create").exists()).toBe(false);
    wrapper.unmount();
  });

  it("renders the create item when category manage permission is present", async () => {
    utils.permission.setUserPermissions([
      "system:categories:view",
      "system:categories:manage",
    ]);

    const wrapper = mountCategorySelect();
    await wrapper.find("input").setValue("new category");
    await flushPromises();

    expect(wrapper.find("#category-create").exists()).toBe(true);
    wrapper.unmount();
  });

  it("disables the input and skips loading when category view permission is missing", async () => {
    utils.permission.setUserPermissions([]);

    const wrapper = mountCategorySelect();
    await flushPromises();

    expect(wrapper.find("input").attributes("disabled")).toBeDefined();
    expect(wrapper.find("input").attributes("placeholder")).toBeTruthy();
    wrapper.unmount();
  });
});

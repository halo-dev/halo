/* eslint-disable vue/one-component-per-file -- Test-local stubs keep this form test isolated. */
import type { Menu, MenuItem } from "@halo-dev/api-client";
import messages from "@intlify/unplugin-vue-i18n/messages";
import { mount } from "@vue/test-utils";
import { beforeEach, describe, expect, it, vi } from "vite-plus/test";
import { defineComponent, h, nextTick } from "vue";
import { createI18n } from "vue-i18n";
import MenuItemEditingModal from "../MenuItemEditingModal.vue";

const apiMocks = vi.hoisted(() => ({
  createMenuItem: vi.fn(),
  updateMenuItem: vi.fn(),
}));

vi.mock("@halo-dev/api-client", async () => {
  const actual = await vi.importActual("@halo-dev/api-client");
  return {
    ...actual,
    MenuItemSpecRouteRefEnum: {
      Archives: "archives",
      Categories: "categories",
      Tags: "tags",
    },
    consoleApiClient: { menuItem: {} },
    coreApiClient: {
      menuItem: {
        createMenuItem: apiMocks.createMenuItem,
        updateMenuItem: apiMocks.updateMenuItem,
      },
    },
  };
});

const FormKitStub = defineComponent({
  name: "FormKit",
  props: {
    disabled: Boolean,
    label: { type: String, default: undefined },
    name: { type: String, default: undefined },
    options: { type: Array, default: () => [] },
  },
  setup(props, { slots }) {
    return () => h("div", { "data-name": props.name }, slots.default?.());
  },
});

const AnnotationsFormStub = defineComponent({
  name: "AnnotationsForm",
  setup(_, { expose }) {
    expose({
      handleSubmit: vi.fn(),
      specFormInvalid: false,
      customFormInvalid: false,
      annotations: {},
      customAnnotations: {},
    });
    return () => h("div");
  },
});

const VModalStub = defineComponent({
  name: "VModal",
  emits: ["close"],
  setup(_, { emit, expose, slots }) {
    expose({ close: () => emit("close") });
    return () => h("div", [slots.default?.(), slots.footer?.()]);
  },
});

const menu: Menu = {
  apiVersion: "v1alpha1",
  kind: "Menu",
  metadata: { name: "primary" },
  spec: { displayName: "Primary" },
};

function mountModal(menuItem?: MenuItem) {
  return mount(MenuItemEditingModal, {
    props: { menu, menuItem },
    global: {
      plugins: [
        createI18n({
          legacy: false,
          locale: "en",
          messages,
        }),
      ],
      stubs: {
        AnnotationsForm: AnnotationsFormStub,
        Button: true,
        FormKit: FormKitStub,
        MenuItemParentSelect: true,
        Modal: VModalStub,
        Space: { template: "<div><slot /></div>" },
        SubmitButton: true,
        VButton: true,
        VModal: VModalStub,
        VSpace: { template: "<div><slot /></div>" },
      },
    },
  });
}

function existingMenuItem(
  spec: MenuItem["spec"],
  status?: MenuItem["status"]
): MenuItem {
  return {
    apiVersion: "v1alpha1",
    kind: "MenuItem",
    metadata: { name: "item" },
    spec,
    status,
  };
}

describe("MenuItemEditingModal", () => {
  beforeEach(() => vi.clearAllMocks());

  it("creates a route-bound item without an href or targetRef", async () => {
    apiMocks.createMenuItem.mockImplementation(({ menuItem }) =>
      Promise.resolve({ data: menuItem })
    );
    const wrapper = mountModal();
    const state = wrapper.vm.$.setupState as Record<string, unknown>;

    state.selectedSourceValue = "archives";
    (state.onMenuItemSourceChange as () => void)();
    await nextTick();
    await (state.handleSaveMenuItem as () => Promise<void>)();

    const saved = apiMocks.createMenuItem.mock.calls[0][0].menuItem as MenuItem;
    expect(saved.spec).toMatchObject({
      displayName: "Article archives",
      routeRef: "archives",
    });
    expect(saved.spec.href).toBeUndefined();
    expect(saved.spec.targetRef).toBeUndefined();
  });

  it("preserves the display name while switching or converting to routes", async () => {
    const wrapper = mountModal(
      existingMenuItem({ displayName: "Writing", href: "/writing" })
    );
    const state = wrapper.vm.$.setupState as Record<string, unknown>;

    state.selectedSourceValue = "tags";
    (state.onMenuItemSourceChange as () => void)();
    state.selectedSourceValue = "categories";
    (state.onMenuItemSourceChange as () => void)();
    await nextTick();

    const formState = state.formState as MenuItem;
    expect(formState.spec.displayName).toBe("Writing");
    expect(formState.spec.routeRef).toBe("categories");
    expect(formState.spec.href).toBeUndefined();
    expect(
      wrapper
        .findAllComponents(FormKitStub)
        .find((input) => input.props("name") === "routeHref")
    ).toBeUndefined();
  });

  it("copies the resolved link when unbinding to a custom link", () => {
    const wrapper = mountModal(
      existingMenuItem(
        { displayName: "Writing", routeRef: "archives" },
        { displayName: "Writing", href: "/writing" }
      )
    );
    const state = wrapper.vm.$.setupState as Record<string, unknown>;

    state.selectedSourceValue = "custom";
    (state.onMenuItemSourceChange as () => void)();

    const formState = state.formState as MenuItem;
    expect(formState.spec.href).toBe("/writing");
    expect(formState.spec.routeRef).toBeUndefined();
    expect(formState.spec.displayName).toBe("Writing");
  });

  it("keeps resource sources locked and hides derived route links", async () => {
    const resourceWrapper = mountModal(
      existingMenuItem({
        targetRef: {
          group: "content.halo.run",
          version: "v1alpha1",
          kind: "Post",
          name: "post",
        },
      })
    );
    const resourceSource = resourceWrapper
      .findAllComponents(FormKitStub)
      .find((input) => input.props("label") === "Type");
    expect(resourceSource?.props("disabled")).toBe(true);

    const routeWrapper = mountModal(
      existingMenuItem(
        { displayName: "Writing", routeRef: "archives" },
        { displayName: "Writing", href: "/writing" }
      )
    );
    await nextTick();
    const routeInputs = routeWrapper.findAllComponents(FormKitStub);
    const routeSource = routeInputs.find(
      (input) => input.props("label") === "Type"
    );
    expect(routeSource?.props("disabled")).toBe(false);
    expect(routeSource?.props("options")).toHaveLength(4);
    const resolvedLink = routeInputs.find(
      (input) => input.props("name") === "routeHref"
    );
    expect(resolvedLink).toBeUndefined();
  });
});

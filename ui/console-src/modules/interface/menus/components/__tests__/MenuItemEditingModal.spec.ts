/* eslint-disable vue/one-component-per-file -- Test-local stubs keep this form test isolated. */
import type { Menu, MenuItem, Ref } from "@halo-dev/api-client";
import { Toast } from "@halo-dev/components";
import messages from "@intlify/unplugin-vue-i18n/messages";
import { enableAutoUnmount, mount } from "@vue/test-utils";
import {
  afterEach,
  beforeEach,
  describe,
  expect,
  it,
  vi,
} from "vite-plus/test";
import { defineComponent, h, nextTick, type PropType } from "vue";
import { createI18n } from "vue-i18n";
import MenuItemEditingModal from "../MenuItemEditingModal.vue";

const apiMocks = vi.hoisted(() => ({
  createMenuItem: vi.fn(),
  updateMenuItem: vi.fn(),
  updateMenuItemPosition: vi.fn(),
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
    consoleApiClient: {
      menuItem: { updateMenuItemPosition: apiMocks.updateMenuItemPosition },
    },
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
    validation: { type: String, default: undefined },
  },
  setup(props, { slots }) {
    return () => h("div", { "data-name": props.name }, slots.default?.());
  },
});

const AnnotationsFormStub = defineComponent({
  name: "AnnotationsForm",
  props: {
    value: {
      type: Object as PropType<Record<string, string>>,
      default: () => ({}),
    },
    formData: { type: Object as PropType<MenuItem>, required: true },
  },
  setup(props, { expose }) {
    expose({
      handleSubmit: vi.fn(),
      specFormInvalid: false,
      customFormInvalid: false,
      annotations: { ...props.value },
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
  enableAutoUnmount(afterEach);
  beforeEach(() => {
    vi.resetAllMocks();
    vi.spyOn(Toast, "success").mockImplementation(() => "");
    vi.spyOn(Toast, "error").mockImplementation(() => "");
    apiMocks.updateMenuItem.mockImplementation(({ menuItem }) =>
      Promise.resolve({ data: menuItem })
    );
    apiMocks.createMenuItem.mockImplementation(({ menuItem }) =>
      Promise.resolve({ data: menuItem })
    );
  });
  afterEach(() => vi.restoreAllMocks());

  it("creates a route-bound item without an href or targetRef", async () => {
    apiMocks.createMenuItem.mockImplementation(({ menuItem }) =>
      Promise.resolve({ data: menuItem })
    );
    const wrapper = mountModal();
    const state = wrapper.vm.$.setupState as Record<string, unknown>;

    state.selectedSourceValue = "archives";
    await nextTick();
    await (state.handleSaveMenuItem as () => Promise<void>)();

    const saved = apiMocks.createMenuItem.mock.calls[0][0].menuItem as MenuItem;
    expect(saved.spec).toMatchObject({
      displayName: "Article archives",
      routeRef: "archives",
    });
    expect(saved.spec.href).toBeUndefined();
    expect(saved.spec.targetRef).toBeUndefined();
    const displayNameInput = wrapper
      .findAllComponents(FormKitStub)
      .find((input) => input.props("name") === "displayName");
    expect(displayNameInput?.props("validation")).toBe(
      "required:trim|length:0,100"
    );
  });

  it("preserves the display name while switching or converting to routes", async () => {
    const wrapper = mountModal(
      existingMenuItem({ displayName: "Writing", href: "/writing" })
    );
    const state = wrapper.vm.$.setupState as Record<string, unknown>;

    state.selectedSourceValue = "tags";
    state.selectedSourceValue = "categories";
    await nextTick();

    const formState = state.normalizedMenuItem as MenuItem;
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

    const formState = state.normalizedMenuItem as MenuItem;
    expect(formState.spec.href).toBe("/writing");
    expect(formState.spec.routeRef).toBeUndefined();
    expect(formState.spec.displayName).toBe("Writing");
  });

  it("preserves unsupported resource references when saving", async () => {
    apiMocks.updateMenuItem.mockImplementation(({ menuItem }) =>
      Promise.resolve({ data: menuItem })
    );
    const targetRef = {
      group: "example.halo.run",
      version: "v1alpha1",
      kind: "ExternalContent",
      name: "external-content",
    };
    const wrapper = mountModal(existingMenuItem({ targetRef }));
    const state = wrapper.vm.$.setupState as Record<string, unknown>;

    await (state.handleSaveMenuItem as () => Promise<void>)();

    const saved = apiMocks.updateMenuItem.mock.calls[0][0].menuItem as MenuItem;
    expect(saved.spec.targetRef).toEqual(targetRef);
    expect(saved.spec.routeRef).toBeUndefined();
  });

  it("offers all sources during editing and hides derived route links", async () => {
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
    expect(resourceSource?.props("disabled")).toBe(false);
    expect(resourceSource?.props("options")).toHaveLength(8);

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
    expect(routeSource?.props("options")).toHaveLength(8);
    const resolvedLink = routeInputs.find(
      (input) => input.props("name") === "routeHref"
    );
    expect(resolvedLink).toBeUndefined();
  });

  const resourceRef = (kind = "Post"): Ref => ({
    group: "content.halo.run",
    version: "v1alpha1",
    kind,
    name: "original",
  });
  const sources: MenuItem["spec"][] = [
    { displayName: "Writing", href: "/writing" },
    { displayName: "Writing", routeRef: "archives" },
    ...["Post", "SinglePage", "Category", "Tag"].map((kind) => ({
      targetRef: resourceRef(kind),
    })),
  ];

  it.each(sources)(
    "converts every supported source without replacing the item: %j",
    async (spec) => {
      const item = existingMenuItem(
        { ...spec, parent: "parent", priority: 3, target: "_blank" },
        { displayName: "Writing", href: "/writing" }
      );
      item.metadata.annotations = { note: "keep" };
      const wrapper = mountModal(item);
      const state = wrapper.vm.$.setupState as Record<string, unknown>;
      for (const source of [
        "custom",
        "archives",
        "categories",
        "tags",
        "Post",
        "SinglePage",
        "Category",
        "Tag",
      ]) {
        state.selectedSourceValue = source;
        if (["Post", "SinglePage", "Category", "Tag"].includes(source)) {
          state.selectedRefName = "selected";
        }
        await (state.handleSaveMenuItem as () => Promise<void>)();
        const saved = apiMocks.updateMenuItem.mock.lastCall![0]
          .menuItem as MenuItem;
        expect(saved.metadata).toEqual(item.metadata);
        expect(saved.spec).toMatchObject({
          parent: "parent",
          priority: 3,
          target: "_blank",
          menuName: "primary",
        });
        if (source === "custom") {
          expect(saved.spec).toMatchObject({
            displayName: "Writing",
            href: "/writing",
          });
          expect(saved.spec.targetRef).toBeUndefined();
          expect(saved.spec.routeRef).toBeUndefined();
        } else if (["archives", "categories", "tags"].includes(source)) {
          expect(saved.spec).toMatchObject({
            displayName: "Writing",
            routeRef: source,
          });
          expect(saved.spec.targetRef).toBeUndefined();
          expect(saved.spec.href).toBeUndefined();
        } else {
          expect(saved.spec.targetRef).toEqual({
            ...resourceRef(source),
            name: "selected",
          });
          expect(saved.spec.routeRef).toBeUndefined();
          expect(saved.spec.displayName).toBeUndefined();
          expect(saved.spec.href).toBeUndefined();
        }
      }
      expect(apiMocks.createMenuItem).not.toHaveBeenCalled();
      expect(apiMocks.updateMenuItemPosition).not.toHaveBeenCalled();
      expect(item.spec).toMatchObject(spec);
    }
  );

  it.each([
    { ...resourceRef(), group: "example.halo.run" },
    { ...resourceRef(), version: "v2" },
    { ...resourceRef(), kind: "ExternalContent" },
  ])(
    "keeps opaque references and source fields untouched: %j",
    async (targetRef) => {
      const spec: MenuItem["spec"] = {
        targetRef,
        href: "/opaque",
        displayName: "Opaque",
      };
      const wrapper = mountModal(existingMenuItem(spec));
      const sourceInput = wrapper
        .findAllComponents(FormKitStub)
        .find((input) => input.props("label") === "Type")!;
      expect(sourceInput.props("disabled")).toBe(true);
      expect(sourceInput.props("options")).toEqual([
        {
          value: "unsupported",
          label: `${targetRef.group}/${targetRef.version}/${targetRef.kind}`,
        },
      ]);
      const state = wrapper.vm.$.setupState as Record<string, unknown>;
      await (state.handleSaveMenuItem as () => Promise<void>)();
      expect(
        apiMocks.updateMenuItem.mock.lastCall![0].menuItem.spec
      ).toMatchObject(spec);
    }
  );

  it.each([undefined, existingMenuItem({ displayName: "Old", href: "/old" })])(
    "preserves typed and cleared text across round trips",
    (item) => {
      const wrapper = mountModal(item);
      const state = wrapper.vm.$.setupState as Record<string, unknown>;
      const draft = state.formState as MenuItem;
      for (const [displayName, href] of [
        ["New", "/new"],
        ["", ""],
      ]) {
        draft.spec.displayName = displayName;
        draft.spec.href = href;
        for (const source of ["archives", "Post", "tags", "custom"]) {
          state.selectedSourceValue = source;
        }
        expect(draft.spec).toMatchObject({ displayName, href });
      }
    }
  );

  it("retains the original resource until the source actually changes", async () => {
    const targetRef = resourceRef();
    const wrapper = mountModal(existingMenuItem({ targetRef }));
    const state = wrapper.vm.$.setupState as Record<string, unknown>;
    await (state.handleSaveMenuItem as () => Promise<void>)();
    expect(
      apiMocks.updateMenuItem.mock.lastCall![0].menuItem.spec.targetRef
    ).toEqual(targetRef);
    state.selectedSourceValue = "Post";
    expect(state.selectedRefName).toBe("original");
    state.selectedSourceValue = "Tag";
    expect(state.selectedRefName).toBe("");
    state.selectedRefName = "tag";
    state.selectedSourceValue = "Post";
    expect(state.selectedRefName).toBe("");
  });

  it("requires missing resolved values and seeds a route name only once", () => {
    const wrapper = mountModal(existingMenuItem({ targetRef: resourceRef() }));
    const state = wrapper.vm.$.setupState as Record<string, unknown>;
    state.selectedSourceValue = "custom";
    const draft = state.formState as MenuItem;
    expect(draft.spec.displayName).toBeUndefined();
    expect(draft.spec.href).toBeUndefined();
    state.selectedSourceValue = "archives";
    expect(draft.spec.displayName).toBe("Article archives");
    state.selectedSourceValue = "tags";
    expect(draft.spec.displayName).toBe("Article archives");
  });

  it.each(["createMenuItem", "updateMenuItem"] as const)(
    "preserves drafts and annotations after %s fails",
    async (method) => {
      vi.spyOn(console, "error").mockImplementation(() => {});
      apiMocks[method].mockRejectedValueOnce(new Error("Rejected"));
      const wrapper = mountModal(
        method === "updateMenuItem"
          ? existingMenuItem({ displayName: "Writing", href: "/writing" })
          : undefined
      );
      const state = wrapper.vm.$.setupState as Record<string, unknown>;
      const draft = state.formState as MenuItem;
      draft.spec.displayName = "Draft";
      draft.spec.href = "/draft";
      const annotationForm = wrapper.findComponent(AnnotationsFormStub);
      const annotationValue = annotationForm.props("value");
      annotationForm.vm.$.exposed!.annotations.note = "unsaved";
      state.selectedSourceValue = "Post";
      state.selectedRefName = "chosen";
      await nextTick();
      expect(annotationForm.props("formData").spec).toMatchObject({
        targetRef: { kind: "Post", name: "chosen" },
      });
      expect(annotationForm.props("formData").spec.href).toBeUndefined();
      expect(annotationForm.props("value")).toBe(annotationValue);
      await (state.handleSaveMenuItem as () => Promise<void>)();
      expect(state.selectedRefName).toBe("chosen");
      expect(draft.spec).toMatchObject({
        displayName: "Draft",
        href: "/draft",
      });
      expect(wrapper.emitted("saved")).toBeUndefined();
      state.selectedSourceValue = "custom";
      await nextTick();
      expect(wrapper.findComponent(AnnotationsFormStub).vm).toBe(
        annotationForm.vm
      );
      expect(annotationForm.props("formData").spec.targetRef).toBeUndefined();
      expect(annotationForm.props("formData").spec.href).toBe("/draft");
      await (state.handleSaveMenuItem as () => Promise<void>)();
      expect(
        apiMocks[method].mock.lastCall![0].menuItem.metadata.annotations
      ).toEqual({ note: "unsaved" });
    }
  );

  it.each([false, true])(
    "preserves parent movement behavior (failure=%s)",
    async (fails) => {
      vi.spyOn(console, "error").mockImplementation(() => {});
      const tree = [
        { menuItem: existingMenuItem({ parent: "new-parent" }), children: [] },
      ];
      if (fails)
        apiMocks.updateMenuItemPosition.mockRejectedValueOnce(
          new Error("Move failed")
        );
      else
        apiMocks.updateMenuItemPosition.mockResolvedValueOnce({ data: tree });
      const wrapper = mountModal(
        existingMenuItem({
          displayName: "Writing",
          href: "/writing",
          parent: "old-parent",
        })
      );
      const state = wrapper.vm.$.setupState as Record<string, unknown>;
      state.selectedSourceValue = "archives";
      state.selectedParentMenuItem = "new-parent";
      await (state.handleSaveMenuItem as () => Promise<void>)();
      expect(apiMocks.updateMenuItemPosition).toHaveBeenCalledWith({
        name: "item",
        menuItemPositionRequest: {
          menuName: "primary",
          parentName: "new-parent",
          beforeName: undefined,
        },
      });
      const saved = apiMocks.updateMenuItem.mock.lastCall![0].menuItem;
      expect(saved.spec.routeRef).toBe("archives");
      expect(saved.spec.parent).toBe("old-parent");
      expect(wrapper.emitted("saved")![0]).toEqual(
        fails ? [saved] : [saved, tree]
      );
      if (fails) {
        expect(Toast.error).toHaveBeenCalled();
        expect(Toast.success).not.toHaveBeenCalled();
        expect(wrapper.emitted("close")).toBeUndefined();
      } else expect(wrapper.emitted("close")).toHaveLength(1);
    }
  );
});

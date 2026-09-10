/* eslint-disable vue/one-component-per-file -- Only the modal shell and annotation boundary are stubbed. */
import { getNode } from "@formkit/core";
import { defaultConfig, plugin as formKitPlugin } from "@formkit/vue";
import type { MenuItem } from "@halo-dev/api-client";
import { Toast } from "@halo-dev/components";
import messages from "@intlify/unplugin-vue-i18n/messages";
import { enableAutoUnmount, flushPromises, mount } from "@vue/test-utils";
import {
  afterEach,
  beforeEach,
  describe,
  expect,
  it,
  vi,
} from "vite-plus/test";
import { defineComponent, h } from "vue";
import { createI18n } from "vue-i18n";
import { form } from "@/formkit/inputs/form";
import { postSelect } from "@/formkit/inputs/post-select";
import { select } from "@/formkit/inputs/select";
import { singlePageSelect } from "@/formkit/inputs/singlePage-select";
import MenuItemEditingModal from "../MenuItemEditingModal.vue";

const api = vi.hoisted(() => ({
  save: vi.fn(),
  posts: vi.fn(),
  pages: vi.fn(),
}));
vi.mock("@halo-dev/api-client", async (original) => ({
  ...(await original<typeof import("@halo-dev/api-client")>()),
  MenuItemSpecRouteRefEnum: {
    Archives: "archives",
    Categories: "categories",
    Tags: "tags",
  },
  coreApiClient: {
    menuItem: { createMenuItem: api.save, updateMenuItem: api.save },
  },
  consoleApiClient: {
    content: {
      post: { listPosts: api.posts },
      singlePage: { listSinglePages: api.pages },
    },
  },
}));

const ModalStub = defineComponent({
  setup(_, { expose, slots }) {
    expose({ close: vi.fn() });
    return () => h("div", slots.default?.());
  },
});
const AnnotationsStub = defineComponent({
  setup(_, { expose }) {
    expose({
      handleSubmit: vi.fn(),
      annotations: {},
      customAnnotations: {},
      customFormInvalid: false,
      specFormInvalid: false,
    });
    return () => h("div");
  },
});

async function mountForm(menuItem?: MenuItem) {
  const wrapper = mount(MenuItemEditingModal, {
    attachTo: document.body,
    props: {
      menu: {
        apiVersion: "v1alpha1",
        kind: "Menu",
        metadata: { name: "primary" },
        spec: { displayName: "Primary" },
      },
      menuItem,
    },
    global: {
      plugins: [
        [
          formKitPlugin,
          defaultConfig({
            inputs: { form, select, postSelect, singlePageSelect },
            config: { delay: 0 },
          }),
        ],
        createI18n({ legacy: false, locale: "en", messages }),
      ],
      directives: { tooltip: () => {} },
      stubs: {
        VModal: ModalStub,
        Modal: ModalStub,
        MenuItemParentSelect: true,
        AnnotationsForm: AnnotationsStub,
        // Render popper slots in place; retain the real Halo select and option components.
        Dropdown: { template: "<div><slot /><slot name='popper' /></div>" },
        VDropdown: { template: "<div><slot /><slot name='popper' /></div>" },
      },
    },
  });
  await flushPromises();
  // Let the modal's deferred initial focus settle before opening a dropdown.
  await new Promise((resolve) => setTimeout(resolve, 0));
  await flushPromises();
  return wrapper;
}

type Wrapper = Awaited<ReturnType<typeof mountForm>>;
function field(wrapper: Wrapper, label: string) {
  return wrapper
    .findAll(".formkit-outer")
    .find(
      (input) =>
        input.find("label").exists() && input.get("label").text() === label
    )!;
}
function sourceInput(wrapper: Wrapper) {
  return field(wrapper, "Type");
}
async function choose(
  wrapper: Wrapper,
  input: ReturnType<typeof sourceInput>,
  label: string
) {
  await input.find(".select-container").trigger("click");
  await flushPromises();
  const option = wrapper
    .findAll(".select-option-item")
    .find((row) => row.text() === label);
  expect(
    option,
    `option ${label}: ${wrapper
      .findAll(".select-option-item")
      .map((row) => row.text())
      .join(", ")}`
  ).toBeDefined();
  await option!.trigger("mousedown");
  await getNode("menuitem-form")!.settled;
  await flushPromises();
}
async function submit(wrapper: Wrapper) {
  await wrapper.find("form").trigger("submit");
  await flushPromises();
}

describe("MenuItemEditingModal real form", () => {
  enableAutoUnmount(afterEach);
  beforeEach(() => {
    vi.resetAllMocks();
    vi.stubGlobal(
      "ResizeObserver",
      class {
        observe() {}
        unobserve() {}
        disconnect() {}
      }
    );
    vi.spyOn(Toast, "success").mockImplementation(() => "");
    api.save.mockImplementation(({ menuItem }) =>
      Promise.resolve({ data: menuItem })
    );
    api.posts.mockResolvedValue({
      data: {
        items: [
          { post: { metadata: { name: "post" }, spec: { title: "A post" } } },
        ],
        total: 1,
        page: 1,
        size: 20,
      },
    });
    api.pages.mockResolvedValue({
      data: {
        items: [
          { page: { metadata: { name: "page" }, spec: { title: "A page" } } },
        ],
        total: 1,
        page: 1,
        size: 20,
      },
    });
  });
  afterEach(() => {
    vi.restoreAllMocks();
    vi.unstubAllGlobals();
  });

  it("defaults a new route once and preserves edited and cleared drafts", async () => {
    const wrapper = await mountForm();
    await choose(wrapper, sourceInput(wrapper), "Article archives");
    expect(
      (wrapper.get("#displayNameInput").element as HTMLInputElement).value
    ).toBe("Article archives");
    await wrapper.get("#displayNameInput").setValue("Draft name");
    await choose(wrapper, sourceInput(wrapper), "Custom");
    await wrapper.get('input[name="href"]').setValue("/draft");
    await choose(wrapper, sourceInput(wrapper), "Tag list");
    await choose(wrapper, sourceInput(wrapper), "Custom");
    expect(
      (wrapper.get('input[name="href"]').element as HTMLInputElement).value
    ).toBe("/draft");
    await wrapper.get('input[name="href"]').setValue("");
    await wrapper.get("#displayNameInput").setValue("");
    await choose(wrapper, sourceInput(wrapper), "Article archives");
    expect(
      (wrapper.get("#displayNameInput").element as HTMLInputElement).value
    ).toBe("");
    await submit(wrapper);
    expect(api.save).not.toHaveBeenCalled();
    await wrapper.get("#displayNameInput").setValue("   ");
    await submit(wrapper);
    expect(api.save).not.toHaveBeenCalled();
    await wrapper.get("#displayNameInput").setValue("Valid");
    await vi.waitFor(() =>
      expect(getNode("menuitem-form")!.context!.state.valid).toBe(true)
    );
    await submit(wrapper);
    await vi.waitFor(() => expect(api.save).toHaveBeenCalled());
    expect(api.save.mock.lastCall![0].menuItem.spec).toMatchObject({
      displayName: "Valid",
      routeRef: "archives",
    });
  });

  it("converts an existing resource using its resolved values and preserves a cleared URL", async () => {
    const wrapper = await mountForm({
      apiVersion: "v1alpha1",
      kind: "MenuItem",
      metadata: { name: "existing" },
      spec: {
        targetRef: {
          group: "content.halo.run",
          version: "v1alpha1",
          kind: "Post",
          name: "post",
        },
      },
      status: { displayName: "Published post", href: "/published" },
    });
    await choose(wrapper, sourceInput(wrapper), "Custom");
    expect(
      (wrapper.get("#displayNameInput").element as HTMLInputElement).value
    ).toBe("Published post");
    expect(
      (wrapper.get('input[name="href"]').element as HTMLInputElement).value
    ).toBe("/published");
    await wrapper.get('input[name="href"]').setValue("");
    await choose(wrapper, sourceInput(wrapper), "Article archives");
    await choose(wrapper, sourceInput(wrapper), "Custom");
    expect(
      (wrapper.get('input[name="href"]').element as HTMLInputElement).value
    ).toBe("");
    await submit(wrapper);
    expect(api.save).not.toHaveBeenCalled();
    await wrapper.get('input[name="href"]').setValue("/converted");
    await vi.waitFor(() =>
      expect(getNode("menuitem-form")!.context!.state.valid).toBe(true)
    );
    await submit(wrapper);
    await vi.waitFor(() => expect(api.save).toHaveBeenCalled());
    const saved = api.save.mock.lastCall![0].menuItem as MenuItem;
    expect(saved.metadata.name).toBe("existing");
    expect(saved.spec).toMatchObject({
      displayName: "Published post",
      href: "/converted",
    });
    expect(saved.spec.targetRef).toBeUndefined();
  });

  it("resets the real resource picker and blocks missing selections without hidden-field validation", async () => {
    const wrapper = await mountForm();
    await choose(wrapper, sourceInput(wrapper), "Post");
    await submit(wrapper);
    expect(api.save).not.toHaveBeenCalled();
    const post = field(wrapper, "Post");
    await choose(wrapper, post, "A post");
    await choose(wrapper, sourceInput(wrapper), "Page");
    expect(getNode("postSelect")).toBeUndefined();
    expect(getNode("singlePageSelect")!.value).toBe("");
    await submit(wrapper);
    expect(api.save).not.toHaveBeenCalled();
    const page = field(wrapper, "Page");
    await choose(wrapper, page, "A page");
    await submit(wrapper);
    expect(api.save).toHaveBeenCalledTimes(1);
    expect(api.save.mock.lastCall![0].menuItem.spec.targetRef).toMatchObject({
      kind: "SinglePage",
      name: "page",
    });
    expect(
      api.save.mock.lastCall![0].menuItem.spec.displayName
    ).toBeUndefined();
  });
});

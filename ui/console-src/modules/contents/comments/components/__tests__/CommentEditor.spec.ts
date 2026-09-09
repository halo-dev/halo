import { defaultConfig, plugin as formKitPlugin } from "@formkit/vue";
import { QueryClient, VueQueryPlugin } from "@tanstack/vue-query";
import { flushPromises, mount } from "@vue/test-utils";
import {
  afterEach,
  beforeEach,
  describe,
  expect,
  it,
  vi,
} from "vite-plus/test";
import { defineComponent, h, markRaw, ref } from "vue";
import { createI18n } from "vue-i18n";
import CommentEditor from "../CommentEditor.vue";

const plugins = vi.hoisted(() => ({ modules: [] as unknown[] }));
vi.mock("@/stores/plugin", () => ({
  usePluginModuleStore: () => ({ pluginModules: plugins.modules }),
}));

const cleanups: (() => void)[] = [];
afterEach(() => cleanups.splice(0).forEach((cleanup) => cleanup()));
beforeEach(() => {
  plugins.modules = [];
});

async function mountEditor(initialContent?: string) {
  const client = new QueryClient({
    defaultOptions: { queries: { retry: false } },
  });
  const wrapper = mount(CommentEditor, {
    props: { initialContent, autoFocus: false },
    global: {
      plugins: [
        [VueQueryPlugin, { queryClient: client }],
        [formKitPlugin, defaultConfig()],
        createI18n({ legacy: false, missingWarn: false, fallbackWarn: false }),
      ],
      stubs: { Dropdown: { template: "<div><slot /></div>" } },
    },
  });
  cleanups.push(() => {
    wrapper.unmount();
    client.clear();
  });
  await flushPromises();
  return wrapper;
}

const PluginEditor = markRaw(
  defineComponent({
    props: { initialContent: { type: String, default: undefined } },
    setup(props) {
      const content = ref(props.initialContent ?? "new plugin reply");
      return () =>
        h("textarea", { "aria-label": "Plugin editor", value: content.value });
    },
  })
);

describe("comment editor initialization", () => {
  it("preserves HTML source and does not replace a draft when the parent refreshes", async () => {
    const wrapper = await mountEditor("<p>Hello <strong>world</strong></p>");
    const input = wrapper.get("textarea");
    expect(wrapper.text()).toContain("core.comment.edit_modal.source_help");
    expect(input.element.value).toBe("<p>Hello <strong>world</strong></p>");
    await input.setValue("<p>Corrected <strong>world</strong></p>");
    await new Promise((resolve) => setTimeout(resolve, 30));
    await wrapper.setProps({ initialContent: "refreshed body" });
    expect(input.element.value).toBe("<p>Corrected <strong>world</strong></p>");
    expect(wrapper.emitted("update")?.at(-1)?.[0]).toEqual({
      content: "<p>Corrected <strong>world</strong></p>",
      characterCount: "<p>Corrected <strong>world</strong></p>".length,
    });
  });

  it("keeps existing plugin editors for creation but uses source editing for unsupported edits", async () => {
    plugins.modules = [
      {
        extensionPoints: {
          "comment:editor:replace": () => ({ component: PluginEditor }),
        },
      },
    ];
    const creation = await mountEditor();
    expect(creation.get("textarea").attributes("aria-label")).toBe(
      "Plugin editor"
    );
    const editing = await mountEditor("<p>Existing body</p>");
    expect(editing.get("textarea").element.value).toBe("<p>Existing body</p>");
    expect(editing.get("textarea").attributes("aria-label")).not.toBe(
      "Plugin editor"
    );
    expect(editing.text()).toContain("core.comment.edit_modal.source_help");
  });

  it("passes the initial body to a plugin that explicitly supports editing", async () => {
    plugins.modules = [
      {
        extensionPoints: {
          "comment:editor:replace": () => ({
            component: PluginEditor,
            supportsEditing: true,
          }),
        },
      },
    ];
    const wrapper = await mountEditor("<p>Formatted body</p>");
    expect(wrapper.get("textarea").attributes("aria-label")).toBe(
      "Plugin editor"
    );
    expect(wrapper.get("textarea").element.value).toBe("<p>Formatted body</p>");
    expect(wrapper.text()).not.toContain("core.comment.edit_modal.source_help");
  });
});

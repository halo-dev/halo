import { flushPromises, shallowMount } from "@vue/test-utils";
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import { nextTick, ref } from "vue";
import DefaultEditor from "../DefaultEditor.vue";

interface EditorOptions {
  content: string;
  onUpdate: () => void;
  onCreate: () => void;
}

interface EditorFixture {
  options: EditorOptions;
  isDestroyed: boolean;
  isEmpty: boolean;
  getHTML: ReturnType<typeof vi.fn>;
  destroy: () => void;
  commands: {
    focus: ReturnType<typeof vi.fn>;
    setContent: ReturnType<typeof vi.fn>;
  };
}

const editorState = vi.hoisted(() => ({ instances: [] as EditorFixture[] }));

vi.mock("@halo-dev/richtext-editor", async (importOriginal) => {
  const original =
    await importOriginal<typeof import("@halo-dev/richtext-editor")>();
  return {
    ...original,
    RichTextEditor: {
      name: "RichTextEditor",
      template: '<div><slot name="content" /></div>',
    },
    VueEditor: class {
      isDestroyed = false;
      isEmpty = false;
      commands = { focus: vi.fn(), setContent: vi.fn() };
      getHTML = vi.fn(() => {
        if (this.isDestroyed) {
          throw new Error("Destroyed editor schema is unavailable");
        }
        return "<p>Latest edit</p>";
      });

      constructor(readonly options: EditorOptions) {
        editorState.instances.push(this);
      }

      destroy() {
        this.isDestroyed = true;
      }
    },
  };
});

vi.mock("@halo-dev/ui-shared", () => ({
  utils: { permission: { has: () => false } },
}));
vi.mock("@/stores/plugin", () => ({
  usePluginModuleStore: () => ({ pluginModules: [] }),
}));
vi.mock("@/locales", () => ({
  i18n: { global: { locale: { value: "en" } } },
}));
vi.mock("vue-i18n", async (importOriginal) => ({
  ...(await importOriginal<typeof import("vue-i18n")>()),
  useI18n: () => ({ t: (key: string) => key }),
}));

function mountEditor(
  props: {
    bodyOnly?: boolean;
    compact?: boolean;
    raw?: string;
  } = {}
) {
  return shallowMount(DefaultEditor, {
    props: { raw: "<p>Initial</p>", compact: true, bodyOnly: true, ...props },
    global: {
      provide: { owner: ref(), publishTime: ref(), permalink: ref() },
      mocks: { $t: (key: string) => key },
      stubs: {
        AttachmentSelectorModal: true,
        HasPermission: true,
        LazyImage: true,
        RichTextEditor: false,
      },
    },
  });
}

describe("default editor lifecycle", () => {
  beforeEach(() => {
    vi.useFakeTimers();
    editorState.instances.length = 0;
  });
  afterEach(() => {
    vi.clearAllTimers();
    vi.useRealTimers();
    vi.restoreAllMocks();
  });

  it("emits the latest live content after the debounce window", async () => {
    const wrapper = mountEditor();
    await flushPromises();
    editorState.instances[0].options.onUpdate();
    await vi.advanceTimersByTimeAsync(249);
    expect(wrapper.emitted("update:raw")).toBeUndefined();
    await vi.advanceTimersByTimeAsync(1);
    expect(wrapper.emitted("update:raw")).toEqual([["<p>Latest edit</p>"]]);
    expect(wrapper.emitted("update:content")).toEqual([["<p>Latest edit</p>"]]);
    wrapper.unmount();
  });

  it("flushes the last edit before unmount without reading the destroyed editor later", async () => {
    const wrapper = mountEditor();
    await flushPromises();
    const editor = editorState.instances[0];
    editor.options.onUpdate();
    wrapper.vm.flush();
    expect(wrapper.emitted("update:raw")).toEqual([["<p>Latest edit</p>"]]);
    expect(wrapper.emitted("update:content")).toEqual([["<p>Latest edit</p>"]]);
    expect(wrapper.emitted("update")).toEqual([["<p>Latest edit</p>"]]);
    const updates = wrapper.emitted("update");
    editor.getHTML.mockClear();
    wrapper.unmount();
    await vi.advanceTimersByTimeAsync(250);
    expect(editor.getHTML).not.toHaveBeenCalled();
    expect(updates).toHaveLength(1);
  });

  it("ignores raw changes and explicit flush after editor destruction", async () => {
    const wrapper = mountEditor();
    await flushPromises();
    const editor = editorState.instances[0];
    editor.destroy();
    editor.getHTML.mockClear();
    await wrapper.setProps({ raw: "<p>External update</p>" });
    wrapper.vm.flush();
    expect(editor.getHTML).not.toHaveBeenCalled();
    expect(editor.commands.setContent).not.toHaveBeenCalled();
    expect(wrapper.emitted("update:raw")).toBeUndefined();
    wrapper.unmount();
  });

  it("applies external raw updates without emitting local edits", async () => {
    const wrapper = mountEditor();
    await flushPromises();
    const editor = editorState.instances[0];
    await wrapper.setProps({ raw: "<p>External update</p>" });
    expect(editor.commands.setContent).toHaveBeenCalledWith(
      "<p>External update</p>",
      { emitUpdate: false }
    );
    expect(wrapper.emitted("update:raw")).toBeUndefined();
    expect(wrapper.emitted("update:content")).toBeUndefined();
    wrapper.unmount();
  });

  it("synchronizes rendered content when the native editor restores cached raw", async () => {
    const wrapper = mountEditor({ bodyOnly: false, compact: false });
    await flushPromises();
    const editor = editorState.instances[0];
    let html = "<p>Server content</p>";
    editor.getHTML.mockImplementation(() => html);
    editor.commands.setContent.mockImplementation((raw, options) => {
      html = raw;
      if (options?.emitUpdate !== false) {
        editor.options.onUpdate();
      }
    });

    await wrapper.setProps({ raw: "<p>Recovered offline content</p>" });
    await vi.advanceTimersByTimeAsync(250);

    expect(wrapper.emitted("update:raw")).toEqual([[html]]);
    expect(wrapper.emitted("update:content")).toEqual([[html]]);
    expect(wrapper.emitted("update")).toEqual([[html]]);
    wrapper.unmount();
  });

  it("preserves autofocus for the article editor", async () => {
    const wrapper = mountEditor({ bodyOnly: false, compact: false });
    await flushPromises();
    const editor = editorState.instances[0];
    editor.options.onCreate();
    await nextTick();
    expect(editor.commands.focus).toHaveBeenCalledOnce();
    wrapper.unmount();
  });

  it("does not steal focus from an embedded body-only editor", async () => {
    const wrapper = mountEditor();
    await flushPromises();
    const editor = editorState.instances[0];
    editor.options.onCreate();
    await nextTick();
    expect(editor.commands.focus).not.toHaveBeenCalled();
    wrapper.unmount();
  });

  it("does not focus a missing title in an empty body-only editor", async () => {
    const wrapper = mountEditor({ raw: "" });
    await flushPromises();
    const editor = editorState.instances[0];
    editor.isEmpty = true;
    editor.options.onCreate();
    await nextTick();
    expect(wrapper.find("input").exists()).toBe(false);
    expect(editor.commands.focus).not.toHaveBeenCalled();
    wrapper.unmount();
  });

  it("keeps title focus for an empty article editor", async () => {
    const titleFocus = vi.spyOn(HTMLInputElement.prototype, "focus");
    const wrapper = mountEditor({ raw: "", bodyOnly: false, compact: false });
    await flushPromises();
    const editor = editorState.instances[0];
    editor.isEmpty = true;
    editor.options.onCreate();
    await nextTick();
    expect(wrapper.find('input[type="text"]').exists()).toBe(true);
    expect(titleFocus).toHaveBeenCalledOnce();
    expect(editor.commands.focus).not.toHaveBeenCalled();
    wrapper.unmount();
  });
});

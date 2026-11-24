/* eslint-disable vue/one-component-per-file -- Small inline components are test fixtures. */
import { readFileSync } from "node:fs";
import { resolve } from "node:path";
import { mount, flushPromises } from "@vue/test-utils";
import { afterEach, describe, expect, it, vi } from "vitest";
import { defineComponent, shallowRef } from "vue";
import { createI18n } from "vue-i18n";
import ContentEditor from "../ContentEditor.vue";

const editorState = vi.hoisted(() => ({ flush: vi.fn() }));
const messages = Object.fromEntries(
  ["en", "es", "zh-CN", "zh-TW"].map((locale) => [
    locale,
    JSON.parse(
      readFileSync(
        resolve(import.meta.dirname, "../../../locales", `${locale}.json`),
        "utf-8"
      )
    ),
  ])
);

vi.mock("@halo-dev/components", () => ({
  VLoading: { template: "<div>Loading</div>" },
  VAlert: { props: ["title"], template: "<div>{{ title }}</div>" },
}));
vi.mock("@/composables/use-editor-extension-points", () => ({
  useEditorExtensionPoints: () => ({
    editorProviders: shallowRef([
      {
        name: "default",
        rawType: "HTML",
        component: defineComponent({
          props: {
            raw: { type: String, default: "" },
            content: { type: String, default: "" },
          },
          emits: ["update:raw", "update:content"],
          setup(props, { emit, expose }) {
            expose({
              flush: async () => {
                await editorState.flush();
                emit("update:raw", props.raw);
                emit("update:content", props.content);
              },
            });
          },
          template: "<div>HTML editor</div>",
        }),
      },
      {
        name: "markdown-one",
        rawType: "markdown",
        component: defineComponent({ template: "<div>Markdown one</div>" }),
      },
      {
        name: "markdown-two",
        rawType: "Markdown",
        component: defineComponent({ template: "<div>Markdown two</div>" }),
      },
    ]),
    fetchEditorProviders: vi.fn().mockResolvedValue(undefined),
  }),
}));

function mountEditor(
  props: InstanceType<typeof ContentEditor>["$props"] = {},
  locale = "en",
  attrs: Record<string, unknown> = {}
) {
  return mount(ContentEditor, {
    props,
    attrs,
    global: {
      plugins: [
        createI18n({
          legacy: false,
          locale,
          messages,
        }),
      ],
    },
  });
}

describe("content editor", () => {
  afterEach(() => vi.clearAllMocks());

  it("never substitutes an incompatible editor for a required plugin editor", async () => {
    const wrapper = mountEditor({
      rawType: "HTML",
      preferredEditor: "custom-nodes",
      requirePreferredEditor: true,
    });
    await flushPromises();
    expect(wrapper.text()).toContain(
      "No editor found that matches the HTML format"
    );
    expect(wrapper.text()).not.toContain("HTML editor");
    await wrapper.setProps({ preferredEditor: "default" });
    expect(wrapper.text()).toBe("HTML editor");
    wrapper.unmount();
  });
  it("prefers the original editor only when its format matches", async () => {
    const wrapper = mountEditor({
      rawType: "MARKDOWN",
      preferredEditor: "markdown-two",
    });
    await flushPromises();
    expect(wrapper.text()).toBe("Markdown two");
    await wrapper.setProps({ rawType: "HTML" });
    expect(wrapper.text()).toBe("HTML editor");
    await wrapper.setProps({ rawType: "unsupported" });
    expect(wrapper.text()).toContain(
      "No editor found that matches the unsupported format"
    );
    expect(wrapper.text()).not.toContain("HTML editor");
    wrapper.unmount();
  });

  it.each([
    ["en", "No editor found that matches the unsupported format"],
    [
      "es",
      "No se encontró ningún editor que coincida con el formato unsupported",
    ],
    ["zh-CN", "未找到符合 unsupported 格式的编辑器"],
    ["zh-TW", "未找到符合 unsupported 格式的編輯器"],
  ])("uses the host's %s editor warning", async (locale, warning) => {
    const wrapper = mountEditor({ rawType: "unsupported" }, locale);
    await flushPromises();
    expect(wrapper.text()).toContain(warning);
    wrapper.unmount();
  });

  it("forwards content updates and waits for the provider to flush", async () => {
    let resolveFlush!: () => void;
    editorState.flush.mockReturnValueOnce(
      new Promise<void>((resolve) => {
        resolveFlush = resolve;
      })
    );
    const onRaw = vi.fn();
    const onContent = vi.fn();
    const wrapper = mountEditor({}, "en", {
      raw: "Latest raw",
      content: "<p>Latest content</p>",
      "onUpdate:raw": onRaw,
      "onUpdate:content": onContent,
    });
    await flushPromises();
    const flushed = wrapper.vm.flush();
    expect(editorState.flush).toHaveBeenCalledOnce();
    expect(onRaw).not.toHaveBeenCalled();
    expect(onContent).not.toHaveBeenCalled();
    resolveFlush();
    await flushed;
    expect(onRaw).toHaveBeenCalledWith("Latest raw");
    expect(onContent).toHaveBeenCalledWith("<p>Latest content</p>");
    wrapper.unmount();
  });
});

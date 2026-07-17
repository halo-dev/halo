// @vitest-environment jsdom

import { shallowMount } from "@vue/test-utils";
import { describe, expect, it, vi } from "vitest";
import type { NodeViewProps } from "@/tiptap/vue-3";
import CodeBlockSelect from "./CodeBlockSelect.vue";
import CodeBlockViewRenderer from "./CodeBlockViewRenderer.vue";

describe("CodeBlockViewRenderer", () => {
  it("removes the auto option without changing an existing value", async () => {
    const updateAttributes = vi.fn();
    const wrapper = shallowMount(CodeBlockViewRenderer, {
      props: {
        node: {
          attrs: {
            language: "auto",
            collapsed: false,
          },
          textContent: "",
        },
        updateAttributes,
        editor: {
          state: {},
          options: {
            element: document.createElement("div"),
          },
          commands: {
            focus: vi.fn(),
          },
        },
        extension: {
          options: {
            languages: [
              { label: "Auto", value: "auto" },
              { label: "JavaScript", value: "javascript" },
            ],
            themes: [],
          },
        },
        getPos: () => 0,
        selected: false,
      } as unknown as NodeViewProps,
      global: {
        stubs: {
          NodeViewWrapper: { template: "<div><slot /></div>" },
          NodeViewContent: true,
        },
        directives: {
          tooltip: {},
        },
      },
    });

    const languageSelect = wrapper.findComponent(CodeBlockSelect);
    expect(languageSelect.props("options")).toEqual([
      { label: "None", value: "" },
      { label: "JavaScript", value: "javascript" },
    ]);
    expect(languageSelect.props("modelValue")).toBe("auto");

    await languageSelect.vm.$emit("update:modelValue", "");
    expect(updateAttributes).toHaveBeenCalledWith({ language: null });
  });
});

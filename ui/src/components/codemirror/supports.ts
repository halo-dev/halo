import type { LanguageSupport } from "@codemirror/language";
import type { EditorStateConfig } from "@codemirror/state";

export const presetLanguages = {
  yaml: async (): Promise<LanguageSupport> => {
    const { yaml } = await import("@codemirror/lang-yaml");
    return yaml();
  },
  html: async (): Promise<LanguageSupport> => {
    const { html } = await import("@codemirror/lang-html");
    return html();
  },
  javascript: async (): Promise<LanguageSupport> => {
    const { javascript } = await import("@codemirror/lang-javascript");
    return javascript({
      jsx: true,
      typescript: true,
    });
  },
  css: async (): Promise<LanguageSupport> => {
    const { css } = await import("@codemirror/lang-css");
    return css();
  },
  json: async (): Promise<LanguageSupport> => {
    const { json } = await import("@codemirror/lang-json");
    return json();
  },
  markdown: async (): Promise<LanguageSupport> => {
    const { markdown } = await import("@codemirror/lang-markdown");
    return markdown({
      addKeymap: true,
      completeHTMLTags: true,
    });
  },
} satisfies Record<string, () => Promise<LanguageSupport>>;

export interface CodemirrorProps {
  modelValue?: string;
  height?: string;
  language: keyof typeof presetLanguages;
  extensions?: EditorStateConfig["extensions"];
}

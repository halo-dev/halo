import { css } from "@codemirror/lang-css";
import { html } from "@codemirror/lang-html";
import { javascript } from "@codemirror/lang-javascript";
import { json } from "@codemirror/lang-json";
import { LanguageSupport, StreamLanguage } from "@codemirror/language";
import { yaml } from "@codemirror/legacy-modes/mode/yaml";
import type { EditorStateConfig } from "@codemirror/state";

export const presetLanguages = {
  yaml: StreamLanguage.define(yaml),
  html: html(),
  javascript: javascript({
    jsx: true,
    typescript: true,
  }),
  css: css(),
  json: json(),
};

export interface CodemirrorProps {
  modelValue?: string;
  height?: string;
  language: keyof typeof presetLanguages | LanguageSupport;
  extensions?: EditorStateConfig["extensions"];
}

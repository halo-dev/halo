import type { StorybookConfig } from "@storybook/vue3-vite";
import { dirname } from "node:path";
import { fileURLToPath } from "node:url";

const config: StorybookConfig = {
  stories: ["../src/**/*.mdx", "../src/**/*.stories.@(js|jsx|ts|tsx)"],

  addons: [
    getAbsolutePath("@storybook/addon-links"),
    getAbsolutePath("@storybook/addon-docs"),
  ],

  framework: {
    name: getAbsolutePath("@storybook/vue3-vite"),
    options: {},
  },

  async viteFinal(config) {
    const { mergeConfig } = await import("vite");

    return mergeConfig(config, {
      assetsInclude: ["/sb-preview/runtime.js"],
    });
  },
};
export default config;

// eslint-disable-next-line @typescript-eslint/no-explicit-any
function getAbsolutePath(value: string): any {
  return dirname(fileURLToPath(import.meta.resolve(`${value}/package.json`)));
}

import randomstring from "randomstring";
import type { HtmlTagDescriptor } from "vite";
import { viteExternalsPlugin as ViteExternals } from "vite-plugin-externals";
import { createHtmlPlugin as VitePluginHtml } from "vite-plugin-html";
import {
  viteStaticCopy as ViteStaticCopy,
  type Target,
} from "vite-plugin-static-copy";

/**
 * It copies the external libraries to the `assets` folder, and injects the script tags into the HTML
 *
 * @param {boolean} isProduction - boolean
 * @param {string} baseUrl - The base url of the application.
 * @returns An array of plugins
 */
export const setupLibraryExternal = (
  isProduction: boolean,
  baseUrl: string,
  entry: string
) => {
  const staticSuffix = randomstring.generate({
    length: 8,
    charset: "hex",
  });

  const staticTargets: Target[] = [
    {
      src: `./node_modules/vue/dist/vue.global${
        isProduction ? ".prod" : ""
      }.js`,
      dest: "assets/vue",
      rename: `vue.global.${staticSuffix}.js`,
    },
    {
      src: `./node_modules/vue-router/dist/vue-router.global${
        isProduction ? ".prod" : ""
      }.js`,
      dest: "assets/vue-router",
      rename: `vue-router.global.${staticSuffix}.js`,
    },
    {
      src: "./node_modules/axios/dist/axios.min.js",
      dest: "assets/axios",
      rename: `axios.${staticSuffix}.js`,
    },
    {
      src: `./node_modules/vue-demi/lib/index.iife.js`,
      dest: "assets/vue-demi",
      rename: `vue-demi.${staticSuffix}.js`,
    },
    {
      src: "./node_modules/@vueuse/shared/index.iife.min.js",
      dest: "assets/vueuse",
      rename: `vueuse.shared.iife.${staticSuffix}.js`,
    },
    {
      src: "./node_modules/@vueuse/core/index.iife.min.js",
      dest: "assets/vueuse",
      rename: `vueuse.core.iife.${staticSuffix}.js`,
    },
    {
      src: "./node_modules/@vueuse/components/index.iife.min.js",
      dest: "assets/vueuse",
      rename: `vueuse.components.iife.${staticSuffix}.js`,
    },
    {
      src: "./node_modules/@vueuse/router/index.iife.min.js",
      dest: "assets/vueuse",
      rename: `vueuse.router.iife.${staticSuffix}.js`,
    },
    {
      src: "./node_modules/@halo-dev/components/dist/halo-components.iife.js",
      dest: "assets/components",
      rename: `halo-components.iife.${staticSuffix}.js`,
    },
    {
      src: "./node_modules/@halo-dev/console-shared/dist/halo-console-shared.iife.js",
      dest: "assets/console-shared",
      rename: `halo-console-shared.iife.${staticSuffix}.js`,
    },
    {
      src: "./node_modules/@halo-dev/richtext-editor/dist/rich-text-editor.iife.js",
      dest: "assets/richtext-editor",
      rename: `halo-rich-text-editor.iife.${staticSuffix}.js`,
    },
    {
      src: "./node_modules/@halo-dev/api-client/dist/halo-api-client.iife.js",
      dest: "assets/api-client",
      rename: `halo-api-client.iife.${staticSuffix}.js`,
    },
  ];

  const injectTags = staticTargets
    .map((target) => {
      return {
        injectTo: "head",
        tag: "script",
        attrs: {
          src: `${isProduction ? baseUrl : "/"}${target.dest}/${target.rename}`,
          type: "text/javascript",
        },
      };
    })
    .filter(Boolean) as HtmlTagDescriptor[];

  return [
    ViteExternals({
      vue: "Vue",
      "vue-router": "VueRouter",
      axios: "axios",
      "@halo-dev/shared": "HaloConsoleShared",
      "@halo-dev/components": "HaloComponents",
      "@vueuse/core": "VueUse",
      "@vueuse/components": "VueUse",
      "@vueuse/router": "VueUse",
      "vue-demi": "VueDemi",
      "@halo-dev/richtext-editor": "RichTextEditor",
      "@halo-dev/api-client": "HaloApiClient",
    }),
    ViteStaticCopy({
      targets: staticTargets,
    }),
    VitePluginHtml({
      minify: false,
      inject: {
        tags: injectTags,
        data: {
          entry: entry,
        },
      },
    }),
  ];
};

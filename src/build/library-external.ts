import { viteExternalsPlugin as ViteExternals } from "vite-plugin-externals";
import {
  viteStaticCopy as ViteStaticCopy,
  type Target,
} from "vite-plugin-static-copy";
import { createHtmlPlugin as VitePluginHtml } from "vite-plugin-html";
import randomstring from "randomstring";

/**
 * It copies the external libraries to the `assets` folder, and injects the script tags into the HTML
 *
 * @param {boolean} isProduction - boolean
 * @param {string} baseUrl - The base url of the application.
 * @returns An array of plugins
 */
export const setupLibraryExternal = (
  isProduction: boolean,
  baseUrl: string
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
  ];

  const injectScript = staticTargets
    .map((target) => {
      return `<script src="${isProduction ? baseUrl : "/"}${target.dest}/${
        target.rename
      }"></script>`;
    })
    .join("\n");

  console.log(injectScript);

  return [
    ViteExternals({
      vue: "Vue",
      "vue-router": "VueRouter",
      "@halo-dev/shared": "HaloConsoleShared",
      "@halo-dev/components": "HaloComponents",
      "@vueuse/core": "VueUse",
      "@vueuse/components": "VueUse",
      "@vueuse/router": "VueUse",
      "vue-demi": "VueDemi",
    }),
    ViteStaticCopy({
      targets: staticTargets,
    }),
    VitePluginHtml({
      minify: false,
      inject: {
        data: {
          injectScript,
        },
      },
    }),
  ];
};

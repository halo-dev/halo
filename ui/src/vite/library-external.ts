import crypto from "node:crypto";
import fs from "node:fs";
import path from "node:path";
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
  const staticTargets: Target[] = [
    {
      src: `./node_modules/vue/dist/vue.global${
        isProduction ? ".prod" : ""
      }.js`,
      dest: "assets/vue",
      rename: `vue.[hash].js`,
    },
    {
      src: `./node_modules/vue-router/dist/vue-router.global${
        isProduction ? ".prod" : ""
      }.js`,
      dest: "assets/vue-router",
      rename: `vue-router.[hash].js`,
    },
    {
      src: `./node_modules/pinia/dist/pinia.iife.prod.js`,
      dest: "assets/pinia",
      rename: `pinia.[hash].js`,
    },
    {
      src: "./node_modules/axios/dist/axios.min.js",
      dest: "assets/axios",
      rename: `axios.[hash].js`,
    },
    {
      src: `./node_modules/vue-demi/lib/index.iife.js`,
      dest: "assets/vue-demi",
      rename: `vue-demi.[hash].js`,
    },
    {
      src: "./node_modules/@vueuse/shared/index.iife.min.js",
      dest: "assets/vueuse",
      rename: `vueuse.shared.[hash].js`,
    },
    {
      src: "./node_modules/@vueuse/core/index.iife.min.js",
      dest: "assets/vueuse",
      rename: `vueuse.core.[hash].js`,
    },
    {
      src: "./node_modules/@vueuse/components/index.iife.min.js",
      dest: "assets/vueuse",
      rename: `vueuse.components.[hash].js`,
    },
    {
      src: "./node_modules/@vueuse/router/index.iife.min.js",
      dest: "assets/vueuse",
      rename: `vueuse.router.[hash].js`,
    },
    {
      src: "./node_modules/@halo-dev/components/dist/index.iife.js",
      dest: "assets/components",
      rename: `components.[hash].js`,
    },
    {
      src: "./node_modules/@halo-dev/api-client/dist/index.iife.js",
      dest: "assets/api-client",
      rename: `api-client.[hash].js`,
    },
    {
      src: "./node_modules/@halo-dev/ui-shared/dist/index.iife.js",
      dest: "assets/ui-shared",
      rename: `ui-shared.[hash].js`,
    },
    // TODO: Remove this in the future, only for compatibility.
    {
      src: "./node_modules/@halo-dev/console-shared/index.js",
      dest: "assets/console-shared",
      rename: `console-shared.[hash].js`,
    },
    {
      src: "./node_modules/@halo-dev/richtext-editor/dist/index.iife.js",
      dest: "assets/editor",
      rename: `editor.[hash].js`,
    },
  ].map((target) => {
    return {
      ...target,
      rename: `${target.rename.replace(
        "[hash]",
        computeLibraryHash(target.src)
      )}`,
    };
  });

  const injectTags = staticTargets
    .map((target) => {
      return {
        injectTo: "head",
        tag: "script",
        attrs: {
          src: `${baseUrl}${target.dest}/${target.rename}`,
          type: "text/javascript",
          "vite-ignore": true,
        },
      };
    })
    .filter(Boolean) as HtmlTagDescriptor[];

  return [
    ViteExternals({
      vue: "Vue",
      "vue-router": "VueRouter",
      pinia: "Pinia",
      axios: "axios",
      "@halo-dev/ui-shared": "HaloUiShared",
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

function computeLibraryHash(file: string) {
  const content = fs.readFileSync(path.resolve(process.cwd(), file), "utf8");
  return crypto.createHash("md5").update(content).digest("hex").substring(0, 8);
}

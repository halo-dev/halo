const GLOBALS = {
  vue: "Vue",
  "vue-router": "VueRouter",
  "@vueuse/core": "VueUse",
  "@vueuse/components": "VueUse",
  "@vueuse/router": "VueUse",
  "@halo-dev/console-shared": "HaloConsoleShared",
  "@halo-dev/components": "HaloComponents",
  "@halo-dev/api-client": "HaloApiClient",
  "@halo-dev/richtext-editor": "RichTextEditor",
  axios: "axios",
};

const EXTERNALS = Object.keys(GLOBALS) as string[];

export { EXTERNALS, GLOBALS };

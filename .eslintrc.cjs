/* eslint-env node */
require("@rushstack/eslint-patch/modern-module-resolution");

module.exports = {
  root: true,
  extends: [
    "plugin:vue/vue3-recommended",
    "eslint:recommended",
    "@vue/eslint-config-typescript/recommended",
    "@vue/eslint-config-prettier",
  ],
  env: {
    node: true,
    "vue/setup-compiler-macros": true,
  },
  rules: {
    "vue/multi-word-component-names": 0,
    "@typescript-eslint/ban-ts-comment": 0,
  },
  overrides: [
    {
      files: ["cypress/integration/**.spec.{js,ts,jsx,tsx}"],
      extends: ["plugin:cypress/recommended"],
    },
  ],
};

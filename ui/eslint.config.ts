import pluginVitest from "@vitest/eslint-plugin";
import skipFormatting from "@vue/eslint-config-prettier/skip-formatting";
import {
  defineConfigWithVueTs,
  vueTsConfigs,
} from "@vue/eslint-config-typescript";
import pluginVue from "eslint-plugin-vue";

export default defineConfigWithVueTs(
  {
    name: "app/global-ignores",
    ignores: [
      "**/dist/**",
      "**/node_modules/**",
      "packages/api-client/src/",
      "**/build/**",
    ],
  },

  pluginVue.configs["flat/recommended"],
  vueTsConfigs.recommended,

  {
    name: "app/base",
    files: ["**/*.{ts,mts,tsx,vue}"],
    rules: {
      "vue/multi-word-component-names": 0,
      "@typescript-eslint/ban-ts-comment": 0,
      "vue/no-v-html": 0,
      "@typescript-eslint/no-unused-vars": [
        "error",
        {
          args: "all",
          argsIgnorePattern: "^_",
          caughtErrors: "all",
          caughtErrorsIgnorePattern: "^_",
          destructuredArrayIgnorePattern: "^_",
          varsIgnorePattern: "^_",
          ignoreRestSiblings: true,
        },
      ],
    },
  },

  {
    name: "app/config-files",
    files: ["**/*.config.{js,cjs}"],
    rules: {
      "@typescript-eslint/no-require-imports": "off",
    },
  },

  {
    ...pluginVitest.configs.recommended,
    files: ["**/__tests__/*"],
  },

  skipFormatting
);

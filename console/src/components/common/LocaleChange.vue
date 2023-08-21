<script lang="ts" setup>
import { useLocalStorage } from "@vueuse/core";
import { locales, getBrowserLanguage, i18n } from "@/locales";
import { watch } from "vue";
import MdiTranslate from "~icons/mdi/translate";

// setup locale
const currentLocale = useLocalStorage(
  "locale",
  getBrowserLanguage() || locales[0].code
);

watch(
  () => currentLocale.value,
  (value) => {
    i18n.global.locale.value = value;
  },
  {
    immediate: true,
  }
);
</script>

<template>
  <label
    for="locale"
    class="block flex-shrink-0 text-sm font-medium text-gray-600"
  >
    <MdiTranslate />
  </label>
  <select
    id="locale"
    v-model="currentLocale"
    class="block appearance-none rounded-md border-0 py-1.5 pl-3 pr-10 text-sm text-gray-800 outline-none ring-1 ring-inset ring-gray-200 focus:ring-primary"
  >
    <template v-for="locale in locales">
      <option v-if="locale.name" :key="locale.code" :value="locale.code">
        {{ locale.name }}
      </option>
    </template>
  </select>
</template>

<script setup lang="ts" name="BubbleMenu">
import { i18n } from "@/locales";
import type { Editor, PluginKey } from "@/tiptap";
import { computed, nextTick, ref, watch, type PropType } from "vue";
import LucideReplace from "~icons/lucide/replace";
import LucideReplaceAll from "~icons/lucide/replace-all";
import MdiArrowDown from "~icons/mdi/arrow-down";
import MdiArrowUp from "~icons/mdi/arrow-up";
import MdiClose from "~icons/mdi/close";
import MdiFormatLetterCase from "~icons/mdi/format-letter-case";
import MdiFormatLetterMatches from "~icons/mdi/format-letter-matches";
import MdiRegex from "~icons/mdi/regex";
import type { SearchAndReplacePluginState } from "./SearchAndReplacePlugin";

const props = defineProps({
  editor: {
    type: Object as PropType<Editor>,
    required: true,
  },
  pluginKey: {
    type: Object as PropType<PluginKey<SearchAndReplacePluginState>>,
    required: true,
  },
  visible: {
    type: Boolean,
    default: false,
  },
});

const searchTerm = ref<string>("");
const replaceTerm = ref<string>("");
const regex = ref<boolean>(false);
const caseSensitive = ref<boolean>(false);
const matchWord = ref<boolean>(false);
const flag = ref<boolean>(false);

const findState = computed(() => {
  void flag.value;
  const { editor, pluginKey } = props;
  if (!editor || !pluginKey) {
    return {
      findIndex: 0,
      findCount: 0,
    };
  }
  const state = pluginKey.getState(editor.state);
  return {
    findIndex: state?.findIndex || 0,
    findCount: state?.findCount || 0,
  };
});

const findNextSearchResult = () => {
  props.editor.commands.findNext();
};

const findPreviousSearchResult = () => {
  props.editor.commands.findPrevious();
};

// eslint-disable-next-line @typescript-eslint/no-explicit-any
const updateSearchReplace = (value: any) => {
  const { editor, pluginKey } = props;
  if (!editor || !pluginKey) {
    return;
  }
  const tr = editor.state.tr;
  tr.setMeta(pluginKey, value);
  editor.view.dispatch(tr);
  flag.value = !flag.value;
};

const replace = () => {
  props.editor.commands.replace();
  flag.value = !flag.value;
};

const replaceAll = () => {
  props.editor.commands.replaceAll();
  flag.value = !flag.value;
};

const handleCloseSearch = () => {
  props.editor.commands.closeSearch();
};

watch(
  () => searchTerm.value.trim(),
  (val, oldVal) => {
    if (val !== oldVal) {
      updateSearchReplace({
        setSearchTerm: val,
      });
    }
  }
);

watch(
  () => replaceTerm.value.trim(),
  (val, oldVal) => {
    if (val !== oldVal) {
      updateSearchReplace({
        setReplaceTerm: val,
      });
    }
  }
);

watch(
  () => regex.value,
  (val, oldVal) => {
    if (val !== oldVal) {
      updateSearchReplace({
        setRegex: val,
      });
    }
  }
);

watch(
  () => caseSensitive.value,
  (val, oldVal) => {
    if (val !== oldVal) {
      updateSearchReplace({
        setCaseSensitive: val,
      });
    }
  }
);

watch(
  () => matchWord.value,
  (val, oldVal) => {
    if (val !== oldVal) {
      updateSearchReplace({
        setMatchWord: val,
      });
    }
  }
);

const searchInput = ref<HTMLInputElement | null>(null);

watch(
  () => props.visible,
  (val) => {
    if (val) {
      nextTick(() => {
        searchInput.value?.focus();
      });
    }
  }
);
</script>
<template>
  <Transition v-show="visible" appear name="slide">
    <div
      class="absolute right-5 top-0 z-50 float-right flex min-w-[500px] justify-end rounded bg-white p-1 !pt-2 shadow"
      @keydown.escape.prevent="handleCloseSearch"
    >
      <section class="flex w-full flex-col gap-1">
        <div class="relative flex items-center">
          <div class="relative w-full max-w-[55%]">
            <input
              ref="searchInput"
              v-model="searchTerm"
              type="text"
              class="block w-full rounded border !border-solid border-gray-300 bg-gray-50 p-1 !pr-[5.5rem] ps-2 !text-sm !leading-7 text-gray-900 focus:border-blue-500 focus:ring-blue-500"
              :placeholder="
                i18n.global.t(
                  'editor.extensions.search_and_replace.search_placeholder'
                )
              "
              tabindex="2"
              @keydown.enter.prevent="findNextSearchResult"
            />
            <div class="absolute inset-y-0 end-0 flex items-center gap-1 pr-1">
              <button
                :title="
                  i18n.global.t(
                    'editor.extensions.search_and_replace.case_sensitive'
                  )
                "
                type="button"
                class="rounded-sm p-0.5 hover:bg-gray-200"
                :class="{
                  '!bg-blue-200 outline outline-1 outline-blue-500 hover:!bg-blue-200':
                    caseSensitive,
                }"
                @click="caseSensitive = !caseSensitive"
              >
                <MdiFormatLetterCase></MdiFormatLetterCase>
              </button>
              <button
                :title="
                  i18n.global.t(
                    'editor.extensions.search_and_replace.match_word'
                  )
                "
                type="button"
                class="rounded-sm p-0.5 hover:bg-gray-200"
                :class="{
                  '!bg-blue-200 outline outline-1 outline-blue-500 hover:!bg-blue-200':
                    matchWord,
                }"
                @click="matchWord = !matchWord"
              >
                <MdiFormatLetterMatches></MdiFormatLetterMatches>
              </button>
              <button
                :title="
                  i18n.global.t(
                    'editor.extensions.search_and_replace.use_regex'
                  )
                "
                type="button"
                class="rounded-sm p-0.5 hover:bg-gray-200"
                :class="{
                  '!bg-blue-200 outline outline-1 outline-blue-500 hover:!bg-blue-200':
                    regex,
                }"
                @click="regex = !regex"
              >
                <MdiRegex></MdiRegex>
              </button>
            </div>
          </div>
          <div class="mx-2 min-w-[130px] text-sm">
            <div v-if="findState.findCount === 0">
              <span :class="{ 'text-red-600': searchTerm.length > 0 }">{{
                i18n.global.t("editor.extensions.search_and_replace.not_found")
              }}</span>
            </div>
            <div v-else>
              <span>
                {{
                  i18n.global.t(
                    "editor.extensions.search_and_replace.occurrence_found",
                    {
                      index: findState.findIndex + 1,
                      total: findState.findCount,
                    }
                  )
                }}
              </span>
            </div>
          </div>
          <div class="absolute right-0 flex h-full items-center">
            <button
              :title="
                i18n.global.t(
                  'editor.extensions.search_and_replace.find_previous'
                )
              "
              type="button"
              class="rounded-sm p-0.5 opacity-50"
              :class="{
                '!opacity-100 hover:!bg-gray-200': findState.findCount > 0,
              }"
              :disabled="findState.findCount === 0"
              @click="findPreviousSearchResult"
            >
              <MdiArrowUp></MdiArrowUp>
            </button>
            <button
              :title="
                i18n.global.t('editor.extensions.search_and_replace.find_next')
              "
              type="button"
              class="rounded-sm p-0.5 opacity-50"
              :class="{
                '!opacity-100 hover:!bg-gray-200': findState.findCount > 0,
              }"
              :disabled="findState.findCount === 0"
              @click="findNextSearchResult"
            >
              <MdiArrowDown></MdiArrowDown>
            </button>
            <button
              :title="
                i18n.global.t('editor.extensions.search_and_replace.close')
              "
              type="button"
              class="rounded-sm p-0.5 hover:bg-gray-200"
              @click="handleCloseSearch"
            >
              <MdiClose></MdiClose>
            </button>
          </div>
        </div>

        <div class="flex items-center">
          <div class="relative w-full max-w-[55%]">
            <input
              v-model="replaceTerm"
              type="text"
              class="block w-full rounded border !border-solid border-gray-300 bg-gray-50 p-1 ps-2 !text-sm !leading-7 text-gray-900 focus:border-blue-500 focus:ring-blue-500"
              :placeholder="
                i18n.global.t(
                  'editor.extensions.search_and_replace.replace_placeholder'
                )
              "
              tabindex="2"
              @keydown.enter.prevent="replace"
            />
          </div>
          <div class="mx-2 flex items-center gap-2">
            <button
              :title="
                i18n.global.t('editor.extensions.search_and_replace.replace')
              "
              type="button"
              class="rounded-sm p-0.5 opacity-50"
              :class="{
                '!opacity-100 hover:!bg-gray-200': findState.findCount > 0,
              }"
              :disabled="findState.findCount === 0"
              @click="replace"
            >
              <LucideReplace></LucideReplace>
            </button>
            <button
              :title="
                i18n.global.t(
                  'editor.extensions.search_and_replace.replace_all'
                )
              "
              type="button"
              class="rounded-sm p-0.5 opacity-50"
              :class="{
                '!opacity-100 hover:!bg-gray-200': findState.findCount > 0,
              }"
              :disabled="findState.findCount === 0"
              @click="replaceAll"
            >
              <LucideReplaceAll></LucideReplaceAll>
            </button>
          </div>
        </div>
      </section>
    </div>
  </Transition>
</template>
<style>
.slide-enter-active,
.slide-leave-active {
  transition: transform 0.25s;
}

.slide-enter-from,
.slide-leave-to {
  transform: translateY(-100%);
}

.slide-enter-to,
.slide-leave-from {
  transform: translateY(0);
}
</style>

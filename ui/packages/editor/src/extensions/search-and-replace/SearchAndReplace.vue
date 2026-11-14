<script setup lang="ts">
import { vTooltip } from "@halo-dev/components";
import { FindAndReplacePluginKey } from "@tiptap/extension-find-and-replace";
import { computed, nextTick, onBeforeUnmount, shallowRef, watch } from "vue";
import LucideReplace from "~icons/lucide/replace";
import LucideReplaceAll from "~icons/lucide/replace-all";
import MdiFormatLetterCase from "~icons/mdi/format-letter-case";
import MdiFormatLetterMatches from "~icons/mdi/format-letter-matches";
import MdiRegex from "~icons/mdi/regex";
import MingcuteArrowDownLine from "~icons/mingcute/arrow-down-line";
import MingcuteArrowUpLine from "~icons/mingcute/arrow-up-line";
import MingcuteCloseLine from "~icons/mingcute/close-line";
import { i18n } from "@/locales";
import type { Editor } from "@/tiptap";
import IconButton from "./IconButton.vue";
import MatchToggleButton from "./MatchToggleButton.vue";
import type { SearchAndReplacePanelState } from "./types";

const props = defineProps<{
  editor: Editor;
  panel: SearchAndReplacePanelState;
}>();

const emit = defineEmits<{
  searchTermChange: [value: string];
  replaceTermChange: [value: string];
  caseSensitiveChange: [value: boolean];
  useRegexChange: [value: boolean];
  wholeWordChange: [value: boolean];
}>();

const searchTerm = shallowRef(props.panel.searchTerm);
const replaceTerm = shallowRef(props.panel.replaceTerm);
const caseSensitive = shallowRef(props.panel.caseSensitive);
const useRegex = shallowRef(props.panel.useRegex);
const wholeWord = shallowRef(props.panel.wholeWord);
const transactionVersion = shallowRef(0);

const handleTransaction = () => {
  transactionVersion.value += 1;
};

props.editor.on("transaction", handleTransaction);
onBeforeUnmount(() => {
  props.editor.off("transaction", handleTransaction);
});

const findState = computed(() => {
  void transactionVersion.value;
  const state = FindAndReplacePluginKey.getState(props.editor.state);

  return {
    findIndex: state?.currentIndex ?? 0,
    findCount: state?.results.length ?? 0,
  };
});

const findNextSearchResult = () => {
  props.editor.commands.goToNextResult();
};

const findPreviousSearchResult = () => {
  props.editor.commands.goToPreviousResult();
};

const replace = () => {
  props.editor.commands.replace();
};

const replaceAll = () => {
  props.editor.commands.replaceAll();
};

const handleCloseSearch = () => {
  props.editor.commands.closeSearch();
};

watch(searchTerm, (value) => {
  emit("searchTermChange", value);
  props.editor.commands.setSearchTerm(value);
});

watch(replaceTerm, (value) => {
  emit("replaceTermChange", value);
  props.editor.commands.setReplaceTerm(value);
});

watch(caseSensitive, (value) => {
  emit("caseSensitiveChange", value);
  props.editor.commands.setCaseSensitive(value);
});

watch(useRegex, (value) => {
  emit("useRegexChange", value);
  props.editor.commands.setUseRegex(value);
});

watch(wholeWord, (value) => {
  emit("wholeWordChange", value);
  props.editor.commands.setWholeWord(value);
});

const searchInput = shallowRef<HTMLInputElement | null>(null);

watch(
  () => props.panel.visible,
  (visible) => {
    if (visible) {
      nextTick(() => {
        searchInput.value?.focus();
      });
    }
  }
);
</script>
<template>
  <Transition v-show="panel.visible" appear name="slide">
    <div
      class="absolute right-5 top-0 z-50 float-right flex min-w-[500px] justify-end rounded bg-white p-1 !pt-2 shadow"
      @keydown.escape.prevent="handleCloseSearch"
    >
      <section class="flex w-full flex-col gap-2">
        <div class="relative flex items-center">
          <div class="relative w-full max-w-[55%]">
            <input
              ref="searchInput"
              v-model="searchTerm"
              type="text"
              class="block size-full h-9 rounded-md bg-white px-3 text-sm text-gray-900 ring-1 ring-gray-100 transition-all placeholder:text-gray-400 focus:!ring-1 focus:!ring-primary"
              :placeholder="
                i18n.global.t(
                  'editor.extensions.search_and_replace.search_placeholder'
                )
              "
              tabindex="2"
              @keydown.enter.prevent="findNextSearchResult"
            />
            <div class="absolute inset-y-0 end-0 flex items-center gap-1 pr-1">
              <MatchToggleButton
                v-tooltip="
                  i18n.global.t(
                    'editor.extensions.search_and_replace.case_sensitive'
                  )
                "
                :is-active="caseSensitive"
                @click="caseSensitive = !caseSensitive"
              >
                <MdiFormatLetterCase />
              </MatchToggleButton>
              <MatchToggleButton
                v-tooltip="
                  i18n.global.t(
                    'editor.extensions.search_and_replace.match_word'
                  )
                "
                :is-active="wholeWord"
                @click="wholeWord = !wholeWord"
              >
                <MdiFormatLetterMatches />
              </MatchToggleButton>
              <MatchToggleButton
                v-tooltip="
                  i18n.global.t(
                    'editor.extensions.search_and_replace.use_regex'
                  )
                "
                :is-active="useRegex"
                @click="useRegex = !useRegex"
              >
                <MdiRegex />
              </MatchToggleButton>
            </div>
          </div>
          <div class="mx-2 min-w-[130px] text-sm">
            <span
              v-if="findState.findCount === 0"
              :class="{ 'text-red-600': searchTerm.length > 0 }"
            >
              {{
                i18n.global.t("editor.extensions.search_and_replace.not_found")
              }}
            </span>
            <span v-else>
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
          <div class="absolute right-0 flex h-full items-center">
            <IconButton
              v-tooltip="
                i18n.global.t(
                  'editor.extensions.search_and_replace.find_previous'
                )
              "
              :disabled="findState.findCount === 0"
              @click="findPreviousSearchResult"
            >
              <MingcuteArrowUpLine />
            </IconButton>
            <IconButton
              v-tooltip="
                i18n.global.t('editor.extensions.search_and_replace.find_next')
              "
              :disabled="findState.findCount === 0"
              @click="findNextSearchResult"
            >
              <MingcuteArrowDownLine />
            </IconButton>
            <IconButton
              v-tooltip="
                i18n.global.t('editor.extensions.search_and_replace.close')
              "
              @click="handleCloseSearch"
            >
              <MingcuteCloseLine />
            </IconButton>
          </div>
        </div>

        <div class="flex items-center">
          <div class="relative w-full max-w-[55%]">
            <input
              v-model="replaceTerm"
              type="text"
              class="block size-full h-9 rounded-md bg-white px-3 text-sm text-gray-900 ring-1 ring-gray-100 transition-all placeholder:text-gray-400 focus:!ring-1 focus:!ring-primary"
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
            <IconButton
              v-tooltip="
                i18n.global.t('editor.extensions.search_and_replace.replace')
              "
              :disabled="findState.findCount === 0"
              @click="replace"
            >
              <LucideReplace />
            </IconButton>

            <IconButton
              v-tooltip="
                i18n.global.t(
                  'editor.extensions.search_and_replace.replace_all'
                )
              "
              :disabled="findState.findCount === 0"
              @click="replaceAll"
            >
              <LucideReplaceAll />
            </IconButton>
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

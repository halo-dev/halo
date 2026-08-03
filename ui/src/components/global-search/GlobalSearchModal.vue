<script lang="ts" setup>
import { VModal } from "@halo-dev/components";
import { useEventListener } from "@vueuse/core";
import { onMounted, ref, watch } from "vue";
import { useRoute, useRouter } from "vue-router";
import GlobalSearchResultItem from "./GlobalSearchResultItem.vue";
import type { GlobalSearchResult } from "./types";
import { useGlobalSearch } from "./use-global-search";

const router = useRouter();
const route = useRoute();

const emit = defineEmits<{
  (e: "close"): void;
}>();

const modal = ref<InstanceType<typeof VModal> | null>(null);
const globalSearchInput = ref<HTMLInputElement | null>(null);

const {
  keyword,
  results,
  selectedIndex,
  selectedResult,
  selectNext,
  selectPrevious,
  isInitial,
  isSearching,
  hasPartialFailure,
  isFinalEmpty,
} = useGlobalSearch();

const handleKeydown = (e: KeyboardEvent) => {
  const { key, ctrlKey } = e;

  if (key === "ArrowUp" || (key === "k" && ctrlKey)) {
    selectPrevious();
    e.preventDefault();
  }

  if (key === "ArrowDown" || (key === "j" && ctrlKey)) {
    selectNext();
    e.preventDefault();
  }

  if (key === "Enter") {
    handleRoute(selectedResult.value);
  }

  if (key === "Escape") {
    modal.value?.close();
    e.preventDefault();
  }
};

const handleRoute = async (item: GlobalSearchResult | null) => {
  if (!item) {
    return;
  }
  // if route has query params, need router.go(0)
  if (typeof item.route !== "string" && "query" in item.route) {
    if ("name" in item.route && route.name === item.route.name) {
      await router.push(item.route);
      router.go(0);
      return;
    }
  }
  router.push(item.route);
  modal.value?.close();
};

watch(
  () => selectedIndex.value,
  (index) => {
    if (index > 0) {
      document.getElementById(`search-item-${index}`)?.scrollIntoView();
      return;
    }
    document.getElementById("search-input")?.scrollIntoView();
  }
);

onMounted(() => {
  setTimeout(() => {
    globalSearchInput.value?.focus();
  }, 100);
});

useEventListener("keydown", handleKeydown);
</script>

<template>
  <VModal
    ref="modal"
    :body-class="['!p-0']"
    :mount-to-body="true"
    :width="650"
    :centered="false"
    :layer-closable="true"
    @close="emit('close')"
  >
    <div id="search-input" class="border-b border-gray-100 px-4 py-2.5">
      <input
        ref="globalSearchInput"
        v-model="keyword"
        :placeholder="$t('core.components.global_search.placeholder')"
        class="w-full px-0 py-1 text-base outline-none"
        autocomplete="off"
        autocorrect="off"
        spellcheck="false"
      />
    </div>
    <div class="px-2 py-2.5">
      <div
        v-if="isInitial"
        class="flex items-center justify-center text-sm text-gray-500"
      >
        <span>{{ $t("core.components.global_search.states.initial") }}</span>
      </div>
      <template v-else>
        <div
          v-if="isFinalEmpty"
          class="flex items-center justify-center text-sm text-gray-500"
        >
          <span>{{ $t("core.components.global_search.no_results") }}</span>
        </div>
        <ul
          v-if="results.length > 0"
          class="box-border flex h-full w-full flex-col gap-1"
          role="list"
        >
          <li
            v-for="(item, itemIndex) in results"
            :id="`search-item-${itemIndex}`"
            :key="item.id"
            @click="handleRoute(item)"
          >
            <GlobalSearchResultItem
              :item="item"
              :selected="selectedIndex === itemIndex"
            />
          </li>
        </ul>
        <div
          v-if="isSearching"
          class="flex items-center justify-center px-2 py-1 text-xs text-gray-500"
        >
          <span>{{ $t("core.components.global_search.states.loading") }}</span>
        </div>
        <div
          v-if="hasPartialFailure"
          class="flex items-center justify-center px-2 py-1 text-xs text-gray-500"
        >
          <span>
            {{ $t("core.components.global_search.states.partial_failure") }}
          </span>
        </div>
      </template>
    </div>
    <div class="border-t border-gray-100 px-4 py-2.5">
      <div class="flex items-center justify-end">
        <span class="mr-1 text-xs text-gray-600">
          {{ $t("core.components.global_search.buttons.select") }}
        </span>
        <kbd
          class="mr-1 w-5 rounded border p-0.5 text-center text-[10px] text-gray-600 shadow-sm"
        >
          ↑
        </kbd>
        <kbd
          class="mr-5 w-5 rounded border p-0.5 text-center text-[10px] text-gray-600 shadow-sm"
        >
          ↓
        </kbd>
        <span class="mr-1 text-xs text-gray-600">
          {{ $t("core.common.buttons.confirm") }}
        </span>
        <kbd
          class="mr-5 rounded border p-0.5 text-[10px] text-gray-600 shadow-sm"
        >
          Enter
        </kbd>
        <span class="mr-1 text-xs text-gray-600">
          {{ $t("core.common.buttons.close") }}
        </span>
        <kbd class="rounded border p-0.5 text-[10px] text-gray-600 shadow-sm">
          Esc
        </kbd>
      </div>
    </div>
  </VModal>
</template>

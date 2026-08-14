<script lang="ts" setup>
import {
  computed,
  onMounted,
  onUnmounted,
  shallowRef,
  useTemplateRef,
} from "vue";
import MingcuteCloseLine from "~icons/mingcute/close-line";
import MingcuteKeyboardLine from "~icons/mingcute/keyboard-line";
import MingcuteSearch2Line from "~icons/mingcute/search-2-line";
import MingcuteSearch3Line from "~icons/mingcute/search-3-line";
import {
  getHaloKeyboardShortcuts,
  subscribeHaloKeyboardShortcutHelp,
  subscribeHaloKeyboardShortcuts,
  type HaloKeyboardShortcutCategory,
  type ResolvedHaloKeyboardShortcut,
} from "@/keyboard-shortcuts";
import { i18n } from "@/locales";
import type { VueEditor } from "@/tiptap";
import { formatShortcut, matchShortcut } from "@/utils";
import KeyboardShortcutKeys from "./KeyboardShortcutKeys.vue";

const props = defineProps<{
  editor: VueEditor;
}>();

const visible = shallowRef(false);
const query = shallowRef("");
const shortcuts = shallowRef<ResolvedHaloKeyboardShortcut[]>([]);
const panel = useTemplateRef<HTMLElement>("panel");
const searchInput = useTemplateRef<HTMLInputElement>("searchInput");
let unsubscribeRegistry: (() => void) | undefined;
let unsubscribeHelp: (() => void) | undefined;

const categoryOrder: HaloKeyboardShortcutCategory[] = [
  "general",
  "formatting",
  "structure",
  "navigation",
];

const categoryLabels = computed<Record<HaloKeyboardShortcutCategory, string>>(
  () => ({
    general: i18n.global.t("editor.shortcuts.categories.general"),
    formatting: i18n.global.t("editor.shortcuts.categories.formatting"),
    structure: i18n.global.t("editor.shortcuts.categories.structure"),
    navigation: i18n.global.t("editor.shortcuts.categories.navigation"),
  })
);

function refreshShortcuts() {
  shortcuts.value = getHaloKeyboardShortcuts(props.editor);
}

function getShortcutSearchText(shortcut: ResolvedHaloKeyboardShortcut) {
  return [
    shortcut.label,
    shortcut.description,
    categoryLabels.value[shortcut.category],
    ...shortcut.keys,
    ...shortcut.keys.map(formatShortcut),
  ]
    .filter(Boolean)
    .join(" ")
    .toLocaleLowerCase();
}

const filteredShortcuts = computed(() => {
  const keywords = query.value
    .trim()
    .toLocaleLowerCase()
    .split(/\s+/)
    .filter(Boolean);
  if (!keywords.length) {
    return shortcuts.value;
  }

  return shortcuts.value.filter((shortcut) => {
    const searchText = getShortcutSearchText(shortcut);
    return keywords.every((keyword) => searchText.includes(keyword));
  });
});

const groups = computed(() =>
  categoryOrder
    .map((category) => ({
      category,
      label: categoryLabels.value[category],
      shortcuts: filteredShortcuts.value.filter(
        (shortcut) => shortcut.category === category
      ),
    }))
    .filter((group) => group.shortcuts.length)
);

function open() {
  visible.value = true;
}

function isOpenHelpShortcut(event: KeyboardEvent) {
  return ["Alt-Mod-/", "Alt-Mod-?", "Mod-/", "Alt-0"].some((shortcut) =>
    matchShortcut(event, shortcut)
  );
}

function close() {
  const shouldRestoreEditorFocus = panel.value?.contains(
    document.activeElement
  );
  visible.value = false;
  if (shouldRestoreEditorFocus) {
    props.editor.commands.focus();
  }
}

function handleWindowKeydown(event: KeyboardEvent) {
  if (visible.value && event.key === "Escape") {
    event.preventDefault();
    event.stopImmediatePropagation();
    close();
    return;
  }

  if (!visible.value && isOpenHelpShortcut(event)) {
    event.preventDefault();
    event.stopImmediatePropagation();
    open();
  }
}

function clearSearch() {
  query.value = "";
  searchInput.value?.focus();
}

onMounted(() => {
  unsubscribeRegistry = subscribeHaloKeyboardShortcuts(
    props.editor,
    refreshShortcuts
  );
  refreshShortcuts();
  unsubscribeHelp = subscribeHaloKeyboardShortcutHelp(props.editor, open);
  window.addEventListener("keydown", handleWindowKeydown, true);
});

onUnmounted(() => {
  unsubscribeRegistry?.();
  unsubscribeHelp?.();
  window.removeEventListener("keydown", handleWindowKeydown, true);
});
</script>

<template>
  <Teleport to="body">
    <Transition name="shortcut-panel">
      <aside
        v-if="visible"
        ref="panel"
        role="complementary"
        :aria-label="i18n.global.t('editor.shortcuts.title')"
        class="fixed inset-y-0 right-0 z-[1000] flex w-[min(30rem,calc(100vw-1rem))] flex-col border-l border-gray-200 bg-white shadow-[-8px_0_28px_rgba(15,23,42,0.12)]"
      >
        <header
          class="flex min-h-14 flex-none items-center justify-between border-b border-gray-100 px-5"
        >
          <div class="flex min-w-0 items-center gap-2.5">
            <MingcuteKeyboardLine class="size-5 flex-none text-gray-500" />
            <h2 class="truncate text-base font-semibold text-gray-900">
              {{ i18n.global.t("editor.shortcuts.title") }}
            </h2>
          </div>
          <button
            type="button"
            class="flex size-8 flex-none items-center justify-center rounded-md text-gray-400 transition-colors hover:bg-gray-100 hover:text-gray-700"
            :aria-label="i18n.global.t('editor.shortcuts.close')"
            @click="close"
          >
            <MingcuteCloseLine class="size-5" />
          </button>
        </header>

        <div class="flex-none border-b border-gray-100 px-5 pb-4 pt-3">
          <p class="mb-3 text-xs leading-5 text-gray-500">
            {{ i18n.global.t("editor.shortcuts.description") }}
          </p>
          <label
            class="shortcut-search group relative block h-10 w-full rounded-lg bg-white shadow-sm ring-1 ring-gray-200 transition focus-within:!ring-2 focus-within:!ring-primary/30"
          >
            <span class="sr-only">
              {{ i18n.global.t("editor.shortcuts.search") }}
            </span>
            <MingcuteSearch2Line
              class="pointer-events-none absolute left-3 top-1/2 z-10 size-4 -translate-y-1/2 text-gray-400 transition-colors group-focus-within:text-primary"
            />
            <input
              ref="searchInput"
              v-model="query"
              type="text"
              role="searchbox"
              class="shortcut-search-input block size-full rounded-lg bg-transparent !pl-9 !pr-20 text-sm text-gray-900 outline-none placeholder:text-gray-400"
              :placeholder="
                i18n.global.t('editor.shortcuts.search_placeholder')
              "
            />
            <div
              v-if="query"
              class="absolute right-2 top-1/2 flex -translate-y-1/2 items-center gap-1.5"
            >
              <span class="text-xs tabular-nums text-gray-400">
                {{ filteredShortcuts.length }}
              </span>
              <button
                type="button"
                class="flex size-6 items-center justify-center rounded-md text-gray-400 transition-colors hover:bg-gray-100 hover:text-gray-700"
                :aria-label="i18n.global.t('editor.shortcuts.clear_search')"
                @click="clearSearch"
              >
                <MingcuteCloseLine class="size-3.5" />
              </button>
            </div>
          </label>
        </div>

        <div class="min-h-0 flex-1 overflow-y-auto px-5 py-4">
          <div v-if="groups.length" class="space-y-6">
            <section
              v-for="group in groups"
              :key="group.category"
              :aria-labelledby="`shortcut-category-${group.category}`"
            >
              <div
                class="grid grid-cols-[minmax(0,1fr)_auto] items-center gap-4 border-b border-gray-200 px-1 pb-2"
              >
                <h3
                  :id="`shortcut-category-${group.category}`"
                  class="text-sm font-semibold text-gray-900"
                >
                  {{ group.label }}
                </h3>
                <span class="text-xs font-medium text-gray-400">
                  {{ i18n.global.t("editor.shortcuts.key_column") }}
                </span>
              </div>
              <div>
                <div
                  v-for="shortcut in group.shortcuts"
                  :key="shortcut.id"
                  class="grid min-h-12 grid-cols-[minmax(0,1fr)_auto] items-center gap-4 border-b border-gray-100 px-1 py-2.5 transition-colors hover:bg-gray-50/70"
                >
                  <div class="min-w-0">
                    <div class="text-sm text-gray-700">
                      {{ shortcut.label }}
                    </div>
                    <p
                      v-if="shortcut.description"
                      class="mt-0.5 text-xs leading-4 text-gray-400"
                    >
                      {{ shortcut.description }}
                    </p>
                  </div>
                  <KeyboardShortcutKeys :shortcuts="shortcut.keys" compact />
                </div>
              </div>
            </section>
          </div>

          <div
            v-else
            class="flex min-h-72 flex-col items-center justify-center px-6 text-center"
          >
            <div
              class="mb-3 flex size-11 items-center justify-center rounded-full bg-gray-100 text-gray-400"
            >
              <MingcuteSearch3Line class="size-5" />
            </div>
            <p class="text-sm font-medium text-gray-700">
              {{ i18n.global.t("editor.shortcuts.empty_title") }}
            </p>
            <p class="mt-1 text-xs text-gray-400">
              {{ i18n.global.t("editor.shortcuts.empty_description") }}
            </p>
          </div>
        </div>
      </aside>
    </Transition>
  </Teleport>
</template>

<style scoped>
.shortcut-search-input {
  border: 0 !important;
  box-shadow: none !important;
}

.shortcut-search-input:focus {
  outline: none !important;
  box-shadow: none !important;
}

.shortcut-panel-enter-active {
  will-change: transform;
  transition: transform 260ms cubic-bezier(0.22, 1, 0.36, 1);
}

.shortcut-panel-leave-active {
  will-change: transform;
  transition: transform 220ms cubic-bezier(0.4, 0, 1, 1);
}

.shortcut-panel-enter-from,
.shortcut-panel-leave-to {
  transform: translate3d(100%, 0, 0);
}

@media (prefers-reduced-motion: reduce) {
  .shortcut-panel-enter-active,
  .shortcut-panel-leave-active {
    transition: none;
  }
}
</style>

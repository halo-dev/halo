<script setup lang="ts">
import { VPopover } from "@halo-dev/components";
import { nextTick, ref, useTemplateRef } from "vue";
import MingcuteEdit4Line from "~icons/mingcute/edit-4-line";
import Input from "@/components/base/Input.vue";
import { i18n } from "@/locales";

const props = defineProps<{ alt: string }>();
const emit = defineEmits<{ "update:alt": [alt: string] }>();
const shown = ref(false);
const draft = ref("");
const trigger = useTemplateRef<HTMLButtonElement>("trigger");

async function close() {
  shown.value = false;
  await nextTick();
  trigger.value?.focus();
}

function onOpenUpdate(open: boolean) {
  shown.value = open;
  if (open) {
    draft.value = props.alt;
  }
}

function save(event: Event) {
  const input = (event.target as HTMLFormElement).elements.item(
    0
  ) as HTMLInputElement;
  emit("update:alt", input.value.trim());
  close();
}
</script>

<template>
  <VPopover :open="shown" :offset="10" @update:open="onOpenUpdate">
    <button
      ref="trigger"
      v-tooltip="i18n.global.t('editor.extensions.image.edit_alt')"
      type="button"
      :aria-label="i18n.global.t('editor.extensions.image.edit_alt')"
      :aria-expanded="shown"
      class="text-grey-900 pointer-events-none flex size-8 cursor-pointer items-center justify-center rounded-md bg-white/90 transition-all hover:bg-white hover:text-black active:!bg-white/80 group-focus-within/actions:pointer-events-auto group-hover/image:pointer-events-auto [@media(hover:none)]:pointer-events-auto"
      @click.stop
      @mousedown.stop
      @dragstart.stop.prevent
    >
      <MingcuteEdit4Line class="size-4" />
    </button>
    <template #content>
      <form
        v-if="shown"
        class="w-56"
        @submit.prevent="save"
        @click.stop
        @mousedown.stop
        @keydown.esc.stop.prevent="close"
      >
        <Input
          v-model="draft"
          auto-focus
          :label="i18n.global.t('editor.common.alt')"
          :placeholder="i18n.global.t('editor.common.placeholder.alt_input')"
        />
      </form>
    </template>
  </VPopover>
</template>

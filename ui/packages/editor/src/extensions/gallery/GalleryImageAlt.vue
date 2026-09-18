<script setup lang="ts">
import { VDropdown } from "@halo-dev/components";
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

function save(event: Event) {
  const input = (event.target as HTMLFormElement).elements.item(
    0
  ) as HTMLInputElement;
  emit("update:alt", input.value.trim());
  close();
}
</script>

<template>
  <VDropdown
    v-model:shown="shown"
    class="pointer-events-none group-focus-within/actions:pointer-events-auto group-hover/image:pointer-events-auto [@media(hover:none)]:pointer-events-auto"
    :triggers="['click']"
    :distance="10"
    :no-auto-focus="true"
    @show="draft = props.alt"
    @click.stop
    @mousedown.stop
    @dragstart.stop.prevent
  >
    <button
      ref="trigger"
      v-tooltip="i18n.global.t('editor.extensions.image.edit_alt')"
      type="button"
      :aria-label="i18n.global.t('editor.extensions.image.edit_alt')"
      :aria-expanded="shown"
      class="text-grey-900 flex size-8 cursor-pointer items-center justify-center rounded-md bg-white/90 transition-all hover:bg-white hover:text-black active:!bg-white/80"
    >
      <MingcuteEdit4Line class="size-4" />
    </button>
    <template #popper>
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
  </VDropdown>
</template>

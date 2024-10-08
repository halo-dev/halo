<script lang="ts" setup>
import type { FormKitFrameworkContext } from "@formkit/core";
import { VButton, VPageHeader } from "@halo-dev/components";
import { useEventListener } from "@vueuse/core";
import { computed, ref, type PropType } from "vue";
import RiFullscreenLine from "~icons/ri/fullscreen-line";

const props = defineProps({
  context: {
    type: Object as PropType<FormKitFrameworkContext>,
    required: true,
  },
});

const codeInputWrapperRef = ref();

const language = props.context.language as string;

const onChange = (value: string) => {
  props.context.node.input(value);
};

const fullscreen = ref(false);

const height = computed(() => {
  return fullscreen.value ? "100%" : (props.context.height as string);
});

useEventListener(codeInputWrapperRef, "keydown", (e: KeyboardEvent) => {
  if (e.key === "Escape" && fullscreen.value) {
    fullscreen.value = false;
  }
});
</script>

<template>
  <Suspense>
    <div
      ref="codeInputWrapperRef"
      :style="{ height }"
      :class="{ '!fixed inset-0 z-[999] !block bg-white': fullscreen }"
      class="group relative h-9 w-full"
    >
      <VPageHeader v-if="fullscreen" :title="context.label" class="border-b">
        <template #actions>
          <VButton @click="fullscreen = false">
            {{ $t("core.formkit.code.fullscreen.exit") }}
          </VButton>
        </template>
      </VPageHeader>

      <VCodemirror
        :model-value="props.context._value"
        v-bind="context.attrs"
        height="100%"
        :language="language"
        class="block w-full"
        @change="onChange"
      />

      <button
        v-if="!fullscreen"
        v-tooltip="$t('core.formkit.code.fullscreen.enter')"
        class="absolute bottom-2 right-2 inline-flex cursor-pointer items-center justify-center rounded-full bg-primary p-1.5 text-white opacity-0 transition-all hover:!opacity-90 hover:shadow group-hover:opacity-100"
        @click="fullscreen = true"
      >
        <RiFullscreenLine class="text-xs" />
      </button>
    </div>

    <template #fallback>
      <span class="p-1 text-xs text-gray-400">
        {{ $t("core.common.status.loading") }}...
      </span>
    </template>
  </Suspense>
</template>

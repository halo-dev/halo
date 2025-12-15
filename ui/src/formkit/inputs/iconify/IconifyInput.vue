<script setup lang="ts">
import type { FormKitFrameworkContext } from "@formkit/core";
import {
  IconClose,
  VButton,
  VDropdown,
  type VDropdownPlacement,
} from "@halo-dev/components";
import { Icon } from "@iconify/vue";
import { computed, useTemplateRef, type PropType } from "vue";
import RiCodeSSlashLine from "~icons/ri/code-s-slash-line";
import IconifyPicker from "./IconifyPicker.vue";
import type { IconifyFormat } from "./types";

const props = defineProps({
  context: {
    type: Object as PropType<FormKitFrameworkContext>,
    required: true,
  },
});

const format = computed(() => props.context.format as IconifyFormat);
const popperPlacement = computed(
  () => props.context.popperPlacement as VDropdownPlacement
);

const onSelect = (icon: string) => {
  props.context.node.input(icon);
};

const editFormDropdown =
  useTemplateRef<InstanceType<typeof VDropdown>>("editFormDropdown");

function onEditFormSubmit({ value }: { value: string }) {
  props.context.node.input(value);
  editFormDropdown.value?.hide();
}
</script>

<template>
  <div class="group/iconify-input inline-flex items-center gap-2">
    <VDropdown
      class="inline-flex"
      popper-class="w-full sm:w-auto"
      :placement="popperPlacement"
    >
      <button
        v-tooltip="$t('core.formkit.iconify.placeholder')"
        type="button"
        class="inline-flex h-9 items-center justify-center rounded-lg border bg-white px-2 transition-all hover:bg-gray-50 hover:shadow active:bg-gray-100"
        :aria-label="$t('core.formkit.iconify.placeholder')"
      >
        <div v-if="!context._value" class="text-sm text-gray-600">
          {{ $t("core.formkit.iconify.placeholder") }}
        </div>
        <div
          v-else
          class="inline-flex size-full items-center justify-center [&>*]:size-5"
        >
          <img
            v-if="['url', 'dataurl'].includes(format)"
            :src="context._value"
            alt="Selected icon"
          />
          <Icon v-else-if="format === 'name'" :icon="context._value" />
          <div
            v-else
            class="inline-flex size-full items-center justify-center"
            v-html="context._value"
          ></div>
        </div>
      </button>
      <template #popper>
        <IconifyPicker :format="format" @select="onSelect" />
      </template>
    </VDropdown>
    <div class="inline-flex items-center gap-1.5">
      <VDropdown ref="editFormDropdown" class="inline-flex">
        <template #default="{ shown }">
          <button
            v-tooltip="$t('core.formkit.iconify.operations.edit')"
            type="button"
            :aria-label="$t('core.formkit.iconify.operations.edit')"
            class="text-gray-500 opacity-0 transition-all hover:text-gray-900 group-hover/iconify-input:opacity-100"
            :class="{ '!text-gray-900 !opacity-100': shown }"
          >
            <RiCodeSSlashLine />
          </button>
        </template>
        <template #popper>
          <div class="w-96">
            <FormKit
              id="icon-edit-form"
              type="form"
              ignore
              name="icon-edit-form"
              @submit="onEditFormSubmit"
            >
              <FormKit
                type="code"
                height="120px"
                :model-value="context._value"
                language="html"
                name="value"
              />
            </FormKit>
            <div class="mt-4">
              <VButton
                type="secondary"
                @click="$formkit.submit('icon-edit-form')"
              >
                {{ $t("core.common.buttons.save") }}
              </VButton>
            </div>
          </div>
        </template>
      </VDropdown>
      <button
        v-if="context._value"
        v-tooltip="$t('core.common.buttons.delete')"
        type="button"
        :aria-label="$t('core.common.buttons.delete')"
        class="text-gray-500 opacity-0 transition-all hover:text-gray-900 group-hover/iconify-input:opacity-100"
        @click="context.node.input(undefined)"
      >
        <IconClose />
      </button>
    </div>
  </div>
</template>

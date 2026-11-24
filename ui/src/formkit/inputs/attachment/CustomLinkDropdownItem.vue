<script lang="ts" setup>
import { VButton, VPopover } from "@halo-dev/components";
import { nextTick, watch } from "vue";
import { setFocus } from "@/formkit/utils/focus";

defineProps<{
  url?: string;
}>();

const emit = defineEmits<{
  (event: "submit", url: string): void;
}>();

// The panel is portaled outside the attachment menu, so the menu needs to know
// when it is open to stay visible while the user fills in the URL.
const open = defineModel<boolean>("open", { default: false });

function onSubmit({ value }: { value: string }) {
  emit("submit", value.trim());
  open.value = false;
}

watch(open, async (value) => {
  if (!value) {
    return;
  }

  await nextTick();
  setFocus("url");
});
</script>
<template>
  <VPopover v-model:open="open">
    <!--
      A `VDropdownItem` always closes the parent menu when clicked. Use a plain
      menu row so the URL panel can open without closing the menu.
    -->
    <div
      class="dropdown-item-wrapper dropdown-item-wrapper--default"
      role="menuitem"
      tabindex="-1"
    >
      <div class="flex items-center gap-3">
        {{ $t("core.formkit.attachment.operations.input") }}
      </div>
    </div>
    <template #content>
      <div class="w-96">
        <FormKit
          id="custom-link-form"
          type="form"
          ignore
          name="custom-link-form"
          @submit="onSubmit"
        >
          <FormKit
            id="url"
            type="text"
            :model-value="url"
            name="value"
            validation="required"
            validation-label="URL"
          />
        </FormKit>
        <div class="mt-4">
          <VButton
            type="secondary"
            @click="$formkit.submit('custom-link-form')"
          >
            {{ $t("core.common.buttons.save") }}
          </VButton>
        </div>
      </div>
    </template>
  </VPopover>
</template>

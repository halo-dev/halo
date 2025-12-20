<script lang="ts" setup>
import { setFocus } from "@/formkit/utils/focus";
import { VButton, VDropdown, VDropdownItem } from "@halo-dev/components";
import { useTemplateRef } from "vue";

defineProps<{
  url?: string;
}>();

const emit = defineEmits<{
  (event: "submit", url: string): void;
}>();

const dropdown = useTemplateRef<InstanceType<typeof VDropdown>>("dropdown");

function onSubmit({ value }: { value: string }) {
  emit("submit", value.trim());
  dropdown.value?.hide();
}

function onDropdownShown() {
  setTimeout(() => {
    setFocus("url");
  }, 100);
}
</script>
<template>
  <VDropdown ref="dropdown" @show="onDropdownShown">
    <VDropdownItem>
      {{ $t("core.formkit.attachment.operations.input") }}
    </VDropdownItem>
    <template #popper>
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
  </VDropdown>
</template>

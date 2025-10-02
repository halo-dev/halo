<script setup lang="ts">
import type { FormKitSchemaDefinition } from "@formkit/core";
import { VButton, VLoading, VModal, VSpace } from "@halo-dev/components";
import type { DashboardWidgetDefinition } from "@halo-dev/console-shared";
import { onMounted, ref, toRaw, useTemplateRef } from "vue";

const props = defineProps<{
  widgetDefinition: DashboardWidgetDefinition;
  config?: Record<string, unknown>;
}>();

const emit = defineEmits<{
  (e: "close"): void;
  (e: "save", config: Record<string, unknown>): void;
}>();

const formSchema = ref<FormKitSchemaDefinition>();
const isLoading = ref(false);

onMounted(async () => {
  const { configFormKitSchema } = props.widgetDefinition || {};
  const isFunction = typeof configFormKitSchema === "function";

  try {
    isLoading.value = true;
    const schema = isFunction
      ? await configFormKitSchema()
      : configFormKitSchema;
    formSchema.value = (schema || []) as FormKitSchemaDefinition;
  } catch (e) {
    console.error(e);
  } finally {
    isLoading.value = false;
  }
});

const initialConfig =
  props.config || props.widgetDefinition.defaultConfig || {};

const modal = useTemplateRef<InstanceType<typeof VModal> | null>("modal");

function onSubmit(config: Record<string, unknown>) {
  emit("save", config);
}
</script>
<template>
  <VModal
    ref="modal"
    mount-to-body
    :title="$t('core.dashboard_designer.config_modal.title')"
    @close="emit('close')"
  >
    <div>
      <VLoading v-if="isLoading" />
      <FormKit
        v-if="formSchema"
        :id="widgetDefinition.id"
        :value="initialConfig"
        :name="widgetDefinition.id"
        :preserve="true"
        type="form"
        @submit="onSubmit"
      >
        <FormKitSchema
          :schema="toRaw(formSchema)"
          :data="toRaw(initialConfig)"
        />
      </FormKit>
    </div>
    <template #footer>
      <VSpace>
        <VButton type="secondary" @click="$formkit.submit(widgetDefinition.id)">
          {{ $t("core.common.buttons.save") }}
        </VButton>
        <VButton @click="modal?.close()">
          {{ $t("core.common.buttons.cancel") }}
        </VButton>
      </VSpace>
    </template>
  </VModal>
</template>

<script lang="ts" setup>
import SubmitButton from "@/components/button/SubmitButton.vue";
import { setFocus } from "@/formkit/utils/focus";
import type { Menu } from "@halo-dev/api-client";
import { coreApiClient } from "@halo-dev/api-client";
import { Toast, VButton, VModal, VSpace } from "@halo-dev/components";
import { useQueryClient } from "@tanstack/vue-query";
import { cloneDeep } from "lodash-es";
import { onMounted, ref } from "vue";
import { useI18n } from "vue-i18n";

const props = withDefaults(
  defineProps<{
    menu?: Menu;
  }>(),
  {
    menu: undefined,
  }
);

const emit = defineEmits<{
  (event: "close"): void;
  (event: "created", menu: Menu): void;
}>();

const queryClient = useQueryClient();
const { t } = useI18n();

const modal = ref<InstanceType<typeof VModal> | null>(null);

const formState = ref<Menu>({
  spec: {
    displayName: "",
    menuItems: [],
  },
  apiVersion: "v1alpha1",
  kind: "Menu",
  metadata: {
    name: "",
    generateName: "menu-",
  },
});

const saving = ref(false);

const modalTitle = props.menu
  ? t("core.menu.menu_editing_modal.titles.update")
  : t("core.menu.menu_editing_modal.titles.create");

const handleSaveMenu = async () => {
  try {
    saving.value = true;
    if (props.menu) {
      await coreApiClient.menu.updateMenu({
        name: formState.value.metadata.name,
        menu: formState.value,
      });
    } else {
      const { data } = await coreApiClient.menu.createMenu({
        menu: formState.value,
      });
      emit("created", data);
    }

    queryClient.invalidateQueries({ queryKey: ["menus"] });

    modal.value?.close();

    Toast.success(t("core.common.toast.save_success"));
  } catch (e) {
    console.error("Failed to create menu", e);
  } finally {
    saving.value = false;
  }
};

onMounted(() => {
  if (props.menu) {
    formState.value = cloneDeep(props.menu);
  }
  setFocus("menuDisplayNameInput");
});
</script>
<template>
  <VModal ref="modal" :width="500" :title="modalTitle" @close="emit('close')">
    <FormKit
      id="menu-form"
      name="menu-form"
      type="form"
      :config="{ validationVisibility: 'submit' }"
      @submit="handleSaveMenu"
    >
      <FormKit
        id="menuDisplayNameInput"
        v-model="formState.spec.displayName"
        :label="$t('core.menu.menu_editing_modal.fields.display_name.label')"
        type="text"
        name="displayName"
        validation="required|length:0,100"
      ></FormKit>
    </FormKit>
    <template #footer>
      <VSpace>
        <SubmitButton
          :loading="saving"
          type="secondary"
          :text="$t('core.common.buttons.submit')"
          @submit="$formkit.submit('menu-form')"
        >
        </SubmitButton>
        <VButton @click="modal?.close()">
          {{ $t("core.common.buttons.cancel_and_shortcut") }}
        </VButton>
      </VSpace>
    </template>
  </VModal>
</template>

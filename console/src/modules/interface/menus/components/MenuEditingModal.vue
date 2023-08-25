<script lang="ts" setup>
import SubmitButton from "@/components/button/SubmitButton.vue";
import { setFocus } from "@/formkit/utils/focus";
import { apiClient } from "@/utils/api-client";
import { reset } from "@formkit/core";
import type { Menu } from "@halo-dev/api-client";
import { Toast, VButton, VModal, VSpace } from "@halo-dev/components";
import cloneDeep from "lodash.clonedeep";
import { computed, ref, watch } from "vue";
import { useI18n } from "vue-i18n";

const props = withDefaults(
  defineProps<{
    visible: boolean;
    menu?: Menu;
    menus?: Menu[];
  }>(),
  {
    visible: false,
    menu: undefined,
    menus: undefined,
  }
);

const emit = defineEmits<{
  (event: "update:visible", visible: boolean): void;
  (event: "close"): void;
  (event: "created", menu: Menu): void;
}>();

const { t } = useI18n();

const initialFormState: Menu = {
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
};

const formState = ref<Menu>(cloneDeep(initialFormState));
const saving = ref(false);

const isUpdateMode = computed(() => {
  return !!formState.value.metadata.creationTimestamp;
});

const modalTitle = computed(() => {
  return isUpdateMode.value
    ? t("core.menu.menu_editing_modal.titles.update")
    : t("core.menu.menu_editing_modal.titles.create");
});

const handleCreateMenu = async () => {
  try {
    saving.value = true;
    if (isUpdateMode.value) {
      await apiClient.extension.menu.updatev1alpha1Menu({
        name: formState.value.metadata.name,
        menu: formState.value,
      });
    } else {
      const { data } = await apiClient.extension.menu.createv1alpha1Menu({
        menu: formState.value,
      });
      emit("created", data);
    }
    onVisibleChange(false);

    Toast.success(t("core.common.toast.save_success"));
  } catch (e) {
    console.error("Failed to create menu", e);
  } finally {
    saving.value = false;
  }
};

const onVisibleChange = (visible: boolean) => {
  emit("update:visible", visible);
  if (!visible) {
    emit("close");
  }
};

const handleResetForm = () => {
  formState.value = cloneDeep(initialFormState);
  reset("menu-form");
};

watch(
  () => props.visible,
  (visible) => {
    if (visible) {
      setFocus("menuDisplayNameInput");
    } else {
      handleResetForm();
    }
  }
);

watch(
  () => props.menu,
  (menu) => {
    if (menu) {
      formState.value = cloneDeep(menu);
    } else {
      handleResetForm();
    }
  }
);
const menusCopy = ref<Menu[]>([]);
watch(
  () => props.menus,
  (n) => {
    menusCopy.value = n as Menu[];
  }
);
const warning = ref<boolean>(false);
const compareMenusWitchDisplayName = (
  menus: Menu[],
  displayName: string
): boolean => {
  for (const menu of menus) {
    if (displayName && menu.spec.displayName === displayName) {
      return true;
    }
  }
  return false;
};
watch(
  formState,
  (n) => {
    warning.value = compareMenusWitchDisplayName(
      menusCopy.value,
      n.spec.displayName
    );
  },
  { deep: true }
);
</script>
<template>
  <VModal
    :visible="visible"
    :width="500"
    :title="modalTitle"
    @update:visible="onVisibleChange"
  >
    <FormKit
      id="menu-form"
      name="menu-form"
      type="form"
      :config="{ validationVisibility: 'submit' }"
      @submit="handleCreateMenu"
    >
      <FormKit
        id="menuDisplayNameInput"
        v-model="formState.spec.displayName"
        :label="$t('core.menu.menu_editing_modal.fields.display_name.label')"
        type="text"
        name="displayName"
        validation="required|length:0,100"
      >
        <template #help>
          <span v-show="warning" class="text-xs text-red-500">
            {{ t("core.menu.menu_editing_modal.fields.display_name.warning") }}
          </span>
        </template>
      </FormKit>
    </FormKit>
    <template #footer>
      <VSpace>
        <SubmitButton
          v-if="visible"
          :loading="saving"
          type="secondary"
          :text="$t('core.common.buttons.submit')"
          @submit="$formkit.submit('menu-form')"
        >
        </SubmitButton>
        <VButton @click="onVisibleChange(false)">
          {{ $t("core.common.buttons.cancel_and_shortcut") }}
        </VButton>
      </VSpace>
    </template>
  </VModal>
</template>

<script lang="ts" setup>
import { Toast, VButton, VModal, VSpace } from "@halo-dev/components";
import SubmitButton from "@/components/button/SubmitButton.vue";
import { computed, nextTick, ref, watch } from "vue";
import type { Menu, MenuItem, Ref } from "@halo-dev/api-client";
import { apiClient } from "@/utils/api-client";
import { reset } from "@formkit/core";
import cloneDeep from "lodash.clonedeep";
import { setFocus } from "@/formkit/utils/focus";
import AnnotationsForm from "@/components/form/AnnotationsForm.vue";
import { useI18n } from "vue-i18n";

const props = withDefaults(
  defineProps<{
    visible: boolean;
    menu?: Menu;
    parentMenuItem: MenuItem;
    menuItem?: MenuItem;
  }>(),
  {
    visible: false,
    menu: undefined,
    parentMenuItem: undefined,
    menuItem: undefined,
  }
);

const emit = defineEmits<{
  (event: "update:visible", visible: boolean): void;
  (event: "close"): void;
  (event: "saved", menuItem: MenuItem): void;
}>();

const { t } = useI18n();

const initialFormState: MenuItem = {
  spec: {
    displayName: "",
    href: "",
    target: "_self",
    children: [],
    priority: 0,
  },
  apiVersion: "v1alpha1",
  kind: "MenuItem",
  metadata: {
    name: "",
    generateName: "menu-item-",
  },
};

const selectedParentMenuItem = ref<string>("");
const formState = ref<MenuItem>(cloneDeep(initialFormState));
const saving = ref(false);

const isUpdateMode = computed(() => {
  return !!formState.value.metadata.creationTimestamp;
});

const modalTitle = computed(() => {
  return isUpdateMode.value
    ? t("core.menu.menu_item_editing_modal.titles.update")
    : t("core.menu.menu_item_editing_modal.titles.create");
});

const annotationsFormRef = ref<InstanceType<typeof AnnotationsForm>>();

const handleSaveMenuItem = async () => {
  annotationsFormRef.value?.handleSubmit();
  await nextTick();

  const { customAnnotations, annotations, customFormInvalid, specFormInvalid } =
    annotationsFormRef.value || {};
  if (customFormInvalid || specFormInvalid) {
    return;
  }

  formState.value.metadata.annotations = {
    ...annotations,
    ...customAnnotations,
  };

  try {
    saving.value = true;

    const menuItemRef = menuItemRefs.find(
      (ref) => ref.ref?.kind === selectedRefKind.value
    );

    if (menuItemRef?.ref) {
      formState.value.spec.targetRef = {
        ...menuItemRef.ref,
        name: selectedRefName.value,
      };
      formState.value.spec.displayName = undefined;
      formState.value.spec.href = undefined;
    }

    if (isUpdateMode.value) {
      const { data } =
        await apiClient.extension.menuItem.updatev1alpha1MenuItem({
          name: formState.value.metadata.name,
          menuItem: formState.value,
        });

      onVisibleChange(false);
      emit("saved", data);
    } else {
      const { data } =
        await apiClient.extension.menuItem.createv1alpha1MenuItem({
          menuItem: formState.value,
        });

      // if parent menu item is selected, add the new menu item to the parent menu item
      if (selectedParentMenuItem.value) {
        const { data: menuItemToUpdate } =
          await apiClient.extension.menuItem.getv1alpha1MenuItem({
            name: selectedParentMenuItem.value,
          });

        menuItemToUpdate.spec.children = [
          ...(menuItemToUpdate.spec.children || []),
          data.metadata.name,
        ];

        await apiClient.extension.menuItem.updatev1alpha1MenuItem({
          name: menuItemToUpdate.metadata.name,
          menuItem: menuItemToUpdate,
        });
      }

      onVisibleChange(false);
      emit("saved", data);
    }

    Toast.success(t("core.common.toast.save_success"));
  } catch (e) {
    console.error("Failed to create menu item", e);
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
  selectedRefKind.value = "";
  selectedRefName.value = "";
  selectedParentMenuItem.value = "";
  reset("menuitem-form");
};

watch(
  () => props.visible,
  (visible) => {
    if (visible) {
      selectedParentMenuItem.value = props.parentMenuItem?.metadata.name;
      setFocus("displayNameInput");

      if (!props.menuItem) {
        selectedRefName.value = "";
      }
    } else {
      handleResetForm();
    }
  }
);

watch(
  () => props.menuItem,
  (menuItem) => {
    if (menuItem) {
      formState.value = cloneDeep(menuItem);

      // Set Ref related
      const { targetRef } = formState.value.spec;

      if (!targetRef) {
        return;
      }

      selectedRefName.value = targetRef.name;
      selectedRefKind.value = targetRef.kind as string;
    } else {
      handleResetForm();
    }
  }
);

interface MenuItemRef {
  label: string;
  inputType?: string;
  ref?: Ref;
}

const baseRef: Ref = {
  group: "content.halo.run",
  version: "v1alpha1",
  name: "",
};

const menuItemRefs: MenuItemRef[] = [
  {
    label: t(
      "core.menu.menu_item_editing_modal.fields.ref_kind.options.custom"
    ),
  },
  {
    label: t("core.menu.menu_item_editing_modal.fields.ref_kind.options.post"),
    inputType: "postSelect",
    ref: {
      ...baseRef,
      kind: "Post",
    },
  },
  {
    label: t(
      "core.menu.menu_item_editing_modal.fields.ref_kind.options.single_page"
    ),
    inputType: "singlePageSelect",
    ref: {
      ...baseRef,
      kind: "SinglePage",
    },
  },
  {
    label: t(
      "core.menu.menu_item_editing_modal.fields.ref_kind.options.category"
    ),
    inputType: "categorySelect",
    ref: {
      ...baseRef,
      kind: "Category",
    },
  },
  {
    label: t("core.menu.menu_item_editing_modal.fields.ref_kind.options.tag"),
    inputType: "tagSelect",
    ref: {
      ...baseRef,
      kind: "Tag",
    },
  },
];

const menuItemRefsMap = menuItemRefs.map((menuItemRef) => {
  return {
    label: menuItemRef.label,
    value: menuItemRef.ref?.kind,
  };
});

const selectedRef = computed(() => {
  return menuItemRefs.find(
    (menuItemRef) => menuItemRef.ref?.kind === selectedRefKind.value
  );
});

const selectedRefKind = ref<string>("");
const selectedRefName = ref<string>("");

const onMenuItemSourceChange = () => {
  selectedRefName.value = "";
};
</script>
<template>
  <VModal
    :visible="visible"
    :width="700"
    :title="modalTitle"
    @update:visible="onVisibleChange"
  >
    <FormKit
      id="menuitem-form"
      name="menuitem-form"
      type="form"
      :preserve="true"
      :config="{ validationVisibility: 'submit' }"
      @submit="handleSaveMenuItem"
    >
      <div>
        <div class="md:grid md:grid-cols-4 md:gap-6">
          <div class="md:col-span-1">
            <div class="sticky top-0">
              <span class="text-base font-medium text-gray-900">
                {{ $t("core.menu.menu_item_editing_modal.groups.general") }}
              </span>
            </div>
          </div>
          <div class="mt-5 divide-y divide-gray-100 md:col-span-3 md:mt-0">
            <FormKit
              v-if="!isUpdateMode && menu && visible"
              v-model="selectedParentMenuItem"
              :label="
                $t('core.menu.menu_item_editing_modal.fields.parent.label')
              "
              :placeholder="
                $t(
                  'core.menu.menu_item_editing_modal.fields.parent.placeholder'
                )
              "
              type="menuItemSelect"
              :menu-items="menu?.spec.menuItems || []"
            />

            <FormKit
              v-model="selectedRefKind"
              :options="menuItemRefsMap"
              :disabled="isUpdateMode"
              :label="
                $t('core.menu.menu_item_editing_modal.fields.ref_kind.label')
              "
              type="select"
              @change="onMenuItemSourceChange"
            />

            <FormKit
              v-if="!selectedRefKind"
              id="displayNameInput"
              v-model="formState.spec.displayName"
              :label="
                $t(
                  'core.menu.menu_item_editing_modal.fields.display_name.label'
                )
              "
              type="text"
              name="displayName"
              validation="required|length:0,100"
            />

            <FormKit
              v-if="!selectedRefKind"
              v-model="formState.spec.href"
              :label="$t('core.menu.menu_item_editing_modal.fields.href.label')"
              type="text"
              name="href"
              validation="required|length:0,1024"
            />

            <FormKit
              v-if="selectedRef?.ref"
              :id="selectedRef.inputType"
              :key="selectedRef.inputType"
              v-model="selectedRefName"
              :placeholder="
                $t(
                  'core.menu.menu_item_editing_modal.fields.ref_kind.placeholder',
                  { label: selectedRef.label }
                )
              "
              :label="selectedRef.label"
              :type="selectedRef.inputType"
              validation="required"
            />

            <FormKit
              v-model="formState.spec.target"
              :label="
                $t('core.menu.menu_item_editing_modal.fields.target.label')
              "
              type="select"
              name="target"
              :options="[
                {
                  label: $t(
                    'core.menu.menu_item_editing_modal.fields.target.options.self'
                  ),
                  value: '_self',
                },
                {
                  label: $t(
                    'core.menu.menu_item_editing_modal.fields.target.options.blank'
                  ),
                  value: '_blank',
                },
                {
                  label: $t(
                    'core.menu.menu_item_editing_modal.fields.target.options.parent'
                  ),
                  value: '_parent',
                },
                {
                  label: $t(
                    'core.menu.menu_item_editing_modal.fields.target.options.top'
                  ),
                  value: '_top',
                },
              ]"
            />
          </div>
        </div>
      </div>
    </FormKit>

    <div class="py-5">
      <div class="border-t border-gray-200"></div>
    </div>

    <div class="md:grid md:grid-cols-4 md:gap-6">
      <div class="md:col-span-1">
        <div class="sticky top-0">
          <span class="text-base font-medium text-gray-900">
            {{ $t("core.menu.menu_item_editing_modal.groups.annotations") }}
          </span>
        </div>
      </div>
      <div class="mt-5 divide-y divide-gray-100 md:col-span-3 md:mt-0">
        <AnnotationsForm
          :key="formState.metadata.name"
          ref="annotationsFormRef"
          :value="formState.metadata.annotations"
          kind="MenuItem"
          group=""
        />
      </div>
    </div>

    <template #footer>
      <VSpace>
        <SubmitButton
          v-if="visible"
          :loading="saving"
          type="secondary"
          :text="$t('core.common.buttons.submit')"
          @submit="$formkit.submit('menuitem-form')"
        >
        </SubmitButton>
        <VButton @click="onVisibleChange(false)">
          {{ $t("core.common.buttons.cancel_and_shortcut") }}
        </VButton>
      </VSpace>
    </template>
  </VModal>
</template>

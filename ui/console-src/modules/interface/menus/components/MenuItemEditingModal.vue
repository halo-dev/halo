<script lang="ts" setup>
import { createInput } from "@formkit/vue";
import type {
  Menu,
  MenuItem,
  MenuItemSpecRouteRefEnum,
  MenuItemTreeNode,
  Ref,
} from "@halo-dev/api-client";
import {
  consoleApiClient,
  coreApiClient,
  MenuItemSpecRouteRefEnum as RouteRef,
} from "@halo-dev/api-client";
import { Toast, VButton, VModal, VSpace } from "@halo-dev/components";
import { cloneDeep } from "es-toolkit";
import { computed, nextTick, onMounted, ref } from "vue";
import { useI18n } from "vue-i18n";
import SubmitButton from "@/components/button/SubmitButton.vue";
import type AnnotationsForm from "@/components/form/AnnotationsForm.vue";
import { setFocus } from "@/formkit/utils/focus";
import { buildMenuItemParentMovePosition } from "../utils";
import MenuItemParentSelect from "./MenuItemParentSelect.vue";

const menuItemParentSelectInput = createInput<string>(MenuItemParentSelect, {
  type: "input",
  props: ["menuItemTree", "excludedNames"],
  forceTypeProp: "text",
});

const props = withDefaults(
  defineProps<{
    menu: Menu;
    menuItemTree?: MenuItemTreeNode[];
    parentMenuItem?: MenuItem;
    menuItem?: MenuItem;
  }>(),
  {
    menuItemTree: () => [],
    parentMenuItem: undefined,
    menuItem: undefined,
  }
);

const emit = defineEmits<{
  (event: "close"): void;
  (event: "saved", menuItem: MenuItem, menuItemTree?: MenuItemTreeNode[]): void;
}>();

const { t } = useI18n();

const modal = ref<InstanceType<typeof VModal> | null>(null);
const selectedParentMenuItem = ref<string>("");
const originalParentMenuItem = ref<string>("");
const formState = ref<MenuItem>({
  spec: {
    displayName: "",
    href: "",
    target: "_self",
    menuName: props.menu.metadata.name,
    priority: 0,
  },
  apiVersion: "v1alpha1",
  kind: "MenuItem",
  metadata: {
    name: "",
    generateName: "menu-item-",
  },
});
const saving = ref(false);

const isUpdateMode = !!props.menuItem;

const modalTitle = props.menuItem
  ? t("core.menu.menu_item_editing_modal.titles.update")
  : t("core.menu.menu_item_editing_modal.titles.create");

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
    formState.value.spec.menuName = props.menu.metadata.name;
    if (!isUpdateMode) {
      formState.value.spec.parent = selectedParentMenuItem.value || undefined;
      formState.value.spec.priority = siblingCount.value;
    }

    const menuItemRef = selectedSource.value;

    formState.value.spec.targetRef = undefined;
    formState.value.spec.routeRef = undefined;

    if (menuItemRef?.ref) {
      formState.value.spec.targetRef = {
        ...menuItemRef.ref,
        name: selectedRefName.value,
      };
      formState.value.spec.displayName = undefined;
      formState.value.spec.href = undefined;
    } else if (menuItemRef?.routeRef) {
      formState.value.spec.routeRef = menuItemRef.routeRef;
      formState.value.spec.href = undefined;
    }

    if (isUpdateMode) {
      const { data } = await coreApiClient.menuItem.updateMenuItem({
        name: formState.value.metadata.name,
        menuItem: formState.value,
      });

      const positionRequest = buildMenuItemParentMovePosition(
        formState.value.metadata.name,
        originalParentMenuItem.value,
        selectedParentMenuItem.value
      );

      if (positionRequest) {
        try {
          const { data: menuItemTree } =
            await consoleApiClient.menuItem.updateMenuItemPosition({
              name: positionRequest.name,
              menuItemPositionRequest: {
                menuName: props.menu.metadata.name,
                parentName: positionRequest.parentName,
                beforeName: positionRequest.beforeName,
              },
            });

          emit("saved", data, menuItemTree);
        } catch (e) {
          console.error("Failed to update menu item parent", e);
          emit("saved", data);
          Toast.error(t("core.common.toast.save_failed_and_retry"));
          return;
        }
      } else {
        emit("saved", data);
      }
    } else {
      const { data } = await coreApiClient.menuItem.createMenuItem({
        menuItem: formState.value,
      });

      emit("saved", data);
    }

    modal.value?.close();

    Toast.success(t("core.common.toast.save_success"));
  } catch (e) {
    console.error("Failed to create menu item", e);
  } finally {
    saving.value = false;
  }
};

interface MenuItemRef {
  value: string;
  label: string;
  inputType?: string;
  ref?: Ref;
  routeRef?: MenuItemSpecRouteRefEnum;
}

const baseRef: Omit<Ref, "kind"> = {
  group: "content.halo.run",
  version: "v1alpha1",
  name: "",
};

const menuItemRefs: MenuItemRef[] = [
  {
    value: "custom",
    label: t(
      "core.menu.menu_item_editing_modal.fields.ref_kind.options.custom"
    ),
  },
  {
    value: RouteRef.Archives,
    label: t(
      "core.menu.menu_item_editing_modal.fields.ref_kind.options.archives"
    ),
    routeRef: RouteRef.Archives,
  },
  {
    value: RouteRef.Categories,
    label: t(
      "core.menu.menu_item_editing_modal.fields.ref_kind.options.categories"
    ),
    routeRef: RouteRef.Categories,
  },
  {
    value: RouteRef.Tags,
    label: t("core.menu.menu_item_editing_modal.fields.ref_kind.options.tags"),
    routeRef: RouteRef.Tags,
  },
  {
    value: "Post",
    label: t("core.menu.menu_item_editing_modal.fields.ref_kind.options.post"),
    inputType: "postSelect",
    ref: {
      ...baseRef,
      kind: "Post",
    },
  },
  {
    value: "SinglePage",
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
    value: "Category",
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
    value: "Tag",
    label: t("core.menu.menu_item_editing_modal.fields.ref_kind.options.tag"),
    inputType: "tagSelect",
    ref: {
      ...baseRef,
      kind: "Tag",
    },
  },
];

const isResourceRefLocked = computed(() => !!props.menuItem?.spec.targetRef);

const availableMenuItemRefs = computed(() => {
  if (isUpdateMode && !isResourceRefLocked.value) {
    return menuItemRefs.filter((menuItemRef) => !menuItemRef.ref);
  }
  return menuItemRefs;
});

const menuItemRefsMap = computed(() =>
  availableMenuItemRefs.value.map(({ label, value }) => ({ label, value }))
);

const selectedSource = computed(() =>
  menuItemRefs.find(
    (menuItemRef) => menuItemRef.value === selectedSourceValue.value
  )
);

const selectedSourceValue = ref("custom");
const selectedRefName = ref<string>("");
const isCustomSource = computed(() => selectedSourceValue.value === "custom");
const isRouteSource = computed(() => !!selectedSource.value?.routeRef);

const excludedParentNames = computed(() => {
  return props.menuItem?.metadata.name ? [props.menuItem.metadata.name] : [];
});

const siblingCount = computed(() => {
  if (!selectedParentMenuItem.value) {
    return props.menuItemTree.length;
  }
  const parent = findMenuItemTreeNode(
    props.menuItemTree,
    selectedParentMenuItem.value
  );
  return parent?.children.length || 0;
});

function findMenuItemTreeNode(
  nodes: MenuItemTreeNode[],
  name: string
): MenuItemTreeNode | undefined {
  for (const node of nodes) {
    if (node.menuItem.metadata.name === name) {
      return node;
    }
    const child = findMenuItemTreeNode(node.children, name);
    if (child) {
      return child;
    }
  }
}

const onMenuItemSourceChange = () => {
  selectedRefName.value = "";
  const source = selectedSource.value;
  if (source?.routeRef) {
    formState.value.spec.displayName ||= source.label;
    formState.value.spec.href = undefined;
    formState.value.spec.targetRef = undefined;
    formState.value.spec.routeRef = source.routeRef;
    return;
  }
  if (!source?.ref) {
    if (formState.value.spec.routeRef) {
      formState.value.spec.href = props.menuItem?.status?.href;
    }
    formState.value.spec.routeRef = undefined;
    formState.value.spec.targetRef = undefined;
  }
};

onMounted(() => {
  if (props.menuItem) {
    formState.value = cloneDeep(props.menuItem);

    // Set Ref related
    const { routeRef, targetRef } = formState.value.spec;

    if (targetRef) {
      selectedRefName.value = targetRef.name;
      selectedSourceValue.value = targetRef.kind as string;
    } else if (routeRef) {
      selectedSourceValue.value = routeRef;
    }
  }

  selectedParentMenuItem.value =
    props.parentMenuItem?.metadata.name || props.menuItem?.spec.parent || "";
  originalParentMenuItem.value = props.menuItem?.spec.parent || "";

  setFocus("displayNameInput");
});
</script>
<template>
  <VModal ref="modal" :width="700" :title="modalTitle" @close="emit('close')">
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
              v-model="selectedParentMenuItem"
              :label="
                $t('core.menu.menu_item_editing_modal.fields.parent.label')
              "
              :type="menuItemParentSelectInput"
              :menu-item-tree="props.menuItemTree"
              :excluded-names="excludedParentNames"
            />

            <FormKit
              v-model="selectedSourceValue"
              :options="menuItemRefsMap"
              :disabled="isResourceRefLocked"
              :label="
                $t('core.menu.menu_item_editing_modal.fields.ref_kind.label')
              "
              type="select"
              @change="onMenuItemSourceChange"
            />

            <FormKit
              v-if="isCustomSource || isRouteSource"
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
              v-if="isCustomSource"
              v-model="formState.spec.href"
              :label="$t('core.menu.menu_item_editing_modal.fields.href.label')"
              type="text"
              name="href"
              validation="required|length:0,1024"
            />

            <FormKit
              v-if="selectedSource?.ref"
              :id="selectedSource.inputType"
              :key="selectedSource.inputType"
              v-model="selectedRefName"
              :placeholder="
                $t(
                  'core.menu.menu_item_editing_modal.fields.ref_kind.placeholder',
                  {
                    label: selectedSource.label,
                  }
                )
              "
              :label="selectedSource.label"
              :type="selectedSource.inputType as any"
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
          :form-data="formState"
          group=""
        />
      </div>
    </div>

    <template #footer>
      <VSpace>
        <SubmitButton
          :loading="saving"
          type="secondary"
          :text="$t('core.common.buttons.submit')"
          @submit="$formkit.submit('menuitem-form')"
        >
        </SubmitButton>
        <VButton @click="modal?.close()">
          {{ $t("core.common.buttons.cancel_and_shortcut") }}
        </VButton>
      </VSpace>
    </template>
  </VModal>
</template>

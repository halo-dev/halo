<script lang="ts" setup>
import type { FormKitFrameworkContext } from "@formkit/core";
import { coreApiClient } from "@halo-dev/api-client";
import {
  IconAddCircle,
  IconClose,
  IconExchange,
  IconInformation,
  VButton,
} from "@halo-dev/components";
import { utils } from "@halo-dev/ui-shared";
import { useQuery } from "@tanstack/vue-query";
import { computed, ref, type PropType } from "vue";
import { useI18n } from "vue-i18n";
import MingcuteFileSecurityLine from "~icons/mingcute/file-security-line";
import RiEditBoxLine from "~icons/ri/edit-box-line";
import { secretAnnotations } from "@/constants/annotations";
import SecretCreationModal from "./components/SecretCreationModal.vue";
import SecretEditModal from "./components/SecretEditModal.vue";
import SecretListModal from "./components/SecretListModal.vue";
import type { RequiredKey } from "./types";

const { t } = useI18n();

const props = defineProps({
  context: {
    type: Object as PropType<FormKitFrameworkContext>,
    required: true,
  },
});

const currentValue = computed(() => props.context._value);
const descriptionPreset = computed(
  () => props.context.descriptionPreset as string | undefined
);
const requiredKeys = computed(
  () => props.context.requiredKeys as RequiredKey[]
);

const hasPermission = utils.permission.has(["*"]);

const { data, isLoading, refetch } = useQuery({
  queryKey: ["core:formkit:inputs:secret", currentValue, hasPermission],
  queryFn: async () => {
    if (!currentValue.value || !hasPermission) {
      return null;
    }

    const { data } = await coreApiClient.secret.getSecret({
      name: currentValue.value as string,
    });

    return data;
  },
});

const title = computed(() => {
  if (isLoading.value) {
    return t("core.common.status.loading");
  }

  if (!data.value) {
    return;
  }

  return (
    data.value?.metadata.annotations?.[secretAnnotations.DESCRIPTION] ||
    data.value?.metadata.name
  );
});

const keys = computed(() => {
  return Object.keys(data.value?.stringData || {});
});

const description = computed(() => {
  if (isLoading.value) {
    return "--";
  }

  if (!hasPermission) {
    return t("core.formkit.secret.no_permission");
  }

  if (!data.value) {
    return t("core.formkit.secret.no_selected");
  }

  if (keys.value.length > 0) {
    return t("core.formkit.secret.includes_keys", {
      keys: keys.value.join(", "),
    });
  }

  return t("core.formkit.secret.no_fields");
});

// Select
const listModalVisible = ref(false);

function onSelect(secretName: string) {
  props.context.node.input(secretName);
  listModalVisible.value = false;
}

function onListModalClose() {
  listModalVisible.value = false;
  refetch();
}

// Create
const creationModalVisible = ref(false);

function onCreated(secretName: string) {
  props.context.node.input(secretName);
  creationModalVisible.value = false;
}

// Edit
const editModalVisible = ref(false);

function onEditModalClose() {
  editModalVisible.value = false;
  refetch();
}

const missingKeys = computed(() => {
  return requiredKeys.value.filter((key) => !keys.value.includes(key.key));
});
</script>
<template>
  <div
    class="flex items-center gap-2 rounded-lg border border-gray-100 px-2.5 py-2"
  >
    <div
      class="inline-flex size-8 flex-none items-center justify-center rounded-full"
      :class="{
        'bg-indigo-50': !!data,
        'bg-gray-100': !data,
        'bg-red-50': !hasPermission,
      }"
    >
      <MingcuteFileSecurityLine
        class="size-5"
        :class="{
          'text-indigo-500': !!data,
          'text-gray-500': !data,
          'text-red-500': !hasPermission,
        }"
      />
    </div>
    <div class="min-w-0 flex-1 shrink space-y-1">
      <div v-if="title" class="line-clamp-1 text-sm font-semibold">
        {{ title }}
      </div>
      <div class="line-clamp-1 text-xs text-gray-500">
        {{ description || "--" }}
      </div>
    </div>
    <div v-if="hasPermission" class="flex items-center gap-1">
      <VButton
        v-if="data"
        :label="$t('core.common.buttons.clear')"
        variant="ghost"
        size="xs"
        @click="context.node.input('')"
      >
        <template #icon><IconClose /></template>
      </VButton>
      <VButton
        v-if="data"
        :label="$t('core.formkit.secret.edit_modal.title')"
        variant="ghost"
        size="xs"
        @click="editModalVisible = true"
      >
        <template #icon><RiEditBoxLine /></template>
      </VButton>
      <VButton
        v-else
        :label="$t('core.formkit.secret.creation_modal.title')"
        variant="ghost"
        size="xs"
        @click="creationModalVisible = true"
      >
        <template #icon><IconAddCircle /></template>
      </VButton>
      <VButton
        :label="$t('core.formkit.secret.list_modal.title')"
        variant="ghost"
        size="xs"
        @click="listModalVisible = true"
      >
        <template #icon><IconExchange /></template>
      </VButton>
    </div>
  </div>

  <div
    v-if="currentValue && missingKeys.length > 0"
    class="my-2 flex items-center gap-1 text-xs text-red-500"
  >
    <IconInformation />
    <span>
      {{
        t("core.formkit.secret.missing_keys", {
          keys: missingKeys.map((key) => key.key).join(", "),
        })
      }}
    </span>
  </div>

  <SecretCreationModal
    v-if="creationModalVisible"
    :description-preset="descriptionPreset"
    :required-keys="requiredKeys"
    @close="creationModalVisible = false"
    @created="onCreated"
  />

  <SecretEditModal
    v-if="editModalVisible && data"
    :secret="data"
    :required-keys="requiredKeys"
    @close="onEditModalClose"
  />

  <SecretListModal
    v-if="listModalVisible"
    :selected-secret-name="currentValue"
    :description-preset="descriptionPreset"
    :required-keys="requiredKeys"
    @select="onSelect"
    @close="onListModalClose"
  />
</template>

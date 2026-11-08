<script lang="ts" setup>
import { consoleApiClient, type User } from "@halo-dev/api-client";
import {
  VAvatar,
  VFilterDropdown,
  type FilterDropdownOption,
  type FilterDropdownValue,
} from "@halo-dev/components";
import { useQuery } from "@tanstack/vue-query";
import { computed, ref, toRefs } from "vue";

const props = withDefaults(
  defineProps<{
    label: string;
    modelValue?: string;
  }>(),
  {
    modelValue: undefined,
  }
);

const { modelValue } = toRefs(props);

const emit = defineEmits<{
  (event: "update:modelValue", value?: string): void;
}>();

const keyword = ref("");

const { data: selectedUser } = useQuery({
  queryKey: ["core:users:by-name", modelValue],
  queryFn: async () => {
    if (!modelValue.value) {
      return null;
    }

    const { data } = await consoleApiClient.user.getUserDetail({
      name: modelValue.value,
    });

    return data.user;
  },
  cacheTime: 0,
});

const { data: users, isFetching } = useQuery({
  queryKey: ["core:users", keyword],
  queryFn: async () => {
    const { data } = await consoleApiClient.user.listUsers({
      fieldSelector: [`name!=anonymousUser`],
      keyword: keyword.value,
      page: 1,
      size: 30,
    });

    return data?.items?.map((user) => user.user) || [];
  },
  staleTime: 2000,
});

const userMap = computed(() => {
  const map = new Map<string, User>();

  users.value?.forEach((user) => {
    map.set(user.metadata.name, user);
  });

  if (selectedUser.value) {
    map.set(selectedUser.value.metadata.name, selectedUser.value);
  }

  return map;
});

const options = computed<FilterDropdownOption[]>(() => {
  const items =
    users.value?.map((user) => ({
      label: user.spec.displayName,
      value: user.metadata.name,
      description: user.metadata.name,
    })) || [];

  if (
    selectedUser.value &&
    !items.some((item) => item.value === selectedUser.value?.metadata.name)
  ) {
    items.unshift({
      label: selectedUser.value.spec.displayName,
      value: selectedUser.value.metadata.name,
      description: selectedUser.value.metadata.name,
    });
  }

  return items;
});

function getUser(option: FilterDropdownOption) {
  if (option.value === undefined) {
    return undefined;
  }

  return userMap.value.get(String(option.value));
}

function handleUpdate(value?: FilterDropdownValue) {
  emit("update:modelValue", typeof value === "string" ? value : undefined);
}
</script>

<template>
  <VFilterDropdown
    :model-value="modelValue"
    :label="label"
    :options="options"
    :selected-label="selectedUser?.spec.displayName"
    searchable
    remote-search
    :loading="isFetching"
    :search-placeholder="$t('core.common.placeholder.search')"
    @update:model-value="handleUpdate"
    @search="keyword = $event"
  >
    <template #option="{ option, selected }">
      <VAvatar
        size="md"
        :alt="option.label"
        :src="getUser(option)?.spec.avatar"
      />
      <div class="flex min-w-0 flex-1 items-baseline gap-2">
        <span
          class="truncate text-sm"
          :class="selected ? 'font-medium text-gray-900' : 'text-gray-700'"
        >
          {{ option.label }}
        </span>
        <span class="truncate text-xs text-gray-500">
          {{ option.description }}
        </span>
      </div>
    </template>
  </VFilterDropdown>
</template>

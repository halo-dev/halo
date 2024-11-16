<script lang="ts" setup>
import { setFocus } from "@/formkit/utils/focus";
import {
  consoleApiClient,
  coreApiClient,
  type User,
} from "@halo-dev/api-client";
import {
  IconArrowDown,
  VAvatar,
  VDropdown,
  VEntity,
  VEntityField,
} from "@halo-dev/components";
import { useQuery } from "@tanstack/vue-query";
import { refDebounced } from "@vueuse/shared";
import { ref, toRefs } from "vue";

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
const debouncedKeyword = refDebounced(keyword, 300);

const { data: selectedUser } = useQuery({
  queryKey: ["core:users:by-name", modelValue],
  queryFn: async () => {
    if (!modelValue.value) {
      return null;
    }

    const { data } = await coreApiClient.user.getUser({
      name: modelValue.value,
    });

    return data;
  },
  cacheTime: 0,
});

const { data: users } = useQuery({
  queryKey: ["core:users", debouncedKeyword],
  queryFn: async () => {
    const { data } = await consoleApiClient.user.listUsers({
      fieldSelector: [`name!=anonymousUser`],
      keyword: debouncedKeyword.value,
      page: 1,
      size: 30,
    });

    const pureUsers = data?.items?.map((user) => user.user);

    if (!pureUsers?.length) {
      return [selectedUser.value].filter(Boolean) as User[];
    }

    if (selectedUser.value) {
      return [
        selectedUser.value,
        ...pureUsers.filter(
          (user) => user.metadata.name !== selectedUser.value?.metadata.name
        ),
      ];
    }

    return pureUsers;
  },
  staleTime: 2000,
});

const dropdown = ref();

const handleSelect = (user: User) => {
  const { name } = user.metadata || {};

  if (name === props.modelValue) {
    emit("update:modelValue", undefined);
  } else {
    emit("update:modelValue", name);
  }

  dropdown.value.hide();
};

function onDropdownShow() {
  setTimeout(() => {
    setFocus("userFilterDropdownInput");
  }, 200);
}
</script>

<template>
  <VDropdown ref="dropdown" :classes="['!p-0']" @show="onDropdownShow">
    <div
      class="flex cursor-pointer select-none items-center text-sm text-gray-700 hover:text-black"
      :class="{ 'font-semibold text-gray-700': modelValue !== undefined }"
    >
      <span v-if="!selectedUser" class="mr-0.5">
        {{ label }}
      </span>
      <span v-else class="mr-0.5">
        {{ label }}：{{ selectedUser.spec.displayName }}
      </span>
      <span>
        <IconArrowDown />
      </span>
    </div>
    <template #popper>
      <div class="h-96 w-80">
        <div class="border-b border-b-gray-100 bg-white p-4">
          <FormKit
            id="userFilterDropdownInput"
            v-model="keyword"
            :placeholder="$t('core.common.placeholder.search')"
            type="text"
          ></FormKit>
        </div>
        <div>
          <ul
            class="box-border h-full w-full divide-y divide-gray-100"
            role="list"
          >
            <li
              v-for="user in users"
              :key="user.metadata.name"
              class="cursor-pointer"
              @click="handleSelect(user)"
            >
              <VEntity :is-selected="modelValue === user.metadata.name">
                <template #start>
                  <VEntityField>
                    <template #description>
                      <VAvatar
                        :key="user.metadata.name"
                        :alt="user.spec.displayName"
                        :src="user.spec.avatar"
                        size="md"
                      ></VAvatar>
                    </template>
                  </VEntityField>
                  <VEntityField
                    :title="user.spec.displayName"
                    :description="user.metadata.name"
                  />
                </template>
              </VEntity>
            </li>
          </ul>
        </div>
      </div>
    </template>
  </VDropdown>
</template>

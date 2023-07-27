<script lang="ts" setup>
import type { User } from "@halo-dev/api-client";
import { useUserFetch } from "@/modules/system/users/composables/use-user";
import {
  IconArrowDown,
  VAvatar,
  VDropdown,
  VEntity,
  VEntityField,
} from "@halo-dev/components";
import { setFocus } from "@/formkit/utils/focus";
import { computed, ref, watch } from "vue";
import Fuse from "fuse.js";

const props = withDefaults(
  defineProps<{
    label: string;
    modelValue?: string;
  }>(),
  {
    modelValue: undefined,
  }
);

const emit = defineEmits<{
  (event: "update:modelValue", value?: string): void;
}>();

const { users } = useUserFetch({ fetchOnMounted: true });

const dropdown = ref();

const handleSelect = (user: User) => {
  if (user.metadata.name === props.modelValue) {
    emit("update:modelValue", undefined);
  } else {
    emit("update:modelValue", user.metadata.name);
  }

  dropdown.value.hide();
};

function onDropdownShow() {
  setTimeout(() => {
    setFocus("userFilterDropdownInput");
  }, 200);
}

// search
const keyword = ref("");

let fuse: Fuse<User> | undefined = undefined;

watch(
  () => users.value,
  () => {
    fuse = new Fuse(users.value, {
      keys: ["spec.displayName", "metadata.name", "spec.email"],
      useExtendedSearch: true,
      threshold: 0.2,
    });
  }
);

const searchResults = computed(() => {
  if (!fuse || !keyword.value) {
    return users.value;
  }

  return fuse?.search(keyword.value).map((item) => item.item);
});

const selectedUser = computed(() => {
  return users.value.find((user) => user.metadata.name === props.modelValue);
});
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
              v-for="(user, index) in searchResults"
              :key="index"
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

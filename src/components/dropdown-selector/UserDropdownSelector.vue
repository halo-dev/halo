<script lang="ts" setup>
import type { User } from "@halo-dev/api-client";
import { useUserFetch } from "@/modules/system/users/composables/use-user";
import { IconCheckboxCircle } from "@halo-dev/components";

const props = withDefaults(
  defineProps<{
    selected?: User;
  }>(),
  {
    selected: undefined,
  }
);

const emit = defineEmits<{
  (event: "update:selected", user?: User): void;
  (event: "select", user?: User): void;
}>();

const { users, handleFetchUsers } = useUserFetch();

const handleSelect = (user: User) => {
  if (props.selected && user.metadata.name === props.selected.metadata.name) {
    emit("update:selected", undefined);
    emit("select", undefined);
    return;
  }

  emit("update:selected", user);
  emit("select", user);
};

function onDropdownShow() {
  handleFetchUsers();
}
</script>

<template>
  <FloatingDropdown @show="onDropdownShow">
    <slot />
    <template #popper>
      <div class="h-96 w-80">
        <div class="bg-white p-4">
          <!--TODO: Auto Focus-->
          <FormKit placeholder="输入关键词搜索" type="text"></FormKit>
        </div>
        <div class="mt-2">
          <ul class="divide-y divide-gray-200" role="list">
            <li
              v-for="(user, index) in users"
              :key="index"
              v-close-popper
              class="cursor-pointer hover:bg-gray-50"
              :class="{
                'bg-gray-100': selected?.metadata.name === user.metadata.name,
              }"
              @click="handleSelect(user)"
            >
              <div class="flex items-center space-x-4 px-4 py-3">
                <div class="flex-shrink-0">
                  <img
                    :alt="user.spec.displayName"
                    :src="user.spec.avatar"
                    class="h-10 w-10 rounded"
                  />
                </div>
                <div class="min-w-0 flex-1">
                  <p class="truncate text-sm font-medium text-gray-900">
                    {{ user.spec.displayName }}
                  </p>
                  <p class="truncate text-sm text-gray-500">
                    @{{ user.metadata.name }}
                  </p>
                </div>
                <div v-if="selected?.metadata.name === user.metadata.name">
                  <IconCheckboxCircle class="text-primary" />
                </div>
              </div>
            </li>
          </ul>
        </div>
      </div>
    </template>
  </FloatingDropdown>
</template>

<script lang="ts" setup>
import type { User } from "@halo-dev/api-client";
import { useUserFetch } from "@/modules/system/users/composables/use-user";
import {
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
  setTimeout(() => {
    setFocus("userDropdownSelectorInput");
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
</script>

<template>
  <VDropdown :classes="['!p-0']" @show="onDropdownShow">
    <slot />
    <template #popper>
      <div class="h-96 w-80">
        <div class="border-b border-b-gray-100 bg-white p-4">
          <FormKit
            id="userDropdownSelectorInput"
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
              v-close-popper
              @click="handleSelect(user)"
            >
              <VEntity
                :is-selected="selected?.metadata.name === user.metadata.name"
              >
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

<script lang="ts" setup>
import { setFocus } from "@/formkit/utils/focus";
import PostTag from "@console/modules/contents/posts/tags/components/PostTag.vue";
import { usePostTag } from "@console/modules/contents/posts/tags/composables/use-post-tag";
import type { Tag } from "@halo-dev/api-client";
import {
  IconArrowDown,
  VDropdown,
  VEntity,
  VEntityContainer,
  VEntityField,
} from "@halo-dev/components";
import Fuse from "fuse.js";
import { computed, ref, watch } from "vue";

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

const { tags } = usePostTag();

const dropdown = ref();

const handleSelect = (tag: Tag) => {
  if (tag.metadata.name === props.modelValue) {
    emit("update:modelValue", undefined);
  } else {
    emit("update:modelValue", tag.metadata.name);
  }

  dropdown.value.hide();
};

function onDropdownShow() {
  setTimeout(() => {
    setFocus("tagFilterDropdownInput");
  }, 200);
}

// search
const keyword = ref("");

let fuse: Fuse<Tag> | undefined = undefined;

watch(
  () => tags.value,
  () => {
    fuse = new Fuse(tags.value || [], {
      keys: ["spec.displayName", "metadata.name", "spec.email"],
      useExtendedSearch: true,
      threshold: 0.2,
    });
  },
  {
    immediate: true,
  }
);

const searchResults = computed(() => {
  if (!fuse || !keyword.value) {
    return tags.value;
  }

  return fuse?.search(keyword.value).map((item) => item.item);
});

const selectedTag = computed(() => {
  return tags.value?.find((tag) => tag.metadata.name === props.modelValue);
});
</script>

<template>
  <VDropdown ref="dropdown" :classes="['!p-0']" @show="onDropdownShow">
    <div
      class="flex cursor-pointer select-none items-center text-sm text-gray-700 hover:text-black"
      :class="{ 'font-semibold text-gray-700': modelValue !== undefined }"
    >
      <span v-if="!selectedTag" class="mr-0.5">
        {{ label }}
      </span>
      <span v-else class="mr-0.5">
        {{ label }}：{{ selectedTag.spec.displayName }}
      </span>
      <span>
        <IconArrowDown />
      </span>
    </div>
    <template #popper>
      <div class="h-96 w-80">
        <div class="border-b border-b-gray-100 bg-white p-4">
          <FormKit
            id="tagFilterDropdownInput"
            v-model="keyword"
            :placeholder="$t('core.common.placeholder.search')"
            type="text"
          ></FormKit>
        </div>
        <div>
          <VEntityContainer>
            <VEntity
              v-for="tag in searchResults"
              :key="tag.metadata.name"
              :is-selected="modelValue === tag.metadata.name"
              @click="handleSelect(tag)"
            >
              <template #start>
                <VEntityField :description="tag.status?.permalink">
                  <template #title>
                    <PostTag :tag="tag" />
                  </template>
                </VEntityField>
              </template>
              <template #end>
                <VEntityField
                  :description="
                    $t('core.common.fields.post_count', {
                      count: tag.status?.postCount || 0,
                    })
                  "
                />
              </template>
            </VEntity>
          </VEntityContainer>
        </div>
      </div>
    </template>
  </VDropdown>
</template>

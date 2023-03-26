<script lang="ts" setup>
import type { Tag } from "@halo-dev/api-client";
import { VEntity, VEntityField, VDropdown } from "@halo-dev/components";
import { setFocus } from "@/formkit/utils/focus";
import { computed, ref, watch } from "vue";
import Fuse from "fuse.js";
import { usePostTag } from "@/modules/contents/posts/tags/composables/use-post-tag";
import PostTag from "@/modules/contents/posts/tags/components/PostTag.vue";

const props = withDefaults(
  defineProps<{
    selected?: Tag;
  }>(),
  {
    selected: undefined,
  }
);

const emit = defineEmits<{
  (event: "update:selected", tag?: Tag): void;
  (event: "select", tag?: Tag): void;
}>();

const { tags } = usePostTag();

const handleSelect = (tag: Tag) => {
  if (props.selected && tag.metadata.name === props.selected.metadata.name) {
    emit("update:selected", undefined);
    emit("select", undefined);
    return;
  }

  emit("update:selected", tag);
  emit("select", tag);
};

function onDropdownShow() {
  setTimeout(() => {
    setFocus("tagDropdownSelectorInput");
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
</script>

<template>
  <VDropdown :classes="['!p-0']" @show="onDropdownShow">
    <slot />
    <template #popper>
      <div class="h-96 w-80">
        <div class="border-b border-b-gray-100 bg-white p-4">
          <FormKit
            id="tagDropdownSelectorInput"
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
              v-for="(tag, index) in searchResults"
              :key="index"
              v-close-popper
              @click="handleSelect(tag)"
            >
              <VEntity
                :is-selected="selected?.metadata.name === tag.metadata.name"
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
            </li>
          </ul>
        </div>
      </div>
    </template>
  </VDropdown>
</template>

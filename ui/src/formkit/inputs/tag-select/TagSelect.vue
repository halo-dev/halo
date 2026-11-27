<script lang="ts" setup>
import PostTag from "@console/modules/contents/posts/tags/components/PostTag.vue";
import { useAllPostTagsQuery } from "@console/modules/contents/posts/tags/composables/use-post-tag";
import type { FormKitFrameworkContext } from "@formkit/core";
import type { Tag } from "@halo-dev/api-client";
import { coreApiClient } from "@halo-dev/api-client";
import {
  IconArrowRight,
  IconCheckboxCircle,
  VPopover,
} from "@halo-dev/components";
import { utils } from "@halo-dev/ui-shared";
import { useEventListener } from "@vueuse/core";
import Fuse from "fuse.js";
import ShortUniqueId from "short-unique-id";
import { slugify } from "transliteration";
import {
  computed,
  nextTick,
  ref,
  useTemplateRef,
  watch,
  type PropType,
} from "vue";
import {
  applyDataPermissionGuard,
  noPermissionMessage,
} from "@/formkit/utils/permission";

const tagPermissions = [
  "system:posts:view",
  "system:posts:manage",
  "uc:posts:manage",
];

const props = defineProps({
  context: {
    type: Object as PropType<FormKitFrameworkContext>,
    required: true,
  },
});

const multiple = computed(() => {
  const { multiple } = props.context;
  if (multiple === undefined) {
    return false;
  }

  if (typeof multiple === "boolean") {
    return multiple;
  }

  return multiple === "true";
});

const selectedTag = ref<Tag>();
const dropdownVisible = ref(false);
const text = ref("");
const inputRef = useTemplateRef<HTMLInputElement>("inputRef");

let blockEscapeKeyup = false;

// Escape closes the panel through the popover itself. The surrounding modal
// also listens for Escape on keyup, so swallow that keyup once the panel has
// handled the key.
useEventListener(
  window,
  "keydown",
  (event: KeyboardEvent) => {
    if (event.key === "Escape" && dropdownVisible.value) {
      blockEscapeKeyup = true;
    }
  },
  { capture: true }
);

useEventListener(
  window,
  "keyup",
  (event: KeyboardEvent) => {
    if (event.key !== "Escape" || !blockEscapeKeyup) {
      return;
    }
    blockEscapeKeyup = false;
    event.stopPropagation();
  },
  { capture: true }
);

// The non-modal popover moves focus to its content when it mounts, so return
// the focus to the search input after the panel has rendered.
watch(dropdownVisible, async (visible) => {
  if (!visible) {
    return;
  }
  await nextTick();
  await nextTick();
  inputRef.value?.focus();
});

const canViewTags = applyDataPermissionGuard(
  props.context.node,
  tagPermissions
);

const { data: postTags, refetch: handleFetchTags } = useAllPostTagsQuery({
  enabled: canViewTags,
});

// search
let fuse: Fuse<Tag> | undefined = undefined;

const searchResults = computed(() => {
  if (!text.value) {
    return postTags.value;
  }

  return fuse?.search(text.value).map((item) => item.item) || [];
});

watch(
  () => searchResults.value,
  (value) => {
    if (value?.length && text.value) {
      selectedTag.value = value[0];
      scrollToSelected();
    } else {
      selectedTag.value = undefined;
    }
  }
);

watch(
  () => postTags.value,
  () => {
    fuse = new Fuse(postTags.value || [], {
      keys: ["spec.displayName", "metadata.name", "spec.email"],
      useExtendedSearch: true,
      threshold: 0.2,
    });
    if (props.context) {
      // eslint-disable-next-line vue/no-mutating-props
      props.context.options =
        postTags.value?.map((tag) => {
          return {
            label: tag.spec.displayName,
            value: tag.metadata.name,
          };
        }) || [];
    }
  },
  {
    immediate: true,
  }
);

const selectedTags = computed(() => {
  if (multiple.value) {
    const selectedTagNames = (props.context._value as string[]) || [];
    return selectedTagNames
      .map((tagName): Tag | undefined => {
        return postTags.value?.find((tag) => tag.metadata.name === tagName);
      })
      .filter(Boolean) as Tag[];
  }

  const tag = postTags.value?.find(
    (tag) => tag.metadata.name === props.context._value
  );

  return [tag].filter(Boolean) as Tag[];
});

const isSelected = (tag: Tag) => {
  if (multiple.value) {
    return (props.context._value || []).includes(tag.metadata.name);
  }

  return props.context._value === tag.metadata.name;
};

const handleSelect = (tag: Tag) => {
  if (multiple.value) {
    const currentValue = props.context._value || [];
    if (currentValue.includes(tag.metadata.name)) {
      props.context.node.input(
        currentValue.filter((t) => t !== tag.metadata.name)
      );
    } else {
      props.context.node.input([...currentValue, tag.metadata.name]);
      text.value = "";
    }
  } else {
    props.context.node.input(
      tag.metadata.name === props.context._value ? "" : tag.metadata.name
    );
  }
  // Selecting with the mouse moves the focus away from the input, so put it
  // back once the tag has been selected.
  nextTick(() => inputRef.value?.focus());
};

const handleKeydown = (e: KeyboardEvent) => {
  if (e.key === "Escape") {
    return;
  }

  if (!searchResults.value) return;

  if (!dropdownVisible.value) {
    if (e.key === "ArrowDown" || e.key === "ArrowUp") {
      e.preventDefault();
      dropdownVisible.value = true;
    }
    return;
  }

  if (e.key === "ArrowDown") {
    e.preventDefault();

    const index = searchResults.value.findIndex(
      (tag) => tag.metadata.name === selectedTag.value?.metadata.name
    );
    if (index < searchResults.value.length - 1) {
      selectedTag.value = searchResults.value[index + 1];
    }

    scrollToSelected();
  }
  if (e.key === "ArrowUp") {
    e.preventDefault();

    const index = searchResults.value.findIndex(
      (tag) => tag.metadata.name === selectedTag.value?.metadata.name
    );
    if (index > 0) {
      selectedTag.value = searchResults.value[index - 1];
    } else {
      selectedTag.value = undefined;
    }

    scrollToSelected();
  }

  if (e.key === "Enter") {
    if (!selectedTag.value && text.value) {
      handleCreateTag();
      return;
    }

    if (selectedTag.value) {
      handleSelect(selectedTag.value);
      text.value = "";

      e.preventDefault();
    }
  }
};

const scrollToSelected = () => {
  // Wait for the options to render before looking them up.
  nextTick(() => {
    const selectedNodeName = selectedTag.value
      ? selectedTag.value?.metadata.name
      : "tag-create";

    const selectedNode = document.getElementById(selectedNodeName);

    if (selectedNode) {
      selectedNode.scrollIntoView({
        behavior: "smooth",
        block: "nearest",
        inline: "start",
      });
    }
  });
};

const uid = new ShortUniqueId();

const handleCreateTag = async () => {
  if (!utils.permission.has(["system:posts:manage"])) {
    return;
  }

  let slug = slugify(text.value, { trim: true });

  // Check if slug is unique, if not, add -1 to the slug
  const { data: tagsWithSameSlug } = await coreApiClient.content.tag.listTag({
    fieldSelector: [`spec.slug=${slug}`],
    page: 1,
    size: 1,
  });

  if (tagsWithSameSlug.total) {
    slug = `${slug}-${uid.randomUUID(8)}`;
  }

  const { data } = await coreApiClient.content.tag.createTag({
    tag: {
      spec: {
        displayName: text.value,
        slug,
        cover: "",
      },
      apiVersion: "content.halo.run/v1alpha1",
      kind: "Tag",
      metadata: {
        name: "",
        generateName: "tag-",
      },
    },
  });

  handleFetchTags();

  handleSelect(data);

  text.value = "";
};

// update value immediately during IME composition
// please see https://vuejs.org//guide/essentials/forms.html#text
const onTextInput = (e: Event) => {
  text.value = (e.target as HTMLInputElement).value;
};

// delete last tag when text input is empty
const handleDelete = () => {
  if (!text.value) {
    if (multiple.value) {
      const selectedTagNames = (props.context._value as string[]) || [];
      props.context.node.input(selectedTagNames.slice(0, -1));
      return;
    }
    props.context.node.input("");
  }
};
</script>

<template>
  <VPopover
    v-model:open="dropdownVisible"
    :disabled="!canViewTags"
    :arrow="false"
    match-trigger-width
    :offset="10"
    content-class="!p-0 overflow-hidden"
  >
    <div :class="context.classes['post-tags-wrapper']" @keydown="handleKeydown">
      <!-- Keep chip interactions from toggling the popover through the trigger. -->
      <div :class="context.classes['post-tags']" @click.stop>
        <div
          v-for="(tag, index) in selectedTags"
          :key="index"
          :class="context.classes['post-tag-wrapper']"
        >
          <PostTag :tag="tag" closable rounded @close="handleSelect(tag)" />
        </div>
        <input
          ref="inputRef"
          :value="text"
          :class="context.classes.input"
          type="text"
          :disabled="!canViewTags"
          :placeholder="canViewTags ? undefined : noPermissionMessage()"
          @input="onTextInput"
          @click.stop="dropdownVisible = true"
          @focus="dropdownVisible = true"
          @keydown.delete="handleDelete"
        />
      </div>

      <div
        :class="[
          context.classes['post-tags-button'],
          { 'cursor-not-allowed opacity-60': !canViewTags },
        ]"
      >
        <IconArrowRight class="rotate-90 text-gray-500 hover:text-gray-700" />
      </div>
    </div>
    <template #content>
      <div :class="context.classes['dropdown-wrapper']">
        <ul class="p-1">
          <HasPermission
            v-if="text.trim()"
            :permissions="['system:posts:manage']"
          >
            <li
              id="tag-create"
              class="group flex cursor-pointer items-center justify-between rounded p-2"
              :class="{
                'bg-gray-100': selectedTag === undefined,
              }"
              @click="handleCreateTag"
            >
              <span class="text-xs text-gray-700 group-hover:text-gray-900">
                {{
                  $t("core.formkit.tag_select.creation_label", { text: text })
                }}
              </span>
            </li>
          </HasPermission>
          <li
            v-for="tag in searchResults"
            :id="tag.metadata.name"
            :key="tag.metadata.name"
            class="group flex cursor-pointer items-center justify-between rounded p-2 hover:bg-gray-100"
            :class="{
              'bg-gray-100': selectedTag?.metadata.name === tag.metadata.name,
            }"
            @click="handleSelect(tag)"
          >
            <div class="inline-flex items-center overflow-hidden">
              <PostTag :tag="tag" />
            </div>
            <IconCheckboxCircle v-if="isSelected(tag)" class="text-primary" />
          </li>
        </ul>
      </div>
    </template>
  </VPopover>
</template>

<script lang="ts" setup>
import {
  IconBookRead,
  IconCalendar,
  IconCharacterRecognition,
  IconLink,
  IconSave,
  IconUserFollow,
  VButton,
  VPageHeader,
  VSpace,
  VTabItem,
  VTabs,
} from "@halo-dev/components";
import PostSettingModal from "./components/PostSettingModal.vue";
import PostPreviewModal from "./components/PostPreviewModal.vue";
import AttachmentSelectorModal from "../attachments/components/AttachmentSelectorModal.vue";
import type { PostRequest } from "@halo-dev/api-client";
import { computed, onMounted, ref, watch } from "vue";
import cloneDeep from "lodash.clonedeep";
import { apiClient } from "@halo-dev/admin-shared";
import { useRouteQuery } from "@vueuse/router";
import { v4 as uuid } from "uuid";
import {
  allExtensions,
  RichTextEditor,
  useEditor,
} from "@halo-dev/richtext-editor";
import ExtensionCharacterCount from "@tiptap/extension-character-count";
import { formatDatetime } from "@/utils/date";
import { useAttachmentSelect } from "../attachments/composables/use-attachment";
import MdiFileImageBox from "~icons/mdi/file-image-box";

const initialFormState: PostRequest = {
  post: {
    spec: {
      title: "",
      slug: "",
      template: "",
      cover: "",
      deleted: false,
      published: false,
      publishTime: undefined,
      pinned: false,
      allowComment: true,
      visible: "PUBLIC",
      version: 1,
      priority: 0,
      excerpt: {
        autoGenerate: true,
        raw: "",
      },
      categories: [],
      tags: [],
      htmlMetas: [],
    },
    apiVersion: "content.halo.run/v1alpha1",
    kind: "Post",
    metadata: {
      name: uuid(),
    },
  },
  content: {
    raw: "",
    content: "",
    rawType: "HTML",
  },
};

const formState = ref<PostRequest>(cloneDeep(initialFormState));
const settingModal = ref(false);
const previewModal = ref(false);
const saving = ref(false);
const extraActiveId = ref("toc");
const attachemntSelectorModal = ref(false);

const isUpdateMode = computed(() => {
  return !!formState.value.post.metadata.creationTimestamp;
});

interface TocNode {
  id: string;
  level: string;
  text: string;
}

const toc = ref<TocNode[]>();
const editor = useEditor({
  content: formState.value.content.raw,
  extensions: [...allExtensions, ExtensionCharacterCount],
  autofocus: "start",
  onUpdate: () => {
    formState.value.content.raw = editor.value?.getHTML() + "";
    handleGenerateTableOfContent();
  },
});

watch(
  () => formState.value.content.raw,
  (newValue) => {
    const isSame = editor.value?.getHTML() === newValue;

    if (isSame) {
      return;
    }

    editor.value?.commands.setContent(newValue as string, false);
  }
);

const { onAttachmentSelect } = useAttachmentSelect(editor);

const handleGenerateTableOfContent = () => {
  if (!editor.value) {
    return;
  }

  const headings: TocNode[] = [];
  const transaction = editor.value.state.tr;

  editor.value.state.doc.descendants((node, pos) => {
    if (node.type.name === "heading") {
      const id = `heading-${headings.length + 1}`;

      if (node.attrs.id !== id) {
        transaction?.setNodeMarkup(pos, undefined, {
          ...node.attrs,
          id,
        });
      }

      headings.push({
        level: node.attrs.level,
        text: node.textContent,
        id,
      });
    }
  });

  transaction.setMeta("addToHistory", false);
  transaction.setMeta("preventUpdate", true);

  editor.value.view.dispatch(transaction);

  toc.value = headings;
};

const handleSave = async () => {
  try {
    saving.value = true;

    // Set rendered content
    formState.value.content.content = formState.value.content.raw;

    // Set default title and slug
    if (!formState.value.post.spec.title) {
      formState.value.post.spec.title = "无标题文章";
    }
    if (!formState.value.post.spec.slug) {
      formState.value.post.spec.slug = uuid();
    }

    if (isUpdateMode.value) {
      const { data } = await apiClient.post.updateDraftPost({
        name: formState.value.post.metadata.name,
        postRequest: formState.value,
      });
      formState.value.post = data;
    } else {
      const { data } = await apiClient.post.draftPost({
        postRequest: formState.value,
      });
      formState.value.post = data;
      name.value = data.metadata.name;
    }

    await handleFetchContent();
  } catch (e) {
    console.error("Failed to save post", e);
  } finally {
    saving.value = false;
  }
};

const handleFetchContent = async () => {
  if (!formState.value.post.spec.headSnapshot) {
    return;
  }
  const { data } = await apiClient.content.obtainSnapshotContent({
    snapshotName: formState.value.post.spec.headSnapshot,
  });

  formState.value.content = data;
};

const onSettingSaved = (post: PostRequest) => {
  // Set route query parameter
  if (!isUpdateMode.value) {
    name.value = post.post.metadata.name;
  }

  formState.value = post;
  settingModal.value = false;
};

// Get post data when the route contains the name parameter
const name = useRouteQuery("name");
onMounted(async () => {
  if (name.value) {
    // fetch post
    const { data: post } =
      await apiClient.extension.post.getcontentHaloRunV1alpha1Post({
        name: name.value as string,
      });
    formState.value.post = post;

    // fetch post content
    await handleFetchContent();
    handleGenerateTableOfContent();
  }
});
</script>

<template>
  <PostSettingModal
    v-model:visible="settingModal"
    :post="formState"
    @saved="onSettingSaved"
  />
  <PostPreviewModal v-model:visible="previewModal" :post="formState.post" />
  <AttachmentSelectorModal
    v-model:visible="attachemntSelectorModal"
    @select="onAttachmentSelect"
  />
  <VPageHeader title="文章">
    <template #icon>
      <IconBookRead class="mr-2 self-center" />
    </template>
    <template #actions>
      <VSpace>
        <VButton size="sm" type="default" @click="previewModal = true">
          预览
        </VButton>
        <VButton :loading="saving" size="sm" type="default" @click="handleSave">
          保存
        </VButton>
        <VButton type="secondary" @click="settingModal = true">
          <template #icon>
            <IconSave class="h-full w-full" />
          </template>
          发布
        </VButton>
      </VSpace>
    </template>
  </VPageHeader>
  <div class="editor border-t" style="height: calc(100vh - 3.5rem)">
    <RichTextEditor
      v-if="editor"
      :editor="editor"
      :addtional-menu-items="[
        {
          type: 'button',
          icon: MdiFileImageBox,
          title: 'SuperScript',
          action: () => (attachemntSelectorModal = true),
          isActive: () => false,
        },
      ]"
    >
      <template #extra>
        <div class="h-full w-72 overflow-y-auto border-l bg-white">
          <VTabs v-model:active-id="extraActiveId" type="outline">
            <VTabItem id="toc" label="大纲">
              <div class="p-1 pt-0">
                <ul class="space-y-1">
                  <li
                    v-for="(item, index) in toc"
                    :key="index"
                    :class="[{ 'bg-gray-100': index === 0 }]"
                    class="cursor-pointer rounded-base px-1.5 py-1 text-sm text-gray-600 hover:bg-gray-100 hover:text-gray-900"
                  >
                    {{ item.text }}
                  </li>
                </ul>
              </div>
            </VTabItem>
            <VTabItem id="information" label="详情">
              <div class="flex flex-col gap-2 p-1 pt-0">
                <div class="grid grid-cols-2 gap-2">
                  <div
                    class="group flex cursor-pointer flex-col gap-y-5 rounded-md bg-gray-100 px-1.5 py-1 transition-all"
                  >
                    <div class="flex items-center justify-between">
                      <div
                        class="text-sm text-gray-500 group-hover:text-gray-900"
                      >
                        字符数
                      </div>
                      <div class="rounded bg-gray-200 p-0.5">
                        <IconCharacterRecognition
                          class="h-4 w-4 text-gray-600 group-hover:text-gray-900"
                        />
                      </div>
                    </div>
                    <div class="text-base font-medium text-gray-900">
                      {{ editor.storage.characterCount.characters() }}
                    </div>
                  </div>
                  <div
                    class="group flex cursor-pointer flex-col gap-y-5 rounded-md bg-gray-100 px-1.5 py-1 transition-all"
                  >
                    <div class="flex items-center justify-between">
                      <div
                        class="text-sm text-gray-500 group-hover:text-gray-900"
                      >
                        词数
                      </div>
                      <div class="rounded bg-gray-200 p-0.5">
                        <IconCharacterRecognition
                          class="h-4 w-4 text-gray-600 group-hover:text-gray-900"
                        />
                      </div>
                    </div>
                    <div class="text-base font-medium text-gray-900">
                      {{ editor.storage.characterCount.words() }}
                    </div>
                  </div>
                </div>

                <div class="grid grid-cols-1 gap-2">
                  <div
                    class="group flex cursor-pointer flex-col gap-y-5 rounded-md bg-gray-100 px-1.5 py-1 transition-all"
                  >
                    <div class="flex items-center justify-between">
                      <div
                        class="text-sm text-gray-500 group-hover:text-gray-900"
                      >
                        创建时间
                      </div>
                      <div class="rounded bg-gray-200 p-0.5">
                        <IconCalendar
                          class="h-4 w-4 text-gray-600 group-hover:text-gray-900"
                        />
                      </div>
                    </div>
                    <div class="text-base font-medium text-gray-900">
                      {{
                        formatDatetime(
                          formState.post.metadata.creationTimestamp
                        ) || "未发布"
                      }}
                    </div>
                  </div>
                </div>
                <div class="grid grid-cols-1 gap-2">
                  <div
                    class="group flex cursor-pointer flex-col gap-y-5 rounded-md bg-gray-100 px-1.5 py-1 transition-all"
                  >
                    <div class="flex items-center justify-between">
                      <div
                        class="text-sm text-gray-500 group-hover:text-gray-900"
                      >
                        创建者
                      </div>
                      <div class="rounded bg-gray-200 p-0.5">
                        <IconUserFollow
                          class="h-4 w-4 text-gray-600 group-hover:text-gray-900"
                        />
                      </div>
                    </div>
                    <div class="text-base font-medium text-gray-900">
                      {{ formState.post.spec.owner }}
                    </div>
                  </div>
                </div>
                <div class="grid grid-cols-1 gap-2">
                  <div
                    class="group flex cursor-pointer flex-col gap-y-5 rounded-md bg-gray-100 px-1.5 py-1 transition-all"
                  >
                    <div class="flex items-center justify-between">
                      <div
                        class="text-sm text-gray-500 group-hover:text-gray-900"
                      >
                        访问链接
                      </div>
                      <div class="rounded bg-gray-200 p-0.5">
                        <IconLink
                          class="h-4 w-4 text-gray-600 group-hover:text-gray-900"
                        />
                      </div>
                    </div>
                    <div class="text-sm text-gray-900 hover:text-blue-600">
                      {{ formState.post.status?.["permalink"] }}
                    </div>
                  </div>
                </div>
              </div>
            </VTabItem>
          </VTabs>
        </div>
      </template>
    </RichTextEditor>
  </div>
</template>

<script lang="ts" setup>
import type { PMNode, Decoration } from "@halo-dev/richtext-editor";
import type { Editor, Node } from "@halo-dev/richtext-editor";
import { computed, ref } from "vue";
import type { AttachmentAttr } from "../../utils/attachment";
import RiVideoAddLine from "~icons/ri/video-add-line";
import { EditorLinkObtain } from "../../components";
import { VButton } from "@halo-dev/components";
import InlineBlockBox from "../../components/InlineBlockBox.vue";

const props = defineProps<{
  editor: Editor;
  node: PMNode;
  decorations: Decoration[];
  selected: boolean;
  extension: Node;
  getPos: () => number;
  updateAttributes: (attributes: Record<string, unknown>) => void;
  deleteNode: () => void;
}>();

const src = computed({
  get: () => {
    return props.node?.attrs.src;
  },
  set: (src: string) => {
    props.updateAttributes({ src: src });
  },
});

const controls = computed(() => {
  return props.node.attrs.controls;
});

const autoplay = computed(() => {
  return props.node.attrs.autoplay;
});

const loop = computed(() => {
  return props.node.attrs.loop;
});

const initialization = computed(() => {
  return !src.value;
});

const editorLinkObtain = ref();

const handleSetExternalLink = (attachment: AttachmentAttr) => {
  props.updateAttributes({
    src: attachment.url,
  });
};

const resetUpload = () => {
  if (props.getPos()) {
    props.updateAttributes({
      width: undefined,
      height: undefined,
      file: undefined,
    });
  }
};

const handleUploadRetry = () => {
  editorLinkObtain.value?.reset();
};

const handleUploadAbort = () => {
  editorLinkObtain.value?.abort();
};

const handleResetInit = () => {
  editorLinkObtain.value?.reset();
  props.updateAttributes({
    src: "",
    file: undefined,
  });
};
</script>

<template>
  <InlineBlockBox>
    <div
      class="relative inline-block h-full max-w-full overflow-hidden rounded-md text-center transition-all"
      :class="{
        'rounded ring-2': selected,
      }"
      :style="{
        width: initialization ? '100%' : node.attrs.width,
      }"
    >
      <div v-if="src" class="group relative">
        <video
          :src="src"
          :controls="controls"
          :autoplay="autoplay"
          :loop="loop"
          playsinline
          preload="metadata"
          class="m-0 rounded-md"
          :style="{
            width: node.attrs.width,
            height: node.attrs.height,
          }"
        ></video>
        <div
          v-if="src"
          class="absolute left-0 top-0 hidden h-1/4 w-full cursor-pointer justify-end bg-gradient-to-b from-gray-300 to-transparent p-2 ease-in-out group-hover:flex"
        >
          <VButton size="sm" type="secondary" @click="handleResetInit">
            {{
              $t(
                "core.components.default_editor.extensions.upload.operations.replace.button"
              )
            }}
          </VButton>
        </div>
      </div>
      <div v-show="!src" class="relative">
        <EditorLinkObtain
          ref="editorLinkObtain"
          :accept="'video/*'"
          :editor="editor"
          :upload-to-attachment-file="extension.options.uploadVideo"
          :uploaded-file="node?.attrs.file"
          @set-external-link="handleSetExternalLink"
          @on-upload-finish="resetUpload"
          @on-upload-abort="resetUpload"
        >
          <template #icon>
            <div
              class="flex h-14 w-14 items-center justify-center rounded-full bg-primary/20"
            >
              <RiVideoAddLine class="text-xl text-primary" />
            </div>
          </template>
          <template #uploading="{ progress }">
            <div class="absolute top-0 h-full w-full bg-black bg-opacity-20">
              <div class="absolute top-[50%] w-full space-y-2 text-white">
                <div class="px-10">
                  <div
                    class="relative h-4 w-full overflow-hidden rounded-full bg-gray-200"
                  >
                    <div
                      class="h-full bg-primary"
                      :style="{
                        width: `${progress || 0}%`,
                      }"
                    ></div>
                    <div
                      class="absolute left-[50%] top-0 -translate-x-[50%] text-xs leading-4 text-white"
                    >
                      {{
                        progress
                          ? `${progress}%`
                          : `${$t(
                              "core.components.default_editor.extensions.upload.loading"
                            )}...`
                      }}
                    </div>
                  </div>
                </div>

                <div
                  class="inline-block cursor-pointer text-sm hover:opacity-70"
                  @click="handleUploadAbort"
                >
                  {{ $t("core.common.buttons.cancel") }}
                </div>
              </div>
            </div>
          </template>
          <template #error>
            <div class="absolute top-0 h-full w-full bg-black bg-opacity-20">
              <div class="absolute top-[50%] w-full space-y-2 text-white">
                <div class="px-10">
                  <div
                    class="relative h-4 w-full overflow-hidden rounded-full bg-gray-200"
                  >
                    <div class="h-full w-full bg-red-600"></div>
                    <div
                      class="absolute left-[50%] top-0 -translate-x-[50%] text-xs leading-4 text-white"
                    >
                      {{
                        $t(
                          "core.components.default_editor.extensions.upload.error"
                        )
                      }}
                    </div>
                  </div>
                </div>
                <div
                  class="inline-block cursor-pointer text-sm hover:opacity-70"
                  @click="handleUploadRetry"
                >
                  {{
                    $t(
                      "core.components.default_editor.extensions.upload.click_retry"
                    )
                  }}
                </div>
              </div>
            </div>
          </template>
        </EditorLinkObtain>
      </div>
    </div>
  </InlineBlockBox>
</template>

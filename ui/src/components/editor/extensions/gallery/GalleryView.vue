<script lang="ts" setup>
import HasPermission from "@/components/permission/HasPermission.vue";
import { VButton, VSpace } from "@halo-dev/components";
import { NodeViewWrapper, type NodeViewProps } from "@halo-dev/richtext-editor";
import { computed } from "vue";
import ProiconsDelete from "~icons/proicons/delete";
import type { GalleryImage } from "./index";
import {
  useAttachmentSelector,
  useUploadGalleryImage,
} from "./useGalleryImages";

const props = defineProps<NodeViewProps>();

const images = computed({
  get: () => {
    return props.node?.attrs.images || [];
  },
  set: (images: GalleryImage[]) => {
    props.updateAttributes({
      images: images,
    });
  },
});

const { openFileDialog } = useUploadGalleryImage(props.editor);

const openAttachmentSelector = useAttachmentSelector(
  props.editor,
  (newImages) => {
    images.value = [...images.value, ...newImages];
  }
);

function handleSetFocus() {
  props.editor.commands.setNodeSelection(props.getPos() || 0);
}

function removeImage(index: number) {
  const newImages = [...images.value];
  newImages.splice(index, 1);
  images.value = newImages;
}

function handleImageLoad(event: Event, index: number) {
  const img = event.target as HTMLImageElement;
  if (img.naturalWidth && img.naturalHeight) {
    const ratio = img.naturalWidth / img.naturalHeight;
    const newImages = [...images.value];
    newImages[index] = {
      src: img.src,
      aspectRatio: ratio,
    };
    images.value = [...newImages];
  }
}

const groupSize = computed(() => {
  return props.node?.attrs.groupSize || props.extension.options?.groupSize || 3;
});

const groups = computed(() => {
  return images.value.reduce(
    (acc: GalleryImage[][], image: GalleryImage, index: number) => {
      const groupIndex = Math.floor(index / groupSize.value);
      acc[groupIndex] = acc[groupIndex] || [];
      acc[groupIndex].push(image);
      return acc;
    },
    []
  );
});
</script>

<template>
  <node-view-wrapper
    as="div"
    class="p-0.5"
    :class="{
      'rounded ring-2': selected,
    }"
    @click="handleSetFocus"
  >
    <div
      v-if="images.length === 0"
      class="relative flex h-full items-center justify-center rounded-md border border-gray-200 bg-gray-50 before:pb-[62.5%]"
    >
      <VSpace>
        <HasPermission :permissions="['uc:attachments:manage']">
          <VButton @click="openFileDialog()">
            {{ $t("core.common.buttons.upload") }}
          </VButton>
        </HasPermission>

        <HasPermission
          :permissions="['system:attachments:view', 'uc:attachments:manage']"
        >
          <VButton @click="openAttachmentSelector">
            {{
              $t(
                "core.components.default_editor.extensions.upload.attachment.title"
              )
            }}
          </VButton>
        </HasPermission>
      </VSpace>
    </div>
    <div v-else class="relative grid gap-2">
      <div
        v-for="(group, groupIndex) in groups"
        :key="groupIndex"
        class="flex flex-row justify-center gap-2"
      >
        <div
          v-for="(image, imgIndex) in group"
          :key="groupIndex * groupSize + imgIndex"
          class="group/image relative"
          :style="{ flex: `${image.aspectRatio} 1 0%` }"
        >
          <img
            :src="image.src"
            :alt="`Gallery image ${groupIndex * groupSize + imgIndex + 1}`"
            class="pointer-events-none block size-full"
            @load="handleImageLoad($event, groupIndex * groupSize + imgIndex)"
          />
          <div
            class="pointer-events-none invisible absolute inset-0 bg-gradient-to-t from-black/0 via-black/5 to-black/30 p-1 opacity-0 transition-all group-hover/image:visible group-hover/image:opacity-100"
          >
            <div class="flex flex-row-reverse">
              <button
                aria-label="Delete"
                class="text-grey-900 group pointer-events-auto relative flex h-8 w-8 cursor-pointer items-center justify-center rounded-md bg-white/90 transition-all hover:bg-white hover:text-black"
                type="button"
                @click.stop="removeImage(groupIndex * groupSize + imgIndex)"
              >
                <ProiconsDelete class="h-4 w-4" />
                <div
                  class="text-2xs dark:bg-grey-900 invisible absolute -top-8 left-1/2 z-50 flex -translate-x-1/2 items-center gap-1 whitespace-nowrap rounded-md bg-black px-4 py-1 font-sans font-medium text-white group-hover:visible"
                >
                  <span>
                    {{
                      $t(
                        "core.components.default_editor.extensions.upload.operations.remove.button"
                      )
                    }}</span
                  >
                </div>
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </node-view-wrapper>
</template>

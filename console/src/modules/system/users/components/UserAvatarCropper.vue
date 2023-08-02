<script setup lang="ts">
import {
  IconZoomInLine,
  IconZoomOutLine,
  IconArrowLeftRightLine,
  IconArrowUpDownLine,
  IconRefreshLine,
  IconRiUpload2Fill,
} from "@halo-dev/components";
import { type Ref } from "vue";
import { ref, watch } from "vue";
import "cropperjs/dist/cropper.css";
import Cropper from "cropperjs";
import { onMounted } from "vue";
import { computed } from "vue";
import type { Component } from "vue";
import { markRaw } from "vue";
import { useI18n } from "vue-i18n";

const { t } = useI18n();

export type ToolbarName =
  | "upload"
  | "zoomIn"
  | "zoomOut"
  | "flipHorizontal"
  | "flipVertical"
  | "reset";

export interface ToolbarItem {
  name: ToolbarName;
  title?: string;
  icon: Component;
  onClick: () => void;
}

const props = withDefaults(
  defineProps<{
    file: File;
    preview?: boolean;
    cropperWidth?: number;
    cropperHeight?: number;
    cropperImageType?: "png" | "jpeg" | "webp" | "jpg";
    toolbars?: boolean | ToolbarName[];
  }>(),
  {
    preview: true,
    cropperWidth: 200,
    cropperHeight: 200,
    cropperImageType: "png",
    toolbars: true,
  }
);

const emit = defineEmits<{
  (event: "changeFile"): void;
}>();

const cropper = ref<Cropper>();
const defaultToolbars: ToolbarItem[] = [
  {
    name: "upload",
    icon: markRaw(IconRiUpload2Fill),
    onClick: () => {
      emit("changeFile");
    },
    title: t("core.user.detail.avatar.tooltips.upload"),
  },
  {
    name: "zoomIn",
    icon: markRaw(IconZoomInLine),
    onClick: () => {
      cropper.value?.zoom(0.1);
    },
    title: t("core.user.detail.avatar.tooltips.zoom_in"),
  },
  {
    name: "zoomOut",
    icon: markRaw(IconZoomOutLine),
    onClick: () => {
      cropper.value?.zoom(-0.1);
    },
    title: t("core.user.detail.avatar.tooltips.zoom_out"),
  },
  {
    name: "flipHorizontal",
    icon: markRaw(IconArrowLeftRightLine),
    onClick: () => {
      cropper.value?.scaleX(-cropper.value?.getData().scaleX || -1);
    },
    title: t("core.user.detail.avatar.tooltips.flip_horizontal"),
  },
  {
    name: "flipVertical",
    icon: markRaw(IconArrowUpDownLine),
    onClick: () => {
      cropper.value?.scaleY(-cropper.value?.getData().scaleY || -1);
    },
    title: t("core.user.detail.avatar.tooltips.flip_vertical"),
  },
  {
    name: "reset",
    icon: markRaw(IconRefreshLine),
    onClick: () => {
      cropper.value?.reset();
    },
    title: t("core.user.detail.avatar.tooltips.reset"),
  },
];
const previewElement = ref<HTMLElement>();
const imageElement = ref<HTMLImageElement>() as Ref<HTMLImageElement>;
const toolbarItems = computed(() => {
  if (props.toolbars === true) {
    return defaultToolbars;
  }
  if (Array.isArray(props.toolbars) && props.toolbars.length > 0) {
    return defaultToolbars.filter((item) =>
      (props.toolbars as string[]).includes(item.name)
    );
  }
  return [];
});

onMounted(() => {
  cropper.value = new Cropper(imageElement.value, {
    aspectRatio: props.cropperHeight / props.cropperWidth,
    viewMode: 1,
    dragMode: "move",
    checkCrossOrigin: false,
    cropBoxResizable: false,
    center: false,
    minCropBoxWidth: props.cropperWidth,
    minCropBoxHeight: props.cropperHeight,
    preview: previewElement.value?.querySelectorAll(".preview"),
  });
});

const imageUrl = ref<string>("");

const renderImages = (file: File) => {
  const reader = new FileReader();
  reader.onload = function () {
    imageUrl.value = reader.result as string;
  };
  reader.readAsDataURL(file);
};

const getCropperFile = (): Promise<File> => {
  return new Promise<File>((resolve, reject) => {
    if (!cropper.value) {
      reject();
      return;
    }
    cropper.value
      .getCroppedCanvas({
        width: props.cropperWidth,
        height: props.cropperHeight,
      })
      .toBlob((blob) => {
        if (blob === null) {
          reject();
          return;
        }
        const fileName = props.file.name.replace(
          /\.[^/.]+$/,
          `.${props.cropperImageType}`
        );
        const file = new File([blob], fileName, {
          type: `image/${props.cropperImageType}`,
        });
        resolve(file);
      });
  });
};

watch(
  () => props.file,
  (file) => {
    if (file) {
      if (file.type.indexOf("image") === -1) {
        return;
      }
      renderImages(file);
    }
  },
  {
    immediate: true,
  }
);

watch(
  () => imageUrl.value,
  (imageUrl) => {
    if (imageUrl) {
      cropper.value?.replace(imageUrl);
    }
  },
  {
    immediate: true,
  }
);

defineExpose({
  getCropperFile,
});
</script>
<template>
  <div class="flex flex-col-reverse sm:flex-row">
    <div
      class="relative max-h-[500px] flex-auto overflow-hidden rounded-md sm:mr-4"
      :style="{ minHeight: `${cropperHeight}px` }"
    >
      <img
        ref="imageElement"
        alt="Uploaded Image"
        class="block h-full w-full object-cover"
      />
      <div class="absolute bottom-3 left-1/2 -translate-x-1/2">
        <ul class="flex rounded-md bg-gray-100 p-0.5 opacity-75">
          <li
            v-for="toolbar in toolbarItems"
            :key="toolbar.name"
            :title="toolbar.title"
          >
            <button
              type="button"
              class="p-2 hover:rounded-md hover:bg-gray-200"
              @click="toolbar.onClick"
            >
              <component :is="toolbar.icon" />
            </button>
          </li>
        </ul>
      </div>
    </div>
    <div
      ref="previewElement"
      class="mb-4 flex justify-around sm:mb-0 sm:inline-block sm:justify-start"
    >
      <div
        class="preview overflow-hidden rounded-md border border-gray-300 shadow-md sm:mb-4"
        :style="{ width: `${cropperWidth}px`, height: `${cropperHeight}px` }"
      ></div>
      <div class="flex flex-col justify-between sm:flex-row">
        <div
          class="preview overflow-hidden rounded-full border border-gray-300"
          :style="{
            width: `${cropperWidth * 0.5}px`,
            height: `${cropperWidth * 0.5}px`,
          }"
        ></div>
        <div
          class="preview overflow-hidden rounded-full border border-gray-300"
          :style="{
            width: `${cropperWidth * 0.3}px`,
            height: `${cropperWidth * 0.3}px`,
          }"
        ></div>
        <div
          class="preview overflow-hidden rounded-full border border-gray-300"
          :style="{
            width: `${cropperWidth * 0.2}px`,
            height: `${cropperWidth * 0.2}px`,
          }"
        ></div>
      </div>
    </div>
  </div>
</template>

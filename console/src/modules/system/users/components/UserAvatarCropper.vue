<script setup lang="ts">
import { type Ref } from "vue";
import { ref, watch } from "vue";
import "cropperjs/dist/cropper.css";
import Cropper from "cropperjs";
import { onMounted } from "vue";

const props = withDefaults(
  defineProps<{
    file: File;
    preview?: boolean;
    cropperWidth?: number;
    cropperHeight?: number;
  }>(),
  {
    preview: true,
    cropperWidth: 200,
    cropperHeight: 200,
  }
);

const isLoading = ref(false);
const cropper = ref<Cropper>();
const previewElement = ref<HTMLElement>();

onMounted(() => {
  cropper.value = new Cropper(imageElement.value, {
    initialAspectRatio: props.cropperHeight / props.cropperWidth,
    viewMode: 1,
    dragMode: "move",
    checkCrossOrigin: false,
    cropBoxResizable: false,
    center: false,
    minCropBoxWidth: props.cropperWidth,
    minCropBoxHeight: props.cropperHeight,
    preview: previewElement.value,
    ready: function () {
      console.log("ready");
    },
  });
});

const imageElement = ref<HTMLImageElement>() as Ref<HTMLImageElement>;
const imageUrl = ref<string>("");

const renderImages = (file: File) => {
  const reader = new FileReader();
  reader.onload = function () {
    imageUrl.value = reader.result as string;
  };
  reader.readAsDataURL(file);
};

const getCropperCanvas = (): HTMLCanvasElement | undefined => {
  if (!cropper.value) {
    return undefined;
  }
  return cropper.value.getCroppedCanvas({
    width: props.cropperWidth,
    height: props.cropperHeight,
  });
};

watch(
  () => props.file,
  (file) => {
    if (file) {
      isLoading.value = true;
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
  getCropperCanvas,
});
</script>
<template>
  <div class="flex">
    <div
      class="mr-4 max-h-[500px] flex-auto"
      :style="{ minHeight: `${cropperHeight}px` }"
    >
      <img
        ref="imageElement"
        alt="Uploaded Image"
        class="block h-full w-full object-cover"
      />
    </div>
    <div class="flex-auto">
      <div
        ref="previewElement"
        class="overflow-hidden"
        :style="{ width: `${cropperWidth}px`, height: `${cropperHeight}px` }"
      ></div>
    </div>
  </div>
</template>

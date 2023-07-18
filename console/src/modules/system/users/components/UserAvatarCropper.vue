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
    cropperImageType?: "png" | "jpeg" | "webp" | "jpg";
  }>(),
  {
    preview: true,
    cropperWidth: 200,
    cropperHeight: 200,
    cropperImageType: "png",
  }
);

const cropper = ref<Cropper>();
const previewElement = ref<HTMLElement>();
const imageElement = ref<HTMLImageElement>() as Ref<HTMLImageElement>;

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

<script setup lang="ts">
import { type Ref } from "vue";
import { ref, watch } from "vue";
import "cropperjs/dist/cropper.css";
import Cropper from "cropperjs";
import { onMounted } from "vue";

const props = defineProps({
  file: {
    type: File,
    required: true,
  },
});

const cropper = ref<Cropper>();

onMounted(() => {
  cropper.value = new Cropper(imageElement.value, {
    initialAspectRatio: 1,
    viewMode: 3,
    dragMode: "move",
    checkCrossOrigin: false,
    cropBoxResizable: false,
    minCropBoxWidth: 200,
    minCropBoxHeight: 200,
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

const cropperContainer = ref();

watch(
  () => props.file,
  (file) => {
    if (file) {
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
</script>
<template>
  <div>
    <img
      ref="imageElement"
      alt="Uploaded Image"
      class="block h-full w-full object-cover"
    />
    <div ref="cropperContainer"></div>
    <button>Crop Image</button>
  </div>
</template>

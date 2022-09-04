<script lang="ts" setup>
import VueFilePond from "vue-filepond";
import "filepond/dist/filepond.min.css";
import type { AxiosRequestConfig, AxiosResponse } from "axios";
import { ref } from "vue";

const props = withDefaults(
  defineProps<{
    allowMultiple?: boolean;
    labelIdle?: string;
    maxFiles?: number | null;
    maxParallelUploads?: number;
    name?: string;
    disabled?: boolean;
    handler: (
      // eslint-disable-next-line
      file: any,
      config: AxiosRequestConfig
      // eslint-disable-next-line
    ) => Promise<AxiosResponse<any, any>>;
  }>(),
  {
    allowMultiple: false,
    labelIdle: "Drop file here",
    maxFiles: null,
    maxParallelUploads: 3,
    name: "file",
    disabled: false,
  }
);

const emit = defineEmits<{
  (event: "uploaded", response: AxiosResponse): void;
}>();

const FilePond = VueFilePond();

const FilePondRef = ref();

const server = {
  process: async (fieldName, file, metadata, load, error, progress, abort) => {
    try {
      const response = await props.handler(file, {
        onUploadProgress: (progressEvent) => {
          if (progressEvent.total > 0) {
            progress(
              progressEvent.lengthComputable,
              progressEvent.loaded,
              progressEvent.total
            );
          }
        },
      });

      emit("uploaded", response);
      load(response);
    } catch (e) {
      error(e);
    }
    return {
      abort: () => {
        abort();
      },
    };
  },
};

const handleRemoveFiles = () => {
  FilePondRef.value.removeFiles();
};

defineExpose({
  handleRemoveFiles,
});
</script>
<template>
  <FilePond
    ref="FilePondRef"
    :allow-multiple="allowMultiple"
    :allow-revert="false"
    :label-idle="labelIdle"
    :max-files="maxFiles"
    :max-parallel-uploads="maxParallelUploads"
    :name="name"
    :server="server"
    :disabled="disabled"
  ></FilePond>
</template>

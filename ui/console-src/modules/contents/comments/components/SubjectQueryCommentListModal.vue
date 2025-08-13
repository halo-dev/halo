<script lang="ts" setup>
import { VButton, VModal } from "@halo-dev/components";
import { useTemplateRef } from "vue";
import SubjectQueryCommentList from "./SubjectQueryCommentList.vue";

const props = defineProps<{
  subjectRefKey: string;
}>();

const emit = defineEmits<{
  (event: "close"): void;
}>();

const modal = useTemplateRef<InstanceType<typeof VModal> | null>("modal");
</script>
<template>
  <VModal
    ref="modal"
    :centered="false"
    :width="1400"
    :title="$t('core.comment.title')"
    :layer-closable="true"
    mount-to-body
    @close="emit('close')"
  >
    <SubjectQueryCommentList :subject-ref-key="props.subjectRefKey" />
    <template #footer>
      <VButton @click="modal?.close()">
        {{ $t("core.common.buttons.close") }}
      </VButton>
    </template>
  </VModal>
</template>

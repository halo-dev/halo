<script lang="ts" setup>
import { computed, nextTick, ref, watch } from "vue";
import { IconClose } from "../../icons/icons";

const props = withDefaults(
  defineProps<{
    visible?: boolean;
    title?: string;
    width?: number;
    fullscreen?: boolean;
    bodyClass?: string[];
  }>(),
  {
    visible: false,
    title: undefined,
    width: 500,
    fullscreen: false,
    bodyClass: undefined,
  }
);

const emit = defineEmits<{
  (event: "update:visible", value: boolean): void;
  (event: "close"): void;
}>();

const rootVisible = ref(false);
const modelWrapper = ref<HTMLElement>();

const wrapperClasses = computed(() => {
  return {
    "modal-wrapper-fullscreen": props.fullscreen,
  };
});

const contentStyles = computed(() => {
  return {
    maxWidth: props.width + "px",
  };
});

function handleClose() {
  emit("update:visible", false);
  emit("close");
}

watch(
  () => props.visible,
  () => {
    if (props.visible) {
      nextTick(() => {
        modelWrapper.value?.focus();
      });
    }
  }
);
</script>
<template>
  <Teleport :disabled="true" to="body">
    <div
      v-show="rootVisible"
      ref="modelWrapper"
      :class="wrapperClasses"
      aria-modal="true"
      class="modal-wrapper"
      role="dialog"
      tabindex="0"
      @keyup.esc="handleClose()"
    >
      <transition
        enter-active-class="ease-out duration-200"
        enter-from-class="opacity-0"
        enter-to-class="opacity-100"
        leave-active-class="ease-in duration-100"
        leave-from-class="opacity-100"
        leave-to-class="opacity-0"
        @before-enter="rootVisible = true"
        @after-leave="rootVisible = false"
      >
        <div v-show="visible" class="modal-layer" @click="handleClose()" />
      </transition>
      <transition
        enter-active-class="ease-out duration-200"
        enter-from-class="opacity-0 translate-y-4 sm:translate-y-0 sm:scale-95"
        enter-to-class="opacity-100 translate-y-0 sm:scale-100"
        leave-active-class="ease-in duration-100"
        leave-from-class="opacity-100 translate-y-0 sm:scale-100"
        leave-to-class="opacity-0 translate-y-4 sm:translate-y-0 sm:scale-95"
      >
        <div
          v-show="visible"
          :style="contentStyles"
          class="modal-content transform transition-all"
        >
          <div v-if="$slots.header || title" class="modal-header">
            <slot name="header">
              <div class="modal-header-title">{{ title }}</div>
              <div class="modal-header-actions flex flex-row">
                <slot name="actions"></slot>
                <div class="modal-header-action" @click="handleClose()">
                  <IconClose />
                </div>
              </div>
            </slot>
          </div>
          <div :class="bodyClass" class="modal-body">
            <slot />
          </div>
          <div v-if="$slots.footer" class="modal-footer">
            <slot name="footer" />
          </div>
        </div>
      </transition>
    </div>
  </Teleport>
</template>

<style lang="scss">
.modal-wrapper {
  @apply fixed
  top-0
  left-0
  h-full
  w-full
  flex
  flex-row
  items-center
  justify-center;
  z-index: 999;

  .modal-layer {
    @apply flex-none
    absolute
    top-0
    left-0
    h-full
    w-full
    transition-opacity
    bg-gray-500
    bg-opacity-75;
  }

  .modal-content {
    @apply flex
    flex-col
    relative
    bg-white
    items-stretch
    shadow-xl
    rounded-base;
    width: calc(100vw - 20px);
    max-height: calc(100vh - 20px);

    .modal-header {
      @apply flex
      justify-between
      border-b;

      .modal-header-title {
        @apply self-center
        text-base
        font-bold;
        padding: 12px 16px;
      }

      .modal-header-actions {
        @apply self-center
        h-full;
        .modal-header-action {
          @apply cursor-pointer;
          padding: 12px 16px;

          &:hover {
            @apply bg-gray-100;
          }
        }
      }
    }

    .modal-body {
      @apply overflow-y-auto
      overflow-x-hidden
      flex-1;
      word-wrap: break-word;
      padding: 12px 16px;
    }

    .modal-footer {
      @apply border-t;
      padding: 12px 16px;
    }
  }

  &.modal-wrapper-fullscreen {
    .modal-content {
      width: 100vw !important;
      max-width: 100vw !important;
      height: 100vh !important;
      max-height: 100vh !important;
      border-radius: 0;
    }
  }
}
</style>

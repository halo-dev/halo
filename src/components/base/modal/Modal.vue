<script lang="ts" setup>
import { VButton } from "../button";
import { computed } from "vue";
import { IconClose } from "@/core/icons";

const props = defineProps({
  visible: {
    type: Boolean,
    default: false,
  },
  title: {
    type: String,
  },
  width: {
    type: Number,
    default: 500,
  },
  fullscreen: {
    type: Boolean,
    default: false,
  },
});

const emit = defineEmits(["update:visible", "close"]);

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
</script>
<template>
  <Teleport to="body">
    <transition
      enter-active-class="ease-out"
      enter-from-class="opacity-0"
      enter-to-class="opacity-100"
      leave-active-class="ease-in"
      leave-from-class="opacity-100"
      leave-to-class="opacity-0"
    >
      <div
        v-show="visible"
        :class="wrapperClasses"
        aria-modal="true"
        class="modal-wrapper transform transition-all duration-200"
        role="dialog"
        tabindex="0"
        @keyup.esc="handleClose()"
      >
        <div class="modal-layer" @click="handleClose()" />
        <div :style="contentStyles" class="modal-content">
          <div class="modal-header">
            <div class="modal-header-title">{{ title }}</div>
            <div class="modal-header-actions">
              <div class="modal-header-action" @click="handleClose()">
                <IconClose />
              </div>
            </div>
          </div>
          <div class="modal-body">
            <slot />
          </div>
          <div class="modal-footer">
            <slot name="footer">
              <VButton @click="handleClose">关闭</VButton>
            </slot>
          </div>
        </div>
      </div>
    </transition>
  </Teleport>
</template>

<style lang="scss">
.modal-wrapper {
  @apply fixed;
  @apply top-0 left-0;
  @apply w-full h-full;
  @apply flex flex-row;
  @apply items-center justify-center;
  z-index: 99999;

  .modal-layer {
    @apply flex-none;
    @apply absolute;
    @apply top-0 left-0;
    @apply w-full h-full;
    background: #9e9eaa;
    opacity: 0.6;
  }

  .modal-content {
    @apply flex;
    @apply flex-col;
    @apply relative;
    @apply bg-white;
    @apply items-stretch;
    @apply shadow-xl;
    width: calc(100vw - 20px);
    max-height: calc(100vh - 20px);
    border-radius: 4px;

    .modal-header {
      @apply flex;
      @apply justify-between;
      @apply border-b;

      .modal-header-title {
        @apply self-center;
        @apply text-base;
        @apply font-bold;
        padding: 12px 16px;
      }

      .modal-header-actions {
        @apply self-center;
        @apply h-full;
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
      @apply overflow-x-hidden overflow-y-auto;
      @apply flex-1;
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

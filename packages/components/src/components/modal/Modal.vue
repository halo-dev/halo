<script lang="ts" setup>
import type { PropType } from "vue";
import { computed, ref } from "vue";
import { IconClose } from "../../icons/icons";

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
  bodyClass: {
    type: Object as PropType<string[]>,
  },
});

const emit = defineEmits(["update:visible", "close"]);

const rootVisible = ref(false);

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
  <Teleport to="body" :disabled="true">
    <div
      v-show="rootVisible"
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
  @apply fixed;
  @apply top-0 left-0;
  @apply h-full w-full;
  @apply flex flex-row;
  @apply items-center justify-center;
  z-index: 999;

  .modal-layer {
    @apply flex-none;
    @apply absolute;
    @apply top-0 left-0;
    @apply h-full w-full;
    @apply transition-opacity;
    @apply bg-gray-500;
    @apply bg-opacity-75;
  }

  .modal-content {
    @apply flex;
    @apply flex-col;
    @apply relative;
    @apply bg-white;
    @apply items-stretch;
    @apply shadow-xl;
    @apply rounded-base;
    width: calc(100vw - 20px);
    max-height: calc(100vh - 20px);

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
      @apply overflow-y-auto overflow-x-hidden;
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

<script lang="ts" setup>
import { computed, nextTick, reactive, ref, watch } from "vue";
import { IconClose } from "../../icons/icons";
import type { UseOverlayScrollbarsParams } from "overlayscrollbars-vue";
import { useOverlayScrollbars } from "overlayscrollbars-vue";

const props = withDefaults(
  defineProps<{
    visible?: boolean;
    title?: string;
    width?: number;
    height?: string;
    fullscreen?: boolean;
    bodyClass?: string[];
    mountToBody?: boolean;
    centered?: boolean;
    layerClosable?: boolean;
  }>(),
  {
    visible: false,
    title: undefined,
    width: 500,
    height: undefined,
    fullscreen: false,
    bodyClass: undefined,
    mountToBody: false,
    centered: true,
    layerClosable: false,
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
    "modal-wrapper-centered": props.centered,
  };
});

const contentStyles = computed(() => {
  return {
    maxWidth: props.width + "px",
    height: props.height,
  };
});

function handleClose() {
  emit("update:visible", false);
  emit("close");
}

const focus = ref(false);

function handleClickLayer() {
  if (props.layerClosable) {
    handleClose();
    return;
  }
  focus.value = true;
  setTimeout(() => {
    focus.value = false;
  }, 300);
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

// body scroll
const modalBody = ref(null);
const reactiveParams = reactive<UseOverlayScrollbarsParams>({
  options: {
    scrollbars: {
      autoHide: "scroll",
      autoHideDelay: 600,
    },
  },
  defer: true,
});
const [initialize, instance] = useOverlayScrollbars(reactiveParams);
watch(
  () => props.visible,
  (value) => {
    if (value) {
      if (modalBody.value) initialize({ target: modalBody.value });
    } else {
      instance()?.destroy();
    }
  }
);
</script>
<template>
  <Teleport :disabled="!mountToBody" to="body">
    <div
      v-show="rootVisible"
      ref="modelWrapper"
      :class="wrapperClasses"
      aria-modal="true"
      class="modal-wrapper"
      role="dialog"
      tabindex="0"
      @keyup.esc.stop="handleClose()"
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
        <div
          v-show="visible"
          class="modal-layer"
          @click.stop="handleClickLayer()"
        />
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
          class="modal-content transform transition-all duration-300"
          :class="{ 'modal-focus': focus }"
        >
          <div v-if="$slots.header || title" class="modal-header group">
            <slot name="header">
              <div class="modal-header-title">{{ title }}</div>
              <div v-if="$slots.center" class="modal-header-center">
                <slot name="center"></slot>
              </div>
              <div class="modal-header-actions">
                <slot name="actions"></slot>
                <span class="bg-gray-50" @click="handleClose()">
                  <IconClose />
                </span>
              </div>
            </slot>
          </div>
          <div ref="modalBody" :class="bodyClass" class="modal-body">
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
  left-0
  h-full
  w-full
  flex
  flex-row
  items-start
  justify-center
  top-0
  py-10;
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
    max-height: calc(100vh - 5rem);

    &.modal-focus {
      @apply scale-[1.02];
    }

    .modal-header {
      @apply flex
      justify-between
      border-b
      items-center
      select-none;
      padding: 10px 16px;

      .modal-header-title {
        @apply text-base
        font-medium;
      }

      .modal-header-actions {
        @apply flex
        flex-row
        gap-2;
        span {
          @apply cursor-pointer 
          rounded-full 
          w-7 
          h-7 
          inline-flex 
          items-center 
          justify-center 
          hover:bg-gray-100
          select-none
          text-gray-600
          hover:text-gray-900
          group-hover:hidden;
        }
      }
    }

    .modal-body {
      @apply overflow-y-hidden
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

  &.modal-wrapper-centered {
    @apply py-0 items-center;
    .modal-content {
      max-height: calc(100vh - 20px) !important;
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

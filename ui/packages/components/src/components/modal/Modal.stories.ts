import type { Meta, StoryObj } from "@storybook/vue3-vite";
import { shallowRef } from "vue";
import { IconArrowLeft, IconArrowRight } from "@/icons/icons";
import { VModal } from ".";
import { VButton } from "../button";
import { VSpace } from "../space";

const meta: Meta<typeof VModal> = {
  title: "Components/Modal",
  component: VModal,
  tags: ["autodocs"],
  render: (args) => ({
    components: { IconArrowLeft, IconArrowRight, VButton, VModal, VSpace },
    setup() {
      const visible = shallowRef(false);
      return { args, visible };
    },
    template: `
      <VButton type="primary" @click="visible = true">打开预览</VButton>
      <VModal
        v-model:visible="visible"
        :title="args.title"
        :width="args.width"
        :height="args.height"
        :fullscreen="args.fullscreen"
        :centered="args.centered"
        :layer-closable="args.layerClosable"
      >
        <template #actions>
          <button class="inline-flex h-7 w-7 items-center justify-center rounded-full text-gray-600 hover:bg-gray-100">
            <IconArrowLeft />
          </button>
          <button class="inline-flex h-7 w-7 items-center justify-center rounded-full text-gray-600 hover:bg-gray-100">
            <IconArrowRight />
          </button>
        </template>

        <article class="space-y-4">
          <div>
            <div class="text-xs font-medium uppercase text-gray-400">文章预览</div>
            <h3 class="mt-2 text-lg font-semibold text-gray-900">Halo 主题开发工作流</h3>
            <p class="mt-2 text-sm leading-6 text-gray-600">
              这是一段用于预览弹窗排版的正文内容，展示标题、正文和操作区在中等宽度下的布局。
            </p>
          </div>
          <div class="rounded bg-gray-50 p-4 text-sm text-gray-600">
            当前预览不会改变文章状态，关闭弹窗后可以继续编辑草稿。
          </div>
        </article>

        <template #footer>
          <VSpace spacing="sm">
            <VButton type="primary" @click="visible = false">确认发布</VButton>
            <VButton @click="visible = false">关闭</VButton>
          </VSpace>
        </template>
      </VModal>
    `,
  }),
  argTypes: {
    centered: {
      control: { type: "boolean" },
    },
    fullscreen: {
      control: { type: "boolean" },
    },
    layerClosable: {
      control: { type: "boolean" },
    },
  },
  args: {
    title: "预览文章",
    width: 640,
    centered: true,
    fullscreen: false,
    layerClosable: true,
  },
};

export default meta;
type Story = StoryObj<typeof VModal>;

export const Default: Story = {};

export const Fullscreen: Story = {
  args: {
    title: "全屏编辑",
    fullscreen: true,
    centered: false,
  },
};

import type { Meta, StoryObj } from "@storybook/vue3-vite";
import { shallowRef } from "vue";
import { VDrawer } from ".";
import { VButton } from "../button";
import { VSpace } from "../space";

const meta: Meta<typeof VDrawer> = {
  title: "Components/Drawer",
  component: VDrawer,
  tags: ["autodocs"],
  render: (args) => ({
    components: { VButton, VDrawer, VSpace },
    setup() {
      const visible = shallowRef(false);
      return { args, visible };
    },
    template: `
      <VButton type="primary" @click="visible = true">打开抽屉</VButton>
      <VDrawer v-model:visible="visible" v-bind="args">
        <div class="space-y-4">
          <div>
            <div class="text-xs font-medium uppercase text-gray-400">文章设置</div>
            <h3 class="mt-2 text-lg font-semibold text-gray-900">Halo 主题开发工作流</h3>
            <p class="mt-2 text-sm leading-6 text-gray-600">
              抽屉适合承载次级任务或上下文操作，不会打断当前的阅读和编辑流程。
            </p>
          </div>
          <div class="rounded bg-gray-50 p-4 text-sm text-gray-600">
            固定头部和底部操作区，正文区域独立滚动，内容再长也不会撑破抽屉。
          </div>
          <div v-for="index in 12" :key="index" class="text-sm text-gray-600">
            第 {{ index }} 条内容，用于演示长列表滚动。
          </div>
        </div>

        <template #footer>
          <VSpace spacing="sm">
            <VButton type="primary" @click="visible = false">保存</VButton>
            <VButton @click="visible = false">取消</VButton>
          </VSpace>
        </template>
      </VDrawer>
    `,
  }),
  argTypes: {
    placement: {
      control: { type: "select" },
      options: ["left", "right", "top", "bottom"],
    },
    size: {
      control: { type: "text" },
    },
    modal: {
      control: { type: "boolean" },
    },
    inset: {
      control: { type: "boolean" },
    },
    layerClosable: {
      control: { type: "boolean" },
    },
    closeOnEsc: {
      control: { type: "boolean" },
    },
    showClose: {
      control: { type: "boolean" },
    },
    showHandle: {
      control: { type: "boolean" },
    },
    swipeToDismiss: {
      control: { type: "boolean" },
    },
    handleOnly: {
      control: { type: "boolean" },
    },
  },
  args: {
    title: "文章设置",
    description: "调整发布选项和展示方式",
    placement: "right",
    size: 480,
    modal: true,
    inset: false,
    layerClosable: true,
    closeOnEsc: true,
    showClose: true,
    showHandle: false,
    swipeToDismiss: true,
    handleOnly: false,
    mountToBody: true,
    unmountOnHide: true,
  },
};

export default meta;
type Story = StoryObj<typeof VDrawer>;

export const Default: Story = {};

export const Inset: Story = {
  args: {
    inset: true,
    size: 420,
  },
};

export const Left: Story = {
  args: {
    placement: "left",
    size: 360,
  },
};

export const Top: Story = {
  args: {
    placement: "top",
    size: "40vh",
  },
};

export const Bottom: Story = {
  args: {
    placement: "bottom",
    size: "50vh",
    showHandle: true,
  },
};

export const SnapPoints: Story = {
  args: {
    placement: "bottom",
    showHandle: true,
    snapPoints: [0.4, 0.7, 1],
  },
  render: (args) => ({
    components: { VButton, VDrawer, VSpace },
    setup() {
      const visible = shallowRef(false);
      const snapPoint = shallowRef<number>(0.4);
      return { args, snapPoint, visible };
    },
    template: `
      <VButton type="primary" @click="visible = true">打开分档抽屉</VButton>
      <VDrawer v-model:visible="visible" v-model:snap-point="snapPoint" v-bind="args">
        <div class="space-y-4">
          <p class="text-sm text-gray-600">
            向下拖动或点击遮罩可关闭，向上拖动可在 40%、70% 和全屏之间切换。
          </p>
          <p class="text-sm text-gray-600">当前档位：{{ snapPoint }}</p>
          <div v-for="index in 20" :key="index" class="text-sm text-gray-600">
            第 {{ index }} 条内容
          </div>
        </div>
        <template #footer>
          <VSpace spacing="sm">
            <VButton type="primary" @click="visible = false">完成</VButton>
            <VButton @click="visible = false">取消</VButton>
          </VSpace>
        </template>
      </VDrawer>
    `,
  }),
};

export const NonModal: Story = {
  args: {
    modal: false,
    size: 360,
  },
};

export const CustomHeader: Story = {
  args: {
    title: "自定义头部",
    size: 420,
  },
  render: (args) => ({
    components: { VButton, VDrawer, VSpace },
    setup() {
      const visible = shallowRef(false);
      return { args, visible };
    },
    template: `
      <VButton type="primary" @click="visible = true">打开抽屉</VButton>
      <VDrawer v-model:visible="visible" v-bind="args">
        <template #header>
          <div class="flex flex-1 items-center justify-between">
            <div>
              <div class="text-sm font-medium text-gray-900">自定义头部</div>
              <div class="text-xs text-gray-500">通过 header 插槽完全替换头部内容</div>
            </div>
            <VButton size="sm" @click="visible = false">关闭</VButton>
          </div>
        </template>
        <div class="text-sm text-gray-600">
          头部插槽会替换标题、描述、操作区和关闭按钮。
        </div>
      </VDrawer>
    `,
  }),
};

export const ConfirmBeforeClose: Story = {
  args: {
    size: 420,
  },
  render: (args) => ({
    components: { VButton, VDrawer, VSpace },
    setup() {
      const visible = shallowRef(false);
      const beforeClose = () =>
        window.confirm("表单尚未保存，确定关闭抽屉吗？");
      return { args, beforeClose, visible };
    },
    template: `
      <VButton type="primary" @click="visible = true">打开抽屉</VButton>
      <VDrawer
        v-model:visible="visible"
        :before-close="beforeClose"
        v-bind="args"
      >
        <div class="text-sm text-gray-600">
          点击关闭按钮、遮罩或按 Esc 时都会先执行 beforeClose。
        </div>
        <template #footer>
          <VButton type="primary" @click="visible = false">保存并关闭</VButton>
        </template>
      </VDrawer>
    `,
  }),
};

export const Persistent: Story = {
  args: {
    unmountOnHide: false,
    size: 400,
  },
};

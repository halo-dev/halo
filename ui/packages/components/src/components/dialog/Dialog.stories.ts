import type { Meta, StoryObj } from "@storybook/vue3-vite";
import { shallowRef } from "vue";
import { Dialog, VDialog } from ".";
import { VButton } from "../button";
import { VSpace } from "../space";
import type { DialogType } from "./types";

const meta: Meta<typeof VDialog> = {
  title: "Components/Dialog",
  component: VDialog,
  tags: ["autodocs"],
  render: (args) => ({
    components: { VButton, VDialog, VSpace },
    setup() {
      const visible = shallowRef(false);
      return { args, visible };
    },
    template: `
      <VSpace>
        <VButton type="primary" @click="visible = true">打开确认框</VButton>
      </VSpace>
      <VDialog
        v-model:visible="visible"
        :type="args.type"
        :size="args.size"
        :title="args.title"
        :description="args.description"
        :confirm-type="args.confirmType"
        :confirm-text="args.confirmText"
        :cancel-text="args.cancelText"
        :show-cancel="args.showCancel"
        :show-close="args.showClose"
        :close-on-esc="args.closeOnEsc"
        :layer-closable="args.layerClosable"
      />
    `,
  }),
  argTypes: {
    type: {
      control: { type: "select" },
      options: ["success", "info", "warning", "error"],
    },
    size: {
      control: { type: "select" },
      options: ["sm", "md", "lg"],
    },
    confirmType: {
      control: { type: "select" },
      options: ["default", "primary", "secondary", "danger"],
    },
    showCancel: {
      control: { type: "boolean" },
    },
    showClose: {
      control: { type: "boolean" },
    },
    closeOnEsc: {
      control: { type: "boolean" },
    },
    layerClosable: {
      control: { type: "boolean" },
    },
  },
  args: {
    type: "warning",
    size: "md",
    title: "确认发布当前文章？",
    description: "发布后读者将可以在前台访问该内容，你仍然可以随时撤回或更新。",
    confirmType: "primary",
    confirmText: "确认发布",
    cancelText: "继续编辑",
    showCancel: true,
    showClose: true,
    closeOnEsc: true,
    layerClosable: false,
  },
};

export default meta;
type Story = StoryObj<typeof VDialog>;

export const Default: Story = {};

export const DangerConfirm: Story = {
  args: {
    type: "error",
    title: "确认删除这个主题？",
    description:
      "删除后主题文件将从当前工作目录移除，正在使用的主题不能被删除。",
    confirmType: "danger",
    confirmText: "删除主题",
    cancelText: "取消",
  },
};

export const Success: Story = {
  args: {
    type: "success",
    title: "保存成功",
    description: "配置已经生效，无需刷新页面。",
    showCancel: false,
  },
};

export const AsyncConfirm: Story = {
  render: (args) => ({
    components: { VButton, VDialog, VSpace },
    setup() {
      const visible = shallowRef(false);

      const onConfirm = () =>
        new Promise<void>((resolve) => {
          setTimeout(resolve, 1500);
        });

      return { args, visible, onConfirm };
    },
    template: `
      <VSpace>
        <VButton type="primary" @click="visible = true">打开异步确认框</VButton>
      </VSpace>
      <VDialog
        v-model:visible="visible"
        :title="args.title"
        :description="args.description"
        :on-confirm="onConfirm"
        confirm-text="开始保存"
      />
    `,
  }),
  args: {
    title: "保存并发布",
    description: "确认按钮会展示加载状态，成功后自动关闭。",
  },
};

export const ManagerApi: Story = {
  render: (args) => ({
    components: { VButton, VSpace },
    setup() {
      const showDialog = (type: DialogType) => {
        Dialog[type]({
          title: args.title,
          description: args.description,
          confirmText: args.confirmText,
          cancelText: args.cancelText,
          confirmType: args.confirmType,
          showCancel: args.showCancel,
        });
      };

      return { showDialog };
    },
    template: `
      <VSpace spacing="sm">
        <VButton @click="showDialog('info')">提示</VButton>
        <VButton type="primary" @click="showDialog('success')">成功</VButton>
        <VButton type="secondary" @click="showDialog('warning')">警告</VButton>
        <VButton type="danger" @click="showDialog('error')">错误</VButton>
      </VSpace>
    `,
  }),
};

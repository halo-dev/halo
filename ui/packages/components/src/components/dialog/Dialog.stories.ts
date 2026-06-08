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
        :title="args.title"
        :description="args.description"
        :confirm-type="args.confirmType"
        :confirm-text="args.confirmText"
        :cancel-text="args.cancelText"
        :show-cancel="args.showCancel"
      />
    `,
  }),
  argTypes: {
    type: {
      control: { type: "select" },
      options: ["success", "info", "warning", "error"],
    },
    confirmType: {
      control: { type: "select" },
      options: ["default", "primary", "secondary", "danger"],
    },
    showCancel: {
      control: { type: "boolean" },
    },
  },
  args: {
    type: "warning",
    title: "确认发布当前文章？",
    description: "发布后读者将可以在前台访问该内容，你仍然可以随时撤回或更新。",
    confirmType: "primary",
    confirmText: "确认发布",
    cancelText: "继续编辑",
    showCancel: true,
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

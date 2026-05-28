import type { Meta, StoryObj } from "@storybook/vue3-vite";
import { VButton } from "../button";
import { VSpace } from "../space";
import { Toast } from "./toast-manager";
import ToastComponent from "./Toast.vue";
import type { ToastType } from "./types";

const meta: Meta<typeof ToastComponent> = {
  title: "Components/Toast",
  component: ToastComponent,
  tags: ["autodocs"],
  render: (args) => ({
    components: { ToastComponent },
    setup() {
      return { args };
    },
    template: `
      <div class="toast-container !static !w-auto !items-start !p-0">
        <ToastComponent
          :type="args.type"
          :content="args.content"
          :duration="-1"
          :closable="args.closable"
          :count="args.count"
        />
      </div>
    `,
  }),
  argTypes: {
    type: {
      control: { type: "select" },
      options: ["success", "info", "warning", "error"],
    },
    closable: {
      control: { type: "boolean" },
    },
  },
  args: {
    type: "success",
    content: "文章已发布，前台页面将在缓存刷新后更新。",
    closable: true,
    count: 0,
  },
};

export default meta;
type Story = StoryObj<typeof ToastComponent>;

export const Default: Story = {};

export const Types: Story = {
  render: () => ({
    components: { ToastComponent },
    setup() {
      const messages = [
        { type: "success", content: "站点设置已保存" },
        { type: "info", content: "正在同步远程附件" },
        { type: "warning", content: "主题存在可用更新" },
        { type: "error", content: "插件启用失败" },
      ];

      return { messages };
    },
    template: `
      <div class="toast-container !static !w-auto !items-start !p-0">
        <ToastComponent
          v-for="message in messages"
          :key="message.type"
          :type="message.type"
          :content="message.content"
          :duration="-1"
        />
      </div>
    `,
  }),
};

export const ManagerApi: Story = {
  render: () => ({
    components: { VButton, VSpace },
    setup() {
      const messages: Record<ToastType, string> = {
        success: "站点设置已保存",
        info: "正在同步远程附件",
        warning: "主题存在可用更新",
        error: "插件启用失败",
      };
      const showToast = (type: ToastType) => {
        Toast[type](messages[type], { duration: 5000 });
      };

      return { showToast };
    },
    template: `
      <VSpace spacing="sm">
        <VButton type="primary" @click="showToast('success')">成功</VButton>
        <VButton @click="showToast('info')">提示</VButton>
        <VButton type="secondary" @click="showToast('warning')">警告</VButton>
        <VButton type="danger" @click="showToast('error')">错误</VButton>
      </VSpace>
    `,
  }),
};

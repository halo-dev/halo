import type { Meta, StoryObj } from "@storybook/vue3-vite";
import { createToastManager } from "reka-ui";
import { onMounted, onUnmounted } from "vue";
import { VButton } from "../button";
import { VSpace } from "../space";
import { createToastApi } from "./toast-manager";
import ToastComponent from "./Toast.vue";
import ToastHost from "./ToastHost.vue";
import type { ToastType } from "./types";

const meta: Meta<typeof ToastComponent> = {
  title: "Components/Toast",
  component: ToastComponent,
  tags: ["autodocs"],
  argTypes: {
    toast: {
      table: {
        disable: true,
      },
    },
  },
  parameters: {
    docs: {
      description: {
        component:
          "基于 Reka UI 的轻提示。通过 `Toast.success/error/info/warning/loading/promise` 调用，支持合并相同消息、悬停/聚焦暂停、滑动关闭、最大条数与全局配置。",
      },
    },
  },
};

export default meta;
type Story = StoryObj<typeof ToastComponent>;

function createStoryHost() {
  const manager = createToastManager();
  const toast = createToastApi(manager);

  return { manager, toast };
}

export const Default: Story = {
  render: () => ({
    components: { ToastHost },
    setup() {
      const { manager, toast } = createStoryHost();

      onMounted(() => {
        toast.success("文章已发布，前台页面将在缓存刷新后更新。", {
          duration: -1,
        });
      });

      return { manager };
    },
    template: `<ToastHost :manager="manager" />`,
  }),
};

export const Types: Story = {
  render: () => ({
    components: { ToastHost },
    setup() {
      const { manager, toast } = createStoryHost();

      onMounted(() => {
        toast.success("站点设置已保存", { duration: -1 });
        toast.info("正在同步远程附件", { title: "同步中", duration: -1 });
        toast.warning("主题存在可用更新", { duration: -1 });
        toast.error("插件启用失败", { duration: -1 });
      });

      return { manager };
    },
    template: `<ToastHost :manager="manager" />`,
  }),
};

export const Multiple: Story = {
  render: () => ({
    components: { ToastHost },
    setup() {
      const { manager, toast } = createStoryHost();

      onMounted(() => {
        for (let index = 1; index <= 6; index++) {
          toast.info(`第 ${index} 条通知，最多同时显示五条。`, {
            duration: -1,
          });
        }
      });

      return { manager };
    },
    template: `<ToastHost :manager="manager" />`,
  }),
};

export const ManagerApi: Story = {
  render: () => ({
    components: { ToastHost, VButton, VSpace },
    setup() {
      const { manager, toast } = createStoryHost();

      const messages: Record<ToastType, string> = {
        success: "站点设置已保存",
        info: "正在同步远程附件",
        warning: "主题存在可用更新",
        error: "插件启用失败",
      };

      const showToast = (type: ToastType) => {
        toast[type](messages[type], { duration: 5000 });
      };

      const showGrouped = () => {
        toast.success("后台任务仍在执行", { duration: 5000 });
      };

      const showRich = () => {
        toast.error("远程附件同步失败", {
          title: "同步中断",
          action: {
            label: "重试",
            onClick: () => toast.info("已重新发起同步"),
          },
          duration: 8000,
        });
      };

      const showPromise = () => {
        toast.promise(
          new Promise<string>((resolve) => setTimeout(resolve, 1500)),
          {
            loading: "正在上传附件",
            success: (name) => `附件 ${name} 已上传`,
            error: "附件上传失败",
          }
        );
      };

      return {
        manager,
        showToast,
        showGrouped,
        showRich,
        showPromise,
      };
    },
    template: `
      <ToastHost :manager="manager" />
      <VSpace spacing="sm">
        <VButton type="primary" @click="showToast('success')">成功</VButton>
        <VButton @click="showToast('info')">提示</VButton>
        <VButton type="secondary" @click="showToast('warning')">警告</VButton>
        <VButton type="danger" @click="showToast('error')">错误</VButton>
        <VButton @click="showGrouped">合并计数</VButton>
        <VButton @click="showRich">带操作</VButton>
        <VButton @click="showPromise">Promise</VButton>
      </VSpace>
    `,
  }),
};

export const Placement: Story = {
  render: () => ({
    components: { ToastHost, VButton, VSpace },
    setup() {
      const { manager, toast } = createStoryHost();
      const placements = [
        "top-left",
        "top-center",
        "top-right",
        "bottom-left",
        "bottom-center",
        "bottom-right",
      ] as const;

      const show = (placement: (typeof placements)[number]) => {
        toast.configure({ placement });
        toast.info(`当前位置：${placement}`, { duration: 3000 });
      };

      onUnmounted(() => {
        toast.configure({ placement: "top-center" });
      });

      return { manager, placements, show };
    },
    template: `
      <ToastHost :manager="manager" />
      <VSpace spacing="sm">
        <VButton v-for="placement in placements" :key="placement" @click="show(placement)">
          {{ placement }}
        </VButton>
      </VSpace>
    `,
  }),
};

export const ContentWidth: Story = {
  render: () => ({
    components: { ToastHost, VButton, VSpace },
    setup() {
      const { manager, toast } = createStoryHost();

      const showShort = () => {
        toast.success("已保存", { duration: 5000 });
      };

      const showLong = () => {
        toast.info(
          "文章已发布，前台页面将在缓存刷新后更新，已订阅的读者会收到通知。",
          { duration: 5000 }
        );
      };

      const showBoth = () => {
        toast.success("已保存", { duration: 5000 });
        toast.info(
          "文章已发布，前台页面将在缓存刷新后更新，已订阅的读者会收到通知。",
          { duration: 5000 }
        );
      };

      return { manager, showShort, showLong, showBoth };
    },
    template: `
      <ToastHost :manager="manager" />
      <VSpace spacing="sm">
        <VButton @click="showShort">短文案</VButton>
        <VButton @click="showLong">长文案</VButton>
        <VButton @click="showBoth">短 + 长</VButton>
      </VSpace>
    `,
  }),
};

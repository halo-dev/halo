import type { Meta, StoryObj } from "@storybook/vue3-vite";
import { markRaw, shallowRef } from "vue";
import { VSegmentedControl } from ".";
import { IconComputer, IconPhone, IconTablet } from "../../icons/icons";

const meta: Meta<typeof VSegmentedControl> = {
  title: "Components/SegmentedControl",
  component: VSegmentedControl,
  tags: ["autodocs"],
  render: (args) => ({
    components: { VSegmentedControl },
    setup() {
      const active = shallowRef("all");
      const items = [
        { value: "all", label: "全部" },
        { value: "pending", label: "待支付" },
        { value: "processing", label: "待发货" },
        { value: "done", label: "已完成" },
      ];

      return { active, args, items };
    },
    template: `
      <div class="max-w-xl">
        <VSegmentedControl
          v-model="active"
          aria-label="订单状态"
          :disabled="args.disabled"
          :direction="args.direction"
          :items="items"
          :size="args.size"
          :stretch="args.stretch"
        />
      </div>
    `,
  }),
  argTypes: {
    direction: {
      control: { type: "select" },
      options: ["row", "column"],
    },
    size: {
      control: { type: "select" },
      options: ["sm", "md"],
    },
    stretch: {
      control: "boolean",
    },
    disabled: {
      control: "boolean",
    },
  },
  args: {
    direction: "row",
    size: "md",
    stretch: false,
    disabled: false,
  },
};

export default meta;
type Story = StoryObj<typeof VSegmentedControl>;

export const Default: Story = {};

export const Compact: Story = {
  args: {
    size: "sm",
  },
};

export const Stretch: Story = {
  args: {
    stretch: true,
  },
};

export const WithIcons: Story = {
  render: () => ({
    components: { VSegmentedControl },
    setup() {
      const active = shallowRef("desktop");
      const items = [
        {
          value: "desktop",
          label: "桌面端",
          icon: markRaw(IconComputer),
        },
        {
          value: "tablet",
          label: "平板",
          icon: markRaw(IconTablet),
        },
        {
          value: "phone",
          label: "手机",
          icon: markRaw(IconPhone),
        },
      ];

      return { active, items };
    },
    template: `
      <div class="max-w-xl">
        <VSegmentedControl
          v-model="active"
          aria-label="预览设备"
          :items="items"
          size="sm"
        />
      </div>
    `,
  }),
};

export const IconOnly: Story = {
  render: () => ({
    components: { VSegmentedControl },
    setup() {
      const active = shallowRef("desktop");
      const items = [
        {
          value: "desktop",
          icon: markRaw(IconComputer),
          ariaLabel: "桌面",
          title: "桌面 1280px",
        },
        {
          value: "tablet",
          icon: markRaw(IconTablet),
          ariaLabel: "平板",
          title: "平板 768px",
        },
        {
          value: "phone",
          icon: markRaw(IconPhone),
          ariaLabel: "手机",
          title: "手机 390px",
        },
      ];

      return { active, items };
    },
    template: `
      <div class="max-w-xl">
        <VSegmentedControl
          v-model="active"
          aria-label="预览设备"
          :items="items"
        />
      </div>
    `,
  }),
};

export const Vertical: Story = {
  args: {
    direction: "column",
  },
};

export const WithDisabledItem: Story = {
  render: () => ({
    components: { VSegmentedControl },
    setup() {
      const active = shallowRef("basic");
      const items = [
        { value: "basic", label: "基础设置" },
        { value: "security", label: "安全策略" },
        { value: "notice", label: "通知" },
        { value: "audit", label: "审计日志", disabled: true },
      ];

      return { active, items };
    },
    template: `
      <div class="max-w-xl">
        <VSegmentedControl
          v-model="active"
          aria-label="设置分区"
          :items="items"
          size="sm"
        />
      </div>
    `,
  }),
};

export const Disabled: Story = {
  args: {
    disabled: true,
  },
};

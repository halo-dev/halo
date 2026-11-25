import type { Meta, StoryObj } from "@storybook/vue3-vite";
import { shallowRef } from "vue";
import { VSwitch } from ".";
import { VCard } from "../card";
import { VSpace } from "../space";

const meta: Meta<typeof VSwitch> = {
  title: "Components/Switch",
  component: VSwitch,
  tags: ["autodocs"],
  render: (args) => ({
    components: { VSwitch },
    setup() {
      const enabled = shallowRef(args.modelValue);
      return { args, enabled };
    },
    template: `
      <VSwitch
        v-model="enabled"
        :disabled="args.disabled"
        :loading="args.loading"
        :label="args.label"
        :label-position="args.labelPosition"
      />
    `,
  }),
  argTypes: {
    disabled: {
      control: { type: "boolean" },
    },
    loading: {
      control: { type: "boolean" },
    },
    label: {
      control: { type: "text" },
    },
    labelPosition: {
      control: { type: "inline-radio" },
      options: ["left", "right"],
    },
  },
  args: {
    modelValue: true,
    disabled: false,
    loading: false,
    label: undefined,
    labelPosition: "right",
  },
};

export default meta;
type Story = StoryObj<typeof VSwitch>;

export const Default: Story = {};

export const WithLabel: Story = {
  args: {
    label: "Enable notifications",
  },
};

export const LabelPosition: Story = {
  render: () => ({
    components: { VSwitch },
    setup() {
      const labelLeft = shallowRef(true);
      const labelRight = shallowRef(false);
      return { labelLeft, labelRight };
    },
    template: `
      <div class="flex flex-col items-start gap-4">
        <VSwitch v-model="labelRight" label="Label on the right" />
        <VSwitch
          v-model="labelLeft"
          label="Label on the left"
          label-position="left"
        />
      </div>
    `,
  }),
};

export const SettingsPanel: Story = {
  render: () => ({
    components: { VCard, VSpace, VSwitch },
    setup() {
      const commentReview = shallowRef(true);
      const emailNotice = shallowRef(false);
      const syncing = shallowRef(true);
      return { commentReview, emailNotice, syncing };
    },
    template: `
      <div class="max-w-md">
        <VCard title="评论设置">
          <div class="space-y-4">
            <div class="flex items-center justify-between gap-4">
              <div>
                <div class="text-sm font-medium text-gray-900">评论审核</div>
                <div class="mt-1 text-xs text-gray-500">新评论需要审核后才会展示。</div>
              </div>
              <VSwitch v-model="commentReview" />
            </div>
            <div class="flex items-center justify-between gap-4">
              <div>
                <div class="text-sm font-medium text-gray-900">邮件通知</div>
                <div class="mt-1 text-xs text-gray-500">有新评论时通知站点管理员。</div>
              </div>
              <VSwitch v-model="emailNotice" />
            </div>
            <div class="flex items-center justify-between gap-4">
              <div>
                <div class="text-sm font-medium text-gray-900">同步状态</div>
                <div class="mt-1 text-xs text-gray-500">正在保存远程通知设置。</div>
              </div>
              <VSwitch v-model="syncing" loading />
            </div>
          </div>
        </VCard>
      </div>
    `,
  }),
};

export const Disabled: Story = {
  args: {
    disabled: true,
    modelValue: false,
  },
};

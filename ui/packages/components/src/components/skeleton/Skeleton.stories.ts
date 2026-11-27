import type { Meta, StoryObj } from "@storybook/vue3-vite";
import { VSkeleton, VSkeletonText } from ".";

const meta: Meta<typeof VSkeleton> = {
  title: "Components/Skeleton",
  component: VSkeleton,
  tags: ["autodocs"],
  render: (args) => ({
    components: { VSkeleton },
    setup() {
      return { args };
    },
    template: `
      <div class="max-w-xl rounded border border-gray-100 bg-white p-6">
        <VSkeleton v-bind="args" />
      </div>
    `,
  }),
  args: {
    width: "100%",
    height: "1rem",
    shape: "rect",
    animation: "shimmer",
  },
};

export default meta;
type Story = StoryObj<typeof VSkeleton>;

export const Default: Story = {};

export const Shapes: Story = {
  render: () => ({
    components: { VSkeleton },
    template: `
      <div class="max-w-xl space-y-6 rounded border border-gray-100 bg-white p-6">
        <div>
          <p class="mb-2 text-xs font-medium text-gray-500">Rect</p>
          <VSkeleton width="100%" height="2.5rem" />
        </div>
        <div>
          <p class="mb-2 text-xs font-medium text-gray-500">Text</p>
          <VSkeleton width="80%" height="0.875rem" shape="text" />
        </div>
        <div>
          <p class="mb-2 text-xs font-medium text-gray-500">Circle</p>
          <VSkeleton width="2.5rem" shape="circle" />
        </div>
      </div>
    `,
  }),
};

export const Animations: Story = {
  render: () => ({
    components: { VSkeleton },
    template: `
      <div class="max-w-xl space-y-5 rounded border border-gray-100 bg-white p-6">
        <div>
          <p class="mb-2 text-xs font-medium text-gray-500">Shimmer</p>
          <VSkeleton width="100%" height="1rem" />
        </div>
        <div>
          <p class="mb-2 text-xs font-medium text-gray-500">Pulse</p>
          <VSkeleton width="100%" height="1rem" animation="pulse" />
        </div>
        <div>
          <p class="mb-2 text-xs font-medium text-gray-500">None</p>
          <VSkeleton width="100%" height="1rem" animation="none" />
        </div>
      </div>
    `,
  }),
};

export const TextLines: Story = {
  render: () => ({
    components: { VSkeletonText },
    template: `
      <div class="max-w-xl rounded border border-gray-100 bg-white p-6">
        <VSkeletonText />
      </div>
    `,
  }),
};

export const CardSkeleton: Story = {
  render: () => ({
    components: { VSkeleton, VSkeletonText },
    template: `
      <div class="max-w-xl rounded border border-gray-100 bg-white p-5">
        <div class="flex items-start gap-3">
          <VSkeleton width="2.5rem" shape="circle" />
          <div class="min-w-0 flex-1 space-y-2">
            <VSkeleton width="40%" height="0.875rem" />
            <VSkeleton width="24%" height="0.75rem" />
          </div>
        </div>
        <div class="mt-5">
          <VSkeletonText :lines="4" last-line-width="72%" />
        </div>
        <div class="mt-5 flex gap-2">
          <VSkeleton width="5rem" height="2rem" />
          <VSkeleton width="4rem" height="2rem" />
        </div>
      </div>
    `,
  }),
};

import type { Meta, StoryObj } from "@storybook/vue3-vite";
import { VSkeletonText } from ".";

const meta: Meta<typeof VSkeletonText> = {
  title: "Components/SkeletonText",
  component: VSkeletonText,
  tags: ["autodocs"],
  render: (args) => ({
    components: { VSkeletonText },
    setup() {
      return { args };
    },
    template: `
      <div class="max-w-xl rounded border border-gray-100 bg-white p-6">
        <VSkeletonText v-bind="args" />
      </div>
    `,
  }),
  args: {
    lines: 3,
    lastLineWidth: "60%",
    lineHeight: "0.875rem",
    spacing: "0.5rem",
    animation: "shimmer",
  },
};

export default meta;
type Story = StoryObj<typeof VSkeletonText>;

export const Default: Story = {};

export const CustomWidths: Story = {
  args: {
    lines: 4,
    widths: ["100%", "92%", "84%", "48%"],
  },
};

export const Pulse: Story = {
  args: {
    animation: "pulse",
  },
};

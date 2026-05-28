import type { Meta, StoryObj } from "@storybook/vue3-vite";
import { VLoading } from ".";
import { VCard } from "../card";

const meta: Meta<typeof VLoading> = {
  title: "Components/Loading",
  component: VLoading,
  tags: ["autodocs"],
  render: () => ({
    components: { VLoading },
    template: `<VLoading />`,
  }),
};

export default meta;
type Story = StoryObj<typeof VLoading>;

export const Default: Story = {};

export const InContentPanel: Story = {
  render: () => ({
    components: { VCard, VLoading },
    template: `
      <div class="max-w-lg">
        <VCard title="正在加载文章列表">
          <div class="space-y-3">
            <div class="h-4 w-2/3 rounded bg-gray-100"></div>
            <div class="h-4 w-5/6 rounded bg-gray-100"></div>
            <VLoading />
            <div class="h-4 w-1/2 rounded bg-gray-100"></div>
          </div>
        </VCard>
      </div>
    `,
  }),
};

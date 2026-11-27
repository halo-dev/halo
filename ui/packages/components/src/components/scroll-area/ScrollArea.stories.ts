import type { Meta, StoryObj } from "@storybook/vue3-vite";
import { VScrollArea } from ".";

const rows = Array.from({ length: 40 }, (_, index) => `Item ${index + 1}`);

const meta: Meta<typeof VScrollArea> = {
  title: "Components/ScrollArea",
  component: VScrollArea,
  tags: ["autodocs"],
  render: (args) => ({
    components: { VScrollArea },
    setup() {
      return { args, rows };
    },
    template: `
      <VScrollArea v-bind="args" class="h-64 w-full rounded-base border border-gray-200">
        <ul class="divide-y divide-gray-100">
          <li v-for="row in rows" :key="row" class="px-4 py-2 text-sm text-gray-700">
            {{ row }}
          </li>
        </ul>
      </VScrollArea>
    `,
  }),
  args: {
    type: "scroll",
    scrollHideDelay: 600,
    orientation: "vertical",
    overscrollContain: true,
  },
  argTypes: {
    type: {
      control: { type: "inline-radio" },
      options: ["hover", "scroll", "auto", "always", "glimpse"],
    },
    orientation: {
      control: { type: "inline-radio" },
      options: ["vertical", "horizontal", "both"],
    },
    scrollHideDelay: {
      control: { type: "number" },
    },
  },
};

export default meta;

type Story = StoryObj<typeof meta>;

export const Default: Story = {};

export const Horizontal: Story = {
  render: (args) => ({
    components: { VScrollArea },
    setup() {
      return { args };
    },
    template: `
      <VScrollArea v-bind="args" orientation="horizontal" class="w-full rounded-base border border-gray-200">
        <div class="flex gap-3 p-4">
          <div
            v-for="index in 12"
            :key="index"
            class="flex h-16 w-32 flex-none items-center justify-center rounded-base bg-gray-50 text-sm text-gray-600"
          >
            Cell {{ index }}
          </div>
        </div>
      </VScrollArea>
    `,
  }),
};

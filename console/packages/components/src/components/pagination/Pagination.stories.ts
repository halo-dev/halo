import type { Meta, StoryObj } from "@storybook/vue3";

import { VPagination } from ".";

const meta: Meta<typeof VPagination> = {
  title: "Pagination",
  component: VPagination,
  tags: ["autodocs"],
  render: (args) => ({
    components: { VPagination },
    setup() {
      return { args };
    },
    template: `
      <VPagination        
        v-model:page="args.page"
        v-model:size="args.size"
        :total="args.total"
      />
    `,
  }),
  args: {
    page: 1,
    size: 10,
    total: 100,
  },
};

export default meta;
type Story = StoryObj<typeof VPagination>;

export const Default: Story = {
  args: {},
};

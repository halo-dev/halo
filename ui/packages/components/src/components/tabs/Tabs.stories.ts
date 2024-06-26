import type { Meta, StoryObj } from "@storybook/vue3";
import { VTabItem, VTabs } from ".";

const meta: Meta<typeof VTabs> = {
  title: "Tabs",
  component: VTabs,
  tags: ["autodocs"],
  render: (args) => ({
    components: { VTabs, VTabItem },
    setup() {
      return {
        args,
      };
    },
    template: `
      <div class="p-3">
        <VTabs v-model:activeId="args.activeId" type="pills">
          <VTabItem id="johnniang" label="JohnNiang">
            This is JohnNiang's Item
          </VTabItem>
          <VTabItem id="ryanwang" label="Ryan Wang">
            This is Ryan Wang's Item
          </VTabItem>
          <VTabItem id="guqing" label="guqing">
            This is guqing's Item
          </VTabItem>
        </VTabs>
      </div>
    `,
  }),
  argTypes: {
    type: {
      control: { type: "select" },
      options: ["default", "pills", "outline"],
    },
  },
};

export default meta;
type Story = StoryObj<typeof VTabs>;

export const Default: Story = {
  args: {
    activeId: "ryanwang",
    type: "default",
  },
};

export const Pills: Story = {
  args: {
    activeId: "ryanwang",
    type: "pills",
  },
};

export const Outline: Story = {
  args: {
    activeId: "ryanwang",
    type: "outline",
  },
};

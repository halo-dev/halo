import type { Meta, StoryObj } from "@storybook/vue3";

import { VDialog } from ".";
import { VButton } from "../button";

const meta: Meta<typeof VDialog> = {
  title: "Dialog",
  component: VDialog,
  tags: ["autodocs"],
  render: (args) => ({
    components: { VDialog, VButton },
    height: 400,
    setup() {
      const showDialog = () => {
        args.visible = true;
      };

      return {
        args,
        showDialog,
      };
    },
    template: `
      <div style="height: 400px">
        <VButton @click="showDialog" >点击显示Dialog</VButton>
        <VDialog v-bind="args" @onCancel="args.visible = false" />
      </div>
    `,
  }),
  argTypes: {
    type: {
      control: { type: "select" },
      options: ["success", "info", "warning", "error"],
    },
    confirmType: {
      control: { type: "select" },
      options: ["default", "primary", "secondary", "danger"],
    },
  },
  args: {
    title: "Hello",
    visible: false,
    description: "Hello World",
  },
};

export default meta;
type Story = StoryObj<typeof VDialog>;

export const Default: Story = {
  args: {
    type: "info",
  },
};

export const Success: Story = {
  args: {
    type: "success",
  },
};

export const Warning: Story = {
  args: {
    type: "warning",
  },
};

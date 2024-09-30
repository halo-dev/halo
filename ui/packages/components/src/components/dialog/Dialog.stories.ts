import type { Meta, StoryObj } from "@storybook/vue3";

import { Dialog, VDialog } from ".";
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
        Dialog.success({
          title: "Hi",
          // @ts-ignore
          type: args.type,
        });
      };

      return {
        args,
        showDialog,
      };
    },
    template: `
      <div style="height: 400px">
        <VButton @click="showDialog" >点击显示Dialog</VButton>
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

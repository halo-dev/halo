import type { Meta, StoryObj } from "@storybook/vue3";
import { VSwitch } from ".";

const meta: Meta<typeof VSwitch> = {
  title: "Switch",
  component: VSwitch,
  tags: ["autodocs"],
  render: (args) => ({
    components: { VSwitch },
    setup() {
      return {
        args,
      };
    },
    template: `
        <VSwitch v-bind="args" v-model="args.modelValue" />
    `,
  }),
  argTypes: {},
};

export default meta;
type Story = StoryObj<typeof VSwitch>;

export const Default: Story = {
  args: {},
};

import type { Meta, StoryObj } from "@storybook/vue3";

import { VDropdown, VDropdownItem, VDropdownDivider } from ".";
import { VButton } from "../button";

const meta: Meta<typeof VDropdown> = {
  title: "Dropdown",
  component: VDropdown,
  tags: ["autodocs"],
  render: (args) => ({
    components: { VDropdown, VDropdownItem, VDropdownDivider, VButton },
    height: 400,
    setup() {
      return {
        args,
      };
    },
    template: `
    <div style="height: 300px">
      <VDropdown>
        ${args.default}
        <template #popper>
          ${args.popper}
        </template>
      </VDropdown>
    </div>

    `,
  }),
  argTypes: {},
};

export default meta;
type Story = StoryObj<typeof VDropdown>;

export const Default: Story = {
  args: {
    default: `<VButton>Hello</VButton>`,
    popper: `
      <VDropdownItem>删除</VDropdownItem>
      <VDropdownDivider></VDropdownDivider>
      <VDropdownItem>删除</VDropdownItem>
      <VDropdownItem>编辑</VDropdownItem>
    `,
  },
};

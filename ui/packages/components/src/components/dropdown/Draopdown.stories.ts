import type { Meta, StoryObj } from "@storybook/vue3";

import { VDropdown, VDropdownDivider, VDropdownItem } from ".";
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
        <VButton>Hello</VButton>
        <template #popper>
          <VDropdownItem>删除</VDropdownItem>
          <VDropdownDivider></VDropdownDivider>
          <VDropdownItem>删除</VDropdownItem>
          <VDropdownItem>编辑</VDropdownItem>
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
  args: {},
};

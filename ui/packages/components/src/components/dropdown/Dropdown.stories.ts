import type { Meta, StoryObj } from "@storybook/vue3-vite";
import {
  IconCheckboxCircle,
  IconDeleteBin,
  IconExternalLinkLine,
  IconMore,
  IconSettings,
} from "@/icons/icons";
import { VDropdown, VDropdownDivider, VDropdownItem } from ".";
import { VButton } from "../button";

const meta: Meta<typeof VDropdown> = {
  title: "Components/Dropdown",
  component: VDropdown,
  tags: ["autodocs"],
  render: (args) => ({
    components: {
      IconCheckboxCircle,
      IconDeleteBin,
      IconExternalLinkLine,
      IconMore,
      IconSettings,
      VButton,
      VDropdown,
      VDropdownDivider,
      VDropdownItem,
    },
    setup() {
      return { args };
    },
    template: `
      <div class="flex h-72 items-start">
        <VDropdown class="inline-flex" :placement="args.placement">
          <VButton>
            <template #icon>
              <IconMore />
            </template>
            文章操作
          </VButton>
          <template #popper>
            <VDropdownItem selected>
              <template #prefix-icon>
                <IconCheckboxCircle />
              </template>
              已发布
            </VDropdownItem>
            <VDropdownItem>
              <template #prefix-icon>
                <IconExternalLinkLine />
              </template>
              打开前台页面
            </VDropdownItem>
            <VDropdownItem>
              <template #prefix-icon>
                <IconSettings />
              </template>
              编辑发布设置
            </VDropdownItem>
            <VDropdownDivider />
            <VDropdownItem type="danger">
              <template #prefix-icon>
                <IconDeleteBin />
              </template>
              移入回收站
            </VDropdownItem>
          </template>
        </VDropdown>
      </div>
    `,
  }),
  argTypes: {
    placement: {
      control: { type: "select" },
      options: ["bottom-start", "bottom", "bottom-end", "top-start", "right"],
    },
  },
  args: {
    placement: "bottom-start",
  },
};

export default meta;
type Story = StoryObj<typeof VDropdown>;

export const Default: Story = {};

export const DisabledItem: Story = {
  render: () => ({
    components: {
      IconMore,
      VButton,
      VDropdown,
      VDropdownDivider,
      VDropdownItem,
    },
    template: `
      <div class="flex h-72 items-start">
        <VDropdown class="inline-flex">
          <VButton>
            <template #icon>
              <IconMore />
            </template>
            批量操作
          </VButton>
          <template #popper>
            <VDropdownItem>设为已发布</VDropdownItem>
            <VDropdownItem disabled>生成摘要</VDropdownItem>
            <VDropdownDivider />
            <VDropdownItem type="danger">批量删除</VDropdownItem>
          </template>
        </VDropdown>
      </div>
    `,
  }),
};

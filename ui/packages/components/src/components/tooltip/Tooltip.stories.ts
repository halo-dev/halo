import type { Meta, StoryObj } from "@storybook/vue3-vite";
import { VTooltipComponent, vTooltip } from ".";
import { VButton } from "../button";

const meta: Meta<typeof VTooltipComponent> = {
  title: "Components/Tooltip",
  component: VTooltipComponent,
  tags: ["autodocs"],
  render: (args) => ({
    components: { VButton, VTooltipComponent },
    setup() {
      return { args };
    },
    template: `
      <div class="flex h-40 items-start">
        <VTooltipComponent :placement="args.placement">
          <VButton>悬停查看说明</VButton>
          <template #popper>
            {{ args.content }}
          </template>
        </VTooltipComponent>
      </div>
    `,
  }),
  argTypes: {
    placement: {
      control: { type: "select" },
      options: ["top", "bottom", "left", "right"],
    },
  },
  args: {
    content: "保存后会立即应用到当前站点。",
    placement: "top",
  },
};

export default meta;
type Story = StoryObj<typeof VTooltipComponent>;

export const Component: Story = {};

export const Directive: Story = {
  render: () => ({
    components: { VButton },
    directives: {
      tooltip: vTooltip,
    },
    template: `
      <div class="flex h-40 items-start">
        <VButton v-tooltip="'该操作只会刷新缓存，不会修改任何内容。'">
          刷新缓存
        </VButton>
      </div>
    `,
  }),
};

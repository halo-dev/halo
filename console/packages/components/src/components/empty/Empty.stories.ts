import type { Meta, StoryObj } from "@storybook/vue3";
import { VEmpty } from ".";
import { VSpace } from "../space";
import { VButton } from "../button";

const meta: Meta<typeof VEmpty> = {
  title: "Empty",
  component: VEmpty,
  tags: ["autodocs"],
  render: (args) => ({
    components: { VEmpty, VSpace, VButton },
    height: 400,
    setup() {
      return {
        args,
      };
    },
    template: `
      <VEmpty>
        <template #actions>
          ${args.actions}
        </template>
      </VEmpty>
    `,
  }),
  argTypes: {},
};

export default meta;
type Story = StoryObj<typeof VEmpty>;

export const Default: Story = {
  args: {
    title: "没有找到与搜索条件匹配的文章",
    message: "没有找到与搜索条件匹配的文章，你可以清空搜索条件或者新建文章",
    actions: `
      <VSpace>
        <VButton>清空搜索</VButton>
        <VButton type="primary">新建文章</VButton>
      </VSpace>
    `,
  },
};

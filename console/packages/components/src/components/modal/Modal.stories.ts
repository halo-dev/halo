import type { Meta, StoryObj } from "@storybook/vue3";

import { VModal } from ".";
import { VButton } from "../button";
import { IconArrowLeft, IconArrowRight } from "@/icons/icons";
import { VSpace } from "../space";

const meta: Meta<typeof VModal> = {
  title: "Modal",
  component: VModal,
  tags: ["autodocs"],
  render: (args) => ({
    components: { VModal, VButton, VSpace, IconArrowLeft, IconArrowRight },
    setup() {
      return { args };
    },
    template: `
      <VButton type="secondary" @click="args.visible = true">打开</VButton>
      <VModal
        v-model:visible="args.visible"
        :fullscreen="args.fullscreen"
        :title="args.title"
        :width="args.width"
        :mount-to-body="true"
      >
        <template #actions>
          <span>
            <IconArrowLeft role="button" />
          </span>

          <span>
            <IconArrowRight role="button" />
          </span>
        </template>
        <div class="flex flex-col">
          <img class="w-full" src="https://ryanc.cc/avatar" />
          <img class="w-full" src="https://ryanc.cc/avatar" />
          <img class="w-full" src="https://halo.run/logo" />
        </div>

        <template #footer>
          <VSpace>
            <VButton loading type="primary" @click="args.visible = false"
              >确定
            </VButton>
            <VButton @click="args.visible = false">取消</VButton>
          </VSpace>
        </template>
      </VModal>
    `,
  }),
  args: {
    visible: false,
  },
};

export default meta;
type Story = StoryObj<typeof VModal>;

export const Default: Story = {
  args: {},
};

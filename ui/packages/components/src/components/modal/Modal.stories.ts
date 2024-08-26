import type { Meta, StoryObj } from "@storybook/vue3";

import { IconArrowLeft, IconArrowRight } from "@/icons/icons";
import { ref } from "vue";
import { VModal } from ".";
import { VButton } from "../button";
import { VSpace } from "../space";

const meta: Meta<typeof VModal> = {
  title: "Modal",
  component: VModal,
  tags: ["autodocs"],
  render: (args) => ({
    components: { VModal, VButton, VSpace, IconArrowLeft, IconArrowRight },
    setup() {
      const modal = ref();
      return { args, modal };
    },
    template: `
      <VButton type="secondary" @click="args.visible = true">打开</VButton>
      <VModal
        ref="modal"
        v-if="args.visible"
        :fullscreen="args.fullscreen"
        :title="args.title"
        :width="args.width"
        :mount-to-body="true"
        :layerClosable="true"
        @close="args.visible = false"
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
          <img class="w-full" src="https://www.halo.run/logo" />
        </div>

        <template #footer>
          <VSpace>
            <VButton loading type="primary" @click="args.visible = false"
              >确定
            </VButton>
            <VButton @click="modal.close()">取消</VButton>
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

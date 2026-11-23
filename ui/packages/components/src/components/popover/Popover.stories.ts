import type { Meta, StoryObj } from "@storybook/vue3-vite";
import { ref } from "vue";
import { VPopover } from ".";
import { VButton } from "../button";

const meta: Meta<typeof VPopover> = {
  title: "Components/Popover",
  component: VPopover,
  tags: ["autodocs"],
  render: (args) => ({
    components: { VButton, VPopover },
    setup() {
      const name = ref("Halo");

      return { args, name };
    },
    template: `
      <div class="flex h-72 items-start">
        <VPopover v-bind="args">
          <VButton>Rename</VButton>
          <template #content="{ close }">
            <div class="w-72 space-y-3 p-3">
              <label class="block text-sm font-medium text-gray-900" for="popover-name">
                Display name
              </label>
              <input
                id="popover-name"
                v-model="name"
                class="block h-9 w-full rounded-md border border-gray-300 px-3 text-sm text-gray-900 focus:border-primary focus:outline-none focus:ring-1 focus:ring-primary"
                type="text"
              />
              <label class="flex items-center gap-2 text-sm text-gray-600">
                <input type="checkbox" />
                Also update the slug
              </label>
              <div class="flex justify-end gap-2">
                <VButton type="secondary" @click="close">Cancel</VButton>
                <VButton type="primary" @click="close">Save</VButton>
              </div>
            </div>
          </template>
        </VPopover>
      </div>
    `,
  }),
  argTypes: {
    placement: {
      control: { type: "select" },
      options: [
        "top",
        "top-start",
        "top-end",
        "right",
        "right-start",
        "right-end",
        "bottom",
        "bottom-start",
        "bottom-end",
        "left",
        "left-start",
        "left-end",
      ],
    },
  },
  args: {
    placement: "bottom-start",
  },
};

export default meta;
type Story = StoryObj<typeof VPopover>;

export const Default: Story = {};

export const MatchTriggerWidth: Story = {
  render: (args) => ({
    components: { VButton, VPopover },
    setup() {
      return { args };
    },
    template: `
      <div class="flex h-72 items-start">
        <VPopover v-bind="args">
          <div class="w-64">
            <VButton block>Choose an option</VButton>
          </div>
          <template #content>
            <div class="p-2 text-sm text-gray-600">
              The panel follows the trigger width.
            </div>
          </template>
        </VPopover>
      </div>
    `,
  }),
  args: {
    matchTriggerWidth: true,
  },
};

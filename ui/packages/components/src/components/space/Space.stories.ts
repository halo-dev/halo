import type { Meta, StoryObj } from "@storybook/vue3-vite";
import { VSpace } from ".";
import { VButton } from "../button";

const meta: Meta<typeof VSpace> = {
  title: "Components/Space",
  component: VSpace,
  tags: ["autodocs"],
  render: (args) => ({
    components: { VButton, VSpace },
    setup() {
      return { args };
    },
    template: `
      <VSpace
        :align="args.align"
        :as="args.as"
        :block="args.block"
        :direction="args.direction"
        :fill="args.fill"
        :justify="args.justify"
        :spacing="args.spacing"
        :wrap="args.wrap"
      >
        <VButton type="primary">保存</VButton>
        <VButton>预览</VButton>
        <VButton type="danger" ghost>删除</VButton>
      </VSpace>
    `,
  }),
  argTypes: {
    align: {
      control: { type: "select" },
      options: ["start", "end", "center", "stretch", "baseline"],
    },
    direction: {
      control: { type: "select" },
      options: ["row", "column"],
    },
    justify: {
      control: { type: "select" },
      options: ["start", "end", "center", "between", "around", "evenly"],
    },
    spacing: {
      control: { type: "select" },
      options: ["xs", "sm", "md", "lg", "xl", "2xl"],
    },
    wrap: {
      control: { type: "boolean" },
    },
    block: {
      control: { type: "boolean" },
    },
    fill: {
      control: { type: "boolean" },
    },
    as: {
      control: { type: "select" },
      options: ["div", "ul", "nav", "section"],
    },
  },
  args: {
    align: "center",
    direction: "row",
    spacing: "sm",
    wrap: false,
    block: false,
    fill: false,
    as: "div",
  },
};

export default meta;
type Story = StoryObj<typeof VSpace>;

export const Row: Story = {};

export const Column: Story = {
  args: {
    align: "stretch",
    direction: "column",
    spacing: "xs",
  },
};

export const Wrap: Story = {
  args: {
    wrap: true,
    spacing: "md",
  },
  render: (args) => ({
    components: { VSpace },
    setup() {
      return { args };
    },
    template: `
      <VSpace :spacing="args.spacing" :wrap="args.wrap" style="max-width: 360px">
        <span
          v-for="item in 12"
          :key="item"
          :style="{
            padding: '4px 8px',
            background: '#f3f4f6',
            borderRadius: '4px',
          }"
        >
          Tag {{ item }}
        </span>
      </VSpace>
    `,
  }),
};

export const Justify: Story = {
  args: {
    align: "center",
    block: true,
    justify: "between",
    spacing: "sm",
  },
  render: (args) => ({
    components: { VButton, VSpace },
    setup() {
      return { args };
    },
    template: `
      <VSpace
        :align="args.align"
        :block="args.block"
        :justify="args.justify"
        style="width: 100%"
      >
        <span>标题</span>
        <VSpace :spacing="args.spacing">
          <VButton>取消</VButton>
          <VButton type="primary">保存</VButton>
        </VSpace>
      </VSpace>
    `,
  }),
};

export const CustomGap: Story = {
  args: {
    spacing: 24,
  },
};

export const TupleGap: Story = {
  args: {
    direction: "column",
    spacing: [20, 4],
  },
};

export const Fill: Story = {
  args: {
    block: true,
    fill: true,
    spacing: "sm",
  },
};

export const Polymorphic: Story = {
  args: {
    align: "baseline",
    as: "ul",
    spacing: "sm",
  },
  render: (args) => ({
    components: { VButton, VSpace },
    setup() {
      return { args };
    },
    template: `
      <VSpace :align="args.align" :as="args.as" :spacing="args.spacing">
        <li><VButton>一</VButton></li>
        <li><VButton>二</VButton></li>
        <li><VButton>三</VButton></li>
      </VSpace>
    `,
  }),
};

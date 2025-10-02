import type { Preview } from "@storybook/vue3";

import "overlayscrollbars/overlayscrollbars.css";
import "../src/styles/tailwind.css";

const preview: Preview = {
  parameters: {
    actions: { argTypesRegex: "^on[A-Z].*" },
    controls: {
      matchers: {
        color: /(background|color)$/i,
        date: /Date$/,
      },
    },
    layout: "padded",
  },
};

export default preview;

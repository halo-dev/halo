import type { Preview } from "@storybook/vue3";

import "../src/styles/tailwind.css";
import "overlayscrollbars/overlayscrollbars.css";

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

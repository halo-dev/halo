const { themeable } = require("tailwindcss-themeable");

module.exports = {
  content: ["./index.html", "./src/**/*.{vue,js,ts,jsx,tsx}"],
  theme: {
    extend: {},
  },
  plugins: [
    require("tailwindcss-safe-area"),
    require("@tailwindcss/aspect-ratio"),
    themeable({
      defaultTheme: "default",
      themes: [
        {
          name: "default",
          palette: {
            primary: "#4CCBA0",
            secondary: "#0E1731",
          },
        },
      ],
    }),
  ],
};

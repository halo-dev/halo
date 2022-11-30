/** @type {import('tailwindcss').Config} */
module.exports = {
  content: [
    "./index.html",
    "./src/**/*.{vue,js,ts,jsx,tsx}",
    "./packages/shared/src/**/*.{vue,js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      animation: {
        breath: "breath 1s ease-in-out infinite",
      },
      keyframes: {
        breath: {
          "0%": { transform: "scale(1)", opacity: 0.8 },
          "50%": { transform: "scale(1.02)", opacity: 1 },
          "100%": { transform: "scale(1)", opacity: 0.8 },
        },
      },
    },
  },
  plugins: [
    require("tailwindcss-safe-area"),
    require("@tailwindcss/aspect-ratio"),
    require("@formkit/themes/tailwindcss"),
    require("@tailwindcss/container-queries"),
    require("tailwindcss-themer")({
      defaultTheme: {
        extend: {
          colors: {
            primary: "#4CCBA0",
            secondary: "#0E1731",
          },
          borderRadius: {
            base: "4px",
          },
        },
      },
      themes: [
        {
          name: "theme-dark",
          extend: {
            colors: {
              primary: "black",
              secondary: "#0E1731",
            },
            borderRadius: {
              base: "2px",
            },
          },
        },
      ],
    }),
  ],
  safelist: ["theme-dark"],
};

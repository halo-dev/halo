import formkit from "@formkit/themes/tailwindcss";
import aspectRatio from "@tailwindcss/aspect-ratio";
import containerQueries from "@tailwindcss/container-queries";
import forms from "@tailwindcss/forms";
import { Config } from "tailwindcss";
import themer from "tailwindcss-themer";

export default {
  content: ["./index.html", "./{src,uc-src,console-src}/**/*.{vue,js,ts,tsx}"],
  theme: {
    extend: {
      animation: {
        breath: "breath 1s ease-in-out infinite",
      },
      keyframes: {
        breath: {
          "0%": { transform: "scale(1)", opacity: "0.8" },
          "50%": { transform: "scale(1.02)", opacity: "1" },
          "100%": { transform: "scale(1)", opacity: "0.8" },
        },
      },
    },
  },
  plugins: [
    aspectRatio,
    formkit,
    containerQueries,
    forms,
    themer({
      defaultTheme: {
        extend: {
          colors: {
            primary: "#4CCBA0",
            secondary: "#0E1731",
            danger: "#D71D1D",
          },
          borderRadius: {
            base: "4px",
          },
        },
      },
    }),
  ],
} satisfies Config;

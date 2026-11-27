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
            "primary-ink": "#0E7A5F",
            "primary-soft": "#E6F8F1",
            secondary: "#0E1731",
            "secondary-soft": "#EEF0F4",
            danger: "#D71D1D",
            "danger-ink": "#B91C1C",
            "danger-soft": "#FEE2E2",
            success: "#15803D",
            "success-soft": "#DCFCE7",
            warning: "#B45309",
            "warning-soft": "#FEF3C7",
            info: "#3D5A80",
            "info-soft": "#E8EEF5",
          },
          borderRadius: {
            base: "4px",
          },
        },
      },
    }),
  ],
} satisfies Config;

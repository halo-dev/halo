import type { IconifyFormat, IconifyValue } from "../iconify/types";

export type ArrayItemFormMode = "modal" | "accordion";

export type FormattedItemLabel =
  | {
      type: "text" | "image" | "color";
      value: string;
    }
  | {
      type: "iconify";
      value: string | IconifyValue;
      format: IconifyFormat;
      valueOnly?: boolean;
    };

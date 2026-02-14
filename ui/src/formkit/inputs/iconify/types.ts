export type IconifyFormat = "svg" | "dataurl" | "url" | "name";

export interface IconifyValue {
  value: string;
  name?: string;
  width?: string;
  color?: string;
}

export type IconifySizing = {
  enabled?: boolean;
  default?: string;
  presets?: string[];
};

import type { InjectionKey } from "vue";

export type AvatarSize = "lg" | "md" | "sm" | "xs";

export interface AvatarProps {
  src?: string;
  alt?: string;
  size?: AvatarSize;
  width?: string;
  height?: string;
  circle?: boolean;
}

export type AvatarGroupProps = Omit<AvatarProps, "src" | "alt">;

export const AvatarGroupContextInjectionKey: InjectionKey<AvatarGroupProps> =
  Symbol("avatar-group-context");

import {
  LocalThumbnailSpecSizeEnum,
  LocalThumbnailStatusPhaseEnum,
  type LocalThumbnail,
} from "@halo-dev/api-client";
import { IconCheckboxCircle, IconErrorWarning } from "@halo-dev/components";
import { computed, type Component, type Ref } from "vue";
import RiTimeLine from "~icons/ri/time-line";

export const SIZE_MAP: Record<LocalThumbnailSpecSizeEnum, string> = {
  XL: "1600w",
  L: "1200w",
  M: "800w",
  S: "400w",
};

export const PHASE_MAP: Record<
  LocalThumbnailStatusPhaseEnum,
  {
    icon: Component;
    label: string;
    color: string;
  }
> = {
  PENDING: {
    icon: RiTimeLine,
    label: "core.attachment.thumbnails.phase.pending",
    color: "text-gray-500",
  },
  SUCCEEDED: {
    icon: IconCheckboxCircle,
    label: "core.attachment.thumbnails.phase.succeeded",
    color: "text-green-500",
  },
  FAILED: {
    icon: IconErrorWarning,
    label: "core.attachment.thumbnails.phase.failed",
    color: "text-red-500",
  },
};
export function useThumbnailDetail(thumbnail: Ref<LocalThumbnail>) {
  const phase = computed(() => {
    return PHASE_MAP[
      thumbnail.value.status.phase || LocalThumbnailStatusPhaseEnum.Pending
    ];
  });

  return {
    phase,
  };
}

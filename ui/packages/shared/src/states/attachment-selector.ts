import type { Attachment } from "@halo-dev/api-client";
import type { Component } from "vue";

export type AttachmentLike =
  | Attachment
  | string
  | {
      url: string;
      type: string;
    };

export interface AttachmentSelectProvider {
  id: string;
  label: string;
  component: Component | string;
  callback?: (attachments: AttachmentLike[]) => void;
}

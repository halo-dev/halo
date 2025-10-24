import type { Attachment } from "@halo-dev/api-client";
import type { Component } from "vue";

export interface AttachmentSimple {
  url: string;
  mediaType?: string;
  alt?: string;
  caption?: string;
}

export type AttachmentLike = Attachment | AttachmentSimple | string;

export interface AttachmentSelectProvider {
  id: string;
  label: string;
  component: Component | string;
  callback?: (attachments: AttachmentLike[]) => void;
}

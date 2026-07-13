import type { Body, Meta, Restrictions, UppyFile } from "@uppy/core";

export type UppyUploadFile = UppyFile<Meta, Body>;

export type UppyUploadRestrictions = Partial<Restrictions>;

export interface UppyUploadSuccessResponse {
  uploadURL?: string;
  status?: number;
  body?: unknown;
  bytesUploaded?: number;
}

export interface UppyUploadErrorResponse {
  status: number;
  body: unknown;
}

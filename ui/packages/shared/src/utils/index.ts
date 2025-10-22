import { AttachmentUtils } from "./attachment";
import { DateUtils } from "./date";

export const utils = {
  date: new DateUtils(),
  attachment: new AttachmentUtils(),
};

export { THUMBNAIL_WIDTH_MAP } from "./attachment";

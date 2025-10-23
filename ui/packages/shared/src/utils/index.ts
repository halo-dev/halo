import { AttachmentUtils } from "./attachment";
import { DateUtils } from "./date";
import { PermissionUtils } from "./permission";

export const utils = {
  date: new DateUtils(),
  attachment: new AttachmentUtils(),
  permission: new PermissionUtils(),
};

export { THUMBNAIL_WIDTH_MAP } from "./attachment";

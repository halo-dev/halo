import { AttachmentUtils } from "./attachment";
import { DateUtils } from "./date";
import { IdUtils } from "./id";
import { PermissionUtils } from "./permission";

export const utils = {
  date: new DateUtils(),
  attachment: new AttachmentUtils(),
  permission: new PermissionUtils(),
  id: new IdUtils(),
};

export { THUMBNAIL_WIDTH_MAP } from "./attachment";

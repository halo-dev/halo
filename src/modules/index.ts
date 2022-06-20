import dashboardModule from "./dashboard/module";
import postModule from "./contents/posts/module";
import sheetModule from "./contents/sheets/module";
import commentModule from "./contents/comments/module";
import attachmentModule from "./contents/attachments/module";
import themeModule from "./interface/themes/module";
import menuModule from "./interface/menus/module";
import pluginModule from "./system/plugins/module";
import userModule from "./system/users/module";
import roleModule from "./system/roles/module";
import settingModule from "./system/settings/module";

const coreModules = [
  dashboardModule,
  postModule,
  sheetModule,
  commentModule,
  attachmentModule,
  themeModule,
  menuModule,
  pluginModule,
  userModule,
  roleModule,
  settingModule,
];

export { coreModules };

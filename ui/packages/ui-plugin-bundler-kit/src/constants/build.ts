const DEFAULT_OUT_DIR_DEV = "../build/resources/main/console";
const DEFAULT_OUT_DIR_DEV_BASE = "../build/resources/main";
const DEFAULT_OUT_DIR_PROD = "./build/dist";

function getDefaultOutDirDev(bundleLocation: string) {
  return `${DEFAULT_OUT_DIR_DEV_BASE}/${bundleLocation}`;
}

export { DEFAULT_OUT_DIR_DEV, DEFAULT_OUT_DIR_PROD, getDefaultOutDirDev };

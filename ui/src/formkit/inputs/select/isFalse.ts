// eslint-disable-next-line @typescript-eslint/no-explicit-any
export const isFalse = (value: any, onlyBoolean = false) => {
  if (onlyBoolean) {
    return [false, "false"].includes(value);
  }
  return [undefined, null, "false", false].includes(value);
};

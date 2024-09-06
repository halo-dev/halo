export const isFalse = (value: string | boolean | undefined | null) => {
  return [undefined, null, "false", false].includes(value);
};

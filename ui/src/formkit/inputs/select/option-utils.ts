import { get, has } from "es-toolkit/compat";
import type { PropertyPath } from "lodash-es";
import type { SelectActionRequest, SelectOption } from "./types";

export function mapItemsToSelectOptions(
  items: Array<object>,
  requestOption: Pick<
    SelectActionRequest,
    "labelField" | "valueField" | "iconField" | "descriptionField"
  >
): SelectOption[] {
  const {
    descriptionField,
    iconField,
    labelField = "label",
    valueField = "value",
  } = requestOption;

  if (!items) {
    console.warn(
      "Select options: data items are empty, please check the itemsField."
    );
    return [];
  }

  return items.map((item) => {
    if (!has(item, labelField as PropertyPath)) {
      console.error(
        `labelField: ${labelField?.toString()} not found in response data items.`
      );
      return { label: "", value: "" };
    }
    if (!has(item, valueField as PropertyPath)) {
      console.error(
        `valueField: ${valueField?.toString()} not found in response data items.`
      );
      return { label: "", value: "" };
    }

    const option: SelectOption = {
      label: get(item, labelField) as string,
      value: get(item, valueField) as string,
    };

    setStringMetadata(option, "icon", item, iconField);
    setStringMetadata(option, "description", item, descriptionField);

    return option;
  });
}

export function isSelectOptionMatched(option: SelectOption, keyword: string) {
  const normalizedKeyword = keyword.toLocaleLowerCase();
  return [option.label, option.description]
    .filter(Boolean)
    .some((text) =>
      text?.toString().toLocaleLowerCase().includes(normalizedKeyword)
    );
}

function setStringMetadata(
  option: SelectOption,
  key: "description" | "icon",
  item: object,
  field?: PropertyPath
) {
  if (!field || !has(item, field)) {
    return;
  }

  const value = get(item, field);
  if (typeof value === "string" && value) {
    option[key] = value;
  }
}

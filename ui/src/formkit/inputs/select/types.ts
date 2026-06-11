import type { PropertyPath } from "lodash-es";

export interface SelectOption<Value = string> extends Record<string, unknown> {
  label: string;
  value: Value;
  icon?: string;
  description?: string;
  attrs?: {
    disabled?: boolean;
  } & Record<string, unknown>;
}

export interface SelectResponse {
  options: SelectOption[];
  page: number;
  size: number;
  total: number;
}

export interface SelectRemoteRequest {
  keyword: string;
  page: number;
  size: number;
}

export interface SelectRemoteOption {
  search: ({
    keyword,
    page,
    size,
  }: SelectRemoteRequest) => Promise<SelectResponse>;

  findOptionsByValues: (values: string[]) => Promise<SelectOption[]>;
}

export interface SelectActionRequest {
  method?: "GET" | "POST";

  /**
   * Parses the returned data from the request.
   */
  parseData?: (data: unknown) => SelectResponse;

  /**
   * Field name for the page number in the request parameters, default is `page`.
   */
  pageField?: PropertyPath;

  /**
   * Field name for size, default is `size`.
   */
  sizeField?: PropertyPath;

  /**
   * Field name for total, default is `total`.
   */
  totalField?: PropertyPath;

  /**
   * Field name for items, default is `items`.
   */
  itemsField?: PropertyPath;

  /**
   * Field name for label, default is `label`.
   */
  labelField?: PropertyPath;

  /**
   * Field name for value, default is `value`.
   */
  valueField?: PropertyPath;

  /**
   * Field name for option icon image source.
   */
  iconField?: PropertyPath;

  /**
   * Field name for secondary option description.
   */
  descriptionField?: PropertyPath;

  /**
   * When using value to query detailed information, the default query
   * parameter key for fieldSelector is `metadata.name`.
   */
  fieldSelectorKey?: PropertyPath;
}

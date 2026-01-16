export interface SecretFormState {
  description?: string;
  stringDataArray: { key: string; value: string }[];
}

export interface RequiredKey {
  key: string;
  description?: string;
}

export interface PagesPublicState {
  functionalPages: FunctionalPagesState[];
}

export interface FunctionalPagesState {
  name: string;
  path: string;
  url?: string;
  permissions?: Array<string>;
}

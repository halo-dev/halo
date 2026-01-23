import type { AxiosResponse } from "axios";

type ListResponse<TItem> = {
  items: TItem[];
  hasNext: boolean;
};

/**
 * Query all items from the list function.
 *
 * @param listFn - The function to list the items.
 * @param params - The parameters to list the items.
 * @returns The items.
 */
export async function paginate<TParams extends { page?: number }, TItem>(
  listFn: (params: TParams) => Promise<AxiosResponse<ListResponse<TItem>>>,
  params?: Omit<TParams, "page">
): Promise<TItem[]> {
  const result: TItem[] = [];
  let page = 1;
  let hasNext = true;

  while (hasNext) {
    const { data } = await listFn({
      ...params,
      page,
    } as TParams);
    result.push(...data.items);
    page += 1;
    hasNext = data.hasNext;
  }

  return result;
}

import { describe, expect, it } from "vitest";
import { formatDatetime, toDatetimeLocal, toISOString } from "../date";

describe("date#formatDatetime", () => {
  it("should return formatted datetime", () => {
    const formattedDatetime = formatDatetime(
      "2022-08-17T06:01:16.511575Z",
      "Asia/Shanghai"
    );

    expect(formattedDatetime).toEqual("2022-08-17 14:01");
  });
});

describe("date#toISOString", () => {
  it("should return ISO string", () => {
    const currentDate = new Date();

    const isoString = toISOString(currentDate);

    expect(isoString).toEqual(currentDate.toISOString());
  });
});

describe("date#toDatetimeLocal", () => {
  it("should return datetime local", () => {
    const datetimeLocal = toDatetimeLocal(
      "2022-08-17T06:01:00.000Z",
      "Asia/Shanghai"
    );

    expect(datetimeLocal).toEqual("2022-08-17T14:01");
  });
});

const textClassification = {
  label: "block text-sm font-medium text-gray-700 formkit-invalid:text-red-500",
  wrapper: "flex flex-col gap-4",
  inner:
    "inline-flex items-center w-full relative box-border border border-gray-300 formkit-invalid:border-red-500 h-9 rounded-base overflow-hidden focus-within:border-primary focus-within:shadow-sm w-full sm:max-w-lg transition-all",
  input:
    "outline-0 bg-white antialiased resize-none w-full text-black block transition-all appearance-none h-full px-3 text-sm",
};

const boxClassification = {
  label: textClassification.label,
  legend: `${textClassification.label} px-2`,
  fieldset:
    "group border border-gray-300 rounded-base px-2 py-2 focus-within:border-primary max-w-lg",
  wrapper:
    "flex items-center mb-1 cursor-pointer group-[.formkit-fieldset]:px-2",
  help: "mb-2 mt-0 px-2",
  input:
    "form-check-input h-4 w-4 mr-2 border border-gray-500 rounded-sm bg-white checked:bg-primary focus:outline-none focus:ring-0 transition duration-200",
  inner: "flex items-center",
};

const buttonClassification = {
  wrapper: "mb-1",
  input:
    "bg-blue-500 hover:bg-blue-700 text-white text-sm font-normal py-3 px-5 rounded",
};

const theme: Record<string, Record<string, string>> = {
  global: {
    form: "divide-y divide-gray-100",
    outer:
      "formkit-disabled:opacity-50 py-4 first:pt-0 last:pb-0 transition-all",
    help: "text-xs mt-2 text-gray-500",
    messages: "list-none p-0 mt-1.5 mb-0 transition-all",
    message: "text-red-500 mt-2 text-xs",
  },
  button: buttonClassification,
  color: {
    ...textClassification,
    inner: "",
    input:
      "w-16 h-8 appearance-none cursor-pointer border border-gray-300 rounded-md mb-2 p-1",
  },
  file: {
    ...textClassification,
    noFiles: "block text-gray-800 text-sm mb-1",
    fileItem: "block flex text-gray-800 text-sm mb-1",
    fileRemove: "ml-auto text-blue-500 text-sm",
  },
  checkbox: boxClassification,
  radio: {
    ...boxClassification,
    input: boxClassification.input.replace("rounded-sm", "rounded-full"),
  },
  range: {
    ...textClassification,
    inner: "w-full sm:max-w-lg",
    input:
      "form-range appearance-none w-full h-2 p-0 bg-gray-200 rounded-full focus:outline-none focus:ring-0 focus:shadow-none",
  },
  search: textClassification,
  select: textClassification,
  submit: buttonClassification,
  tel: textClassification,
  text: textClassification,
  time: textClassification,
  url: textClassification,
  week: textClassification,
  month: textClassification,
  number: textClassification,
  password: textClassification,
  email: textClassification,
  date: textClassification,
  "datetime-local": textClassification,
  textarea: {
    ...textClassification,
    inner: textClassification.inner.replace("h-9", "h-full"),
    input:
      textClassification.input.replace("resize-none", "resize-y") + " py-2",
  },
  repeater: {
    label: textClassification.label,
    legend: `${textClassification.label} px-2`,
    fieldset: boxClassification.fieldset,
    wrapper: boxClassification.wrapper,
    help: boxClassification.wrapper,
    inner: "flex flex-col gap-4",
    items: "flex flex-col w-full gap-2 rounded-base",
    item: "border rounded-base grid grid-cols-12 focus-within:border-primary transition-all overflow-visible focus-within:shadow-sm",
    content: "flex-1 p-2 col-span-11 divide-y divide-gray-100",
    controls: "bg-gray-200 col-span-1 flex items-center justify-center",
  },
  group: {
    label: textClassification.label,
    legend: `${textClassification.label} px-2`,
    fieldset: boxClassification.fieldset,
    wrapper: boxClassification.wrapper,
    help: boxClassification.wrapper,
    inner: "flex flex-col px-2 divide-y divide-gray-100",
  },
  tagSelect: {
    ...textClassification,
    inner: `${textClassification.inner} !overflow-visible !h-auto min-h-[2.25rem]`,
    input: `w-0 flex-grow outline-0 bg-transparent py-1 px-3 block transition-all appearance-none text-sm antialiased`,
    "post-tags-wrapper": "flex w-full items-center",
    "post-tags": "flex w-full flex-wrap items-center",
    "post-tag-wrapper": "inline-flex items-center p-1",
    "post-tag-close":
      "h-4 w-4 cursor-pointer text-gray-600 hover:text-gray-900",
    "post-tags-button": "inline-flex h-full cursor-pointer items-center px-1",
    "dropdown-wrapper":
      "absolute ring-1 ring-gray-100 top-full bottom-auto right-0 z-10 mt-1 max-h-96 w-full overflow-auto rounded bg-white shadow-lg",
  },
  categorySelect: {
    ...textClassification,
    inner: `${textClassification.inner} !overflow-visible !h-auto min-h-[2.25rem]`,
    input: `w-0 flex-grow outline-0 bg-transparent py-1 px-3 block transition-all appearance-none text-sm antialiased`,
    "post-categories-wrapper": "flex w-full items-center",
    "post-categories": "flex w-full flex-wrap items-center",
    "post-categories-button":
      "inline-flex h-full cursor-pointer items-center px-1",
    "dropdown-wrapper":
      "absolute ring-1 ring-gray-100 top-full bottom-auto right-0 z-10 mt-1 max-h-96 w-full overflow-auto rounded bg-white shadow-lg",
  },
};

export default theme;

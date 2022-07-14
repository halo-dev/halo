const textClassification = {
  label: "block font-bold text-sm formkit-invalid:text-red-500 w-56",
  wrapper: "flex flex-col sm:flex-row items-start sm:items-center",
  inner:
    "inline-flex items-center w-full relative box-border border border-gray-300 formkit-invalid:border-red-500 rounded-base overflow-hidden focus-within:border-primary mt-2 sm:mt-0",
  input:
    "outline-0 bg-white antialiased resize-none w-full text-black block transition-all appearance-none h-9 px-3 text-sm",
};

const boxClassification = {
  fieldset: "border border-gray-400 rounded-md px-2 pb-1",
  legend: "font-bold text-sm",
  wrapper: "flex items-center mb-1 cursor-pointer",
  help: "mb-2",
  input:
    "form-check-input appearance-none h-5 w-5 mr-2 border border-gray-500 rounded-sm bg-white checked:bg-blue-500 focus:outline-none focus:ring-0 transition duration-200",
  label: "text-sm text-gray-700 mt-1",
};

const buttonClassification = {
  wrapper: "mb-1",
  input:
    "bg-blue-500 hover:bg-blue-700 text-white text-sm font-normal py-3 px-5 rounded",
};

const theme: Record<string, Record<string, string>> = {
  global: {
    outer: "formkit-disabled:opacity-50",
    help: "text-xs text-gray-500",
    messages: "list-none p-0 mt-1 mb-0",
    message: "text-red-500 mb-1 text-xs",
    form: "flex flex-col space-y-4",
  },
  button: buttonClassification,
  color: {
    label: "block mb-1 font-bold text-sm",
    input:
      "w-16 h-8 appearance-none cursor-pointer border border-gray-300 rounded-md mb-2 p-1",
  },
  file: {
    label: "block mb-1 font-bold text-sm",
    inner: "cursor-pointer",
    input:
      "text-gray-600 text-sm mb-1 file:mr-4 file:py-2 file:px-4 file:rounded-full file:border-0 file:text-sm file:bg-blue-500 file:text-white hover:file:bg-blue-600",
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
    inner: "",
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
    input:
      "outline-0 bg-white antialiased w-full text-black block transition-all appearance-none h-32 px-3 py-2 text-sm",
  },
};

export default theme;

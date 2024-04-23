import { common, createLowlight } from "lowlight";
import xml from "highlight.js/lib/languages/xml";
import dart from "highlight.js/lib/languages/dart";

const lowlight = createLowlight(common);
lowlight.register("html", xml);
lowlight.register("dart", dart);
export default lowlight;

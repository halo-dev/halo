import { common, createLowlight } from "lowlight";
import xml from "highlight.js/lib/languages/xml";

const lowlight = createLowlight(common);
lowlight.register("html", xml);
export default lowlight;

// CodeMirror, copyright (c) by Marijn Haverbeke and others
// Distributed under an MIT license: http://codemirror.net/LICENSE

(function(mod) {
  if (typeof exports == "object" && typeof module == "object") // CommonJS
    mod(require("../../lib/codemirror"));
  else if (typeof define == "function" && define.amd) // AMD
    define(["../../lib/codemirror"], mod);
  else // Plain browser env
    mod(CodeMirror);
})(function(CodeMirror) {
  "use strict";

  CodeMirror.defineMode("stylus", function(config) {

    var operatorsRegexp = /^(\?:?|\+[+=]?|-[\-=]?|\*[\*=]?|\/=?|[=!:\?]?=|<=?|>=?|%=?|&&|\|=?|\~|!|\^|\\)/,
        delimitersRegexp = /^(?:[()\[\]{},:`=;]|\.\.?\.?)/,
        wordOperatorsRegexp = wordRegexp(wordOperators),
        commonKeywordsRegexp = wordRegexp(commonKeywords),
        commonAtomsRegexp = wordRegexp(commonAtoms),
        commonDefRegexp = wordRegexp(commonDef),
        vendorPrefixesRegexp = new RegExp(/^\-(moz|ms|o|webkit)-/),
        cssValuesWithBracketsRegexp = new RegExp("^(" + cssValuesWithBrackets_.join("|") + ")\\([\\w\-\\#\\,\\.\\%\\s\\(\\)]*\\)");

    var tokenBase = function(stream, state) {

      if (stream.eatSpace()) return null;

      var ch = stream.peek();

      // Single line Comment
      if (stream.match('//')) {
        stream.skipToEnd();
        return "comment";
      }

      // Multiline Comment
      if (stream.match('/*')) {
        state.tokenizer = multilineComment;
        return state.tokenizer(stream, state);
      }

      // Strings
      if (ch === '"' || ch === "'") {
        stream.next();
        state.tokenizer = buildStringTokenizer(ch);
        return "string";
      }

      // Def
      if (ch === "@") {
        stream.next();
        if (stream.match(/extend/)) {
          dedent(state); // remove indentation after selectors
        } else if (stream.match(/media[\w-\s]*[\w-]/)) {
          indent(state);
        } else if(stream.eatWhile(/[\w-]/)) {
          if(stream.current().match(commonDefRegexp)) {
            indent(state);
          }
        }
        return "def";
      }

      // Number
      if (stream.match(/^-?[0-9\.]/, false)) {

        // Floats
        if (stream.match(/^-?\d*\.\d+(e[\+\-]?\d+)?/i) || stream.match(/^-?\d+\.\d*/)) {

          // Prevent from getting extra . on 1..
          if (stream.peek() == ".") {
            stream.backUp(1);
          }
          // Units
          stream.eatWhile(/[a-z%]/i);
          return "number";
        }
        // Integers
        if (stream.match(/^-?[1-9]\d*(e[\+\-]?\d+)?/) || stream.match(/^-?0(?![\dx])/i)) {
          // Units
          stream.eatWhile(/[a-z%]/i);
          return "number";
        }
      }

      // Hex color and id selector
      if (ch === "#") {
        stream.next();

        // Hex color
        if (stream.match(/^[0-9a-f]{6}|[0-9a-f]{3}/i)) {
          return "atom";
        }

        // ID selector
        if (stream.match(/^[\w-]+/i)) {
          indent(state);
          return "builtin";
        }
      }

      // Vendor prefixes
      if (stream.match(vendorPrefixesRegexp)) {
        return "meta";
      }

      // Gradients and animation as CSS value
      if (stream.match(cssValuesWithBracketsRegexp)) {
        return "atom";
      }

      // Mixins / Functions with indentation
      if (stream.sol() && stream.match(/^\.?[a-z][\w-]*\(/i)) {
        stream.backUp(1);
        indent(state);
        return "keyword";
      }

      // Mixins / Functions
      if (stream.match(/^\.?[a-z][\w-]*\(/i)) {
        stream.backUp(1);
        return "keyword";
      }

      // +Block mixins
      if (stream.match(/^(\+|\-)[a-z][\w-]+\(/i)) {
        stream.backUp(1);
        indent(state);
        return "keyword";
      }

      // url tokens
      if (stream.match(/^url/) && stream.peek() === "(") {
        state.tokenizer = urlTokens;
        if(!stream.peek()) {
          state.cursorHalf = 0;
        }
        return "atom";
      }

      // Class
      if (stream.match(/^\.[a-z][\w-]*/i)) {
        indent(state);
        return "qualifier";
      }

      // & Parent Reference with BEM naming
      if (stream.match(/^(_|__|-|--)[a-z0-9-]+/)) {
        return "qualifier";
      }

      // Pseudo elements/classes
      if (ch == ':' && stream.match(/^::?[\w-]+/)) {
        indent(state);
        return "variable-3";
      }

      // Conditionals
      if (stream.match(wordRegexp(["for", "if", "else", "unless"]))) {
        indent(state);
        return "keyword";
      }

      // Keywords
      if (stream.match(commonKeywordsRegexp)) {
        return "keyword";
      }

      // Atoms
      if (stream.match(commonAtomsRegexp)) {
        return "atom";
      }

      // Variables
      if (stream.match(/^\$?[a-z][\w-]+\s?=(\s|[\w-'"\$])/i)) {
        stream.backUp(2);
        var cssPropertie = stream.current().toLowerCase().match(/[\w-]+/)[0];
        return cssProperties[cssPropertie] === undefined ? "variable-2" : "property";
      } else if (stream.match(/\$[\w-\.]+/i)) {
        return "variable-2";
      } else if (stream.match(/\$?[\w-]+\.[\w-]+/i)) {
        var cssTypeSelector = stream.current().toLowerCase().match(/[\w]+/)[0];
        if(cssTypeSelectors[cssTypeSelector] === undefined) {
          return "variable-2";
        } else stream.backUp(stream.current().length);
      }

      // !important
      if (ch === "!") {
        stream.next();
        return stream.match(/^[\w]+/) ? "keyword": "operator";
      }

      // / Root Reference
      if (stream.match(/^\/(:|\.|#|[a-z])/)) {
        stream.backUp(1);
        return "variable-3";
      }

      // Operators and delimiters
      if (stream.match(operatorsRegexp) || stream.match(wordOperatorsRegexp)) {
        return "operator";
      }
      if (stream.match(delimitersRegexp)) {
        return null;
      }

      // & Parent Reference
      if (ch === "&") {
        stream.next();
        return "variable-3";
      }

      // Font family
      if (stream.match(/^[A-Z][a-z0-9-]+/)) {
        return "string";
      }

      // CSS rule
      // NOTE: Some css selectors and property values have the same name
      // (embed, menu, pre, progress, sub, table),
      // so they will have the same color (.cm-atom).
      if (stream.match(/[\w-]*/i)) {

        var word = stream.current().toLowerCase();

        if(cssProperties[word] !== undefined) {
          // CSS property
          if(!stream.eol())
            return "property";
          else
            return "variable-2";

        } else if(cssValues[word] !== undefined) {
          // CSS value
          return "atom";

        } else if(cssTypeSelectors[word] !== undefined) {
          // CSS type selectors
          indent(state);
          return "tag";

        } else if(word) {
          // By default variable-2
          return "variable-2";
        }
      }

      // Handle non-detected items
      stream.next();
      return null;

    };

    var tokenLexer = function(stream, state) {

      if (stream.sol()) {
        state.indentCount = 0;
      }

      var style = state.tokenizer(stream, state);
      var current = stream.current();

      if (stream.eol() && (current === "}" || current === ",")) {
        dedent(state);
      }

      if (style !== null) {
        var startOfToken = stream.pos - current.length;
        var withCurrentIndent = startOfToken + (config.indentUnit * state.indentCount);

        var newScopes = [];

        for (var i = 0; i < state.scopes.length; i++) {
          var scope = state.scopes[i];

          if (scope.offset <= withCurrentIndent) {
            newScopes.push(scope);
          }
        }

        state.scopes = newScopes;
      }

      return style;
    };

    return {
      startState: function() {
        return {
          tokenizer: tokenBase,
          scopes: [{offset: 0, type: 'styl'}]
        };
      },

      token: function(stream, state) {
        var style = tokenLexer(stream, state);
        state.lastToken = { style: style, content: stream.current() };
        return style;
      },

      indent: function(state) {
        return state.scopes[0].offset;
      },

      lineComment: "//",
      fold: "indent"

    };

    function urlTokens(stream, state) {
      var ch = stream.peek();

      if (ch === ")") {
        stream.next();
        state.tokenizer = tokenBase;
        return "operator";
      } else if (ch === "(") {
        stream.next();
        stream.eatSpace();

        return "operator";
      } else if (ch === "'" || ch === '"') {
        state.tokenizer = buildStringTokenizer(stream.next());
        return "string";
      } else {
        state.tokenizer = buildStringTokenizer(")", false);
        return "string";
      }
    }

    function multilineComment(stream, state) {
      if (stream.skipTo("*/")) {
        stream.next();
        stream.next();
        state.tokenizer = tokenBase;
      } else {
        stream.next();
      }
      return "comment";
    }

    function buildStringTokenizer(quote, greedy) {

      if(greedy == null) {
        greedy = true;
      }

      function stringTokenizer(stream, state) {
        var nextChar = stream.next();
        var peekChar = stream.peek();
        var previousChar = stream.string.charAt(stream.pos-2);

        var endingString = ((nextChar !== "\\" && peekChar === quote) ||
                            (nextChar === quote && previousChar !== "\\"));

        if (endingString) {
          if (nextChar !== quote && greedy) {
            stream.next();
          }
          state.tokenizer = tokenBase;
          return "string";
        } else if (nextChar === "#" && peekChar === "{") {
          state.tokenizer = buildInterpolationTokenizer(stringTokenizer);
          stream.next();
          return "operator";
        } else {
          return "string";
        }
      }

      return stringTokenizer;
    }

    function buildInterpolationTokenizer(currentTokenizer) {
      return function(stream, state) {
        if (stream.peek() === "}") {
          stream.next();
          state.tokenizer = currentTokenizer;
          return "operator";
        } else {
          return tokenBase(stream, state);
        }
      };
    }

    function indent(state) {
      if (state.indentCount == 0) {
        state.indentCount++;
        var lastScopeOffset = state.scopes[0].offset;
        var currentOffset = lastScopeOffset + config.indentUnit;
        state.scopes.unshift({ offset:currentOffset });
      }
    }

    function dedent(state) {
      if (state.scopes.length == 1) { return true; }
      state.scopes.shift();
    }

  });

  // https://developer.mozilla.org/en-US/docs/Web/HTML/Element
  var cssTypeSelectors_ = ["a","abbr","address","area","article","aside","audio", "b", "base","bdi","bdo","bgsound","blockquote","body","br","button","canvas","caption","cite","code","col","colgroup","data","datalist","dd","del","details","dfn","div","dl","dt","em","embed","fieldset","figcaption","figure","footer","form","h1","h2","h3","h4","h5","h6","head","header","hgroup","hr","html","i","iframe","img","input","ins","kbd","keygen","label","legend","li","link","main","map","mark","marquee","menu","menuitem","meta","meter","nav","nobr","noframes","noscript","object","ol","optgroup","option","output","p","param","pre","progress","q","rp","rt","ruby","s","samp","script","section","select","small","source","span","strong","style","sub","summary","sup","table","tbody","td","textarea","tfoot","th","thead","time","title","tr","track","u","ul","var","video","wbr"];
  // https://github.com/csscomb/csscomb.js/blob/master/config/zen.json
  var cssProperties_ = ["position","top","right","bottom","left","z-index","display","visibility","flex-direction","flex-order","flex-pack","float","clear","flex-align","overflow","overflow-x","overflow-y","overflow-scrolling","clip","box-sizing","margin","margin-top","margin-right","margin-bottom","margin-left","padding","padding-top","padding-right","padding-bottom","padding-left","min-width","min-height","max-width","max-height","width","height","outline","outline-width","outline-style","outline-color","outline-offset","border","border-spacing","border-collapse","border-width","border-style","border-color","border-top","border-top-width","border-top-style","border-top-color","border-right","border-right-width","border-right-style","border-right-color","border-bottom","border-bottom-width","border-bottom-style","border-bottom-color","border-left","border-left-width","border-left-style","border-left-color","border-radius","border-top-left-radius","border-top-right-radius","border-bottom-right-radius","border-bottom-left-radius","border-image","border-image-source","border-image-slice","border-image-width","border-image-outset","border-image-repeat","border-top-image","border-right-image","border-bottom-image","border-left-image","border-corner-image","border-top-left-image","border-top-right-image","border-bottom-right-image","border-bottom-left-image","background","filter:progid:DXImageTransform\\.Microsoft\\.AlphaImageLoader","background-color","background-image","background-attachment","background-position","background-position-x","background-position-y","background-clip","background-origin","background-size","background-repeat","box-decoration-break","box-shadow","color","table-layout","caption-side","empty-cells","list-style","list-style-position","list-style-type","list-style-image","quotes","content","counter-increment","counter-reset","writing-mode","vertical-align","text-align","text-align-last","text-decoration","text-emphasis","text-emphasis-position","text-emphasis-style","text-emphasis-color","text-indent","-ms-text-justify","text-justify","text-outline","text-transform","text-wrap","text-overflow","text-overflow-ellipsis","text-overflow-mode","text-size-adjust","text-shadow","white-space","word-spacing","word-wrap","word-break","tab-size","hyphens","letter-spacing","font","font-weight","font-style","font-variant","font-size-adjust","font-stretch","font-size","font-family","src","line-height","opacity","filter:\\\\\\\\'progid:DXImageTransform.Microsoft.Alpha","filter:progid:DXImageTransform.Microsoft.Alpha\\(Opacity","interpolation-mode","filter","resize","cursor","nav-index","nav-up","nav-right","nav-down","nav-left","transition","transition-delay","transition-timing-function","transition-duration","transition-property","transform","transform-origin","animation","animation-name","animation-duration","animation-play-state","animation-timing-function","animation-delay","animation-iteration-count","animation-direction","pointer-events","unicode-bidi","direction","columns","column-span","column-width","column-count","column-fill","column-gap","column-rule","column-rule-width","column-rule-style","column-rule-color","break-before","break-inside","break-after","page-break-before","page-break-inside","page-break-after","orphans","widows","zoom","max-zoom","min-zoom","user-zoom","orientation","text-rendering","speak","animation-fill-mode","backface-visibility","user-drag","user-select","appearance"];
  // https://github.com/codemirror/CodeMirror/blob/master/mode/css/css.js#L501
  var cssValues_ = ["above","absolute","activeborder","activecaption","afar","after-white-space","ahead","alias","all","all-scroll","alternate","always","amharic","amharic-abegede","antialiased","appworkspace","arabic-indic","armenian","asterisks","auto","avoid","avoid-column","avoid-page","avoid-region","background","backwards","baseline","below","bidi-override","binary","bengali","block","block-axis","bold","bolder","border","border-box","both","bottom","break","break-all","break-word","button-bevel","buttonface","buttonhighlight","buttonshadow","buttontext","cambodian","capitalize","caps-lock-indicator","captiontext","caret","cell","center","checkbox","circle","cjk-earthly-branch","cjk-heavenly-stem","cjk-ideographic","clear","clip","close-quote","col-resize","collapse","column","compact","condensed","contain","content","content-box","context-menu","continuous","copy","cover","crop","cross","crosshair","currentcolor","cursive","dashed","decimal","decimal-leading-zero","default","default-button","destination-atop","destination-in","destination-out","destination-over","devanagari","disc","discard","document","dot-dash","dot-dot-dash","dotted","double","down","e-resize","ease","ease-in","ease-in-out","ease-out","element","ellipse","ellipsis","embed","end","ethiopic","ethiopic-abegede","ethiopic-abegede-am-et","ethiopic-abegede-gez","ethiopic-abegede-ti-er","ethiopic-abegede-ti-et","ethiopic-halehame-aa-er","ethiopic-halehame-aa-et","ethiopic-halehame-am-et","ethiopic-halehame-gez","ethiopic-halehame-om-et","ethiopic-halehame-sid-et","ethiopic-halehame-so-et","ethiopic-halehame-ti-er","ethiopic-halehame-ti-et","ethiopic-halehame-tig","ew-resize","expanded","extra-condensed","extra-expanded","fantasy","fast","fill","fixed","flat","footnotes","forwards","from","geometricPrecision","georgian","graytext","groove","gujarati","gurmukhi","hand","hangul","hangul-consonant","hebrew","help","hidden","hide","higher","highlight","highlighttext","hiragana","hiragana-iroha","horizontal","hsl","hsla","icon","ignore","inactiveborder","inactivecaption","inactivecaptiontext","infinite","infobackground","infotext","inherit","initial","inline","inline-axis","inline-block","inline-table","inset","inside","intrinsic","invert","italic","justify","kannada","katakana","katakana-iroha","keep-all","khmer","landscape","lao","large","larger","left","level","lighter","line-through","linear","lines","list-item","listbox","listitem","local","logical","loud","lower","lower-alpha","lower-armenian","lower-greek","lower-hexadecimal","lower-latin","lower-norwegian","lower-roman","lowercase","ltr","malayalam","match","media-controls-background","media-current-time-display","media-fullscreen-button","media-mute-button","media-play-button","media-return-to-realtime-button","media-rewind-button","media-seek-back-button","media-seek-forward-button","media-slider","media-sliderthumb","media-time-remaining-display","media-volume-slider","media-volume-slider-container","media-volume-sliderthumb","medium","menu","menulist","menulist-button","menulist-text","menulist-textfield","menutext","message-box","middle","min-intrinsic","mix","mongolian","monospace","move","multiple","myanmar","n-resize","narrower","ne-resize","nesw-resize","no-close-quote","no-drop","no-open-quote","no-repeat","none","normal","not-allowed","nowrap","ns-resize","nw-resize","nwse-resize","oblique","octal","open-quote","optimizeLegibility","optimizeSpeed","oriya","oromo","outset","outside","outside-shape","overlay","overline","padding","padding-box","painted","page","paused","persian","plus-darker","plus-lighter","pointer","polygon","portrait","pre","pre-line","pre-wrap","preserve-3d","progress","push-button","radio","read-only","read-write","read-write-plaintext-only","rectangle","region","relative","repeat","repeat-x","repeat-y","reset","reverse","rgb","rgba","ridge","right","round","row-resize","rtl","run-in","running","s-resize","sans-serif","scroll","scrollbar","se-resize","searchfield","searchfield-cancel-button","searchfield-decoration","searchfield-results-button","searchfield-results-decoration","semi-condensed","semi-expanded","separate","serif","show","sidama","single","skip-white-space","slide","slider-horizontal","slider-vertical","sliderthumb-horizontal","sliderthumb-vertical","slow","small-caps","small-caption","smaller","solid","somali","source-atop","source-in","source-out","source-over","space","square","square-button","start","static","status-bar","stretch","stroke","sub","subpixel-antialiased","super","sw-resize","table","table-caption","table-cell","table-column","table-column-group","table-footer-group","table-header-group","table-row","table-row-group","telugu","text","text-bottom","text-top","textfield","thai","thick","thin","threeddarkshadow","threedface","threedhighlight","threedlightshadow","threedshadow","tibetan","tigre","tigrinya-er","tigrinya-er-abegede","tigrinya-et","tigrinya-et-abegede","to","top","transparent","ultra-condensed","ultra-expanded","underline","up","upper-alpha","upper-armenian","upper-greek","upper-hexadecimal","upper-latin","upper-norwegian","upper-roman","uppercase","urdu","url","vertical","vertical-text","visible","visibleFill","visiblePainted","visibleStroke","visual","w-resize","wait","wave","wider","window","windowframe","windowtext","x-large","x-small","xor","xx-large","xx-small","bicubic","optimizespeed","grayscale"];
  var cssColorValues_ = ["aliceblue","antiquewhite","aqua","aquamarine","azure","beige","bisque","black","blanchedalmond","blue","blueviolet","brown","burlywood","cadetblue","chartreuse","chocolate","coral","cornflowerblue","cornsilk","crimson","cyan","darkblue","darkcyan","darkgoldenrod","darkgray","darkgreen","darkkhaki","darkmagenta","darkolivegreen","darkorange","darkorchid","darkred","darksalmon","darkseagreen","darkslateblue","darkslategray","darkturquoise","darkviolet","deeppink","deepskyblue","dimgray","dodgerblue","firebrick","floralwhite","forestgreen","fuchsia","gainsboro","ghostwhite","gold","goldenrod","gray","grey","green","greenyellow","honeydew","hotpink","indianred","indigo","ivory","khaki","lavender","lavenderblush","lawngreen","lemonchiffon","lightblue","lightcoral","lightcyan","lightgoldenrodyellow","lightgray","lightgreen","lightpink","lightsalmon","lightseagreen","lightskyblue","lightslategray","lightsteelblue","lightyellow","lime","limegreen","linen","magenta","maroon","mediumaquamarine","mediumblue","mediumorchid","mediumpurple","mediumseagreen","mediumslateblue","mediumspringgreen","mediumturquoise","mediumvioletred","midnightblue","mintcream","mistyrose","moccasin","navajowhite","navy","oldlace","olive","olivedrab","orange","orangered","orchid","palegoldenrod","palegreen","paleturquoise","palevioletred","papayawhip","peachpuff","peru","pink","plum","powderblue","purple","red","rosybrown","royalblue","saddlebrown","salmon","sandybrown","seagreen","seashell","sienna","silver","skyblue","slateblue","slategray","snow","springgreen","steelblue","tan","teal","thistle","tomato","turquoise","violet","wheat","white","whitesmoke","yellow","yellowgreen"];
  var cssValuesWithBrackets_ = ["gradient","linear-gradient","radial-gradient","repeating-linear-gradient","repeating-radial-gradient","cubic-bezier","translateX","translateY","translate3d","rotate3d","scale","scale3d","perspective","skewX"];

  var wordOperators = ["in", "and", "or", "not", "is a", "is", "isnt", "defined", "if unless"],
      commonKeywords = ["for", "if", "else", "unless", "return"],
      commonAtoms = ["null", "true", "false", "href", "title", "type", "not-allowed", "readonly", "disabled"],
      commonDef = ["@font-face", "@keyframes", "@media", "@viewport", "@page", "@host", "@supports", "@block", "@css"],
      cssTypeSelectors = keySet(cssTypeSelectors_),
      cssProperties = keySet(cssProperties_),
      cssValues = keySet(cssValues_.concat(cssColorValues_)),
      hintWords = wordOperators.concat(commonKeywords,
                                       commonAtoms,
                                       commonDef,
                                       cssTypeSelectors_,
                                       cssProperties_,
                                       cssValues_,
                                       cssValuesWithBrackets_,
                                       cssColorValues_);

  function wordRegexp(words) {
    return new RegExp("^((" + words.join(")|(") + "))\\b");
  };

  function keySet(array) {
    var keys = {};
    for (var i = 0; i < array.length; ++i) {
      keys[array[i]] = true;
    }
    return keys;
  };

  CodeMirror.registerHelper("hintWords", "stylus", hintWords);
  CodeMirror.defineMIME("text/x-styl", "stylus");

});

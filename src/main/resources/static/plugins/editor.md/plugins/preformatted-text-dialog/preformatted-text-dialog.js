/*!
 * Preformatted text dialog plugin for Editor.md
 *
 * @file        preformatted-text-dialog.js
 * @author      pandao
 * @version     1.2.0
 * @updateTime  2015-03-07
 * {@link       https://github.com/pandao/editor.md}
 * @license     MIT
 */

(function() {

    var factory = function (exports) {
		var cmEditor;
		var pluginName   = "preformatted-text-dialog";

		exports.fn.preformattedTextDialog = function() {

            var _this       = this;
            var cm          = this.cm;
            var lang        = this.lang;
			var editor      = this.editor;
            var settings    = this.settings;
            var cursor      = cm.getCursor();
            var selection   = cm.getSelection();
            var classPrefix = this.classPrefix;
			var dialogLang  = lang.dialog.preformattedText;
			var dialogName  = classPrefix + pluginName, dialog;

			cm.focus();

            if (editor.find("." + dialogName).length > 0)
            {
                dialog = editor.find("." + dialogName);
                dialog.find("textarea").val(selection);

                this.dialogShowMask(dialog);
                this.dialogLockScreen();
                dialog.show();
            }
            else 
            {      
                var dialogContent = "<textarea placeholder=\"coding now....\" style=\"display:none;\">" + selection + "</textarea>";

                dialog = this.createDialog({
                    name   : dialogName,
                    title  : dialogLang.title,
                    width  : 780,
                    height : 540,
                    mask   : settings.dialogShowMask,
                    drag   : settings.dialogDraggable,
                    content : dialogContent,
                    lockScreen : settings.dialogLockScreen,
                    maskStyle  : {
                        opacity         : settings.dialogMaskOpacity,
                        backgroundColor : settings.dialogMaskBgColor
                    },
                    buttons : {
                        enter  : [lang.buttons.enter, function() {
                            var codeTexts  = this.find("textarea").val();

                            if (codeTexts === "")
                            {
                                alert(dialogLang.emptyAlert);
                                return false;
                            }

                            codeTexts = codeTexts.split("\n");

                            for (var i in codeTexts)
                            {
                                codeTexts[i] = "    " + codeTexts[i];
                            }
                            
                            codeTexts = codeTexts.join("\n");
                            
                            if (cursor.ch !== 0) {
                                codeTexts = "\r\n\r\n" + codeTexts;
                            }

                            cm.replaceSelection(codeTexts);

                            this.hide().lockScreen(false).hideMask();

                            return false;
                        }],
                        cancel : [lang.buttons.cancel, function() {                                  
                            this.hide().lockScreen(false).hideMask();

                            return false;
                        }]
                    }
                });
            }
		
			var cmConfig = {
				mode                      : "text/html",
				theme                     : settings.theme,
				tabSize                   : 4,
				autofocus                 : true,
				autoCloseTags             : true,
				indentUnit                : 4,
				lineNumbers               : true,
				lineWrapping              : true,
				extraKeys                 : {"Ctrl-Q": function(cm){ cm.foldCode(cm.getCursor()); }},
				foldGutter                : true,
				gutters                   : ["CodeMirror-linenumbers", "CodeMirror-foldgutter"],
				matchBrackets             : true,
				indentWithTabs            : true,
				styleActiveLine           : true,
				styleSelectedText         : true,
				autoCloseBrackets         : true,
				showTrailingSpace         : true,
				highlightSelectionMatches : true
			};
			
			var textarea = dialog.find("textarea");
			var cmObj    = dialog.find(".CodeMirror");

			if (dialog.find(".CodeMirror").length < 1) 
			{
				cmEditor = exports.$CodeMirror.fromTextArea(textarea[0], cmConfig);
				cmObj    = dialog.find(".CodeMirror");

				cmObj.css({
					"float"   : "none", 
					margin    : "0 0 5px",
					border    : "1px solid #ddd",
					fontSize  : settings.fontSize,
					width     : "100%",
					height    : "410px"
				});

				cmEditor.on("change", function(cm) {
					textarea.val(cm.getValue());
				});
			} 
			else 
			{
				cmEditor.setValue(cm.getSelection());
			}
		};

	};

	// CommonJS/Node.js
	if (typeof require === "function" && typeof exports === "object" && typeof module === "object")
    { 
        module.exports = factory;
    }
	else if (typeof define === "function")  // AMD/CMD/Sea.js
    {
		if (define.amd) { // for Require.js

			define(["editormd"], function(editormd) {
                factory(editormd);
            });

		} else { // for Sea.js
			define(function(require) {
                var editormd = require("./../../editormd");
                factory(editormd);
            });
		}
	} 
	else
	{
        factory(window.editormd);
	}

})();

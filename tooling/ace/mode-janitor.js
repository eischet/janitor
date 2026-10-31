// noinspection JSUnusedGlobalSymbols

/*
 * ACE editor mode for Janitor: syntax highlighting, brace folding, auto-indent, auto-pairing and comment toggling.
 *
 * The highlighting rules follow the lexer in janitor-lang/src/main/antlr4/com/eischet/janitor/lang/Janitor.g4;
 * keep both in sync when the grammar changes.
 *
 * Put this file next to ace.js (or into ACE's modePath) and select the mode with session.setMode("ace/mode/janitor").
 */

ace.define("ace/mode/janitor_highlight_rules",
    ["require", "exports", "module", "ace/lib/oop", "ace/mode/text_highlight_rules"],
    function (require, exports, module) {
        "use strict";

        var oop = require("ace/lib/oop");
        var TextHighlightRules = require("ace/mode/text_highlight_rules").TextHighlightRules;

        // like the lexer's Letter fragment: every character above 0x7F counts as a letter
        var identifier = "[a-zA-Z_$\\u0080-\\uffff][a-zA-Z0-9_$\\u0080-\\uffff]*";
        // digits may be separated by underscores, but neither start nor end with one
        var digits = "\\d(?:[\\d_]*\\d)?";
        var exponent = "[eE][+-]?" + digits;
        var escape = "\\\\(?:[btnfr\"'\\\\/]|[0-3]?[0-7]?[0-7]|u+[0-9a-fA-F]{4})";

        var JanitorHighlightRules = function () {
            var keywordMapper = this.createKeywordMapper({
                "keyword.control": "if|then|else|for|from|to|while|do|break|continue|return|try|catch|finally|throw",
                "keyword.operator": "and|or|not|in",
                "keyword.other": "import|as",
                "storage.type": "function",
                "constant.language": "true|false|null"
            }, "identifier");

            var stringEscapes = [
                {token: "constant.language.escape", regex: escape},
                {token: "invalid.illegal", regex: "\\\\."}
            ];

            this.$rules = {
                "start": [
                    // a "#!" line is only allowed as the first line of a script, but '#' means nothing anywhere else
                    {token: "comment", regex: "^#.*$"},
                    {token: "comment", regex: "\\/\\/.*$"},
                    {token: "comment", regex: "\\/\\*", next: "comment"},

                    {token: "string", regex: '"""', next: "qqqstring"},
                    {token: "string", regex: "'''", next: "qqstring_single"},
                    {token: "string", regex: '"', next: "qstring_double"},
                    {token: "string", regex: "'", next: "qstring_single"},
                    {token: "string.regexp", regex: "re\\/(?:\\\\\\/|[^\\r\\n])*?\\/"},

                    // dates, times and durations; the longer forms first
                    {token: "constant.numeric", regex: "@\\d{4}-\\d?\\d-\\d?\\d-\\d?\\d:\\d\\d(?::\\d\\d)?"},
                    {token: "constant.numeric", regex: "@\\d{4}-\\d?\\d-\\d?\\d"},
                    {token: "constant.numeric", regex: "@\\d+(?:y|mo|w|d|h|mi|s)"},
                    {token: "constant.numeric", regex: "@(?:today|now)\\b"},
                    {token: "invalid.illegal", regex: "@\\S*"},

                    {token: "constant.numeric", regex: "0[xX][0-9a-fA-F](?:[0-9a-fA-F_]*[0-9a-fA-F])?\\b"},
                    {token: "constant.numeric", regex: "0[bB][01](?:[01_]*[01])?\\b"},
                    {token: "constant.numeric", regex: "(?:" + digits + "\\.(?:" + digits + ")?|\\.\\d(?:[\\d_]*\\d)?)(?:" + exponent + ")?[fFdD]?"},
                    {token: "constant.numeric", regex: digits + "(?:" + exponent + "[fFdD]?|[fFdD])"},
                    // octal literals are rejected by the compiler
                    {token: "invalid.illegal", regex: "0_*[0-7][0-7_]*\\b"},
                    {token: "constant.numeric", regex: "0|[1-9](?:[\\d_]*\\d)?"},

                    {token: ["storage.type", "text", "entity.name.function"], regex: "(function)(\\s+)(" + identifier + ")"},
                    // after '.' or '?.' every name is a plain member name, even 'from', 'to' or 'in'
                    {token: ["punctuation.operator", "text", "identifier"], regex: "(\\??\\.)(\\s*)(" + identifier + ")"},
                    {token: keywordMapper, regex: identifier},

                    {token: "keyword.operator", regex: "->|\\*\\*|\\+\\+|--|[-+*/%]=|==|!=|<>|<=|>=|~=|!~|&&|\\|\\||[-+*/%<>=!~]"},
                    {token: "punctuation.operator", regex: "\\?\\.|\\.\\.|[?:,;.]"},
                    {token: "paren.lparen", regex: "[\\[({]"},
                    {token: "paren.rparen", regex: "[\\])}]"},
                    {token: "text", regex: "\\s+"}
                ],
                "comment": [
                    {token: "comment", regex: "\\*\\/", next: "start"},
                    {defaultToken: "comment"}
                ],
                // single-line strings end at the end of the line, even if unterminated
                "qstring_double": stringEscapes.concat([
                    {token: "string", regex: '"|$', next: "start"},
                    {defaultToken: "string"}
                ]),
                "qstring_single": stringEscapes.concat([
                    {token: "string", regex: "'|$", next: "start"},
                    {defaultToken: "string"}
                ]),
                "qqqstring": stringEscapes.concat([
                    {token: "string", regex: '"""', next: "start"},
                    {defaultToken: "string"}
                ]),
                "qqstring_single": stringEscapes.concat([
                    {token: "string", regex: "'''", next: "start"},
                    {defaultToken: "string"}
                ])
            };
            this.normalizeRules();
        };

        oop.inherits(JanitorHighlightRules, TextHighlightRules);

        exports.JanitorHighlightRules = JanitorHighlightRules;
    });

ace.define("ace/mode/folding/janitor",
    ["require", "exports", "module", "ace/lib/oop", "ace/mode/folding/fold_mode"],
    function (require, exports, module) {
        "use strict";

        var oop = require("ace/lib/oop");
        var BaseFoldMode = require("ace/mode/folding/fold_mode").FoldMode;

        // folds bracket blocks and multi-line comments, like ACE's C-style folding
        var FoldMode = exports.FoldMode = function () {
        };
        oop.inherits(FoldMode, BaseFoldMode);

        (function () {
            this.foldingStartMarker = /([\{\[\(])[^\}\]\)]*$|^\s*(\/\*)/;
            this.foldingStopMarker = /^[^\[\{\(]*([\}\]\)])|^[\s\*]*(\*\/)/;

            this.getFoldWidgetRange = function (session, foldStyle, row) {
                var line = session.getLine(row);
                var match = line.match(this.foldingStartMarker);
                if (match) {
                    if (match[1]) {
                        return this.openingBracketBlock(session, match[1], row, match.index);
                    }
                    var range = session.getCommentFoldRange(row, match.index + match[0].length, 1);
                    return range && range.isMultiLine() ? range : undefined;
                }
                if (foldStyle === "markbegin") {
                    return undefined;
                }
                match = line.match(this.foldingStopMarker);
                if (match) {
                    var column = match.index + match[0].length;
                    if (match[1]) {
                        return this.closingBracketBlock(session, match[1], row, column);
                    }
                    return session.getCommentFoldRange(row, column, -1);
                }
                return undefined;
            };
        }).call(FoldMode.prototype);
    });

ace.define("ace/mode/janitor",
    ["require", "exports", "module", "ace/lib/oop", "ace/mode/text", "ace/range", "ace/mode/janitor_highlight_rules",
        "ace/mode/behaviour/cstyle", "ace/mode/folding/janitor"],
    function (require, exports, module) {
        "use strict";

        var oop = require("ace/lib/oop");
        var TextMode = require("ace/mode/text").Mode;
        var Range = require("ace/range").Range;
        var JanitorHighlightRules = require("ace/mode/janitor_highlight_rules").JanitorHighlightRules;
        var CstyleBehaviour = require("ace/mode/behaviour/cstyle").CstyleBehaviour;
        var FoldMode = require("ace/mode/folding/janitor").FoldMode;

        var Mode = function () {
            this.HighlightRules = JanitorHighlightRules;
            this.$behaviour = new CstyleBehaviour();
            this.foldingRules = new FoldMode();
        };
        oop.inherits(Mode, TextMode);

        (function () {
            this.lineCommentStart = "//";
            this.blockComment = {start: "/*", end: "*/"};
            this.$quotes = {'"': '"', "'": "'"};

            // indent after a line that opens a block, list, map or argument list
            this.getNextLineIndent = function (state, line, tab) {
                var indent = this.$getIndent(line);
                var tokens = this.getTokenizer().getLineTokens(line, state).tokens;
                if (tokens.length && tokens[tokens.length - 1].type === "comment") {
                    return indent;
                }
                if (state === "start" && /[\{\[\(]\s*$/.test(line)) {
                    indent += tab;
                }
                return indent;
            };

            // a closing brace typed on an otherwise empty line moves back to the indentation of its opening line
            this.checkOutdent = function (state, line, input) {
                return /^\s+$/.test(line) && /^\s*[\}\]\)]/.test(input);
            };

            this.autoOutdent = function (state, doc, row) {
                var match = doc.getLine(row).match(/^(\s*[\}\]\)])/);
                if (!match) {
                    return;
                }
                var column = match[1].length;
                var openBracePos = doc.findMatchingBracket({row: row, column: column});
                if (!openBracePos || openBracePos.row === row) {
                    return;
                }
                var indent = this.$getIndent(doc.getLine(openBracePos.row));
                doc.replace(new Range(row, 0, row, column - 1), indent);
            };

            this.$id = "ace/mode/janitor";
        }).call(Mode.prototype);

        exports.Mode = Mode;
    });

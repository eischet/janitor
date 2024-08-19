ace.define('ace/mode/janitor',
    ["require", "exports", "module", "ace/lib/oop", "ace/mode/text", "ace/mode/text_highlight_rules", "ace/worker/worker_client"],
    function (require, exports, module) {
        var oop = require("ace/lib/oop");
        var TextMode = require("ace/mode/text").Mode;
        var TextHighlightRules = require("ace/mode/text_highlight_rules").TextHighlightRules;

        var MyHighlightRules = function () {
            var keywordMapper = this.createKeywordMapper({
                "keyword.control": "if|then|else|break|continue|do|while|try|catch|finally|for|return",
                "keyword.operator": "and|or|not",
                "keyword.other": "class|print|assert|import|function",
                "constant.language": "true|false|null"
            }, "identifier");

            this.$rules = {
                "start": [
                    {
                        token : "comment",
                        regex : "\\/\\/.*$"
                    },
                    {
                        token : "comment", // multi line comment
                        regex : "\\/\\*",
                        next : "comment"
                    },
                    {token: "string", regex: /"""/, next: "triple-string"},
                    {token: "string", regex: /'''/, next: "triple-string-s"},
                    {
                        token : "string", // single line
                        regex : '["](?:(?:\\\\.)|(?:[^"\\\\]))*?["]'
                    }, {
                        token : "string", // single line
                        regex : "['](?:(?:\\\\.)|(?:[^'\\\\]))*?[']"
                    },
                    {token: "constant.numeric", regex: "@now|@today|@[0-9]+(?:y|mo|w|d|h|mi|s)|@\\d{4}-\\d\\d-\\d\\d-\\d\\d:\\d\\d(?::\\d\\d)?|@\\d{4}-\\d\\d-\\d\\d"}, // dates!

                    {token: "string", regex: '["](?:(?:\\\\.)|(?:[^"\\\\]))*?["]'},

                    {token: "constant.numeric", regex: "0[xX][0-9a-fA-F]+\\b"},
                    {token: "constant.numeric", regex: "[+-]?\\d+(?:(?:\\.\\d*)?(?:[eE][+-]?\\d+)?)?\\b"},

                    {token: "keyword.operator", regex: "!|%|\\\\|/|\\*|\\-|\\+|~=|==|<>|!=|<=|>=|=|<|>|&&|\\|\\|"},

                    {token: "punctuation.operator", regex: "\\?|\\:|\\,|\\;|\\."},

                    {token: "paren.lparen", regex: "[[({]"},
                    {token: "paren.rparen", regex: "[\\])}]"},

                    {token: "text", regex: "\\s+"},

                    {token: keywordMapper, regex: "[a-zA-Z_$][a-zA-Z0-9_$]*\\b"}
                ],
                "comment": [
                    {token: "comment", regex: "\\*\\/", next: "start"},
                    {defaultToken: "comment"}
                ],
                "triple-string": [
                    {token: "string", regex: /"""/, next: "start"},
                    {token: "variable", regex: "\\$[a-zA-Z_][a-zA-Z0-9_.]+"},
                    {defaultToken: "string"}
                ],
                "triple-string-s": [
                    {token: "string", regex: /'''/, next: "start"},
                    {token: "variable", regex: "\\$[a-zA-Z_][a-zA-Z0-9_.]+"},
                    {defaultToken: "string"}
                ]

            };
        };
        oop.inherits(MyHighlightRules, TextHighlightRules);
        var MyMode = function () {
            this.HighlightRules = MyHighlightRules;
        };
        oop.inherits(MyMode, TextMode);
        (function () {
            this.$id = "ace/mode/janitor";
        }).call(MyMode.prototype);
        exports.Mode = MyMode;
    });
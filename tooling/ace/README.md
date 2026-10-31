# ACE Editor mode for Janitor

The file mode-janitor.js implements a Janitor mode for the [ACE editor](https://ace.c9.io/):

* syntax highlighting for all tokens of the grammar (`janitor-lang/src/main/antlr4/com/eischet/janitor/lang/Janitor.g4`),
  including regex, date and duration literals, escape sequences and the "#!" line
* folding of blocks, lists, maps and multi-line comments
* auto-indent after an opening bracket, outdent on the closing one, auto-pairing of brackets and quotes
* line and block comment toggling (Ctrl+/, Ctrl+Shift+/)
* the keywords for ACE's basic autocompletion (ext-language_tools)

Copy mode-janitor.js next to ace.js (or into ACE's modePath) and select the mode with `session.setMode("ace/mode/janitor")`;
ACE loads the file on demand.

Tested with ACE 1.4.7 and 1.44.0. demo.html shows the mode with all kinds of tokens; serve this folder with any web server
(e.g. `jwebserver -d .`) and open demo.html, demo.html?ace=1.4.7 or demo.html?theme=monokai.

When the grammar changes, update the highlighting rules and the sample code in demo.html.


# Further Reading

https://ace.c9.io/#nav=higlighter

https://github.com/ajaxorg/ace/blob/master/src/mode/javascript_highlight_rules.js

https://github.com/ajaxorg/ace/blob/master/tool/tmtheme.js

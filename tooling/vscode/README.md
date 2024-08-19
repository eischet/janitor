# janitor-vsc README

Rudimentary VS Code extension for Janitor, providing basic syntax highlighting and a file icon.

This has been generated with "yo code" and then modified to cover the language.

# Further Reading

https://neuroning.com/post/implementing-code-completion-for-vscode-with-antlr/

An interesting read, but I do not yet see how a code completer should access custom modules, classes, function that are bound into the runtime. A completer
would need to interact with your own embedding code to make this work. Maybe Janitor could be its own [language server](https://microsoft.github.io/language-server-protocol/),
but implementing that correctly is going to be a lot of work.
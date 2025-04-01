package generated;

import com.intellij.lexer.FlexLexer;
import com.intellij.psi.tree.IElementType;

import static com.intellij.psi.TokenType.BAD_CHARACTER;
import static com.intellij.psi.TokenType.WHITE_SPACE;
import static generated.GeneratedTypes.*;

%%

%{
  public _JanitorLexer() {
    this((java.io.Reader)null);
  }
%}

%public
%class _JanitorLexer
%implements FlexLexer
%function advance
%type IElementType
%unicode

EOL=\R
WHITE_SPACE=\s+


%%
<YYINITIAL> {
  {WHITE_SPACE}                                                     { return WHITE_SPACE; }

  "function"                                                        { return FUNCTION; }
  "break"                                                           { return BREAK; }
  "catch"                                                           { return CATCH; }
  "continue"                                                        { return CONTINUE; }
  "do"                                                              { return DO; }
  "else"                                                            { return ELSE; }
  "finally"                                                         { return FINALLY; }
  "for"                                                             { return FOR; }
  "if"                                                              { return IF; }
  "then"                                                            { return THEN; }
  "import"                                                          { return IMPORT; }
  "return"                                                          { return RETURN; }
  "throw"                                                           { return THROW; }
  "try"                                                             { return TRY; }
  "while"                                                           { return WHILE; }
  "true"                                                            { return TRUE; }
  "false"                                                           { return FALSE; }
  "null"                                                            { return NULL; }
  "from"                                                            { return FROM; }
  "to"                                                              { return TO; }
  "in"                                                              { return IN; }
  "[0-9]+"                                                          { return DECIMAL_LITERAL; }
  "0[xX][0-9a-fA-F]+"                                               { return HEX_LITERAL; }
  "0[0-7]+"                                                         { return OCT_LITERAL; }
  "0[bB][01]+"                                                      { return BINARY_LITERAL; }
  "[0-9]+\\\\.[0-9]*([eE][+-]?[0-9]+)?"                             { return FLOAT_LITERAL; }
  "@\\\\d{4}-\\\\d{2}-\\\\d{2}"                                     { return DATE_LITERAL; }
  "@\\\\d{4}-\\\\d{2}-\\\\d{2}-\\\\d{2}:\\\\d{2}(:\\\\d{2})?"       { return DATE_TIME_LITERAL; }
  "@\\\\d+y"                                                        { return YEARS_LITERAL; }
  "@\\\\d+mo"                                                       { return MONTHS_LITERAL; }
  "@\\\\d+w"                                                        { return WEEKS_LITERAL; }
  "@\\\\d+d"                                                        { return DAYS_LITERAL; }
  "@\\\\d+h"                                                        { return HOURS_LITERAL; }
  "@\\\\d+mi"                                                       { return MINUTES_LITERAL; }
  "@\\\\d+s"                                                        { return SECONDS_LITERAL; }
  "@today"                                                          { return TODAY_LITERAL; }
  "@now"                                                            { return NOW_LITERAL; }
  "'([^'\\\\\\\\]|\\\\\\\\.)*'"                                     { return STRING_LITERAL_SINGLE; }
  "\"([^\"\\\\\\\\]|\\\\\\\\.)*\""                                  { return STRING_LITERAL_DOUBLE; }
  "'''([^\\\\\\\\]|\\\\\\\\.)*?'''"                                 { return STRING_LITERAL_TRIPLE_SINGLE; }
  "\"\"\"([^\\\\\\\\]|\\\\\\\\.)*?\"\"\""                           { return STRING_LITERAL_TRIPLE_DOUBLE; }
  "re/([^/\\\\\\\\]|\\\\\\\\.)*/"                                   { return REGEX_LITERAL; }
  "\\\\("                                                           { return LPAREN; }
  "\\\\)"                                                           { return RPAREN; }
  "\\\\{"                                                           { return LBRACE; }
  "\\\\}"                                                           { return RBRACE; }
  "\\\\["                                                           { return LBRACK; }
  "\\\\]"                                                           { return RBRACK; }
  ";"                                                               { return STMT_TERM; }
  ";"                                                               { return SEMICOLON; }
  ","                                                               { return COMMA; }
  ".."                                                              { return DOUBLE_DOT; }
  "."                                                               { return DOT; }
  "\\\\?."                                                          { return QDOT; }
  "#"                                                               { return HASH; }
  "|"                                                               { return ALT_CATCH; }
  "="                                                               { return ASSIGN; }
  "\\\\+="                                                          { return PLUS_ASSIGN; }
  "-="                                                              { return MINUS_ASSIGN; }
  "\\\\*="                                                          { return MUL_ASSIGN; }
  "/="                                                              { return DIV_ASSIGN; }
  "%="                                                              { return MOD_ASSIGN; }
  ">"                                                               { return GT; }
  "<"                                                               { return LT; }
  "~"                                                               { return MATCH; }
  "!~"                                                              { return MATCH_NOT; }
  "\\\\?"                                                           { return QUESTION; }
  ":"                                                               { return COLON; }
  "=="                                                              { return EQUAL; }
  "<="                                                              { return LE; }
  ">="                                                              { return GE; }
  "!="                                                              { return NOTEQUAL; }
  "<>"                                                              { return ALT_NOTEQUAL; }
  "not"                                                             { return NOT; }
  "!"                                                               { return ALT_NOT; }
  "and"                                                             { return AND; }
  "&&"                                                              { return CAND; }
  "or"                                                              { return OR; }
  "\\\\|\\\\|"                                                      { return COR; }
  "\\\\+\\\\+"                                                      { return INC; }
  "--"                                                              { return DEC; }
  "\\\\+"                                                           { return ADD; }
  "-"                                                               { return SUB; }
  "\\\\*"                                                           { return MUL; }
  "/"                                                               { return DIV; }
  "%"                                                               { return MOD; }
  "->"                                                              { return ARROW; }
  "[ \\\\t\\\\u000C]+"                                              { return WS; }
  "/\\\\*[^*]*\\\\*+(?:[^/*][^*]*\\\\*+)*/"                         { return COMMENT; }
  "//[^\\\\r\\\\n]*"                                                { return LINE_COMMENT; }
  "[\\\\r\\\\n]+"                                                   { return NEWLINE; }
  "[a-zA-Z$_][a-zA-Z0-9$_]*"                                        { return IDENTIFIER; }


}

[^] { return BAD_CHARACTER; }

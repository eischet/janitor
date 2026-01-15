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

WHITE_SPACE=[ \t\f]+
NEWLINE=\r\n|\r|\n

DIGIT=[0-9]
DIGITS={DIGIT}([0-9_]*{DIGIT})?
HEXDIGIT=[0-9a-fA-F]
HEXDIGITS={HEXDIGIT}([0-9a-fA-F_]*{HEXDIGIT})?
EXPONENT=[eE][+-]?{DIGITS}
OCT_LITERAL_BODY=0_*[0-7]([0-7_]*[0-7])?[lL]?
DATE_LITERAL_BODY=@-?[0-9][0-9][0-9][0-9]-[0-9]?[0-9]-[0-9]?[0-9]
DATE_TIME_LITERAL_BODY=@-?[0-9][0-9][0-9][0-9]-[0-9]?[0-9]-[0-9]?[0-9]-[0-9]?[0-9]:[0-9][0-9](:[0-9][0-9])?

%%
<YYINITIAL> {
  {WHITE_SPACE}                                                     { return WHITE_SPACE; }
  {NEWLINE}                                                         { return NEWLINE; }

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

  ("0"|[1-9]([0-9_]*{DIGIT})?)[lL]?                                 { return DECIMAL_LITERAL; }
  "0"[xX]{HEXDIGITS}[lL]?                                           { return HEX_LITERAL; }
  {OCT_LITERAL_BODY}                                               { return OCT_LITERAL; }
  "0"[bB][01]([01_]*[01])?[lL]?                                    { return BINARY_LITERAL; }
  ({DIGITS}"."{DIGITS}?|"."{DIGITS}){EXPONENT}?[fFdD]?             { return FLOAT_LITERAL; }
  {DIGITS}({EXPONENT}[fFdD]?|[fFdD])                                { return FLOAT_LITERAL; }

  {DATE_LITERAL_BODY}                                              { return DATE_LITERAL; }
  {DATE_TIME_LITERAL_BODY}                                         { return DATE_TIME_LITERAL; }
  "@"[0-9]+"y"                                                     { return YEARS_LITERAL; }
  "@"[0-9]+"mo"                                                    { return MONTHS_LITERAL; }
  "@"[0-9]+"w"                                                     { return WEEKS_LITERAL; }
  "@"[0-9]+"d"                                                     { return DAYS_LITERAL; }
  "@"[0-9]+"h"                                                     { return HOURS_LITERAL; }
  "@"[0-9]+"mi"                                                    { return MINUTES_LITERAL; }
  "@"[0-9]+"s"                                                     { return SECONDS_LITERAL; }
  "@today"                                                          { return TODAY_LITERAL; }
  "@now"                                                            { return NOW_LITERAL; }

  "'"([^'\\\r\n]|\\.)*"'"                                     { return STRING_LITERAL_SINGLE; }
  "\""([^\"\\\r\n]|\\.)*"\""                              { return STRING_LITERAL_DOUBLE; }
  "'''"([^\\]|\\.)*?"'''"                                      { return STRING_LITERAL_TRIPLE_SINGLE; }
  "\"\"\""([^\\]|\\.)*?"\"\"\""                      { return STRING_LITERAL_TRIPLE_DOUBLE; }
  "re/"([^/\r\n]|\\.)*"/"                                      { return REGEX_LITERAL; }

  "("                                                              { return LPAREN; }
  ")"                                                              { return RPAREN; }
  "{"                                                              { return LBRACE; }
  "}"                                                              { return RBRACE; }
  "["                                                              { return LBRACK; }
  "]"                                                              { return RBRACK; }
  ";"                                                              { return SEMICOLON; }
  ","                                                              { return COMMA; }
  ".."                                                             { return DOUBLE_DOT; }
  "?."                                                             { return QDOT; }
  "."                                                              { return DOT; }
  "="                                                              { return ASSIGN; }
  "+="                                                             { return PLUS_ASSIGN; }
  "-="                                                             { return MINUS_ASSIGN; }
  "*="                                                             { return MUL_ASSIGN; }
  "/="                                                             { return DIV_ASSIGN; }
  "%="                                                             { return MOD_ASSIGN; }
  "!~"                                                             { return MATCH_NOT; }
  "~"                                                              { return MATCH; }
  "?"                                                              { return QUESTION; }
  ":"                                                              { return COLON; }
  "=="                                                             { return EQUAL; }
  "<="                                                             { return LE; }
  ">="                                                             { return GE; }
  "!="                                                             { return NOTEQUAL; }
  "<>"                                                             { return ALT_NOTEQUAL; }
  "not"                                                            { return NOT; }
  "!"                                                              { return ALT_NOT; }
  "and"                                                            { return AND; }
  "&&"                                                             { return CAND; }
  "or"                                                             { return OR; }
  "||"                                                             { return COR; }
  "++"                                                             { return INC; }
  "--"                                                             { return DEC; }
  "**"                                                             { return DOUBLE_STAR; }
  "+"                                                              { return ADD; }
  "-"                                                              { return SUB; }
  "*"                                                              { return MUL; }
  "/"                                                              { return DIV; }
  "%"                                                              { return MOD; }
  "->"                                                             { return ARROW; }
  ">"                                                              { return GT; }
  "<"                                                              { return LT; }

  "/\\*"([^*]|\\*+[^*/])*"\\*/"                               { return COMMENT; }
  "//"[^\r\n]*                                                   { return LINE_COMMENT; }

  [a-zA-Z$_][a-zA-Z0-9$_]*                                          { return IDENTIFIER; }
}

[^] { return BAD_CHARACTER; }

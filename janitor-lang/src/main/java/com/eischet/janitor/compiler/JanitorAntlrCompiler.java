package com.eischet.janitor.compiler;

import com.eischet.janitor.api.types.BuiltinTypes;
import com.eischet.janitor.api.JanitorEnvironment;
import com.eischet.janitor.api.scopes.Location;
import com.eischet.janitor.api.scopes.ScriptModule;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.api.types.builtin.*;
import com.eischet.janitor.compiler.ast.Ast;
import com.eischet.janitor.compiler.ast.AstNode;
import com.eischet.janitor.compiler.ast.expression.Expression;
import com.eischet.janitor.compiler.ast.expression.ExpressionList;
import com.eischet.janitor.compiler.ast.expression.Identifier;
import com.eischet.janitor.compiler.ast.expression.QualifiedName;
import com.eischet.janitor.compiler.ast.expression.binary.*;
import com.eischet.janitor.compiler.ast.expression.literal.*;
import com.eischet.janitor.compiler.ast.expression.ternary.IfThenElse;
import com.eischet.janitor.compiler.ast.expression.ternary.TernaryOperation;
import com.eischet.janitor.compiler.ast.expression.unary.LogicalNot;
import com.eischet.janitor.compiler.ast.expression.unary.Negation;
import com.eischet.janitor.compiler.ast.function.ScriptFunction;
import com.eischet.janitor.compiler.ast.statement.*;
import com.eischet.janitor.compiler.ast.statement.assignment.*;
import com.eischet.janitor.compiler.ast.statement.controlflow.*;
import com.eischet.janitor.compiler.ast.statement.specops.PostfixDecrement;
import com.eischet.janitor.compiler.ast.statement.specops.PostfixIncrement;
import com.eischet.janitor.compiler.ast.statement.specops.PrefixDecrement;
import com.eischet.janitor.compiler.ast.statement.specops.PrefixIncrement;
import com.eischet.janitor.lang.JanitorBaseVisitor;
import com.eischet.janitor.lang.JanitorLexer;
import com.eischet.janitor.lang.JanitorParser;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.eischet.janitor.api.util.ObjectUtilities.simpleClassNameOf;

/**
 * Compiler for the Janitor language.
 * This gets called by ANTLR4 to build the AST from the parse tree.
 */
public class JanitorAntlrCompiler extends JanitorBaseVisitor<Ast> implements JanitorCompiler {

    private static final Logger log = LoggerFactory.getLogger(JanitorAntlrCompiler.class);
    public static final String INDEXED_GET_METHOD = "__get__";

    private static final BooleanLiteral LITERAL_TRUE = new BooleanLiteral(null, JBool.TRUE);
    private static final BooleanLiteral LITERAL_FALSE = new BooleanLiteral(null, JBool.FALSE);
    private final ScriptModule module;
    private final boolean verbose;
    private final String source;
    private final JanitorEnvironment env;
    private final BuiltinTypes builtinTypes;


    public JanitorAntlrCompiler(final JanitorEnvironment env, final ScriptModule module, final boolean verbose, final String source) {
        this.env = env;
        this.module = module;
        this.verbose = verbose;
        this.source = source;
        this.builtinTypes = env.getBuiltinTypes();
    }

    public static JString parseLiteral(final JanitorEnvironment env, final @NotNull String literal) {
        return env.getBuiltinTypes().string(unescapeJava(literal));
    }

    public static String unescapeJava(final String literal) {
        // vorher: return StringEscapeUtils.unescapeJava(literal);
        return literal.translateEscapes();
    }


    @Override
    public Script visitScript(final JanitorParser.ScriptContext ctx) {
        if (verbose) log.info("visitScript");
        final List<Statement> topLevelStatements = new ArrayList<>(ctx.topLevelStatement().size());
        for (final JanitorParser.TopLevelStatementContext topLevelStatementContext : ctx.topLevelStatement()) {
            final Ast construct = visit(topLevelStatementContext);
            if (construct == null) {
                log.warn("null statement from: {}, parent {}, children {}", topLevelStatementContext.getText(), topLevelStatementContext.getParent(), topLevelStatementContext.getChildCount());
                for (int i = 0; i < topLevelStatementContext.getChildCount(); i++) {
                    log.warn("child #{}: {}", i, topLevelStatementContext.getChild(i).getClass());
                }
            }
            if (construct instanceof final Expression expression) {
                // das fängt z.b. auch function call expressions!
                if (verbose) log.info("top level expression: {}", expression);
                final ExpressionStatement expressionStatement = new ExpressionStatement(location(topLevelStatementContext.start, topLevelStatementContext.stop), expression);
                topLevelStatements.add(expressionStatement);
            } else if (construct instanceof final Statement stmt) {
                if (verbose) log.info("top level statement: {}", stmt);
                topLevelStatements.add(stmt);
            } else {
                log.warn("invalid top level construct: {} at {}", construct, ctx.getText());
            }
        }
        return new Script(location(ctx.start, ctx.stop), topLevelStatements, source);
    }

    @Override
    public Ast visitExpressionStatement(final JanitorParser.ExpressionStatementContext ctx) {
        if (verbose) log.info("visitExpressionStatement");
        return visit(ctx.statementExpression);
    }



    @Override
    public Block visitBlock(final JanitorParser.BlockContext ctx) {
        if (ctx == null) {
            return null;
        }
        final List<Statement> statements = new ArrayList<>(ctx.blockStatement().size());
        for (final JanitorParser.BlockStatementContext blockStatementContext : ctx.blockStatement()) {
            final Statement stmt = (Statement) visit(blockStatementContext);
            if (verbose) log.info("block statement: {}", stmt);
            statements.add(stmt);
        }
        return new Block(location(ctx.start, ctx.stop), statements);
    }

    @Override
    public Ast visitTopLevelBlockStatement(final JanitorParser.TopLevelBlockStatementContext ctx) {
        if (verbose)  log.info("visitTopLevelBlockStatement");
        return visit(ctx.blockStatement());
    }

    @Override
    public AstNode visitReturnStatement(final JanitorParser.ReturnStatementContext ctx) {
        return new ReturnStatement(location(ctx.start, ctx.stop), ctx.expression() == null ? null : (Expression) visit(ctx.expression()));
    }

    @Override
    public Ast visitEmptyStatement(final JanitorParser.EmptyStatementContext ctx) {
        return new EmptyStatement(location(ctx.start, ctx.stop));
    }


    @Override
    public AstNode visitBreakStatement(final JanitorParser.BreakStatementContext ctx) {
        return new BreakStatement(location(ctx.start, ctx.stop));
    }

    @Override
    public AstNode visitContinueStatement(final JanitorParser.ContinueStatementContext ctx) {
        return new ContinueStatement(location(ctx.start, ctx.stop));
    }

    @Override
    public ExpressionList visitExpressionList(final JanitorParser.ExpressionListContext ctx) {
        if (verbose) log.info("visitExpressionList: {}", ctx);
        if (ctx.isEmpty()) {
            if (verbose) log.info("empty expression list");
        }
        final ExpressionList el = new ExpressionList(location(ctx.start, ctx.stop));
        final List<JanitorParser.ExpressionContext> expr = ctx.expression();
        if (expr != null) {
            for (final JanitorParser.ExpressionContext expressionContext : expr) {
                final Ast result = visit(expressionContext);
                if (verbose) log.info("child: {}", result);
                el.addExpression((Expression) result);
            }
        }
        return el;
    }

    @Override
    public Ast visitIdentifier(final JanitorParser.IdentifierContext ctx) {
        //String text = ctx.getText();
        // System.out.println("visitIdentifier: " + text + ", TO = " + ctx.TO() + ", FROM = " + ctx.FROM());
        // Sonderlogik für Keywords, die auch Identifier sein können, z.B. from und to: die müssen hier separat abgearbeitet werden!
        // Komischerweise scheint man das hier aber nicht zu brauchen, weil...?
        return new Identifier(location(ctx.start, ctx.stop), builtinTypes.intern(ctx.getText()));
    }

    @Override
    public Identifier visitValidIdentifier(final JanitorParser.ValidIdentifierContext ctx) {
        return new Identifier(location(ctx.start, ctx.stop), builtinTypes.intern(ctx.getText()));
    }

    @Override
    public IntegerLiteral visitIntegerLiteral(final JanitorParser.IntegerLiteralContext ctx) {
        if (verbose) log.info("visitIntegerLiteral {}", ctx.getText());
        return new IntegerLiteral(location(ctx.start, ctx.stop), env.getBuiltinTypes().integer(Long.parseLong(ctx.getText())));
    }

    @Override
    public StringLiteral visitStringLiteralSingle(final JanitorParser.StringLiteralSingleContext ctx) {
        final String text = ctx.getText();
        return new StringLiteral(location(ctx.start, ctx.stop), parseLiteral(env, text.substring(1, text.length() - 1)));
    }

    @Override
    public Literal visitDateLiteral(final JanitorParser.DateLiteralContext ctx) {
        final String text = ctx.getText().substring(1); // cut @
        if ("today".equals(text)) {
            return new TodayLiteral(location(ctx.start, ctx.stop));
        }
        final JanitorObject litConst = env.getBuiltinTypes().nullableDateFromLiteral(text);
        if (litConst instanceof JDate date) {
            return new DateLiteral(location(ctx.start, ctx.stop), date);
        } else {
            throw new RuntimeException("invalid date literal: " + text);
        }
    }

    @Override
    public Literal visitDateTimeLiteral(final JanitorParser.DateTimeLiteralContext ctx) {
        final String text = ctx.getText().substring(1); // cut @
        if ("now".equals(text)) {
            return new NowLiteral(location(ctx.start, ctx.stop));
        }
        final @NotNull JanitorObject literalValue = env.getBuiltinTypes().nullableDateTimeFromLiteral(text);
        if (literalValue instanceof JDateTime dt) {
            return new DateTimeLiteral(location(ctx.start, ctx.stop), dt);
        } else {
            throw new RuntimeException("invalid datetime literal: " + text);
        }
    }

    @Override
    public Ast visitDurationLiteral(final JanitorParser.DurationLiteralContext ctx) {
        final String text = ctx.getText().substring(1); // cut @
        return new DurationLiteral(location(ctx.start, ctx.stop), text, env.getBuiltinTypes());
    }


    @Override
    public Literal visitRegexLiteral(final JanitorParser.RegexLiteralContext ctx) {
        final String raw = ctx.getText();
        final String text = raw.substring(3, raw.length() - 1);
        return new RegexLiteral(location(ctx.start, ctx.stop), text);
    }


    @Override
    public StringLiteral visitStringLiteralDouble(final JanitorParser.StringLiteralDoubleContext ctx) {
        final String text = ctx.getText();
        return new StringLiteral(location(ctx.start, ctx.stop), parseLiteral(env, text.substring(1, text.length() - 1)));
    }

    @Override
    public StringLiteral visitStringLiteralTripleSingle(final JanitorParser.StringLiteralTripleSingleContext ctx) {
        final String text = ctx.getText();
        return new StringLiteral(location(ctx.start, ctx.stop), parseLiteral(env, text.substring(3, text.length() - 3)));
    }

    @Override
    public StringLiteral visitStringLiteralTripleDouble(final JanitorParser.StringLiteralTripleDoubleContext ctx) {
        final String text = ctx.getText();
        return new StringLiteral(location(ctx.start, ctx.stop), parseLiteral(env, text.substring(3, text.length() - 3)));
    }

    @Override
    public IfStatement visitIfStatement(final JanitorParser.IfStatementContext ctx) {
        if (verbose) log.info("visitIfStatement");
        return visitIfStatementDef(ctx.ifStatementDef());
    }


    @Override
    public Block visitFinallyBlock(final JanitorParser.FinallyBlockContext ctx) {
        return visitBlock(ctx.block());
    }

    @Override
    public TryCatchFinally visitTryCatchStatement(final JanitorParser.TryCatchStatementContext ctx) {
        final Block tryBlock = visitBlock(ctx.block());
        Block catchBlock = null;
        Block finallyBlock = null;
        String catchBind = null;

        final JanitorParser.CatchClauseContext catchClause = ctx.catchClause();
        if (catchClause != null) {
            catchBlock = visitBlock(catchClause.block());
            catchBind = catchClause.validIdentifier().getText();
        }

        final JanitorParser.FinallyBlockContext finallyBlockContext = ctx.finallyBlock();
        if (finallyBlockContext != null) {
            finallyBlock = visitBlock(finallyBlockContext.block());
        }

        if (verbose) {
            log.info("tryBlock: {}\ncatchBlock: (identifier {}) {}\nfinallyBlock: {}\n", tryBlock, catchBind, catchBlock, finallyBlock);
        }

        return new TryCatchFinally(location(ctx.start, ctx.stop), tryBlock, catchBind, catchBlock, finallyBlock);
    }

    @Override
    public Ast visitThrowStatement(final JanitorParser.ThrowStatementContext ctx) {
        return super.visitThrowStatement(ctx); // LATER: throw
    }

    @Override
    public IfStatement visitIfStatementDef(final JanitorParser.IfStatementDefContext ctx) {
        final Location loc = location(ctx.start, ctx.stop);
        if (ctx.ifStatementDef() != null) { // else if
            final Block elseBlock = new Block(location(ctx.start, ctx.stop), List.of(visitIfStatementDef(ctx.ifStatementDef())));
            final Ast nested = visit(ctx.expression());
            if (nested instanceof Expression expr) {
                return new IfStatement(loc,
                        expr,
                        visitBlock(ctx.block(0)),
                        elseBlock);
            } else {
                throw new RuntimeException("invalid if clause " + nested + " ; expected an expression but got " + simpleClassNameOf(nested));
            }
        } else {
            final Ast nested = visit(ctx.expression());
            if (nested instanceof Expression expr) {
                return new IfStatement(loc,
                        expr,
                        visitBlock(ctx.block(0)),
                        visitBlock(ctx.block(1)));
            } else {
                throw new RuntimeException("invalid if clause " + nested + " ; expected an expression but got " + simpleClassNameOf(nested));
            }

        }
    }

    @Override
    public Ast visitPostfixExpression(final JanitorParser.PostfixExpressionContext ctx) {
        if (verbose) log.info("postfixExpression");
        final Expression expr = (Expression) visit(ctx.expression());
        switch (ctx.postfix.getType()) {
            case JanitorLexer.INC: return new PostfixIncrement(location(ctx.start, ctx.stop), expr);
            case JanitorLexer.DEC: return new PostfixDecrement(location(ctx.start, ctx.stop), expr);
        }
        throw new RuntimeException("unimplemented postfix expression: " + ctx.getText());
    }

    @Override
    public Ast visitPrefixExpression(final JanitorParser.PrefixExpressionContext ctx) {
        if (verbose) log.info("prefixExpression");
        final Expression expr = (Expression) visit(ctx.expression());
        switch (ctx.prefix.getType()) {
            case JanitorLexer.INC: return new PrefixIncrement(location(ctx.start, ctx.stop), expr);
            case JanitorLexer.DEC: return new PrefixDecrement(location(ctx.start, ctx.stop), expr);
            case JanitorLexer.SUB: return new Negation(location(ctx.start, ctx.stop), expr);
        }
        throw new RuntimeException("unimplemented prefix expression: " + ctx.getText());
    }

    @Override
    public AstNode visitAssignmentExpression(final JanitorParser.AssignmentExpressionContext ctx) {
        if (verbose) log.info("visitAssignmentExpression");
        final Expression left = (Expression) visit(ctx.expression(0));
        final Expression right = (Expression) visit(ctx.expression(1));
        return switch (ctx.bop.getType()) {
            case JanitorLexer.ASSIGN -> new RegularAssignment(location(ctx.start, ctx.stop), left, right);
            case JanitorLexer.PLUS_ASSIGN -> new PlusAssignment(location(ctx.start, ctx.stop), left, right);
            case JanitorLexer.MINUS_ASSIGN -> new MinusAssignment(location(ctx.start, ctx.stop), left, right);
            case JanitorLexer.DIV_ASSIGN -> new DivAssignment(location(ctx.start, ctx.stop), left, right);
            case JanitorLexer.MOD_ASSIGN -> new ModAssignment(location(ctx.start, ctx.stop), left, right);
            case JanitorLexer.MUL_ASSIGN -> new MulAssignment(location(ctx.start, ctx.stop), left, right);
            default ->
                    throw new RuntimeException("unimplemented assignment operation: " + ctx.getText() + " (" + ctx.bop.getText() + ")");
        };
    }

    @Override
    public LogicalNot visitNotExpression(final JanitorParser.NotExpressionContext ctx) {
        return new LogicalNot(location(ctx.start, ctx.stop), (Expression) visit(ctx.expression()));
    }

    @Override
    public Expression visitBinaryExpression(final JanitorParser.BinaryExpressionContext ctx) {
        if (verbose) log.info("visitBinaryExpression");
        final Expression left = (Expression) visit(ctx.expression(0));
        final Expression right = (Expression) visit(ctx.expression(1));
        return switch (ctx.bop.getType()) {
            case JanitorLexer.LT -> new LessThan(location(ctx.start, ctx.stop), left, right);
            case JanitorLexer.LE -> new LessThanOrEquals(location(ctx.start, ctx.stop), left, right);
            case JanitorLexer.GT -> new GreaterThan(location(ctx.start, ctx.stop), left, right);
            case JanitorLexer.GE -> new GreaterThanOrEquals(location(ctx.start, ctx.stop), left, right);
            case JanitorLexer.MUL -> new Multiplication(location(ctx.start, ctx.stop), left, right);
            case JanitorLexer.DIV -> new Division(location(ctx.start, ctx.stop), left, right);
            case JanitorLexer.MOD -> new Modulo(location(ctx.start, ctx.stop), left, right);
            case JanitorLexer.ADD -> new Addition(location(ctx.start, ctx.stop), left, right);
            case JanitorLexer.SUB -> new Subtraction(location(ctx.start, ctx.stop), left, right);
            case JanitorLexer.AND, JanitorLexer.CAND -> new LogicAnd(location(ctx.start, ctx.stop), left, right);
            case JanitorLexer.OR, JanitorLexer.COR -> new LogicOr(location(ctx.start, ctx.stop), left, right);
            case JanitorLexer.EQUAL -> new Equality(location(ctx.start, ctx.stop), left, right);
            case JanitorLexer.ALT_NOTEQUAL, JanitorLexer.NOTEQUAL -> new NonEquality(location(ctx.start, ctx.stop), left, right);
            case JanitorLexer.MATCH -> new MatchesGlob(location(ctx.start, ctx.stop), left, right);
            case JanitorLexer.MATCH_NOT -> new MatchesNotGlob(location(ctx.start, ctx.stop), left, right);
            default -> throw new RuntimeException("unimplemented binary operation: " + ctx.getText() + " (" + ctx.bop.getText() + ")");
        };
    }

    @Override
    public DoWhileLoop visitDoWhileStatement(final JanitorParser.DoWhileStatementContext ctx) {
        final Expression expression = (Expression) visit(ctx.expression());
        final Block block = visitBlock(ctx.block());
        return new DoWhileLoop(location(ctx.start, ctx.stop), block, expression);
    }

    @Override
    public WhileLoop visitWhileStatement(final JanitorParser.WhileStatementContext ctx) {
        final Expression expression = (Expression) visit(ctx.expression());
        final Block block = visitBlock(ctx.block());
        return new WhileLoop(location(ctx.start, ctx.stop), expression, block);
    }

    @Override
    public Ast visitForStatement(final JanitorParser.ForStatementContext ctx) {
        final String loopVar = ctx.validIdentifier().getText();
        final Expression expression = (Expression) visit(ctx.expression());
        final Block block = visitBlock(ctx.block());
        return new ForLoop(location(ctx.start, ctx.stop), loopVar, expression, block);
    }

    @Override
    public Ast visitForRangeStatement(final JanitorParser.ForRangeStatementContext ctx) {
        final String loopVar = ctx.validIdentifier().getText();
        final Expression from = (Expression) visit(ctx.expression(0));
        final Expression to = (Expression) visit(ctx.expression(1));
        final Block block = visitBlock(ctx.block());
        return new ForRangeLoop(location(ctx.start, ctx.stop), loopVar, from, to, block);
    }

    @Override
    public BooleanLiteral visitBoolLiteral(final JanitorParser.BoolLiteralContext ctx) {
        final String text = ctx.getText();
        if ("true".equals(text)) {
            return LITERAL_TRUE;
        }
        if ("false".equals(text)) {
            return LITERAL_FALSE;
        }
        throw new RuntimeException("invalid bool literal: " + text);
    }


    @Override
    public Ast visitTopLevelImportStatement(final JanitorParser.TopLevelImportStatementContext ctx) {
        return visit(ctx.importStatement()); // just unpack, the top level import statement is not important by itself
    }

    @Override
    public Ast visitImportAlias(final JanitorParser.ImportAliasContext ctx) {
        return ctx == null ? null : visitValidIdentifier(ctx.validIdentifier()); // just unpack, we only need the alias identifier
    }

    @Override
    public ImportClause visitImportStringSingle(final JanitorParser.ImportStringSingleContext ctx) {
        final String text = ctx.STRING_LITERAL_SINGLE().getText();
        final String string = unescapeJava(text.substring(1, text.length() - 1));
        return new ImportClause(location(ctx.start, ctx.stop), string, (Identifier) visitImportAlias(ctx.importAlias()));
    }

    @Override
    public ImportClause visitImportStringDouble(final JanitorParser.ImportStringDoubleContext ctx) {
        final String text = ctx.STRING_LITERAL_DOUBLE().getText();
        final String string = unescapeJava(text.substring(1, text.length() - 1));
        return new ImportClause(location(ctx.start, ctx.stop), string, (Identifier) visitImportAlias(ctx.importAlias()));
    }

    @Override
    public ImportStatement visitImportStatement(final JanitorParser.ImportStatementContext ctx) {
        final List<ImportClause> clauses = new ArrayList<>(ctx.importClause().size());
        for (final JanitorParser.ImportClauseContext clauseContext : ctx.importClause()) {
            final Ast ast = visit(clauseContext);
            if (ast instanceof ImportClause clause) {
                clauses.add(clause);
            } else {
                throw new RuntimeException("invalid import clause: " + ast);
            }
        }
        return new ImportStatement(location(ctx.start, ctx.stop), clauses);
        // alt: final String alias = Optional.ofNullable(ctx.importAlias()).map(RuleContext::getText).orElse(null);
        // alt: final QualifiedName qname = visitQualifiedName(ctx.qualifiedName());
        // return new ImportStatement(location(ctx.start, ctx.stop), /* qname, alias */ clauses);
    }

    @Override
    public Ast visitImportPlain(final JanitorParser.ImportPlainContext ctx) {
        return new ImportClause(location(ctx.start, ctx.stop),
            visitQualifiedName(ctx.qualifiedName()),
            (Identifier) visitImportAlias(ctx.importAlias()));
    }

    @Override
    public QualifiedName visitQualifiedName(final JanitorParser.QualifiedNameContext ctx) {
        final List<String> parts = ctx.validIdentifier().stream().map(ParseTree::getText).toList();
        return new QualifiedName(location(ctx.start, ctx.stop), parts);
    }

    @Override
    public Block visitNestedBlock(final JanitorParser.NestedBlockContext ctx) {
        return visitBlock(ctx.block());
    }

    @Override
    public Ast visitParensExpression(final JanitorParser.ParensExpressionContext ctx) {
        return visit(ctx.expression()); // just unpack
    }

    @Override
    public Ast visitIfThenElseExpression(final JanitorParser.IfThenElseExpressionContext ctx) {
        Expression ifClause = (Expression) visit(ctx.expression(0));
        Expression thenClause = (Expression) visit(ctx.expression(1));
        final JanitorParser.ExpressionContext elseExpression = ctx.expression(2);
        Expression elseClause = elseExpression == null ? null : (Expression) visit(elseExpression);
        return new IfThenElse(location(ctx.start, ctx.stop), ifClause, thenClause, elseClause);
    }

    @Override
    public TernaryOperation visitTernaryExpression(final JanitorParser.TernaryExpressionContext ctx) {
        Expression ifClause = (Expression) visit(ctx.expression(0));
        Expression thenClause = (Expression) visit(ctx.expression(1));
        Expression elseClause = (Expression) visit(ctx.expression(2));
        return new IfThenElse(location(ctx.start, ctx.stop), ifClause, thenClause, elseClause);
    }

    @Override
    public Ast visitNullLiteral(final JanitorParser.NullLiteralContext ctx) {
        return NullLiteral.NULL;
    }

    @Override
    public Ast visitFunctionDeclarationStatement(final JanitorParser.FunctionDeclarationStatementContext ctx) {
        if (verbose) log.info("functionDeclarationStatement");
        return visit(ctx.functionDeclaration());
    }

    @Override
    public AstNode visitFunctionDeclaration(final JanitorParser.FunctionDeclarationContext ctx) {
        if (verbose) log.info("functionDeclaration");
        final JanitorParser.FormalParametersContext formalParameters = ctx.formalParameters();
        final List<String> finishedParams = helpExtractFormalParameters(formalParameters);

        final Location loc = location(ctx.start, ctx.stop);

        return new RegularAssignment(loc,
            new Identifier(loc, ctx.validIdentifier().getText()),
            new ScriptFunction(loc, ctx.validIdentifier().getText(), finishedParams, visitBlock(ctx.block()))
        );
    }

    private List<String> helpExtractFormalParameters(final JanitorParser.FormalParametersContext formalParameters) {
        if (verbose) log.info("formal parameters: {}", formalParameters);
        if (formalParameters != null) {
            return helpExtractFormalParametersList(formalParameters.formalParameterList());
        } else {
            return Collections.emptyList();
        }
    }

    private List<String> helpExtractFormalParametersList(final JanitorParser.FormalParameterListContext fpl) {
        if (verbose) log.info("fpl: {}", fpl);
        if (fpl != null) {
            final List<String> parameterNames = new ArrayList<>();
            for (final JanitorParser.FormalParameterContext formalParameterContext : fpl.formalParameter()) {
                parameterNames.add(formalParameterContext.validIdentifier().getText());
            }
            return parameterNames;
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public Ast visitGenericInvocationExpression(final JanitorParser.GenericInvocationExpressionContext ctx) {
        if (verbose) log.info("genericInvocation??");
        return super.visitGenericInvocationExpression(ctx);
    }

    @Override
    public Statement visitExplicitGenericInvocationSuffix(final JanitorParser.ExplicitGenericInvocationSuffixContext ctx) {
        if (verbose) log.info("genericInvocationSuffix??");
        return new FunctionCallStatement(location(ctx.start, ctx.stop), ctx.validIdentifier().getText(), null, visitArguments(ctx.arguments()));

        // return super.visitExplicitGenericInvocationSuffix(ctx);
    }

    @Override
    public ExpressionList visitArguments(final JanitorParser.ArgumentsContext ctx) {
        final JanitorParser.ExpressionListContext expList = ctx.expressionList();
        return expList == null ? null : visitExpressionList(ctx.expressionList());
    }

    @Override
    public AstNode visitFunctionCall(final JanitorParser.FunctionCallContext ctx) {
        if (verbose) log.info("functionCall");
        // ctx.expressionList()
        return null;
    }

    @Override
    public Ast visitIndexExpression(final JanitorParser.IndexExpressionContext ctx) {
        final JanitorParser.ExpressionContext index = ctx.expression(1);
        return new FunctionCallStatement(
            location(ctx.start, ctx.stop),
            INDEXED_GET_METHOD,
            (Expression) visit(ctx.expression(0)),
            new ExpressionList(location(index.start, index.stop)).addExpression((Expression) visit(index))
        );
    }

    @Override
    public Ast visitIndexExpressionRangeHead(final JanitorParser.IndexExpressionRangeHeadContext ctx) {
        final JanitorParser.ExpressionContext head = ctx.expression(1);
        return parseIndexExpression(ctx.expression(0), ctx.start, ctx.stop, head, null);
    }

    @Override
    public Ast visitIndexExpressionRangeTail(final JanitorParser.IndexExpressionRangeTailContext ctx) {
        final JanitorParser.ExpressionContext tail = ctx.expression(1);
        return parseIndexExpression(ctx.expression(0), ctx.start, ctx.stop, null, tail);
    }

    @Override
    public Ast visitIndexExpressionRange(final JanitorParser.IndexExpressionRangeContext ctx) {
        final JanitorParser.ExpressionContext head = ctx.expression(1);
        final JanitorParser.ExpressionContext tail = ctx.expression(2);
        return parseIndexExpression(ctx.expression(0), ctx.start, ctx.stop, head, tail);
    }

    @Override
    public Ast visitIndexExpressionFullRange(final JanitorParser.IndexExpressionFullRangeContext ctx) {
        return new FunctionCallStatement(
            location(ctx.start, ctx.stop),
            INDEXED_GET_METHOD,
            (Expression) visit(ctx.expression()),
            new ExpressionList(location(ctx.start, ctx.stop))
                .addExpression(NullLiteral.NULL)
                .addExpression(NullLiteral.NULL)
        );
    }

    private Ast parseIndexExpression(JanitorParser.ExpressionContext main,
                                     Token start,
                                     Token stop,
                                     JanitorParser.ExpressionContext head,
                                     JanitorParser.ExpressionContext tail) {
        if (head == null && tail == null) {
            return new FunctionCallStatement(
                location(start, stop),
                INDEXED_GET_METHOD,
                (Expression) visit(main),
                new ExpressionList(location(start, stop))
                    .addExpression(NullLiteral.NULL)
                    .addExpression(NullLiteral.NULL)
            );
        } else if (tail == null) {
            return new FunctionCallStatement(
                location(start, stop),
                INDEXED_GET_METHOD,
                (Expression) visit(main),
                new ExpressionList(location(head.start, head.stop))
                    .addExpression((Expression) visit(head))
                    .addExpression(NullLiteral.NULL)
            );
        } else if (head == null) {
            return new FunctionCallStatement(
                location(start, stop),
                INDEXED_GET_METHOD,
                (Expression) visit(main),
                new ExpressionList(location(tail.start, tail.stop))
                    .addExpression(NullLiteral.NULL)
                    .addExpression((Expression) visit(tail))
            );
        } else {
            return new FunctionCallStatement(
                location(start, stop),
                INDEXED_GET_METHOD,
                (Expression) visit(main),
                new ExpressionList(location(head.start, head.stop))
                    .addExpression((Expression) visit(head))
                    .addExpression((Expression) visit(tail))
            );
        }

    }


    @Override
    public Expression visitCallExpression(final JanitorParser.CallExpressionContext ctx) {
        if (verbose) log.info("callExpression");

        String identifierText = null;

        if (ctx.validIdentifier() != null) {
            identifierText = ctx.validIdentifier().getText();
        }

        // ⬇️ Ergänzung: Funktionsname aus functionCall holen
        if (identifierText == null && ctx.functionCall() != null) {
            final JanitorParser.FunctionCallContext fc = ctx.functionCall();
            if (fc.validIdentifier() != null) {
                identifierText = fc.validIdentifier().getText();
            }
        }

        if (identifierText == null) {
            log.warn("*** invalid call expression {} (no identifier)", ctx.getText());
        }

        identifierText = env.getBuiltinTypes().intern(identifierText);

        final JanitorParser.FunctionCallContext functionCallContext = ctx.functionCall();
        final JanitorParser.ExpressionContext expr = ctx.expression();

        if (verbose) {
            log.info("identifier: {}", identifierText);
            log.info("functionCallContext: {}", functionCallContext);
            log.info("expression: {}", expr);
        }

        if (identifierText != null && functionCallContext != null) {
            if (verbose) log.info("case 1: call with identifier and function call context");
            final JanitorParser.ExpressionListContext expList = functionCallContext.expressionList();
            return new FunctionCallStatement(
                    location(ctx.start, ctx.stop),
                    identifierText,
                    expr == null ? null : (Expression) visit(expr),
                    expList == null ? null : visitExpressionList(expList)
            );
        }

        if (identifierText != null && expr != null) {
            final boolean guarded = ctx.QDOT() != null;
            return new FunctionLookup(
                    location(ctx.start, ctx.stop),
                    identifierText,
                    (Expression) visit(expr),
                    null,
                    guarded
            );
        }

        if (identifierText == null && expr != null && functionCallContext != null) {
            final ExpressionList callExpr =
                    functionCallContext.expressionList() == null ? null : visitExpressionList(functionCallContext.expressionList());
            return new FunctionCallStatement(
                    location(ctx.start, ctx.stop),
                    functionCallContext.validIdentifier().getText(),
                    (Expression) visit(expr),
                    callExpr
            );
        }

        log.warn("invalid call expression {}", ctx.getText());
        return null;
    }

    @Override
    public Ast visitLambdaExpression(final JanitorParser.LambdaExpressionContext ctx) {
        if (verbose) log.info("lambdaExpression");

        final List<String> lambdaParameters = helpExtractLambdaParameters(ctx.lambdaParameters());
        if (verbose) log.info("lambda parameters: {}", lambdaParameters);

        final JanitorParser.LambdaBodyContext lambdaBodyContext = ctx.lambdaBody();
        final JanitorParser.ExpressionContext lambdaExpressionContext = lambdaBodyContext.expression();
        final JanitorParser.BlockContext lambdaBlockContext = lambdaBodyContext.block();

        final Location loc = location(ctx.start, ctx.stop);

        if (lambdaExpressionContext != null) {
            if (verbose) log.info("it's an expression lambda");

            return new ScriptFunction(loc, "lambda", lambdaParameters,
                new Block(loc, List.of(
                    new ReturnStatement(loc, (Expression) visit(lambdaExpressionContext))
                ))
                );

        }
        if (lambdaBlockContext != null) {
            if (verbose) log.info("it's a block lambda");
            return new ScriptFunction(loc, "lambda", lambdaParameters,
                visitBlock(lambdaBlockContext));
        }

        log.error("unknown lambda construction at {}: {}", loc, ctx.getText());
        return super.visitLambdaExpression(ctx);
    }

    private List<String> helpExtractLambdaParameters(final JanitorParser.LambdaParametersContext ctx) {
        if (verbose) log.info("lambdaParameters");
        final List<JanitorParser.ValidIdentifierContext> identifiers = ctx.validIdentifier();
        final JanitorParser.FormalParameterListContext formalParams = ctx.formalParameterList();
        if (formalParams != null) {
            if (verbose) log.info("this lambda uses formal params");
            final List<String> fp = helpExtractFormalParametersList(formalParams);
            if (verbose) log.info("names: {}", fp);
            return fp;
        }
        if (identifiers != null) {
            if (verbose) log.info("this lambda uses identifiers");

            final List<String> names = new ArrayList<>(identifiers.size());
            for (final var identifier : identifiers) {
                names.add(identifier.getText());
            }
            if (verbose) log.info("names: {}", names);
            return names;
            //para
        }
        if (verbose) log.warn("unable to extract parameters!");
        return null;
    }

    @Override
    public Ast visitLambdaBody(final JanitorParser.LambdaBodyContext ctx) {
        return super.visitLambdaBody(ctx); // it's not actually used, because higher-up code picks up these parts manually
    }

    @Override
    public Ast visitLambdaParameters(final JanitorParser.LambdaParametersContext ctx) {
        return super.visitLambdaParameters(ctx); // it's not actually used, because higher-up code picks up these parts manually
    }

    @Override
    public Ast visitFloatLiteral(final JanitorParser.FloatLiteralContext ctx) {
        if (verbose) log.info("visitFloatLiteral {}", ctx.getText());
        return new FloatLiteral(location(ctx.start, ctx.stop), Double.parseDouble(ctx.getText()), env.getBuiltinTypes());
    }

    @Override
    public Ast visitExplicitGenericInvocation(final JanitorParser.ExplicitGenericInvocationContext ctx) {
        return super.visitExplicitGenericInvocation(ctx); // LATER: what the fck is this?
    }

    @Override
    public Ast visitListExpression(final JanitorParser.ListExpressionContext ctx) {
        final List<JanitorParser.ExpressionContext> containedExpressions = ctx.expression();
        if (containedExpressions != null && !containedExpressions.isEmpty()) {
            final List<Expression> elements = new ArrayList<>(containedExpressions.size());
            for (final JanitorParser.ExpressionContext containedExpression : containedExpressions) {
                final Ast expr = visit(containedExpression);
                // log.info("list, contained expression: {}", expr);
                elements.add((Expression) expr);
            }
            return new ListLiteral(location(ctx.start, ctx.stop), elements);
        } else {
            return new ListLiteral(location(ctx.start, ctx.stop), Collections.emptyList());
        }

    }

    @Override
    public Ast visitMapExpression(final JanitorParser.MapExpressionContext ctx) {
        final List<JanitorParser.PropertyAssignmentContext> props = ctx.propertyAssignment();
        if (props != null && !props.isEmpty()) {
            final List<MapLiteral.Preset> presets = new ArrayList<>();
            for (final JanitorParser.PropertyAssignmentContext prop : props) {
                StringLiteral lit = null;
                final TerminalNode litS = prop.STRING_LITERAL_SINGLE();
                final TerminalNode litD = prop.STRING_LITERAL_DOUBLE();
                final TerminalNode litTS = prop.STRING_LITERAL_TRIPLE_SINGLE();
                final TerminalNode litTD = prop.STRING_LITERAL_TRIPLE_DOUBLE();
                final JanitorParser.ValidIdentifierContext litIdent = prop.validIdentifier();
                if (litS != null) {
                    final String text = litS.getText();
                    lit = new StringLiteral(location(prop.start, ctx.stop), parseLiteral(env, text.substring(1, text.length() - 1)));
                } else if (litD != null) {
                    final String text = litD.getText();
                    lit = new StringLiteral(location(prop.start, ctx.stop), parseLiteral(env, text.substring(1, text.length() - 1)));
                } else if (litTS != null) {
                    final String text = litTS.getText();
                    lit = new StringLiteral(location(prop.start, ctx.stop), parseLiteral(env, text.substring(3, text.length() - 3)));
                }  else if (litTD != null) {
                    final String text = litTD.getText();
                    lit = new StringLiteral(location(prop.start, ctx.stop), parseLiteral(env, text.substring(3, text.length() - 3)));
                } else if (litIdent != null) {
                    final String text = litIdent.getText();

                    lit = new StringLiteral(location(prop.start, ctx.stop), env.getBuiltinTypes().string(text));
                }
                if (lit == null) {
                    throw new RuntimeException("map literal: keys must be single or double quoted strings, instead of: " + prop.getText());
                }
                final Expression expr = (Expression) visit(prop.expression());
                // log.info("map, property assignment: {} = {}", lit, expr);
                presets.add(new MapLiteral.Preset(lit, expr));
            }
            return new MapLiteral(location(ctx.start, ctx.stop), presets);
        } else {
            return new MapLiteral(location(ctx.start, ctx.stop), Collections.emptyList());
        }
    }

    private Location location(final Token start, final Token stop) {
        return Location.at(module, start.getLine(), start.getCharPositionInLine(),
                stop == null ? start.getLine() : stop.getLine(),
                stop == null ? start.getCharPositionInLine() : stop.getCharPositionInLine());
    }

}

package com.eischet.janitor.template;

import com.eischet.janitor.api.JanitorRuntime;
import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.RunnableScript;
import com.eischet.janitor.api.calls.JCallArgs;
import com.eischet.janitor.api.errors.compiler.JanitorCompilerException;
import com.eischet.janitor.api.errors.runtime.JanitorArgumentException;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.traits.JCallable;
import com.eischet.janitor.api.types.JNull;
import com.eischet.janitor.api.types.JString;
import com.eischet.janitor.api.types.JanitorObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A parser for template strings.
 * Think JSP or Lua Server Pages.
 */
public class TemplateParser {

    private static final String EXPR_START = "<%=";
    private static final String BLOCK_START = "<%";
    private static final String BLOCK_END = "%>";
    private static final String COMMENT_START = "<%--";
    private static final String COMMEND_END = "--%>";
    private static final Pattern SHORT_DOLLAR_VAR = Pattern.compile("\\$\\{([^}]+)}");
    private final List<TemplateBlock> blocks;

    /**
     * Create a new template parser.
     * @param _script the script
     */
    public TemplateParser(final String _script) {
        final List<TemplateBlock> blocks = new ArrayList<>();
        if (_script != null && !_script.isBlank()) {
            final String script = replaceDollarVars(_script); // replace ${foo} with <%=foo%>
            final int endPos = script.length();
            int startPos = 0;
            while (startPos < endPos) {
                int nextComment = script.indexOf(COMMENT_START, startPos);
                if (nextComment == startPos) {
                    final int nextCommendEnd = script.indexOf(COMMEND_END, startPos + COMMENT_START.length());
                    if (nextCommendEnd == -1) {
                        blocks.add(new TemplateBlock(BlockType.invalid, script.substring(nextComment)));
                        startPos = endPos;
                    } else {
                        blocks.add(new TemplateBlock(BlockType.comment, script.substring(nextComment + COMMENT_START.length(), nextCommendEnd).trim()));
                        startPos = nextCommendEnd + COMMEND_END.length();
                    }
                } else {
                    int nextStart = script.indexOf(BLOCK_START, startPos);
                    if (nextStart == -1) {
                        blocks.add(new TemplateBlock(BlockType.text, script.substring(startPos)));
                        startPos = endPos;
                    } else if (nextStart == startPos) {
                        int nextEnd = script.indexOf(BLOCK_END, startPos);
                        if (nextEnd == -1) {
                            blocks.add(new TemplateBlock(BlockType.invalid, script.substring(startPos)));
                            startPos = endPos;
                        } else {
                            final String functionalBlock = script.substring(startPos, nextEnd + BLOCK_END.length());
                            if (functionalBlock.startsWith(EXPR_START) && functionalBlock.endsWith(BLOCK_END)) {
                                blocks.add(new TemplateBlock(BlockType.expression, functionalBlock.substring(EXPR_START.length(), functionalBlock.length() - BLOCK_END.length())));
                            } else if (functionalBlock.startsWith(BLOCK_START) && functionalBlock.endsWith(BLOCK_END)) {
                                blocks.add(new TemplateBlock(BlockType.statement, functionalBlock.substring(BLOCK_START.length(), functionalBlock.length() - BLOCK_END.length())));
                            } else {
                                blocks.add(new TemplateBlock(BlockType.invalid, functionalBlock));
                            }
                            startPos = nextEnd + BLOCK_END.length();
                        }
                    } else {
                        blocks.add(new TemplateBlock(BlockType.text, script.substring(startPos, nextStart)));
                        startPos = nextStart;
                    }
                }
            }

        }
        this.blocks = blocks;
    }

    /**
     * Replace the ${foo} shorthand syntax with template tags.
     * @param script original script code
     * @return script with replaced shorthand syntax
     */
    public static String replaceDollarVars(final @NotNull String script) {
        final Matcher matcher = SHORT_DOLLAR_VAR.matcher(script);
        return matcher.replaceAll(match -> "<%= " + match.group(1).trim() + " %>");
    }

    /**
     * Render a stream of blocks as a script.
     * @param blocks the blocks
     * @return the script
     */
    public static String plainRenderer(final Stream<TemplateBlock> blocks) {
        return blocks.map(block -> switch (block.getType()) {
            case text -> "__OUT__('''" + block.getSource() + "''');";
            case expression -> "__OUT__(" + block.getSource() + ");";
            case statement -> block.getSource();
            case comment -> "/* " + block.getSource().replace("*/", "*_/") + " */";
            case invalid -> "/* INVALID: " + block.getSource().replace("*/", "*_/") + " */";
        }).filter(Objects::nonNull).collect(Collectors.joining());
    }

    /**
     * Expand a template string with arguments.
     * @param runtime the runtime
     * @param process the process
     * @param templateString the template string
     * @param arguments the arguments
     * @return the expanded template
     * @throws JanitorRuntimeException on errors
     */
    public static JString expand(final JanitorRuntime runtime, JanitorScriptProcess process, final JString templateString, final JCallArgs arguments) throws JanitorRuntimeException {
        final TemplateParser parser = new TemplateParser(templateString.janitorToString());

        if (!parser.isValid()) {
            throw new JanitorArgumentException(process, "invalid template");
        }

        try {
            final String scriptSource = parser.toScript(TemplateParser::plainRenderer);
            RunnableScript script = runtime.compile("TEMPLATE", scriptSource);
            arguments.require(0, 1);
            final JanitorObject values = arguments.size() > 0 ? arguments.get(0) : null;

            final StringWriter stringWriter = new StringWriter();
            final JCallable writer = (runningScript, arguments1) -> {
                for (final JanitorObject jObj : arguments1.getList()) {
                    if (jObj != JNull.NULL) {
                        stringWriter.append(jObj.janitorToString());
                    }
                }
                return JNull.NULL;
            };
            script.runInScope(g -> {
                        g.bind("__OUT__", writer.asObject("__OUT__"));
                        if (values != null) {
                            g.setImplicitObject(values);
                        }
                    }, process.getCurrentScope()
            );
            return runtime.getEnvironment().getBuiltins().string(stringWriter.toString());
        } catch (JanitorCompilerException e) {
            throw new JanitorArgumentException(process, "invalid template", e);
        }
    }

    /**
     * Return whether the template is pure text, in which case we never need to actually run it as a script.
     * @return true if pure
     */
    public boolean isPure() {
        return blocks.stream().allMatch(block -> block.getType() == BlockType.text);
    }

    /**
     * Return whether the template is valid.
     * @return true if valid
     */
    public boolean isValid() {
        return blocks.stream().noneMatch(block -> block.getType() == BlockType.invalid);
    }

    /**
     * Return the first invalid block in the template.
     * @return the block or null
     */
    private @Nullable TemplateBlock firstInvalidBlock() {
        return blocks.stream().filter(block -> block.getType() == BlockType.invalid).findFirst().orElse(null);
    }

    /**
     * Return the blocks of the template.
     * @return the blocks
     */
    public List<TemplateBlock> getBlocks() {
        return blocks;
    }

    /**
     * Return the template as a script that, when executed, renders the template.
     * @param renderer the renderer to use
     * @return the script
     */
    public String toScript(final TemplateRenderer renderer) {
        return renderer.renderBlocks(getBlocks().stream());
    }

    /**
     * Types of blocks in a template.
     */
    public enum BlockType {text, expression, statement, comment, invalid}

}

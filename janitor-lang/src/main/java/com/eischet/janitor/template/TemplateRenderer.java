package com.eischet.janitor.template;

import java.util.stream.Stream;

/**
 * A functional interface for rendering a stream of template blocks to a string.
 */
@FunctionalInterface
public interface TemplateRenderer {
    /**
     * Render the given stream of template blocks to a string.
     * @param blocks the stream of template blocks to render
     * @return the rendered string
     */
    String renderBlocks(Stream<TemplateBlock> blocks);
}

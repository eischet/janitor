package com.eischet.janitor.template;

import java.util.stream.Stream;

@FunctionalInterface
public interface TemplateRenderer {
    String renderBlocks(Stream<TemplateBlock> blocks);
}

package com.eischet.janitor.cleanup.template;

import java.util.stream.Stream;

@FunctionalInterface
public interface TemplateRenderer {
    String renderBlocks(Stream<TemplateBlock> blocks);
}

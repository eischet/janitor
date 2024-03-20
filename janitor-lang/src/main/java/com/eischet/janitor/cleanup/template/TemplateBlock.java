package com.eischet.janitor.cleanup.template;

public class TemplateBlock {
    final TemplateParser.BlockType type;
    final String source;

    public TemplateBlock(TemplateParser.BlockType type, String source) {
        this.type = type;
        this.source = source;
    }

    public TemplateParser.BlockType getType() {
        return type;
    }

    public String getSource() {
        return source;
    }

    @Override
    public String toString() {
        return "Block{" +
            "type=" + type +
            ", source='" + source + '\'' +
            '}';
    }

}

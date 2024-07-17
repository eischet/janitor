package com.eischet.janitor.template;

/**
 * Represents a block (part) of a template.
 */
public class TemplateBlock {
    private final TemplateParser.BlockType type;
    private final String source;

    /**
     * Create a new block.
     * @param type the type of block
     * @param source the source of the block
     */
    public TemplateBlock(TemplateParser.BlockType type, String source) {
        this.type = type;
        this.source = source;
    }

    /**
     * Get the type of the block.
     * @return the type of the block
     */
    public TemplateParser.BlockType getType() {
        return type;
    }

    /**
     * Get the source of the block.
     * @return the source of the block
     */
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

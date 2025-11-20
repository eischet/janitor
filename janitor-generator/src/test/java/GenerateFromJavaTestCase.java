import com.eischet.janitor.generator.Generator;
import com.eischet.janitor.generator.types.SimpleBuiltinType;
import com.eischet.janitor.generator.writing.TestWriter;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class GenerateFromJavaTestCase {

    @Test
    void foo() {
        final var testWriter = new TestWriter();
        final var g = new Generator();
        g.setPath("src/test/java");
        g.setJavaPackageName("janitor.test");
        g.setJavaWriter(testWriter);
        g.newClass("Foo").addField(SimpleBuiltinType.STRING, "bar");

        g.write();

        assertEquals(1, testWriter.getGeneratedFiles().size());
        @Nullable final String contents = testWriter.getGeneratedFiles().get("src/test/java/janitor/test/Foo.java");
        assertNotNull(contents);

        @Language("java")
        final String expected = """
                package janitor.test;
                
                import com.eischet.janitor.api.types.dispatch.DispatchTable;
                import org.jetbrains.annotations.NotNull;
                import org.jetbrains.annotations.Nullable;
                
                public class Foo {
                
                    public static final DispatchTable<Foo> DISPATCH = new DispatchTable<>();
                
                    static {
                        DISPATCH.addStringProperty("bar", Foo::getBar, Foo::setBar);
                    }
                
                    private @Nullable String bar;
                
                    public @Nullable String getBar() {
                        return bar;
                    }
                
                    public void setBar(final @Nullable String bar) {
                        this.bar = bar;
                    }
                
                    public @NotNull Foo withBar(final @Nullable String bar) {
                        this.bar = bar;
                        return this;
                    }
                
                }
                """;

        assertEquals(expected, contents);


    }

}

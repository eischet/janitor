package com.eischet.janitor;

import com.eischet.janitor.api.*;
import com.eischet.janitor.api.Janitor;
import com.eischet.janitor.api.types.functions.JCallArgs;
import com.eischet.janitor.api.errors.compiler.JanitorCompilerException;
import com.eischet.janitor.api.errors.runtime.JanitorNameException;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.api.types.builtin.JNull;
import com.eischet.janitor.env.JanitorDefaultEnvironment;
import com.eischet.janitor.runtime.BaseRuntime;
import com.eischet.janitor.runtime.JanitorFormattingGerman;
import org.intellij.lang.annotations.Language;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test/demonstrate adding properties and methods to built-in classes.
 * Janitor users are expected to add a few of those here and there, so this process should be easy and straightforward.
 *
 */
@Execution(ExecutionMode.SAME_THREAD)
public class ExtensionMethodsTestCase extends JanitorTest {

    /**
     * Create an environment.
     * We'll later use this to access the builtins and add to their internals.
     */
    private final JanitorDefaultEnvironment ENV = new JanitorDefaultEnvironment(new JanitorFormattingGerman()) {
        @Override
        public void warn(final String message) {
            // not needed for this test
        }
    };

    /**
     * Create a runtime.
     * It's not particularly important for this test case.
     */
    private final BaseRuntime RT = new BaseRuntime(ENV) {
        @Override
        public JanitorObject print(final JanitorScriptProcess process, final JCallArgs args) {
            // not needed for this test
            return JNull.NULL;
        }
    };

    final AtomicLong runCount = new AtomicLong(0);

    /**
     * Test adding methods and properties to built-in classes.
     * @throws JanitorRuntimeException if the script fails
     * @throws JanitorCompilerException if the script can't be compiled
     */
    @Test
    public void testExtensionMethods() throws JanitorRuntimeException, JanitorCompilerException {
        Janitor.setUserProvider(JanitorEnvironmentProvider.returning(ENV));

        assertEquals(1L, runCount.incrementAndGet());

        // add a method to the string class
        final @Language("Janitor") String scriptSource = "'John Dorian'.sayHello();";
        final RunnableScript script = RT.compile("test", scriptSource);
        assertThrows(JanitorNameException.class, script::run); // can't work, because there's no sayHello method on strings!
        ENV.getBuiltinTypes().internals().getStringDispatcher().addMethod("sayHello", (self, runningScript, arguments) -> ENV.getBuiltinTypes().string("Hello, " + self.janitorGetHostValue()));
        assertEquals("Hello, John Dorian", script.run().janitorGetHostValue()); // works, because now the method exists!

        // add a property to the string class
        final @Language("Janitor") String scriptSource2 = "return ('cbaa23' + 'thx1138').numberOfDigits * 2;";
        final RunnableScript script2 = RT.compile("test", scriptSource2);

        RT.setTraceListener(message -> System.out.println("TRACE: " + message));
        assertFalse(ENV.getBuiltinTypes().internals().getStringDispatcher().has("numberOfDigits"));


        assertThrows(JanitorNameException.class, script2::run); // can't work, because there's no such property


        ENV.getBuiltinTypes().internals().getStringDispatcher().addLongProperty("numberOfDigits", (self) -> self.janitorGetHostValue().codePoints().filter(Character::isDigit).count());
        assertEquals(12L, script2.run().janitorGetHostValue()); // now it works

        // add a method to the int class
        final @Language("Janitor") String scriptSource3 = "return '12345'.numberOfDigits.times2;";
        final RunnableScript script3 = RT.compile("test", scriptSource3);
        assertThrows(JanitorNameException.class, script3::run); // can't work, because there's no such property
        ENV.getBuiltinTypes().internals().getIntDispatcher().addLongProperty("times2", (self) -> self.janitorGetHostValue() * 2);
        assertEquals(10L, script3.run().janitorGetHostValue()); // now there it is


        // Finally, add a property to all built-in classes. Your own classes can opt in to this, too, by accessing the baseDispatcher
        // explicitly when creating your own dispatch table, or by calling into the baseDispatcher at runtime.
        ENV.getBuiltinTypes().internals().getBaseDispatcher().addStringProperty("omega", self -> "Ω");
        final @Language("Janitor") String scriptSource4 = "return (17).omega;"; // parens are necessary here, because 17.omega would (fail to) parse as float
        final RunnableScript script4 = RT.compile("test", scriptSource4);
        assertEquals("Ω", script4.run().janitorGetHostValue());

        // Yes, dates have the new attribute, too.
        final @Language("Janitor") String scriptSource5 = "return @today.omega;";
        final RunnableScript script5 = RT.compile("test", scriptSource5);
        assertEquals("Ω", script5.run().janitorGetHostValue());

        // Extending built-in classes couldn't be easier, IMHO. The signature (self, runningScript, arguments) is a bit cumbersome to remember, though.
        // Workaround: when your IDE suggests to write "new JUnboundMethod...", autocomplete this and have a normal signature to look at. Then,
        // write your code, and finally let your IDE collapse everything to be written like above, if you prefer. That works fine in IntelliJ IDEA.
        // Properties are easier to write; just keep in mind that the method names in the Dispatch Table refer to Java "Long", "Int" etc., not to
        // scripting types, then the mental model should be clear.
    }



}

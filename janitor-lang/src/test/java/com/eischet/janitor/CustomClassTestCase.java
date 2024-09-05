package com.eischet.janitor;

import com.eischet.janitor.api.RunnableScript;
import com.eischet.janitor.api.types.functions.JCallArgs;
import com.eischet.janitor.api.errors.compiler.JanitorCompilerException;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.api.types.builtin.JNull;
import com.eischet.janitor.api.types.builtin.JString;
import com.eischet.janitor.api.types.composed.JanitorComposed;
import com.eischet.janitor.api.types.dispatch.DispatchTable;
import com.eischet.janitor.runtime.OutputCatchingTestRuntime;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test and demonstrate how a custom class can be added to the interpreter and how it can be used from within scripts.
 */
public class CustomClassTestCase {

    /**
     * Our custom object, based on the JanitorComposed approach.
     */
    public static class Dog extends JanitorComposed<Dog> {

        // Create a dispatch table. This can be shared between all instances of a class, so make it static:
        public static final DispatchTable<Dog> DISPATCH = new DispatchTable<>();

        // Define instance variables as usual (because they ARE just regular ones):
        // (These definitions are up here so you can see them before you look at the dispatch table next.
        // Usually I'd keep static and non-static class attributes separate.)
        private int walks;
        private String name;
        private String furColor;

        // Fill the Dispatch Table with some attributes.
        static {
            // Read-only ears property, fixed to 2, and legs, fixed to 4.
            // There's no equivalent field in the Java code, which is totally OK for us.
            DISPATCH.addIntegerProperty("ears", dog -> 2);
            DISPATCH.addIntegerProperty("legs", dog -> 4);
            // Define a bark() method: I usually start with "new UnboundMethod<...>, which my IDE (IntelliJ IDEA)
            // later folds into this nice short form:
            DISPATCH.addMethod("bark", (self, process, arguments) -> {
                @NotNull JString sound = process.getBuiltins().string("woof");
                @NotNull JCallArgs args = new JCallArgs("print", process, List.of(sound));
                process.getRuntime().print(process, args);
                return JNull.NULL; // "void methods" simply return NULL.
            });
            // Get walked, counting how often it happened -- I'm sure many dogs keep track of that ;-)
            DISPATCH.addMethod("walk", (self, process, arguments) -> {
                self.walks++;
                return JNull.NULL;
            });
            DISPATCH.addStringProperty("name", Dog::getName, Dog::setName);
            DISPATCH.addStringProperty("furColor", Dog::getFurColor, Dog::setFurColor);
            DISPATCH.addIntegerProperty("numberOfWalks", dog -> dog.walks, (dog, newNumber) -> dog.walks = newNumber);
            DISPATCH.setConstructor((runningScript, arguments) -> {
                arguments.require(0); // no args expected, fail if any are given.
                return new Dog();
            });
        }

        public Dog() {
            super(DISPATCH);
        }

        public int getWalks() {
            return walks;
        }

        public void setWalks(int walks) {
            this.walks = walks;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getFurColor() {
            return furColor;
        }

        public void setFurColor(String furColor) {
            this.furColor = furColor;
        }
    }

    @Test
    public void parcours() throws JanitorCompilerException, JanitorRuntimeException {
        final OutputCatchingTestRuntime rt = OutputCatchingTestRuntime.fresh();
        final RunnableScript runnable = rt.compile("parcour", """
                snoopy = Dog();
                snoopy.name = "Snoopy";
                snoopy.furColor = "black-white";
                snoopy.walk();
                snoopy.bark();
                snoopy.walk();
                snoopy.bark();
                assert(snoopy.numberOfWalks == 2);
                assert(snoopy.legs == 4);
                assert(snoopy.ears == 2);
                return snoopy;
                """);
        final @NotNull JanitorObject result = runnable.run(g -> g.bind("Dog", Dog.DISPATCH.getConstructor().asObject("Dog")));
        assertInstanceOf(Dog.class, result);

        assertEquals("woof\nwoof\n", rt.getAllOutput());


        final Dog snoopy = (Dog) result;
        assertEquals("Snoopy", snoopy.getName());
        assertEquals("black-white", snoopy.getFurColor());
        assertEquals(2, snoopy.walks);
        snoopy.setWalks(7);
        assertEquals(7, snoopy.walks);

        rt.compile("check", "assert(snoopy.numberOfWalks == 7)").run(g -> g.bind("snoopy", snoopy));

    }

    /**
     * Two dogs "racing". This variant does not supply the Dog constructor to the script, instead binding two
     * ready-made dogs.
     * @throws JanitorCompilerException on errors
     */
    @Test
    public void race() throws JanitorCompilerException, JanitorRuntimeException {
        final Dog john = new Dog();
        john.setName("John");
        final Dog jane = new Dog();
        jane.setName("Jane");

        final OutputCatchingTestRuntime rt = OutputCatchingTestRuntime.fresh();
        final RunnableScript runnable = rt.compile("race", """
                for (i in [1,2,3,4,5,6,7,8,9,10]) {
                    for (dog in [john, jane]) {
                        dog.walk();
                    }
                }
                jane.walk(); // in a last second effort, Jane jumps ahead for the gold medal ;-)
                """);
        runnable.run(g -> g.bind("john", john).bind("jane", jane));
        final Dog winner = Stream.of(john, jane).max(Comparator.comparing(dog -> dog.walks)).orElse(null);
        assertSame(winner, jane);

        assertEquals(10, john.walks);
        assertEquals(11, jane.walks);
    }

}

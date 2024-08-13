package com.eischet.janitor;

import com.eischet.janitor.CustomClassTestCase.Dog;
import com.eischet.janitor.api.FilterPredicate;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.api.types.builtin.JList;
import com.eischet.janitor.runtime.OutputCatchingTestRuntime;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Use scripts as filters.
 * <p>
 * Being able to do user-defined filtering records from a set to a smaller set was one of the original
 * motivations for writing Janitor. The idea is to write a script that doubles as a Predicate and then
 * use normal Java Stream functionality etc. to apply this.
 * </p>
 * <p>
 * You obviously can only apply this kind of Predicate to objects that derive from JanitorObject, therefore
 * the FilterPredicate interface extends Predicate&lt;JanitorObject&gt; accordingly.
 * </p>
 */
public class FilterScriptTestCase {

    /**
     * Very simple: take a list of numbers, manually converted to Janitor Objects, and apply various
     * filters to them.
     */
    @Test
    public void testFilterScript() {
        final OutputCatchingTestRuntime rt = OutputCatchingTestRuntime.fresh();
        @NotNull JList numbers = rt.getBuiltinTypes().list(
                Stream.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9)
                        .map(number -> rt.getEnvironment().getBuiltinTypes().integer(number))
        );

        final @NotNull FilterPredicate passAll = rt.getEnvironment().filterScript("all", "value >= 0", null);
        final @NotNull FilterPredicate passEven = rt.getEnvironment().filterScript("even", "value % 2 == 0", null);
        final @NotNull FilterPredicate passOdd = rt.getEnvironment().filterScript("odd", "value % 2 == 1", null);
        final @NotNull FilterPredicate passNone = rt.getEnvironment().filterScript("none", "value > 1000", null);
        final @NotNull FilterPredicate passToFour = rt.getEnvironment().filterScript("toFour", "value < 5", null);
        final @NotNull FilterPredicate passFromFive = rt.getEnvironment().filterScript("fromFive", "value >= 5", null);

        assertTrue(passAll.test(rt.getBuiltinTypes().integer(1)));
        assertTrue(passAll.test(rt.getBuiltinTypes().integer(0)));
        assertFalse(passAll.test(rt.getBuiltinTypes().integer(-1)));

        // The L suffixes are required because of how JInt works internally (with a long), which might change in the future!
        assertEquals(List.of(0L, 1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L), numbers.stream().filter(passAll).map(JanitorObject::janitorGetHostValue).toList());
        assertEquals(List.of(0L, 2L, 4L, 6L, 8L), numbers.stream().filter(passEven).map(JanitorObject::janitorGetHostValue).toList());
        assertEquals(List.of(1L, 3L, 5L, 7L, 9L), numbers.stream().filter(passOdd).map(JanitorObject::janitorGetHostValue).toList());
        assertEquals(Collections.emptyList(), numbers.stream().filter(passNone).map(JanitorObject::janitorGetHostValue).toList());
        assertEquals(List.of(0L, 1L, 2L, 3L, 4L), numbers.stream().filter(passToFour).map(JanitorObject::janitorGetHostValue).toList());
        assertEquals(List.of(5L, 6L, 7L, 8L, 9L), numbers.stream().filter(passFromFive).map(JanitorObject::janitorGetHostValue).toList());

        // Try combining two different filter predicates via Predicate::and:
        assertEquals(List.of(6L, 8L),
                numbers.stream().filter(passEven.and(passFromFive)).map(JanitorObject::janitorGetHostValue).toList()
        );

    }

    /**
     * Slightly more complex: borrow the Dog class from the CustomClassTestCase and apply filters to
     * some of human's best friends.
     */
    @Test
    public void doggyBag() {
        final Dog snoopy = new Dog();
        snoopy.setName("Snoopy");

        final Dog jane = new Dog();
        jane.setName("Jane");

        final Dog john = new Dog();
        john.setName("John");

        final List<Dog> allDogs = List.of(snoopy, jane, john);

        final OutputCatchingTestRuntime rt = OutputCatchingTestRuntime.fresh();
        @NotNull FilterPredicate regular = rt.getEnvironment().filterScript("regular", "value.legs > 3 and value.legs < 5", null);
        @NotNull FilterPredicate movieStars = rt.getEnvironment().filterScript("movieStars", "value.name == 'Snoopy'", null);
        @NotNull FilterPredicate startsWithJ = rt.getEnvironment().filterScript("startsWithJ", "value.name.startsWith('J')", null);

        assertEquals(List.of(snoopy, jane, john), allDogs.stream().filter(regular).toList());
        assertEquals(List.of(snoopy), allDogs.stream().filter(movieStars).toList());
        assertEquals(List.of(jane, john), allDogs.stream().filter(startsWithJ).toList());

        assertEquals(Collections.emptyList(), allDogs.stream().filter(movieStars.and(startsWithJ)).toList());
        assertEquals(Collections.emptyList(), allDogs.stream().filter(movieStars).filter(startsWithJ).toList());



    }

}

/*
 * © Eischet Software e.K., Köln
 */

package com.eischet.janitor.tests;

import com.eischet.janitor.api.errors.compiler.JanitorCompilerException;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.types.JBool;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.runtime.JanitorScript;
import com.eischet.janitor.runtime.TestingRuntime;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;


public class AppScriptingTestCase2 {

    private static final Logger log = LoggerFactory.getLogger(AppScriptingTestCase2.class);

    @Test
    public void propertyPlucking() throws JanitorCompilerException, JanitorRuntimeException {
        /*
        final AssystBuilding building = new AssystBuilding();
        building.name.setValue("bar");

        final AssystBuildingRoom buildingRoom = new AssystBuildingRoom();
        buildingRoom.building.setValue(building);

        final AssystContactUser contact = new AssystContactUser();
        contact.buildingRoom.setValue(buildingRoom);
        final AssystSectionDepartment sd = new AssystSectionDepartment();
        sd.id.setValue(17);
        contact.sectionDepartment.setValue(sd);

        final TestingRuntime tr = new TestingRuntime();
        final JanitorScript cs = tr.compile("test", "'foo' + contact.buildingRoom.building.name");
        final JanitorObject result = cs.run(g -> g.bind("contact", contact));
        log.info("script result: " + result);
        assertEquals("foobar", result.janitorGetHostValue());

        final JanitorScript cs2 = tr.compile("test", "1 + contact.sectionDepartment.id / 2.0");
        final JanitorObject result2 = cs2.run(g -> g.bind("contact", contact));
        log.info("script result 2: " + result2);
        assertEquals(9.5d, result2.janitorGetHostValue());

         */
    }


    @Test
    public void customFilteringInts() throws JanitorCompilerException, JanitorRuntimeException {
        final List<Integer> someInts = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        final JanitorScript filterScript = new JanitorScript(new TestingRuntime(), "test", "i > 2 and i < 6");
        final List<Integer> filteredInts = new ArrayList<>();
        for (final Integer someInt : someInts) {
            final JanitorObject result = filterScript.run(g -> g.bind("i", someInt));
            System.out.printf("i = %s --> %s [%s]%n", someInt, result, result.getClass());
            if (JBool.TRUE.equals(result)) {
                filteredInts.add(someInt);
            }
        }
        log.info("list:     " + someInts);
        log.info("script:   " + filterScript.getSource());
        log.info("filtered: " + filteredInts);
        assertEquals(Arrays.asList(3, 4, 5), filteredInts);
    }

    @Test
    public void customFilteringUsers() {
        // build some users
        /*
        final List<AssystContactUser> users = new ArrayList<>();
        final AssystSection ev = new AssystSection();
        ev.shortCode.setValue("IT-EVTMGMT");
        final AssystSection oth = new AssystSection();
        oth.shortCode.setValue("OTHER");

        final AssystSectionDepartment a = new AssystSectionDepartment();
        a.section.setValue(ev);

        final AssystSectionDepartment b = new AssystSectionDepartment();
        b.section.setValue(oth);

        for (int i=1; i<=20; i++) {
            final AssystContactUser c = new AssystContactUser();
            c.id.setValue(i);
            c.sectionDepartment.setValue(a);
            users.add(c);
        }
        for (int i=101; i<=120; i++) {
            final AssystContactUser c = new AssystContactUser();
            c.id.setValue(i);
            c.sectionDepartment.setValue(b);
            users.add(c);
        }

        final Predicate<JanitorObject> fi = FilterScript.of("test", "section.shortCode != 'IT-EVTMGMT'");

        assertEquals(40, users.size());
        assertEquals(20, users.stream().filter(fi).count());


        final FilterPredicate fi1 = FilterScript.of("test", "not ( shortCode ~ 'MAIL_*' ) and not ( shortCode ~ 'SMS_*' ) and not ( shortCode ~ 'Ring_*' ) and not ( shortCode ~ 'SYS_*' ) and not ( shortCode ~ 'OMD_*' ) and not ( shortCode ~ 'XXX_*' )");
        assertNotNull(fi1.getFilterScript());
        // LATER: tatsächlich fi1 ausführen!

        final FilterPredicate fi2 = FilterScript.of("test", "shortCode !~ 'Z*_*'");
        assertNotNull(fi2.getFilterScript());
        // LATER: tatsächlich fi2 ausführen!

        final FilterPredicate fi3 = FilterScript.of("test", "not ( shortCode ~ 'MAIL_*' ) and not ( shortCode ~ 'SMS_*' ) and not ( shortCode ~ 'Ring_*' ) and not ( shortCode ~ 'SYS_*' ) and not ( shortCode ~ 'OMD_*' ) and not ( shortCode ~ 'XXX_*' )");
        assertNotNull(fi3.getFilterScript());
        // LATER: tatsächlich fi3 ausführen!

        final FilterPredicate fi4 = FilterScript.of("test", "not ( shortCode ~ 'MAIL_*' ) and not ( shortCode ~ 'SMS_*' ) and not ( shortCode ~ 'Ring_*' ) and not ( shortCode ~ 'SYS_*' ) and not ( shortCode ~ 'OMD_*' ) and not ( shortCode ~ 'XXX_*' )");
        assertNotNull(fi4.getFilterScript());
        // LATER: tatsächlich fi4 ausführen!

        assertEquals(20, users.stream().filter(FilterScript.of("test", "id <= 10 or id >= 111")).count());
        assertEquals(20, users.stream().filter(FilterScript.of("test", "id > 10 and id <= 110")).count());
        assertEquals(20, users.stream().filter(FilterScript.of("test", "section.shortCode == 'IT-EVTMGMT'")).count());

        assertEquals(40, users.stream().filter(FilterScript.of("test", "true")).count());
        assertEquals(40, users.stream().filter(FilterScript.of("test", "not false")).count());
        assertEquals(40, users.stream().filter(FilterScript.of("test", "not not true")).count());
        assertEquals(0, users.stream().filter(FilterScript.of("test", "false")).count());
        assertEquals(0, users.stream().filter(FilterScript.of("test", "not true")).count());
        assertEquals(0, users.stream().filter(FilterScript.of("test", "not not false")).count());

        assertEquals(0, users.stream().filter(FilterScript.of("test", "false and true")).count());
        assertEquals(40, users.stream().filter(FilterScript.of("test", "false or true")).count());

        assertSame(ev, Stream.of(ev, oth).filter(FilterScript.of("test", "shortCode == 'IT-EVTMGMT'")).findFirst().orElse(null));
        assertSame(oth, Stream.of(ev, oth).filter(FilterScript.of("test", "shortCode != 'IT-EVTMGMT'")).findFirst().orElse(null));
        // <> wird nicht mehr unterstützt, da nur behämmert: assertSame(oth, Stream.of(ev, oth).filter(FilterScript.of("shortCode <> 'IT-EVTMGMT'")).findFirst().orElse(null));

        assertSame(oth, Stream.of(ev, oth).filter(FilterScript.of("test", "if shortCode != 'IT-EVTMGMT' then true else false")).findFirst().orElse(null));
*/

    }

}

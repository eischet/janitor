package com.eischet.janitor.modules.pdf;

import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.errors.runtime.JanitorArgumentException;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.api.types.builtin.JList;
import com.eischet.janitor.api.types.builtin.JNumber;
import com.eischet.janitor.api.types.builtin.JString;
import com.eischet.janitor.api.types.functions.JCallArgs;
import com.eischet.janitor.api.types.wrapped.JanitorWrapper;
import org.jetbrains.annotations.NotNull;
import org.openpdf.text.Element;

import java.util.ArrayList;
import java.util.List;

/**
 * Shared helpers for converting between Janitor script values and OpenPDF/LibrePDF types.
 */
public final class PdfElements {

    private PdfElements() {
    }

    /**
     * Unwraps a script value that must be one of our {@code JPDF*} wrappers around an OpenPDF {@link Element}
     * (e.g. Chunk, Phrase, Paragraph, List, ListItem, Anchor, Chapter, Section, Image, ...).
     */
    public static @NotNull Element requireElement(final @NotNull JanitorScriptProcess process, final @NotNull JanitorObject value) throws JanitorRuntimeException {
        if (value instanceof JanitorWrapper<?> wrapper && wrapper.janitorGetHostValue() instanceof Element element) {
            return element;
        }
        throw new JanitorArgumentException(process, "expected a pdf element (Chunk, Phrase, Paragraph, List, ListItem, Anchor, ...), but got " + value.janitorClassName());
    }

    public static @NotNull Element requireElement(final @NotNull JanitorScriptProcess process, final @NotNull JCallArgs args, final int position) throws JanitorRuntimeException {
        return requireElement(process, args.get(position));
    }

    /**
     * Reads a float-ish argument (a JFloat or a JInt -- Janitor int literals like "18" don't auto-coerce
     * to JCallArgs.getFloat()'s JFloat, so we accept any JNumber here instead).
     */
    public static float floatArg(final @NotNull JanitorScriptProcess process, final @NotNull JCallArgs args, final int position) throws JanitorRuntimeException {
        final JanitorObject value = args.get(position);
        if (value instanceof JNumber number) {
            return (float) number.toDouble();
        }
        throw new JanitorArgumentException(process, "argument " + position + " must be a number, but got " + value.janitorClassName());
    }

    public static float optionalFloatArg(final @NotNull JanitorScriptProcess process, final @NotNull JCallArgs args, final int position, final float defaultValue) throws JanitorRuntimeException {
        if (args.size() <= position) {
            return defaultValue;
        }
        return floatArg(process, args, position);
    }

    /**
     * Converts a script-side list of numbers (e.g. [1, 2, 3]) into a float[], for APIs like
     * PdfPTable(float[]) / PdfPTable.setWidths(float[]).
     */
    public static float @NotNull [] floatArrayArg(final @NotNull JanitorScriptProcess process, final @NotNull JanitorObject value) throws JanitorRuntimeException {
        if (!(value instanceof JList list)) {
            throw new JanitorArgumentException(process, "expected a list of numbers, but got " + value.janitorClassName());
        }
        final float[] result = new float[list.size()];
        for (int i = 0; i < result.length; i++) {
            if (!(list.get(i) instanceof JNumber number)) {
                throw new JanitorArgumentException(process, "expected a list of numbers, but element " + i + " is " + list.get(i).janitorClassName());
            }
            result[i] = (float) number.toDouble();
        }
        return result;
    }

    /**
     * Converts a script-side list of strings (e.g. ["a.pdf", "b.pdf"]) into a List&lt;String&gt;, for APIs
     * like PdfModule.concat(target, sourceFileNames).
     */
    public static @NotNull List<String> stringListArg(final @NotNull JanitorScriptProcess process, final @NotNull JanitorObject value) throws JanitorRuntimeException {
        if (!(value instanceof JList list)) {
            throw new JanitorArgumentException(process, "expected a list of strings, but got " + value.janitorClassName());
        }
        final List<String> result = new ArrayList<>(list.size());
        for (int i = 0; i < list.size(); i++) {
            if (!(list.get(i) instanceof JString string)) {
                throw new JanitorArgumentException(process, "expected a list of strings, but element " + i + " is " + list.get(i).janitorClassName());
            }
            result.add(string.janitorGetHostValue());
        }
        return result;
    }

}

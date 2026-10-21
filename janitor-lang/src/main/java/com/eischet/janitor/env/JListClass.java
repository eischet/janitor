// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.env;

import com.eischet.janitor.api.Janitor;
import com.eischet.janitor.api.JanitorEnvironment;
import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.types.dispatch.DispatchTable;
import com.eischet.janitor.api.types.functions.JCallArgs;
import com.eischet.janitor.api.errors.runtime.JanitorArgumentException;
import com.eischet.janitor.api.errors.runtime.JanitorNativeException;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.types.builtin.*;
import com.eischet.janitor.compiler.JanitorAntlrCompiler;
import com.eischet.janitor.toolbox.json.api.JsonException;
import com.eischet.janitor.api.types.functions.JCallable;
import com.eischet.janitor.api.types.*;
import com.eischet.janitor.toolbox.json.api.JsonInputStream;
import org.intellij.lang.annotations.Language;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Operations for list objects.
 */
public class JListClass {


    /**
     * Script method {@code list.parseJson(json)}: reads a JSON array into this list.
     * @param self the object that the method is called on
     * @param process the running script process
     * @param arguments the call arguments
     * @return the list
     * @throws JanitorRuntimeException if the call arguments are invalid or the operation fails
     */
    public static JList __parseJson(final JList self, final JanitorScriptProcess process, final JCallArgs arguments) throws JanitorRuntimeException {
        try {
            final String json = arguments.require(1).getString(0).janitorGetHostValue();
            if (json == null || json.isBlank()) {
                return self;
            }
            // use the script's own environment, not Janitor.current(): hosts may run several differently configured sandboxes
            final JanitorEnvironment environment = process.getEnvironment();
            self.readJson(environment, environment.getLenientJsonConsumer(json));
            return self;
        } catch (JsonException e) {
            throw new JanitorNativeException(process, "error parsing json", e);
        }
    }

    /**
     * Read a JSON string, representing a list, appending its elements to this list.
     *
     * @param self the list to read into
     * @param json the JSON string; null or blank strings leave the list unchanged
     * @return this list
     * @throws JsonException if the JSON is invalid, e.g. it's not really a list
     */
    public static JList parseJson(final JList self, @Language("JSON") final String json) throws JsonException {
        if (json == null || json.isBlank()) {
            return self;
        }
        final JsonInputStream reader = Janitor.current().getLenientJsonConsumer(json);
        return parseJson(self, reader);
    }


    /**
     * Read a JSON array from a stream, appending its elements to this list.
     * This delegates to {@link JList#readJson(JsonInputStream)}.
     *
     * @param self the list to read into
     * @param reader the JSON reader, positioned at the start of an array
     * @return this list
     * @throws JsonException if the JSON is invalid, e.g. it's not really a list
     */
    public static JList parseJson(final JList self, final JsonInputStream reader) throws JsonException {
        self.readJson(reader);
        return self;
    }


    /**
     * Script method {@code list.toJson()}: converts this list to JSON.
     * @param self the object that the method is called on
     * @param process the running script process
     * @param arguments the call arguments
     * @return the JSON text
     * @throws JanitorRuntimeException if the call arguments are invalid or the operation fails
     */
    public static JString __toJson(final JList self, final JanitorScriptProcess process, final JCallArgs arguments) throws JanitorRuntimeException {
        try {
            arguments.require(0);
            return Janitor.string(self.exportToJson(process.getRuntime().getEnvironment()));
        } catch (JsonException e) {
            throw new JanitorNativeException(process, "error exporting json", e);
        }
    }

    /**
     * Script method {@code list.count(x)}: counts how often an element occurs in this list.
     * @param self the object that the method is called on
     * @param process the running script process
     * @param arguments the call arguments
     * @return the number of occurrences
     * @throws JanitorRuntimeException if the call arguments are invalid or the operation fails
     */
    public static JInt __count(final JList self, final JanitorScriptProcess process, final JCallArgs arguments) throws JanitorRuntimeException {
        final JanitorObject countable = arguments.require(1).get(0);
        int count = 0;
        for (final JanitorObject element : self.janitorGetHostValue()) {
            if (Objects.equals(countable, element)) {
                ++count;
            }
        }
        return Janitor.integer(count);
    }

    /**
     * Script method {@code list.filter(f)}: creates a new list with those elements for which the function returns true.
     * @param self the object that the method is called on
     * @param process the running script process
     * @param arguments the call arguments
     * @return the filtered list
     * @throws JanitorRuntimeException if the call arguments are invalid or the operation fails
     */
    public static JList __filter(final JList self, final JanitorScriptProcess process, final JCallArgs arguments) throws JanitorRuntimeException {
        arguments.require(1);
        final JanitorObject callable = arguments.getRequired(0, JanitorObject.class);
        if (callable instanceof JCallable func) {
            JList result = Janitor.list();
            for (final JanitorObject e : self.janitorGetHostValue()) {
                if (func.call(process, new JCallArgs("filter", process, Collections.singletonList(e))).janitorIsTrue()) {
                    result.add(e);
                }
            }
            return result;
        } else {
            throw new JanitorArgumentException(process, "invalid list::filter parameter: " + callable);
        }
    }

    /**
     * Script method {@code list.map(f)}: creates a new list with the results of calling the function on each element.
     * @param self the object that the method is called on
     * @param process the running script process
     * @param arguments the call arguments
     * @return the mapped list
     * @throws JanitorRuntimeException if the call arguments are invalid or the operation fails
     */
    public static JList __map(final JList self, final JanitorScriptProcess process, final JCallArgs arguments) throws JanitorRuntimeException {
        arguments.require(1);
        final JanitorObject callable = arguments.getRequired(0, JanitorObject.class);
        if (callable instanceof JCallable func) {
            final JList result = Janitor.list(self.janitorGetHostValue().size());
            for (final JanitorObject e : self.janitorGetHostValue()) {
                result.add(func.call(process, new JCallArgs("map", process, Collections.singletonList(e))));
            }
            return result;
        } else {
            throw new JanitorArgumentException(process, "invalid list::map parameter: " + callable);
        }
    }

    /**
     * Script method {@code list.join(separator)}: joins the string representations of the elements, separated by a space by default.
     * @param self the object that the method is called on
     * @param process the running script process
     * @param arguments the call arguments
     * @return the joined string
     * @throws JanitorRuntimeException if the call arguments are invalid or the operation fails
     */
    public static JString __join(final JList self, final JanitorScriptProcess process, final JCallArgs arguments) throws JanitorRuntimeException {
        arguments.require(0, 1);
        final String separator = arguments.getOptionalStringValue(0, " ");
        return Janitor.string(self.janitorGetHostValue().stream().map(JanitorObject::janitorToString).collect(Collectors.joining(separator)));
    }

    /**
     * Script method {@code list.toList()}: creates a copy of this list.
     * @param self the object that the method is called on
     * @param process the running script process
     * @param arguments the call arguments
     * @return the copy
     * @throws JanitorRuntimeException if the call arguments are invalid or the operation fails
     */
    public static JList __toList(final JList self, final JanitorScriptProcess process, final JCallArgs arguments) throws JanitorRuntimeException {
        arguments.require(0);
        return Janitor.list(self.janitorGetHostValue());
    }

    /**
     * Script method {@code list.toSet()}: creates a set from the elements of this list.
     * @param self the object that the method is called on
     * @param process the running script process
     * @param arguments the call arguments
     * @return the set
     * @throws JanitorRuntimeException if the call arguments are invalid or the operation fails
     */
    public static JSet __toSet(final JList self, final JanitorScriptProcess process, final JCallArgs arguments) throws JanitorRuntimeException {
        arguments.require(0);
        return Janitor.set(self.janitorGetHostValue().stream());
    }

    /**
     * Script method {@code list.isEmpty()}: checks whether this list has no elements.
     * @param self the object that the method is called on
     * @param process the running script process
     * @param arguments the call arguments
     * @return true if the list is empty
     * @throws JanitorRuntimeException if the call arguments are invalid or the operation fails
     */
    public static JBool __isEmpty(final JList self, final JanitorScriptProcess process, final JCallArgs arguments) throws JanitorRuntimeException {
        arguments.require(0);
        return Janitor.toBool(self.janitorGetHostValue().isEmpty());
    }

    /**
     * Script method {@code list.size()}: the number of elements in this list.
     * @param self the object that the method is called on
     * @param process the running script process
     * @param arguments the call arguments
     * @return the number of elements
     * @throws JanitorRuntimeException if the call arguments are invalid or the operation fails
     */
    public static JInt __size(final JList self, final JanitorScriptProcess process, final JCallArgs arguments) throws JanitorRuntimeException {
        arguments.require(0);
        return Janitor.integer(self.janitorGetHostValue().size());
    }

    /**
     * Script method {@code list.contains(x)}: checks whether this list contains an element that is equal to the argument.
     * @param self the object that the method is called on
     * @param process the running script process
     * @param arguments the call arguments
     * @return true if the element was found
     * @throws JanitorRuntimeException if the call arguments are invalid or the operation fails
     */
    public static JBool __contains(final JList self, final JanitorScriptProcess process, final JCallArgs arguments) throws JanitorRuntimeException {
        final JanitorObject countable = arguments.require(1).get(0);
        for (final JanitorObject element : self.janitorGetHostValue()) {
            if (Janitor.Semantics.areEquals(countable, element).janitorIsTrue()) {
                return JBool.TRUE;
            }
        }
        return JBool.FALSE;
    }

    /**
     * Script method {@code list.randomSublist(n)}: picks n random elements from this list.
     * @param self the object that the method is called on
     * @param process the running script process
     * @param arguments the call arguments
     * @return a new list with n elements, or a copy of this list if it has fewer
     * @throws JanitorRuntimeException if the call arguments are invalid or the operation fails
     */
    public static JList __randomSublist(final JList self, final JanitorScriptProcess process, final JCallArgs arguments) throws JanitorRuntimeException {
        arguments.require(1);
        final int count = arguments.getInt(0).getAsInt();
        if (count >= self.janitorGetHostValue().size()) {
            return Janitor.list(self.janitorGetHostValue());
        }
        final Random random = new Random();
        final Set<Integer> indexes = new HashSet<>();
        while (indexes.size() < count) {
            indexes.add(random.nextInt(self.janitorGetHostValue().size()));
        }
        final List<JanitorObject> result = new ArrayList<>(indexes.size());
        indexes.forEach(i -> result.add(self.janitorGetHostValue().get(i)));
        return Janitor.list(result);
    }

    /**
     * Script method {@code list.put(index, x)}: replaces the element at the given index.
     * @param self the object that the method is called on
     * @param process the running script process
     * @param arguments the call arguments
     * @return null
     * @throws JanitorRuntimeException if the call arguments are invalid or the operation fails
     */
    public static JNull __put(final JList self, final JanitorScriptProcess process, final JCallArgs arguments) throws JanitorRuntimeException {
        // The size check already happened via arguments.require(2) above -- there used to be a
        // second, errant arguments.require(1) call here (demanding *exactly* 1 argument) before
        // reading the index, which made this method always throw whenever actually called with the
        // 2 arguments it needs, i.e. list.put(i, x) never worked at all.
        arguments.require(2);
        self.put(arguments.getInt(0), arguments.get(1));
        return JNull.NULL;
    }

    /**
     * Script method {@code list.addAll(other)}: appends all elements of another list.
     * @param self the object that the method is called on
     * @param process the running script process
     * @param arguments the call arguments
     * @return null
     * @throws JanitorRuntimeException if the call arguments are invalid or the operation fails
     */
    public static JNull __addAll(final JList self, final JanitorScriptProcess process, final JCallArgs arguments) throws JanitorRuntimeException {
        arguments.require(1);
        for (final JanitorObject jObj : arguments.getRequired(0, JList.class)) {
            self.add(jObj);
        }
        return JNull.NULL;
    }

    /**
     * Script method {@code list.sort()}: sorts this list in place.
     * @param self the object that the method is called on
     * @param process the running script process
     * @param arguments the call arguments
     * @return null
     * @throws JanitorRuntimeException if the call arguments are invalid or the operation fails
     */
    public static JNull __sort(final JList self, final JanitorScriptProcess process, final JCallArgs arguments) throws JanitorRuntimeException {
        arguments.require(0);
        self.replaceAllElements(self.stream().sorted().toList());
        return JNull.NULL;
    }

    /**
     * Script method {@code list.add(x)} or {@code list.add(index, x)}: appends an element, or inserts it at the given index.
     * @param self the object that the method is called on
     * @param process the running script process
     * @param arguments the call arguments
     * @return null
     * @throws JanitorRuntimeException if the call arguments are invalid or the operation fails
     */
    public static JNull __add(final JList self, final JanitorScriptProcess process, final JCallArgs arguments) throws JanitorRuntimeException {
        arguments.require(1, 2);
        if (arguments.size() == 1) {
            self.add(arguments.get(0));
        } else {
            self.add(arguments.getInt(0), arguments.get(1));
        }
        return JNull.NULL;
    }

    /**
     * Copy the list.
     *
     * @param self list
     * @param process process
     * @param arguments args, must be empty
     * @return a shallow copy of the list
     * @throws JanitorRuntimeException on runtime errors
     */
    public static JList __copy(final JList self, final JanitorScriptProcess process, final JCallArgs arguments) throws JanitorRuntimeException {
        arguments.notAllowed();
        return Janitor.list(self.janitorGetHostValue());
    }

    /**
     * Script method {@code list.get(index)}, {@code list.get(from, to)} or {@code list.get(from, to, step)}: gets an element or a (read-only) range of elements.
     * @param self the object that the method is called on
     * @param process the running script process
     * @param arguments the call arguments
     * @return the element or the range
     * @throws JanitorRuntimeException if the call arguments are invalid or the operation fails
     */
    public static JanitorObject __get(final JList self, final JanitorScriptProcess process, final JCallArgs arguments) throws JanitorRuntimeException {
        arguments.require(1, 3);
        if (arguments.size() == 1) {
            return self.get(arguments.require(1).getInt(0));
        }
        if (arguments.size() == 2) {
            if (arguments.get(0) == JNull.NULL && arguments.get(1) == JNull.NULL) {
                return self.getRange(Janitor.integer(0), Janitor.integer(self.size()));
            } else if (arguments.get(0) == JNull.NULL) {
                return self.getRange(Janitor.integer(0), arguments.getInt(1));
            } else if (arguments.get(1) == JNull.NULL) {
                return self.getRange(arguments.getInt(0), Janitor.integer(self.size()));
            }
            return self.getRange(arguments.getInt(0), arguments.getInt(1));
        }
        if (arguments.size() == 3) {
            return self.getSteppedRange(optionalInt(arguments, 0), optionalInt(arguments, 1), arguments.getInt(2).getAsInt());
        }
        throw new IndexOutOfBoundsException("invalid arguments for get: " + arguments);
    }

    // __get results cannot be assigned to, but __getSliced can be

    /**
     * Script method for indexing and slicing, e.g. {@code list[1]} or {@code list[1:3]}. In contrast to {@code get}, the results can be assigned to.
     * @param self the object that the method is called on
     * @param process the running script process
     * @param arguments the call arguments
     * @return the element or the range
     * @throws JanitorRuntimeException if the call arguments are invalid or the operation fails
     */
    public static JanitorObject __getSliced(final JList self, final JanitorScriptProcess process, final JCallArgs arguments) throws JanitorRuntimeException {
        if (arguments.size() == 1) {
            return self.getIndexed(arguments.require(1).getInt(0));
        }
        if (arguments.size() == 2) {
            if (arguments.get(0) == JNull.NULL && arguments.get(1) == JNull.NULL) {
                return self.getAssignableRange(Janitor.integer(0), Janitor.integer(self.size()));
            } else if (arguments.get(0) == JNull.NULL) {
                return self.getAssignableRange(Janitor.integer(0), arguments.getInt(1));
            } else if (arguments.get(1) == JNull.NULL) {
                return self.getAssignableRange(arguments.getInt(0), Janitor.integer(self.size()));
            }
            return self.getAssignableRange(arguments.getInt(0), arguments.getInt(1));
        }
        if (arguments.size() == 3) {
            // stepped slices are read-only for now, e.g. "li[::-1]" -- not assignable yet.
            return self.getSteppedRange(optionalInt(arguments, 0), optionalInt(arguments, 1), arguments.getInt(2).getAsInt());
        }
        throw new IndexOutOfBoundsException("invalid arguments for get: " + arguments);
    }

    /**
     * Resolve an optional start/end slice bound: JNull.NULL (an omitted bound, e.g. "li[::2]") maps
     * to Java null, anything else is unwrapped to its int value.
     */
    private static Integer optionalInt(final JCallArgs arguments, final int position) throws JanitorRuntimeException {
        return arguments.get(position) == JNull.NULL ? null : arguments.getInt(position).getAsInt();
    }


    /**
     * Script method {@code list.remove(x)}: removes an element from this list.
     * @param self the object that the method is called on
     * @param process the running script process
     * @param arguments the call arguments
     * @return null
     * @throws JanitorRuntimeException if the call arguments are invalid or the operation fails
     */
    public static JNull __remove(final JList self, final JanitorScriptProcess process, final JCallArgs arguments) throws JanitorRuntimeException {
        arguments.require(1);
        self.remove(arguments.get(0));
        return JNull.NULL;
    }

    /**
     * Script method {@code list.removeAll(other)}: removes all elements of another list from this list.
     * @param self the object that the method is called on
     * @param process the running script process
     * @param arguments the call arguments
     * @return null
     * @throws JanitorRuntimeException if the call arguments are invalid or the operation fails
     */
    public static JNull __removeAll(final JList self, final JanitorScriptProcess process, final JCallArgs arguments) throws JanitorRuntimeException {
        arguments.require(0);
        for (final JanitorObject jObj : arguments.getRequired(0, JList.class)) {
            self.remove(jObj);
        }
        return JNull.NULL;
    }

    /**
     * Script method {@code list.clear()}: removes all elements from this list.
     * @param self the object that the method is called on
     * @param process the running script process
     * @param arguments the call arguments
     * @return null
     * @throws JanitorRuntimeException if the call arguments are invalid or the operation fails
     */
    public static JNull __clear(final JList self, final JanitorScriptProcess process, final JCallArgs arguments) throws JanitorRuntimeException {
        arguments.require(0);
        self.clear();
        return JNull.NULL;
    }


    /**
     * Registers the standard methods and properties of lists.
     * @param listDispatcher the dispatch table to add them to
     */
    public static void applyDefaults(DispatchTable<JList> listDispatcher) {
        // Every HELP text starts with the call form, "name(arguments): ", which the language reference relies on.
        listDispatcher.addMethod("toJson", JListClass::__toJson)
                .setMetaData(Janitor.MetaData.HELP, "toJson(): Returns the list as a JSON string.");
        listDispatcher.addMethod("parseJson", JListClass::__parseJson)
                .setMetaData(Janitor.MetaData.HELP, "parseJson(json): Parses a JSON array and appends its elements to this list; returns this list. Usage: [].parseJson(text).");
        listDispatcher.addMethod("count", JListClass::__count)
                .setMetaData(Janitor.MetaData.HELP, "count(x): Returns how many elements are equal to x.");
        listDispatcher.addMethod("filter", JListClass::__filter)
                .setMetaData(Janitor.MetaData.HELP, "filter(function): Returns a new list with the elements for which the function returns a truthy value.");
        listDispatcher.addMethod("map", JListClass::__map)
                .setMetaData(Janitor.MetaData.HELP, "map(function): Returns a new list with the results of calling the function on each element.");
        listDispatcher.addMethod("join", JListClass::__join)
                .setMetaData(Janitor.MetaData.HELP, "join(separator): Returns the string forms of the elements, joined by the separator; a single space if none is given.");
        listDispatcher.addMethod("toSet", JListClass::__toSet)
                .setMetaData(Janitor.MetaData.HELP, "toSet(): Returns a new set with the elements of this list.");
        listDispatcher.addMethod("toList", JListClass::__toList) // copies the list
                .setMetaData(Janitor.MetaData.HELP, "toList(): Returns a shallow copy of this list.");
        listDispatcher.addMethod("size", JListClass::__size)
                .setMetaData(Janitor.MetaData.HELP, "size(): Returns the number of elements.");
        listDispatcher.addMethod("isEmpty", JListClass::__isEmpty)
                .setMetaData(Janitor.MetaData.HELP, "isEmpty(): Returns true if the list has no elements.");
        listDispatcher.addMethod("contains", JListClass::__contains)
                .setMetaData(Janitor.MetaData.HELP, "contains(x): Returns true if an element is equal to x. Same as the operator: x in list.");
        listDispatcher.addMethod(Janitor.IN_OPERATOR_METHOD, JListClass::__contains); // 17 in [16, 17, 18] == true
        listDispatcher.addMethod("randomSublist", JListClass::__randomSublist)
                .setMetaData(Janitor.MetaData.HELP, "randomSublist(n): Returns a new list with n elements of this list, picked at random.");
        listDispatcher.addMethod("addAll", JListClass::__addAll)
                .setMetaData(Janitor.MetaData.HELP, "addAll(other): Appends all elements of the other list; returns null.");
        listDispatcher.addMethod("put", JListClass::__put)
                .setMetaData(Janitor.MetaData.HELP, "put(index, x): Replaces the element at the index, like list[index] = x; returns null.");
        listDispatcher.addMethod("add", JListClass::__add)
                .setMetaData(Janitor.MetaData.HELP, "add(x): Appends x; add(index, x) inserts x at the index instead. Returns null.");
        listDispatcher.addMethod("get", JListClass::__get)
                .setMetaData(Janitor.MetaData.HELP, "get(index): Same as list[index]; get(from, to) and get(from, to, step) return a read-only slice like list[from:to:step].");
        listDispatcher.addMethod(JanitorAntlrCompiler.INDEXED_GET_METHOD, JListClass::__getSliced);
        listDispatcher.addMethod("sort", JListClass::__sort)
                .setMetaData(Janitor.MetaData.HELP, "sort(): Sorts the list in place, in natural order, and returns null; the elements must be comparable with each other.");
        listDispatcher.addMethod("remove", JListClass::__remove)
                .setMetaData(Janitor.MetaData.HELP, "remove(x): Removes every element that is equal to x; returns null.");
        listDispatcher.addMethod("removeAll", JListClass::__removeAll)
                .setMetaData(Janitor.MetaData.HELP, "removeAll(other): Meant to remove the elements of the other list, but currently fails with an argument error, whatever is passed.");
        listDispatcher.addMethod("clear", JListClass::__clear)
                .setMetaData(Janitor.MetaData.HELP, "clear(): Removes all elements; returns null.");
    }

}

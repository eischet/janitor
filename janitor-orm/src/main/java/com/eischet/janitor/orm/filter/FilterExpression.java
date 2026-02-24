package com.eischet.janitor.orm.filter;

import com.eischet.janitor.api.Janitor;
import com.eischet.janitor.api.types.composed.JanitorComposed;
import com.eischet.janitor.api.types.dispatch.DispatchTable;
import com.eischet.janitor.toolbox.json.api.JsonException;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Filter expressions, compatible with the Kendo "Filter" component, which I happen to use in my projects.
 */
public class FilterExpression extends JanitorComposed<FilterExpression> {

    private static final Logger log = LoggerFactory.getLogger(FilterExpression.class);

    public static DispatchTable<FilterExpression> DISPATCH = new DispatchTable<>(FilterExpression::new);

    static {
        DISPATCH.setMetaData(Janitor.MetaData.CLASS, FilterExpression.class.getSimpleName());
        DISPATCH.addStringProperty("logic", FilterExpression::getLogicString, FilterExpression::setLogicString);
        DISPATCH.addListProperty("filters", filterExpression -> {
            // log.info("property 'filters' is being read on filterExpression={}", filterExpression);
            return Janitor.responsiveList(
                    FilterExpression.DISPATCH,
                    filterExpression.getFilters().stream(),
                    update -> {
                        // log.info("property 'filters' is being updated on filterExpression={} with update={}", filterExpression, update);
                        filterExpression.setFilters(update.stream().map(FilterExpression.class::cast).toList());
                    }
            );
        });
        DISPATCH.addStringProperty("field", FilterExpression::getField, FilterExpression::setField);
        DISPATCH.addStringProperty("operator", FilterExpression::getOperatorString, FilterExpression::setOperatorString);
        DISPATCH.addNullableBooleanProperty("ignoreCase", FilterExpression::getIgnoreCase, FilterExpression::setIgnoreCase);
        DISPATCH.addStringProperty("valueString", FilterExpression::getValueString, FilterExpression::setValueString);
        DISPATCH.addNullableBooleanProperty("valueBoolean", FilterExpression::getValueBoolean, FilterExpression::setValueBoolean);
        DISPATCH.addNullableDoubleProperty("valueDouble", FilterExpression::getValueDouble, FilterExpression::setValueDouble);
        DISPATCH.addNullableLongProperty("valueLong", FilterExpression::getValueLong, FilterExpression::setValueLong);
        DISPATCH.addDateProperty("valueDate", FilterExpression::getValueDate, FilterExpression::setValueDate);
        DISPATCH.addDateTimeProperty("valueDateTime", FilterExpression::getValueDateTime, FilterExpression::setValueDateTime);
    }

    // Für logische Gruppen
    private @Nullable FilterLogic logic; // "and" | "or"
    private @Nullable @Unmodifiable List<FilterExpression> filters;

    // Für Expressions
    private @Nullable String field;
    private @Nullable FilterOperator operator;
    private @Nullable Boolean ignoreCase;

    private @Nullable String valueString;
    private @Nullable Boolean valueBoolean;
    private @Nullable Double valueDouble;
    private @Nullable Long valueLong;
    private @Nullable LocalDateTime valueDateTime;
    private @Nullable LocalDate valueDate;

    public boolean isComplete() {
        if (filters != null && !filters.isEmpty() && logic != null) {
            return filters.stream().allMatch(FilterExpression::isComplete); // and/or with any number of filters, which are themselves complete
        }
        if (operator == null || field == null || field.isBlank()) {
            return false; // no operator or no field? not complete!
        }
        return switch (operator) {
            case ISNULL, ISNOTNULL, ISEMPTY, ISNOTEMPTY -> true; // these do not need a value
            case EQ, NEQ, LT, GT, GTE, LTE, STARTSWITH, ENDSWITH, CONTAINS, DOESNOTCONTAIN -> hasValueSet(); // these need a value
        };
    }

    public boolean isIncomplete() {
        return !isComplete();
    }

    public boolean hasValueSet() {
        return valueString != null || valueBoolean != null || valueDouble != null || valueLong != null || valueDateTime != null || valueDate != null;
    }

    /**
     * Removes incomplete filters from the expression and returns the compressed expression.
     * Some tools tend to produce invalid or incomplete expressions, and we're removing those.
     *
     * @return A new compressed expression with incomplete filters removed.
     */
    public FilterExpression compress() {
        final FilterExpression compressed = deepCopy();
        if (compressed.filters != null) {
            compressed.filters = new ArrayList<>(compressed.filters.stream().filter(f -> !f.isIncomplete()).toList());
        }
        return compressed;
    }

    public void clear() {
        logic = null;
        filters = null;
        field = null;
        operator = null;
        ignoreCase = null;
        valueString = null;
        valueBoolean = null;
        valueDouble = null;
        valueLong = null;
        valueDateTime = null;
        valueDate = null;
    }

    public void clearAndSetFromJson(@Language("JSON") final String json) throws JsonException {
        clear();
        final FilterExpression temp = DISPATCH.readFromJson(FilterExpression::new, json);
        logic = temp.logic;
        filters = temp.filters;
        field = temp.field;
        operator = temp.operator;
        ignoreCase = temp.ignoreCase;
        valueString = temp.valueString;
        valueBoolean = temp.valueBoolean;
        valueDouble = temp.valueDouble;
        valueLong = temp.valueLong;
        valueDateTime = temp.valueDateTime;
        valueDate = temp.valueDate;
        log.info("parsed {} to {}", json, getValueDescription());
    }

    public String getValueDescription() {
        StringBuilder result = new StringBuilder();
        result.append("{");
        if (valueString != null) {
            result.append(valueString).append(" [string] ");
        }
        if (valueBoolean != null) {
            result.append(valueBoolean).append(" [boolean] ");
        }
        if (valueDouble != null) {
            result.append(valueDouble).append(" [double] ");
        }
        if (valueLong != null) {
            result.append(valueLong).append(" [long] ");
        }
        if (valueDateTime != null) {
            result.append(valueDateTime).append(" [datetime] ");
        }
        if (valueDate != null) {
            result.append(valueDate).append(" [date] ");
        }
        result.append("}");
        return result.toString();
    }

    private void clearAllValues() {
        valueString = null;
        valueBoolean = null;
        valueDouble = null;
        valueLong = null;
        valueDateTime = null;
        valueDate = null;
    }

    public boolean isString() {
        return valueString != null;
    }

    public boolean isLong() {
        return valueLong != null;
    }

    public boolean isBoolean() {
        return valueBoolean != null;
    }

    public boolean isDouble() {
        return valueDouble != null;
    }

    public boolean isDateTime() {
        return valueDateTime != null;
    }

    public boolean isDate() {
        return valueDate != null;
    }

    public FilterExpression() {
        super(DISPATCH);
    }

    public FilterExpression(@NotNull FilterLogic logic, @NotNull @Unmodifiable List<FilterExpression> filters) {
        super(DISPATCH);
        this.logic = logic;
        this.filters = List.copyOf(filters);
    }

    public FilterExpression(@NotNull String field, @NotNull FilterOperator operator, @Nullable Boolean ignoreCase) {
        super(DISPATCH);
        this.field = field;
        this.operator = operator;
        this.ignoreCase = ignoreCase;
    }

    public FilterExpression(@NotNull String field, @NotNull FilterOperator operator) {
        this(field, operator, null);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("{");
        if (logic != null) {
            sb.append("logic: '").append(logic).append('\'').append(", ");
        }
        if (filters != null && !filters.isEmpty()) {
            sb.append("filters: ").append(filters).append(", ");
        }
        if (valueLong != null) {
            sb.append("valueLong: ").append(valueLong).append(", ");
        }
        if (valueDouble != null) {
            sb.append("valueDouble: ").append(valueDouble).append(", ");
        }
        if (valueDate != null) {
            sb.append("valueDate: ").append(valueDate).append(", ");
        }
        if (valueDateTime != null) {
            sb.append("valueDateTime: ").append(valueDateTime).append(", ");
        }
        if (valueBoolean != null) {
            sb.append("valueBoolean: ").append(valueBoolean).append(", ");
        }
        if (valueString != null) {
            sb.append("valueString: '").append(valueString).append('\'').append(", ");
        }
        if (ignoreCase != null) {
            sb.append("ignoreCase: ").append(ignoreCase).append(", ");
        }
        if (operator != null) {
            sb.append("operator: '").append(operator).append('\'').append(", ");
        }
        if (field != null) {
            sb.append("field: '").append(field).append('\'').append(", ");
        }
        sb.append('}');
        return sb.toString();
    }

    public boolean isGroup() {
        return logic != null && filters != null;
    }

    public boolean isExpression() {
        return field != null && operator != null;
    }

    public @Nullable FilterLogic getLogic() {
        return logic;
    }

    public void setLogic(final @Nullable FilterLogic logic) {
        this.logic = logic;
    }



    public String getLogicString() {
        return logic == null ? null : logic.getCode();
    }

    public void setLogicString(final String logic) {
        this.logic = FilterLogic.fromCode(logic);
    }

    public @Unmodifiable List<FilterExpression> getFilters() {
        return filters == null ? Collections.emptyList() : List.copyOf(filters);
    }

    public void setFilters(final @NotNull @Unmodifiable List<FilterExpression> filters) {
        if (filters.isEmpty()) {
            this.filters = null;
        } else {
            this.filters = List.copyOf(filters);
        }
    }

    public void addFilter(final FilterExpression filter) {
        if (this.filters == null) {
            this.filters = List.of(filter);
        } else {
            this.filters = Stream.concat(this.filters.stream(), Stream.of(filter)).toList();
        }
    }

    // TODO: we do not define equals/hashCode right now, and existing tooling uses identity (because the UI control owns the sub-expression),
    //   so figure out how this should look in the end...
    public void removeFilterByEquals(final FilterExpression filter) {
        if (filters != null) {
            int initSize = filters.size();
            filters = filters.stream().filter(e -> !Objects.equals(e, filter)).toList();
            if (initSize == filters.size()) {
                log.warn("removeFilterByEquals({}): the filter was not found and was not actually removed", filter);
            }
        } else {
            log.warn("removeFilterByEquals({}): this expression did not contain any filters! this={}", filter, this);
        }
    }

    public void removeFilterByIdentity(final FilterExpression filter) {
        if (filters != null) {
            int initSize = filters.size();
            filters = filters.stream().filter(e -> e != filter).toList();
            if (initSize == filters.size()) {
                log.warn("removeFilterByIdentity({}): the filter was not found and was not actually removed", filter);
            }
        } else {
            log.warn("removeFilterByIdentity({}): this expression did not contain any filters! this={}", filter, this);
        }
    }


    public @Nullable String getField() {
        return field;
    }

    public void setField(final @Nullable String field) {
        this.field = field;
    }

    public @Nullable FilterOperator getOperator() {
        return operator;
    }

    public void setOperator(final @Nullable FilterOperator operator) {
        this.operator = operator;
    }

    public @Nullable Boolean getIgnoreCase() {
        return ignoreCase;
    }

    public void setIgnoreCase(final @Nullable Boolean ignoreCase) {
        this.ignoreCase = ignoreCase;
    }

    public @Nullable String getValueString() {
        return valueString;
    }

    public void setValueString(final String valueString) {
        clearAllValues();
        this.valueString = valueString;
    }

    public FilterExpression withValueString(final String valueString) {
        setValueString(valueString);
        return this;
    }

    public @Nullable Boolean getValueBoolean() {
        return valueBoolean;
    }

    public FilterExpression withValueBoolean(final Boolean valueBoolean) {
        setValueBoolean(valueBoolean);
        return this;
    }

    public void setValueBoolean(final Boolean valueBoolean) {
        clearAllValues();
        this.valueBoolean = valueBoolean;
    }

    public @Nullable Double getValueDouble() {
        return valueDouble;
    }

    public void setValueDouble(final Double valueNumber) {
        clearAllValues();
        this.valueDouble = valueNumber;
    }

    public FilterExpression withValueDouble(final Double valueNumber) {
        setValueDouble(valueNumber);
        return this;
    }

    public @Nullable Long getValueLong() {
        return valueLong;
    }

    public void setValueLong(final Long valueLong) {
        clearAllValues();
        this.valueLong = valueLong;
    }

    public FilterExpression withValueLong(final long valueLong) {
        setValueLong(valueLong);
        return this;
    }

    public @Nullable LocalDateTime getValueDateTime() {
        return valueDateTime;
    }

    public void setValueDateTime(final LocalDateTime valueDateTime) {
        clearAllValues();
        this.valueDateTime = valueDateTime;
    }

    public FilterExpression withValueDateTime(final LocalDateTime valueDateTime) {
        setValueDateTime(valueDateTime);
        return this;
    }

    public @Nullable LocalDate getValueDate() {
        return valueDate;
    }

    public void setValueDate(final LocalDate valueDate) {
        clearAllValues();
        this.valueDate = valueDate;
    }

    public FilterExpression withValueDate(final LocalDate valueDate) {
        setValueDate(valueDate);
        return this;
    }

    public String getOperatorString() throws MalformedExpression {
        return operator == null ? null : operator.getCode();
    }

    public void setOperatorString(final String operator) throws MalformedExpression {
        this.operator = FilterOperator.fromCode(operator);
    }

    public static FilterExpression and(final @NotNull List<FilterExpression> filters) {
        return new FilterExpression(FilterLogic.AND, filters);
    }

    public static FilterExpression or(final @NotNull List<FilterExpression> filters) {
        return new FilterExpression(FilterLogic.OR, filters);
    }

    public static FilterExpression from(@NotNull String field, @NotNull FilterOperator operator, @NotNull Boolean ignoreCase, @NotNull String valueString) throws MalformedExpression {
        return new FilterExpression(field, operator, ignoreCase).withValueString(valueString);
    }

    public static FilterExpression from(@NotNull String field, @NotNull FilterOperator operator, @NotNull String valueString) throws MalformedExpression {
        return new FilterExpression(field, operator, null).withValueString(valueString);
    }

    public static FilterExpression from(@NotNull String field, @NotNull FilterOperator operator, final long longValue) throws MalformedExpression {
        return new FilterExpression(field, operator).withValueLong(longValue);
    }

    public static FilterExpression from(@NotNull String field, @NotNull FilterOperator operator, final LocalDate dateValue) throws MalformedExpression {
        return new FilterExpression(field, operator).withValueDate(dateValue);
    }

    public static FilterExpression from(@NotNull String field, @NotNull FilterOperator operator, final LocalDateTime dateTimeValue) throws MalformedExpression {
        return new FilterExpression(field, operator).withValueDateTime(dateTimeValue);
    }

    public static FilterExpression from(@NotNull String field, @NotNull FilterOperator operator, final double doubleValue) throws MalformedExpression {
        return new FilterExpression(field, operator).withValueDouble(doubleValue);
    }

    public static FilterExpression fromJson(@NotNull String json) throws MalformedExpression {
        try {
            return FilterExpression.DISPATCH.readFromJson(FilterExpression::new, json);
        } catch (JsonException e) {
            throw new MalformedExpression(e.getMessage(), e);
        }
    }

    public @Language("JSON") String toJson() throws JsonException {
        return DISPATCH.writeToJson(this);
    }

    public FilterExpression deepCopy() {
        final FilterExpression copy = new FilterExpression();
        copy.logic = this.logic;
        copy.valueString = this.valueString;
        copy.valueBoolean = this.valueBoolean;
        copy.valueLong = this.valueLong;
        copy.valueDouble = this.valueDouble;
        copy.valueDateTime = this.valueDateTime;
        copy.valueDate = this.valueDate;
        copy.operator = this.operator;
        copy.field = this.field;
        copy.ignoreCase = this.ignoreCase;
        if (this.filters != null) {
            copy.filters = this.filters.stream().map(FilterExpression::deepCopy).collect(Collectors.toList());
        }
        return copy;
    }

}

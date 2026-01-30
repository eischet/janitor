package com.eischet.janitor.orm;

import com.eischet.janitor.api.Janitor;
import com.eischet.janitor.api.types.composed.JanitorComposed;
import com.eischet.janitor.api.types.dispatch.DispatchTable;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Filter expressions, compatible with the Kendo "Filter" component, which I happen to use in my projects.
 */
public class FilterExpression extends JanitorComposed<FilterExpression> {

    private static final Logger log = LoggerFactory.getLogger(FilterExpression.class);

    public static DispatchTable<FilterExpression> DISPATCH = new DispatchTable<>(FilterExpression::new);

    static {
        DISPATCH.setMetaData(Janitor.MetaData.CLASS, FilterExpression.class.getSimpleName());
        DISPATCH.addStringProperty("logic", FilterExpression::getLogic, FilterExpression::setLogic);
        DISPATCH.addListProperty("filters", filterExpression -> {
            log.info("property 'filters' is being read on filterExpression={}", filterExpression);
            return Janitor.responsiveList(
                    FilterExpression.DISPATCH,
                    filterExpression.getFilters().stream(),
                    update -> {
                        log.info("property 'filters' is being updated on filterExpression={} with update={}", filterExpression, update);
                        filterExpression.setFilters(update.stream().map(FilterExpression.class::cast).toList());
                    }
            );
        });
        DISPATCH.addStringProperty("field", FilterExpression::getField, FilterExpression::setField);
        DISPATCH.addStringProperty("operator", FilterExpression::getOperator, FilterExpression::setOperator);
        DISPATCH.addNullableBooleanProperty("ignoreCase", FilterExpression::getIgnoreCase, FilterExpression::setIgnoreCase);

        DISPATCH.addStringProperty("valueString", FilterExpression::getValueString, FilterExpression::setValueString);
        DISPATCH.addNullableBooleanProperty("valueBoolean", FilterExpression::getValueBoolean, FilterExpression::setValueBoolean);
        DISPATCH.addNullableDoubleProperty("valueDouble", FilterExpression::getValueDouble, FilterExpression::setValueDouble);
        DISPATCH.addNullableLongProperty("valueLong", FilterExpression::getValueLong, FilterExpression::setValueLong);
    }

    // Für logische Gruppen
    private String logic; // "and" | "or"
    private List<FilterExpression> filters;

    // Für Expressions
    private String field;
    private String operator;
    private Boolean ignoreCase;

    private String valueString;
    private Boolean valueBoolean;
    private Double valueDouble;
    private Long valueLong;
    private LocalDateTime valueDateTime;
    private LocalDate valueDate;

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

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("{");
        if (logic != null) {
            sb.append(", logic: '").append(logic).append('\'').append(", ");
        }
        if (filters != null && !filters.isEmpty()) {
            sb.append(", filters: ").append(filters).append(", ");
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
            sb.append(", valueString: '").append(valueString).append('\'').append(", ");
        }
        if (ignoreCase != null) {
            sb.append(", ignoreCase: ").append(ignoreCase).append(", ");
        }
        if (operator != null) {
            sb.append(", operator: '").append(operator).append('\'').append(", ");
        }
        if (field != null) {
            sb.append(", field: '").append(field).append('\'').append(", ");
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

    public String getLogic() {
        return logic;
    }

    public void setLogic(final String logic) {
        this.logic = logic;
    }

    public List<FilterExpression> getFilters() {
        if (filters == null) {
            filters = new ArrayList<>();
        }
        return filters;
    }

    public void setFilters(final List<FilterExpression> filters) {
        this.filters = filters;
    }

    public String getField() {
        return field;
    }

    public void setField(final String field) {
        this.field = field;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(final String operator) {
        this.operator = operator;
    }

    public Boolean getIgnoreCase() {
        return ignoreCase;
    }

    public void setIgnoreCase(final Boolean ignoreCase) {
        this.ignoreCase = ignoreCase;
    }

    public String getValueString() {
        return valueString;
    }

    public void setValueString(final String valueString) {
        this.valueString = valueString;
    }

    public Boolean getValueBoolean() {
        return valueBoolean;
    }

    public void setValueBoolean(final Boolean valueBoolean) {
        this.valueBoolean = valueBoolean;
    }

    public Double getValueDouble() {
        return valueDouble;
    }

    public void setValueDouble(final Double valueNumber) {
        this.valueDouble = valueNumber;
    }

    public Long getValueLong() {
        return valueLong;
    }

    public void setValueLong(final Long valueLong) {
        this.valueLong = valueLong;
    }

    public LocalDateTime getValueDateTime() {
        return valueDateTime;
    }

    public void setValueDateTime(final LocalDateTime valueDateTime) {
        this.valueDateTime = valueDateTime;
    }

    public LocalDate getValueDate() {
        return valueDate;
    }

    public void setValueDate(final LocalDate valueDate) {
        this.valueDate = valueDate;
    }

    @SuppressWarnings("SpellCheckingInspection")
    public enum Operator {
        EQ("eq"),
        NEQ("neq"),
        LT("lt"),
        LTE("lte"),
        GT("gt"),
        GTE("gte"),
        STARTSWITH("startswith"),
        ENDSWITH("endswith"),
        CONTAINS("contains"),
        DOESNOTCONTAIN("doesnotcontain"),
        ISNULL("isnull"),
        ISNOTNULL("isnotnull"),
        ISEMPTY("isempty"),
        ISNOTEMPTY("isnotempty")
        ;

        private final String code;

        Operator(final String code) {
            this.code = code;
        }

        public static final List<Operator> OPERATORS = List.of(values());

        public static @NotNull FilterExpression.Operator fromCode(final String code) throws MalformedExpression {
            return OPERATORS.stream()
                    .filter(it -> Objects.equals(it.code, code))
                    .findFirst()
                    .orElseThrow(() -> new MalformedExpression("invalid operator code '" + code + "'"));
        }

        public String getCode() {
            return code;
        }
    }

    public static class MalformedExpression extends RuntimeException {
        public MalformedExpression(final String message) {
            super(message);
        }
    }

    public @NotNull Operator getOperatorEnum() throws MalformedExpression {
        return Operator.fromCode(operator);
    }

}

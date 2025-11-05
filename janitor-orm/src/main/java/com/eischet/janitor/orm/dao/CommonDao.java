package com.eischet.janitor.orm.dao;

import com.eischet.dbxs.DatabaseConnection;
import com.eischet.dbxs.SimplePreparedStatement;
import com.eischet.dbxs.exceptions.DatabaseError;
import com.eischet.dbxs.results.SimpleResultSet;
import com.eischet.janitor.api.Janitor;
import com.eischet.janitor.api.errors.glue.JanitorGlueException;
import com.eischet.janitor.api.types.JAssignable;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.api.types.builtin.*;
import com.eischet.janitor.orm.meta.EntityIndex;
import com.eischet.janitor.orm.meta.StringMappedEnum;
import com.eischet.janitor.orm.ref.*;
import com.eischet.janitor.orm.sql.ColumnTypeHint;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.NoSuchElementException;

public class CommonDao {

    private static final Logger log = LoggerFactory.getLogger(CommonDao.class);

    public static void readProperty(final EntityIndex entityIndex,
                                    final String columnName,
                                    final DatabaseConnection conn,
                                    final JAssignable assignableProperty,
                                    final SimpleResultSet rs,
                                    final @NotNull ColumnTypeHint columnTypeHint,
                                    final @Nullable String lookupType,
                                    final @Nullable Boolean hostNullable) throws SQLException, JanitorGlueException {
        if (lookupType != null) {
            final @Nullable Long longValue = rs.getLongInstance();
            if (longValue == null) {
                assignableProperty.assign(Janitor.NULL);
            } else {
                assignableProperty.assign(ForeignKeyInteger.createWithForce(longValue, entityIndex.getDao(lookupType)));
            }
        } else {
            try {
                assignableProperty.assign(switch (columnTypeHint) {
                    // ints and decimals require special attention: can the underlying Java field cope with <null>? Not if it's a primitive,
                    // so in case that nullability is not actively proclaimed, we will assume that the field is NOT nullable to be on the safe side.
                    case INT -> {
                        if (hostNullable == Boolean.TRUE) {
                            yield Janitor.nullableInteger(rs.getLongInstance());
                        } else {
                            yield Janitor.integer(rs.getLong());
                        }
                    }
                    case DECIMAL -> {
                        if (hostNullable == Boolean.TRUE) {
                            yield Janitor.nullableFloatingPoint(rs.getDoubleInstance());
                        } else {
                            yield Janitor.floatingPoint(rs.getDouble());
                        }
                    }
                    // we do not bother with boolean fields, translating 0 into false.
                    case BIT -> Janitor.toBool(rs.getLong() > 0);
                    case NCLOB -> Janitor.nullableString(rs.readNationalClob());
                    case VARCHAR, NVARCHAR -> Janitor.nullableString(rs.getString());
                    case DATETIME -> Janitor.nullableDateTime(rs.getLocalDateTime());
                    case DATE -> Janitor.nullableDate(rs.getLocalDate());
                });
            } catch (RuntimeException e) {
                if (e instanceof NullPointerException) {
                    log.warn("runtime error reading column {} with type {}; guess: you forgot to mark a column as HOST_NULLABLE, but the column can be NULL", columnName, columnTypeHint);
                    throw e;
                } else {
                    log.warn("runtime error reading column {} with type {}", columnName, columnTypeHint);
                    throw e;
                }
            }
        }
    }

    public static void writeProperty(final DatabaseConnection conn,
                                     final String className,
                                     final String column,
                                     final String field,
                                     final JanitorObject propertyValue,
                                     final SimplePreparedStatement ps,
                                     final @NotNull ColumnTypeHint columnTypeHint) throws SQLException {
        switch (columnTypeHint) {
            case INT -> {
                if (propertyValue instanceof JNumber number) {
                    ps.addLong(number.toLong());
                    return;
                }
                if (propertyValue == Janitor.NULL) {
                    ps.addNullNumber();
                    return;
                }
                if (propertyValue instanceof ForeignKeyInteger<?> ifk) {
                    ps.addLong(ifk.getId());
                    return;
                } else if (propertyValue instanceof ForeignKeySearchResult<?, ?> fksr) {
                    ps.addLong(fksr.getId());
                    return;
                } else if (propertyValue instanceof ForeignKeyIdentity<?> ifk) {
                    ps.addLong(ifk.getId());
                    return;
                } else if (propertyValue instanceof ForeignKeyNull<?>) {
                    ps.addNullInteger();
                    return;
                } else if (propertyValue instanceof ForeignKeyString<?> fkInstance) {
                    try {
                        ps.addLong(fkInstance.resolve(conn).orElseThrow().getId());
                        return;
                    } catch (DatabaseError | NullPointerException | NoSuchElementException e) {
                        throw new SQLException("foreign key resolution failed for '" + fkInstance.getKey() + "' of field '" + field + "' / column '" + column + "' in class '" + className + "'", e);
                    }
                }
            }
            case DECIMAL -> {
                if (propertyValue instanceof JNumber number) {
                    ps.addDouble(number.toDouble());
                    return;
                }
                if (propertyValue == JNull.NULL) {
                    ps.addNullNumber();
                    return;
                }
            }
            case VARCHAR, NVARCHAR, NCLOB -> {
                if (propertyValue instanceof JString str) {
                    if (columnTypeHint == ColumnTypeHint.NCLOB) {
                        ps.addNationalClob(str.janitorToString());
                    } else {
                        ps.addString(str.janitorToString());
                    }
                    return;
                }
                if (propertyValue == JNull.NULL) {
                    ps.addNullString();
                    return;
                }
                if (propertyValue instanceof StringMappedEnum strMappedEnum) {
                    ps.addString(strMappedEnum.getStringRepresentation());
                    return;
                }
            }
            case BIT -> {
                if (propertyValue instanceof JBool jbool) {
                    ps.addInt(jbool.janitorGetHostValue() ? 1 : 0);
                    return;
                }
                if (propertyValue == JNull.NULL) {
                    ps.addNullInteger();
                    return;
                }
            }
            case DATETIME -> {
                if (propertyValue instanceof JDateTime dateTime) {
                    ps.addLocalDateTime(dateTime.janitorGetHostValue());
                    return;
                }
                if (propertyValue == JNull.NULL) {
                    ps.addLocalDateTime(null);
                    return;
                }
            }
            case DATE -> {
                // TODO: dbxs könnte ggf. noch ein "Date" statt nur "DateTime" ermöglichen, damit der JDBC-Treiber besser bedient wird.
                if (propertyValue instanceof JDate date) {
                    ps.addLocalDateTime(date.janitorGetHostValue().atStartOfDay());
                    return;
                }
                if (propertyValue == JNull.NULL) {
                    ps.addLocalDateTime(null);
                    return;
                }
            }
        }
        log.warn("Invalid combination: property value {} with column type {} on column {} / field {}", propertyValue, columnTypeHint, column, field);
    }



}

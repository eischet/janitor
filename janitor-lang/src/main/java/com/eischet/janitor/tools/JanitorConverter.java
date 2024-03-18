package com.eischet.janitor.tools;

import com.eischet.janitor.api.types.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

public class JanitorConverter {
    public static JanitorObject nativeToScript(Object o) {
        if (o instanceof JanitorObject good) {
            return good;
        }
        if (o == null) {
            return JNull.NULL;
        }
        if (o instanceof Double dou) {
            return JFloat.of(dou);
        }
        if (o instanceof String string) {
            return JString.of(string);
        }
        if (o instanceof Long lo) {
            return JInt.of(lo);
        }
        if (o instanceof Integer in) {
            return JInt.of(in);
        }
        if (o instanceof LocalDateTime localDateTime) {
            return JDateTime.ofNullable(localDateTime);
        }
        if (o instanceof LocalDate localDate) {
            return JDateTime.ofNullable(localDate.atStartOfDay());
        }
        if (o instanceof Timestamp timestamp) {
            return JDateTime.ofNullable(DateTimeUtilities.convert(timestamp));
        }
        if (o instanceof Date date) {
            return JDateTime.ofNullable(DateTimeUtilities.convert(date));
        }
        if (o instanceof BigDecimal bigDecimal) {
            return JFloat.of(bigDecimal.doubleValue());
        }
        if (o instanceof Boolean bool) {
            return JBool.of(bool);
        }
        Logger log = LoggerFactory.getLogger(JanitorObject.class);
        log.warn("nativeToScript(object={} [{}]) --> don't know how to convert this into a script variable!", o, ObjectUtilities.simpleClassNameOf(o));
        return null;
    }

    public static JanitorObject orNull(final JanitorObject obj) {
        return obj == null ? JNull.NULL : obj;
    }
}

package com.home.project.portfolio.utils;

import com.google.protobuf.Timestamp;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

/**
 * @author rlagay
 */
public class DateUtil {

    public static ZonedDateTime toDateTime(Timestamp timestamp) {
        var instant = Instant.ofEpochSecond(timestamp.getSeconds(), timestamp.getNanos());
        return instant.atZone(ZoneId.of("UTC"));
    }
}

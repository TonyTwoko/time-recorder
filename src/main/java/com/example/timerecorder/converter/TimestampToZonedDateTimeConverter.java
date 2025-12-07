package com.example.timerecorder.converter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import java.sql.Timestamp;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@ReadingConverter
public class TimestampToZonedDateTimeConverter implements Converter<Timestamp, ZonedDateTime> {
    private static final ZoneId ZONE = ZoneId.of("Europe/Moscow");

    @Override
    public ZonedDateTime convert(Timestamp source) {
        return source.toInstant().atZone(ZONE);
    }
}
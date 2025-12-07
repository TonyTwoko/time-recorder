package com.example.timerecorder.converter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;
import java.time.ZonedDateTime;
import java.sql.Timestamp;
import java.time.ZoneOffset;

@WritingConverter
public class ZonedDateTimeWriteConverter implements Converter<ZonedDateTime, Timestamp> {

    @Override
    public Timestamp convert(ZonedDateTime source) {
        return Timestamp.from(source.withZoneSameInstant(ZoneOffset.UTC).toInstant());
    }
}
package com.example.timerecorder.config;

import com.example.timerecorder.converter.TimestampToZonedDateTimeConverter;
import com.example.timerecorder.converter.ZonedDateTimeWriteConverter;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jdbc.repository.config.AbstractJdbcConfiguration;
import org.springframework.core.convert.converter.Converter;
import java.util.ArrayList;
import java.util.List;

@Configuration
public class DataJdbcConfig extends AbstractJdbcConfiguration {

    @Override
    protected List<Converter<?, ?>> userConverters() {
        List<Converter<?, ?>> converters = new ArrayList<>();
        converters.add(new ZonedDateTimeWriteConverter());
        converters.add(new TimestampToZonedDateTimeConverter());
        return converters;
    }
}
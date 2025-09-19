package com.loopers.config.web.converter;

import org.springframework.format.Formatter;
import org.threeten.extra.YearWeek;

import java.util.Locale;

public class YearWeekFormatter implements Formatter<YearWeek> {

    @Override
    public YearWeek parse(String text, Locale locale) {
        return YearWeek.parse(text);
    }

    @Override
    public String print(YearWeek object, Locale locale) {
        return object.toString();
    }

}

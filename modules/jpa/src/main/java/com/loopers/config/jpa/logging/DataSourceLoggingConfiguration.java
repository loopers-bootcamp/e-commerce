package com.loopers.config.jpa.logging;

import com.p6spy.engine.spy.appender.MessageFormattingStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class DataSourceLoggingConfiguration {

    @Bean
    P6SpyEventListener p6SpyEventListener() {
        return new P6SpyEventListener();
    }

    @Bean
    MessageFormattingStrategy messageFormattingStrategy() {
        return new P6SpySqlFormat();
    }

}

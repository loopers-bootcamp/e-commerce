package com.loopers.config.kafka;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;

import java.util.Map;

@ConfigurationProperties(prefix = "loopers.kafka")
public record LoopersKafkaProperties(
        Map<String, String> topics
) {

    @ConstructorBinding
    public LoopersKafkaProperties {
    }

    public String getTopic(Object payload) {
        String packageName = payload.getClass().getPackageName();
        String nestedSimpleClassName = payload.getClass().getName().substring(packageName.length() + 1);
        return topics.get(nestedSimpleClassName.replace('$', '.'));
    }

}

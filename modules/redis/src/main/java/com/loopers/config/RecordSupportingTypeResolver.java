package com.loopers.config;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;

/**
 * @see <a href="https://github.com/spring-projects/spring-data-redis/issues/2601">
 * Add a constructor with Module to GenericJackson2JsonRedisSerializer</a>
 * @see <a href="https://github.com/FasterXML/jackson-databind/issues/3512">
 * Serialized result of java Class & Record is different with RedisTemplate</a>
 * @see <a href="https://techblog.woowahan.com/22767">
 * Spring Cache(@Cacheable) + Spring Data Redis 사용 시 record 직렬화 오류 원인과 해결</a>
 */
public class RecordSupportingTypeResolver extends ObjectMapper.DefaultTypeResolverBuilder {

    public RecordSupportingTypeResolver(ObjectMapper.DefaultTyping t, PolymorphicTypeValidator ptv) {
        super(t, ptv);
    }

    @Override
    public boolean useForType(JavaType t) {
        return t.getRawClass().isRecord() || super.useForType(t);
    }

}

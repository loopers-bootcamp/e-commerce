package com.loopers.annotation;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.lang.annotation.*;

/**
 * @see <a href="https://myvelop.tistory.com/256">
 * 실무에서 @Transactional을 제거했더니 성능이 2배 향상된 이유</a>
 * @see <a href="https://tech.kakaopay.com/post/jpa-transactional-bri/">
 * JPA Transactional 잘 알고 쓰고 계신가요?</a>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
@Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
public @interface ReadOnlyTransactional {
}

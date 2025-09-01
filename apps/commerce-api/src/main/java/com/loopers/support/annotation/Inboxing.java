package com.loopers.support.annotation;

import java.lang.annotation.*;

@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Inboxing {

    boolean async() default false;

    boolean idempotent() default false;

}

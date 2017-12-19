package com.yarolegovich.wellsql.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by yarolegovich on 19.11.2015.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Table {
    String name() default "";
    String addOn() default "";

    boolean generateMapper() default true;
    boolean generateTable() default true;
}

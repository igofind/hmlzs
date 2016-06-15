package com.core.security.annotation;

import org.springframework.stereotype.Component;

import java.lang.annotation.*;

/**
 * Created by sunpeng
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Component
public @interface AsRight {
    int id();

    String depict() default ""; // The method with AsRight annotation will be declared as a right,it should have a description.

    int[] needRight() default {}; // another right should be check before

    String msg() default "No permission";
}

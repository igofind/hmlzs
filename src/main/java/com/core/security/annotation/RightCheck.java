package com.core.security.annotation;

import org.springframework.stereotype.Component;

import java.lang.annotation.*;

/**
 * Created by sunpeng
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Component
public @interface RightCheck {

    String rightGroupName() default ""; // also call role name

    String depict();
}

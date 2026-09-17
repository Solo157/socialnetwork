package org.counter.annotation;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import java.lang.annotation.*;

/**
 * blabla.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@ConditionalOnProperty(value = "sqlDB.enabled", havingValue = "true", matchIfMissing = true)
public @interface DialogSqlEnabled {

}

package org.counter.annotation;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import java.lang.annotation.*;

/**
 * Бин активен только при sqlDB.enabled=false.
 * Redis-реализация хранилища диалогов.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@ConditionalOnProperty(value = "sqlDB.enabled", havingValue = "false", matchIfMissing = false)
public @interface DialogSqlDisabled {

}

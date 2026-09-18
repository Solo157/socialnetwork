package org.counter.annotation;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import java.lang.annotation.*;

/**
 * Бин активен при sqlDB.enabled=true (по умолчанию).
 * SQL-реализация хранилища диалогов.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@ConditionalOnProperty(value = "sqlDB.enabled", havingValue = "true", matchIfMissing = true)
public @interface DialogSqlEnabled {

}

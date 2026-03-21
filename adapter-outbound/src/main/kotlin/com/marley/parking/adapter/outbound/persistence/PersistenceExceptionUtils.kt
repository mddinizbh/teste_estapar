package com.marley.parking.adapter.outbound.persistence

import java.sql.SQLIntegrityConstraintViolationException

object PersistenceExceptionUtils {

    fun isOptimisticLockException(e: Throwable): Boolean {
        var cause: Throwable? = e
        while (cause != null) {
            if (cause is jakarta.persistence.OptimisticLockException ||
                cause is org.hibernate.StaleObjectStateException
            ) {
                return true
            }
            cause = cause.cause
        }
        return false
    }

    fun isUniqueConstraintViolation(e: Throwable, constraintName: String): Boolean {
        var cause: Throwable? = e
        while (cause != null) {
            if (cause is SQLIntegrityConstraintViolationException &&
                cause.message?.contains(constraintName) == true
            ) {
                return true
            }
            cause = cause.cause
        }
        return false
    }
}

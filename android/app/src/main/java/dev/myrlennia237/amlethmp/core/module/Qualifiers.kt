package dev.myrlennia237.amlethmp.core.module

import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class IoDispatcher

// Work that must outlive a single screen (caches, background writes) runs in this scope.
@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class ApplicationScope

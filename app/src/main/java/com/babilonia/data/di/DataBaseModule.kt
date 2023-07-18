package com.babilonia.data.di

import android.app.Application
import dagger.Module
import dagger.Provides
import dagger.Reusable
import io.realm.Realm
import io.realm.RealmConfiguration
import javax.inject.Named
import javax.inject.Singleton

// Created by Anton Yatsenko on 26.02.2019.
/**
 * Module for Room Database
 */
private const val DATABASE_NAME = "babilonia-db"
private const val DATABASE_NAME_MEMORY = "babilonia-db-memory"
private const val SCHEMA_VERSION = 1L
const val LOCAL = "local"
const val MEMORY = "memory"

@Module
class DataBaseModule(var application: Application) {

    @Provides
    @Singleton
    @Named(MEMORY)
    fun provideInMemoryRealmConfig(): RealmConfiguration = RealmConfiguration.Builder()
        .name(DATABASE_NAME_MEMORY)
        .deleteRealmIfMigrationNeeded()
        .schemaVersion(SCHEMA_VERSION)
        .inMemory()
        .build()

    @Provides
    @Reusable
    fun provideRealm(configuration: RealmConfiguration): Realm = Realm.getInstance(configuration)

    @Provides
    @Reusable
    @Named(LOCAL)
    fun provideRealmConfig(): RealmConfiguration = RealmConfiguration.Builder()
        .name(DATABASE_NAME)
        .deleteRealmIfMigrationNeeded()
        .schemaVersion(SCHEMA_VERSION)
        .build()
}
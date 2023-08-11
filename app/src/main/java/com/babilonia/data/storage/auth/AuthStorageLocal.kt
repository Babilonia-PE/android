package com.babilonia.data.storage.auth

import android.content.SharedPreferences
import com.babilonia.EmptyConstants
import com.babilonia.data.datasource.AuthDataSourceLocal
import com.babilonia.data.db.model.AppConfigDto
import com.babilonia.data.db.model.TokensDto
import com.babilonia.data.db.model.UserDto
import com.babilonia.data.di.LOCAL
import com.babilonia.domain.model.enums.LoginStatus
import io.reactivex.Completable
import io.reactivex.Single
import io.realm.Realm
import io.realm.RealmConfiguration
import javax.inject.Inject
import javax.inject.Named

// Created by Anton Yatsenko on 06.06.2019.
private const val TOKEN = "token"
private const val REFRESH = "refresh"
private const val VALIDATE_DEFAULT_LOCATION = "validate_default_location"

class AuthStorageLocal @Inject constructor(
    @Named(LOCAL) private val config: RealmConfiguration,
    private val prefs: SharedPreferences
) :
    AuthDataSourceLocal {
    override fun getAppConfig(): Single<AppConfigDto> {
        return Single.create {
            Realm.getInstance(config)
                .use { realm ->
                    val dto = realm.where(AppConfigDto::class.java).findFirst()
                    if (dto != null) {
                        it.onSuccess(realm.copyFromRealm(dto))
                    }
                }
        }
    }

    override fun saveConfig(configDto: AppConfigDto): Completable {
        return Completable.create { emitter ->
            try {
                Realm.getInstance(config).use { realm ->
                    realm.executeTransaction {
                        it.copyToRealmOrUpdate(configDto)
                        emitter.onComplete()
                    }
                }
            } catch (e: Exception) {
                emitter.onError(e)
            }
        }
    }

    override fun isValidateDefaultLocation(): Boolean {
        return prefs.getBoolean(VALIDATE_DEFAULT_LOCATION, false)
    }

    override fun setValidateDefaultLocation(status: Boolean) {
        prefs.edit().putBoolean(VALIDATE_DEFAULT_LOCATION, status).apply()
    }

    override fun signOut(): Completable {
        return Completable.create {
            try {
                Realm.getInstance(config).use { realm ->
                    realm.executeTransaction {
                        it.deleteAll()
                    }
                }
//                prefs.edit().clear()
                prefs.edit().clear().apply()
                it.onComplete()
            } catch (e: Exception) {
                it.onError(e)
            }
        }
    }

    override fun getUser(): Single<UserDto> {
        return Single.create {
            Realm.getInstance(config)
                .use { realm ->
                    val dto = realm.where(UserDto::class.java).findFirst()
                    if (dto != null) {
                        it.onSuccess(realm.copyFromRealm(dto))
                    }
                }
        }
    }

    override fun getToken(): String? = prefs.getString(TOKEN, EmptyConstants.EMPTY_STRING)

    override fun getRefresh(): String? = prefs.getString(REFRESH, EmptyConstants.EMPTY_STRING)


    override fun saveUser(user: UserDto) {
        Realm.getInstance(config).use { realm ->
            realm.beginTransaction()
            realm.copyToRealmOrUpdate(user)
            realm.commitTransaction()
        }
    }

    override fun saveTokens(tokens: TokensDto) {
        prefs.edit().putString(TOKEN, tokens.authentication).apply()
        prefs.edit().putString(REFRESH, tokens.exchange).apply()
    }

    override fun isLoggedIn(): Single<LoginStatus> {
        val user = Realm.getInstance(config).where(UserDto::class.java).findFirst()
        val status: LoginStatus = when {
            user == null -> LoginStatus.UNAUTHORIZED
            isNew(user) -> LoginStatus.PARTIALLY
            else -> LoginStatus.AUTHORIZED
        }
        return Single.just(status)
    }

    private fun isNew(user: UserDto): Boolean {
        return user.fullName.isNullOrEmpty()
    }
}
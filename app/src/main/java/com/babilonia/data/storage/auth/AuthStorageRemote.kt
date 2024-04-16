package com.babilonia.data.storage.auth

import android.webkit.MimeTypeMap
import com.babilonia.EmptyConstants
import com.babilonia.data.datasource.AuthDataSourceRemote
import com.babilonia.data.network.model.*
import com.babilonia.data.network.model.json.AppConfigJson
import com.babilonia.data.network.model.json.ImageJson
import com.babilonia.data.network.model.json.PaisPrefixJson
import com.babilonia.data.network.model.json.UserJson
import com.babilonia.data.network.service.AuthService
import com.babilonia.data.network.service.NewAuthService
import io.reactivex.Single
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import java.io.File
import javax.inject.Inject

// Created by Anton Yatsenko on 27.05.2019.
class AuthStorageRemote @Inject constructor(
    private var authService: AuthService,
    private var newAuthService: NewAuthService,
) : AuthDataSourceRemote {
    override fun getAppConfig(version: Int): Single<AppConfigJson> {
        return newAuthService.getAppConfig(version = version).map { it.data }
    }

    override fun getUser(): Single<UserJson> {
        return newAuthService.getUser().map { it.data }
    }

    override fun updateUser(
        fullName: String,
        email: String,
        phoneNumber: String,
        prefix: String,
        password: String?,
        photoId: Int?
    ): Single<UserJson> {
        //val fullNamePart = MultipartBody.Part.createFormData("data[full_name]", fullName)
        //val emailPart = MultipartBody.Part.createFormData("data[email]", email)

        var photo: List<Int>? = null
        photoId?.let {
            photo = arrayListOf(it)
        }

        return newAuthService.updateUser(
            UpdateUserRequest(
                fullName,
                email,
                phoneNumber,
                prefix,
                password.isNullOrEmpty().not(),
                password,
                photo
            )
        ).map { it.data }
    }

    override fun uploadImages(
        image: String, type: String
    ): Single<List<ImageJson>> {
        val image = File(image)
        val mediaType = MediaType.parse(
            MimeTypeMap.getSingleton().getMimeTypeFromExtension(
                MimeTypeMap.getFileExtensionFromUrl(
                    image.path
                )
            ) ?: EmptyConstants.EMPTY_STRING
        )
        val body = RequestBody.create(
            mediaType, image
        )
        val imagePart = MultipartBody.Part.createFormData("photo[]", image.name, body)
        val sourcePart = MultipartBody.Part.createFormData("source", "android")
        val typePart = MultipartBody.Part.createFormData("type", type)
        return newAuthService.uploadImages(imagePart, sourcePart, typePart)
            .map { it.data.ids }
    }

    override fun refreshToken(token: String): Call<BaseResponse<AuthResponse>> {
        return authService.refreshToken(RefreshTokenRequest(token))
    }

    override fun authenticate(authRequest: AuthRequest): Single<AuthResponse> {
        return authService.authenticate(authRequest)
            .map { it.data }
    }

    override fun signUp(
        fullName: String,
        email: String,
        password: String,
        prefix: String,
        phoneNumber: String,
        ipAddress: String,
        userAgent: String,
        signProvider: String
    ): Single<SignUpResponse> {
        return newAuthService.signUp(
            SignUpRequest(
                fullName,
                email,
                password,
                prefix,
                phoneNumber,
                ipAddress,
                userAgent,
                signProvider
            )
        )
            .map { it.data }
    }

    override fun getListPaisPrefix(): Single<List<PaisPrefixJson>> {
        return  newAuthService.getListPaisPrefix().map { it.data.records }
    }

    override fun logIn(
        email: String,
        password: String,
        ipAddress: String,
        userAgent: String,
        signProvider: String
    ): Single<LogInResponse> {
        return newAuthService.logIn(
            LogInRequest(
                email,
                password,
                ipAddress,
                userAgent,
                signProvider
            )
        )
            .map { it.data }
    }

    override fun deleteAccount(): Single<BaseResponse<Any>> {
        return newAuthService.deleteAccount()
    }
}
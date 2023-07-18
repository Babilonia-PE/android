package com.babilonia.data.storage.auth

import android.webkit.MimeTypeMap
import com.babilonia.EmptyConstants
import com.babilonia.data.datasource.AuthDataSourceRemote
import com.babilonia.data.network.model.AuthRequest
import com.babilonia.data.network.model.AuthResponse
import com.babilonia.data.network.model.BaseResponse
import com.babilonia.data.network.model.RefreshTokenRequest
import com.babilonia.data.network.model.json.AppConfigJson
import com.babilonia.data.network.model.json.UpdateUserJson
import com.babilonia.data.network.model.json.UserJson
import com.babilonia.data.network.service.AuthService
import io.reactivex.Single
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import java.io.File
import javax.inject.Inject

// Created by Anton Yatsenko on 27.05.2019.
class AuthStorageRemote @Inject constructor(private var authService: AuthService) : AuthDataSourceRemote {
    override fun getAppConfig(): Single<AppConfigJson> {
        return authService.getAppConfig().map { it.data }
    }

    override fun getUser(): Single<UserJson> {
        return authService.getUser().map { it.data }
    }

    override fun updateUser(updateUserJson: UpdateUserJson): Single<UserJson> {
        val baseResponse = BaseResponse(updateUserJson)
        return authService.updateUser(baseResponse)
            .map { it.data }
    }

    override fun uploadUserAvatar(
        avatar: String, firstName: String, lastName: String, email: String
    ): Single<UserJson> {
        val image = File(avatar)
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
        val avatarPart = MultipartBody.Part.createFormData("data[avatar]", image.name, body)
        val firstNamePart = MultipartBody.Part.createFormData("data[first_name]", firstName)
        val lastNamePart = MultipartBody.Part.createFormData("data[last_name]", lastName)
        val emailPart = MultipartBody.Part.createFormData("data[email]", email)
        return authService.uploadUserAvatar(avatarPart, firstNamePart, lastNamePart, emailPart)
            .map { it.data }
    }

    override fun refreshToken(token: String): Call<BaseResponse<AuthResponse>> {
        return authService.refreshToken(RefreshTokenRequest(token))
    }

    override fun authenticate(authRequest: AuthRequest): Single<AuthResponse> {
        return authService.authenticate(authRequest)
            .map { it.data }
    }
}
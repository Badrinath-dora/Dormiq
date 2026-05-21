package com.dormiq.di

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.SessionManager
import io.github.jan.supabase.auth.user.UserSession
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.storage.Storage
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SupabaseModule {

    @Provides
    @Singleton
    fun provideSupabaseClient(@ApplicationContext context: Context): SupabaseClient {
        return createSupabaseClient(
            supabaseUrl = "https://bbmzzjmiafpgymoocmni.supabase.co", 
            supabaseKey = "sb_publishable_1EvvAXWG3kpcjYOaUk--GA_kb9eSqd6"
        ) {
            install(Auth) {
                sessionManager = EncryptedSharedPreferencesSessionManager(context)
            }
            install(Postgrest)
            install(Realtime)
            install(Storage)
        }
    }
}

class EncryptedSharedPreferencesSessionManager(context: Context) : SessionManager {
    private val json = Json { ignoreUnknownKeys = true }
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs = EncryptedSharedPreferences.create(
        context,
        "dormiq_secure_auth",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    override suspend fun saveSession(session: UserSession) {
        prefs.edit().putString("session_data", json.encodeToString(session)).apply()
    }

    override suspend fun loadSession(): UserSession? {
        val data = prefs.getString("session_data", null) ?: return null
        return try {
            json.decodeFromString<UserSession>(data)
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun deleteSession() {
        prefs.edit().remove("session_data").apply()
    }
}

package com.dormiq.data.repository

import com.dormiq.domain.model.UserProfile
import com.dormiq.domain.model.UserRole
import com.dormiq.domain.repository.AuthRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.OTP
import io.github.jan.supabase.auth.OtpType
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import com.dormiq.domain.model.Hostel
import com.dormiq.domain.model.Tenant
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.serialization.json.*
import com.dormiq.data.manager.ActiveHostelManager
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.tasks.await

@Singleton
class SupabaseAuthRepository @Inject constructor(
    private val supabaseClient: SupabaseClient,
    private val activeHostelManager: ActiveHostelManager
) : AuthRepository {

    override val sessionStatus = supabaseClient.auth.sessionStatus

    private var currentUser: UserProfile? = null
 
    override suspend fun getCurrentUser(): UserProfile? {
        if (currentUser == null) {
            val supabaseUser = supabaseClient.auth.currentUserOrNull()
            if (supabaseUser != null) {
                val userId = supabaseUser.id
                val rawPhone = supabaseUser.phone ?: ""
                var phone = rawPhone.replace("+91", "").replace(" ", "").trim()
                if (phone.startsWith("91") && phone.length == 12) {
                    phone = phone.substring(2)
                }
                
                // 1. Initial Role (Default to Tenant)
                var role = UserRole.TENANT
                var hostelId: String? = null
                var name = "User"
                var email = ""
                var roomNumber: String? = null
                var bedLabel: String? = null
                
                try {
                    // 2. Check if they own a hostel (Owner identification)
                    val hostels = supabaseClient.from("hostels")
                        .select(columns = Columns.list("id")) {
                            filter { eq("owner_id", userId) }
                        }.decodeList<Hostel>()
                    
                    if (hostels.isNotEmpty()) {
                        role = UserRole.OWNER
                        val activeSavedId = activeHostelManager.getActiveHostelId()
                        val isSavedIdValid = hostels.any { it.id == activeSavedId }
                        hostelId = if (isSavedIdValid) {
                            activeSavedId
                        } else {
                            val defaultId = hostels.first().id
                            activeHostelManager.setActiveHostelId(defaultId)
                            defaultId
                        }
                        
                        // Proactively sync profiles table
                        try {
                            supabaseClient.from("profiles").update(
                                mapOf("hostel_id" to hostelId)
                            ) {
                                filter { eq("id", userId) }
                            }
                        } catch (e: Exception) {
                            // Non-blocking
                        }
                    }

                    // 3. Check profiles table (Explicitly set during registration)
                    try {
                        val profile = supabaseClient.from("profiles")
                            .select { filter { eq("id", userId) } }
                            .decodeSingleOrNull<UserProfile>()
                        
                        if (profile != null) {
                            if (profile.role == UserRole.OWNER) role = UserRole.OWNER
                            name = profile.name
                            email = profile.email ?: ""
                        }
                    } catch (e: Exception) {
                        // Fallback: Check Auth Metadata if table is missing
                        supabaseUser.userMetadata?.let { metadata ->
                            val metaName = metadata["name"]?.jsonPrimitive?.contentOrNull
                            val metaRole = metadata["role"]?.jsonPrimitive?.contentOrNull
                            
                            if (metaName != null && name == "User") name = metaName
                            if (metaRole == "OWNER") role = UserRole.OWNER
                        }
                    }

                    // 4. Check if they are a registered tenant (Only if not already an OWNER)
                    if (role != UserRole.OWNER) {
                        val tenant = supabaseClient.from("tenants")
                            .select { filter { eq("phone", phone) } }
                            .decodeSingleOrNull<Tenant>()
                        if (tenant != null) {
                            role = UserRole.TENANT
                            hostelId = tenant.hostelId
                            if (name == "User") name = tenant.name
                            if (email.isEmpty()) email = tenant.email ?: ""
                            roomNumber = tenant.assignedRoomNumber
                            bedLabel = tenant.assignedBedLabel
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                currentUser = UserProfile(
                    id = userId,
                    name = name,
                    phone = phone,
                    email = email,
                    role = role,
                    hostelId = hostelId,
                    roomNumber = roomNumber,
                    bedLabel = bedLabel
                )
            }
        }
        return currentUser
    }

    override suspend fun isLoggedIn(): Boolean = getCurrentUser() != null

    override suspend fun sendOtp(phone: String, role: UserRole?): Result<Unit> {
        return try {
            val rawPhone = phone.replace("+91", "").replace(" ", "").trim()
            val cleanPhone = if (rawPhone.startsWith("91") && rawPhone.length == 12) rawPhone.substring(2) else rawPhone
            
            // Authorization Check: Only restrict Tenants whose numbers must be pre-registered.
            // Bypasses check entirely for Owners so they can register freely.
            if (role != UserRole.OWNER) {
                // Check if they are a registered tenant
                val tenant = supabaseClient.from("tenants")
                    .select(columns = Columns.list("id")) {
                        filter { eq("phone", cleanPhone) }
                    }.decodeSingleOrNull<Tenant>()
                
                if (tenant == null) {
                    // Check if they are already an owner in the hostels table
                    val hostel = supabaseClient.from("hostels")
                        .select(columns = Columns.list("id")) {
                            filter { eq("phone", cleanPhone) }
                        }.decodeSingleOrNull<Hostel>()
                    
                    if (hostel == null) {
                        return Result.failure(Exception("Unauthorized: This mobile number is not registered."))
                    }
                }
            }

            // Proceed with OTP if authorized
            val formattedPhone = if (phone.startsWith("+")) phone else "+91$cleanPhone"
            supabaseClient.auth.signInWith(OTP) {
                this.phone = formattedPhone
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun verifyOtp(phone: String, otp: String): Result<UserProfile> {
        return try {
            val formattedPhone = if (phone.startsWith("+")) phone else "+91${phone.trim()}"
            supabaseClient.auth.verifyPhoneOtp(
                type = OtpType.Phone.SMS,
                phone = formattedPhone,
                token = otp
            )

            // Reset current user to force a fresh profile fetch
            currentUser = null
            val profile = getCurrentUser()
            
            if (profile != null) {
                try {
                    syncFcmToken()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                Result.success(profile)
            } else {
                Result.failure(Exception("Failed to retrieve profile after login"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun loginWithPassword(userId: String, password: String): Result<UserProfile> {
        // Disabled since we moved to OTP for both roles
        return Result.failure(Exception("Password login disabled. Please use OTP."))
    }

    override suspend fun updateProfile(userId: String, name: String, email: String?, role: UserRole?): Result<Unit> {
        return try {
            val profileData = mutableMapOf<String, String>(
                "id" to userId,
                "name" to name
            )
            email?.let { profileData["email"] = it }
            
            // Priority: Explicit role passed > Cached role > Default
            val finalRole = role ?: currentUser?.role ?: UserRole.TENANT
            profileData["role"] = finalRole.name
            
            // Update auth metadata as well for redundancy
            try {
                supabaseClient.auth.updateUser {
                    data = buildJsonObject {
                        put("name", name)
                        email?.let { put("email", it) }
                        finalRole.let { put("role", it.name) }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            
            try {
                supabaseClient.from("profiles").upsert(profileData)
            } catch (e: Exception) {
                // If table is missing, we still succeed because we updated Auth Metadata
                if (!e.message.isNullOrEmpty() && e.message!!.contains("profiles")) {
                    println("Warning: profiles table missing, but metadata updated")
                } else {
                    throw e
                }
            }
            
            // Clear cache to force refresh on next getCurrentUser call
            currentUser = null
            getCurrentUser()
            
            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    override suspend fun completeRegistration(hostelId: String): Result<Unit> {
        return try {
            val userId = supabaseClient.auth.currentUserOrNull()?.id ?: throw Exception("Not logged in")
            
            // 1. Update active hostel manager
            activeHostelManager.setActiveHostelId(hostelId)

            // 2. Update profiles table
            supabaseClient.from("profiles").update(
                mapOf("hostel_id" to hostelId)
            ) {
                filter { eq("id", userId) }
            }
            
            // 3. Update auth metadata for RLS redundancy
            supabaseClient.auth.updateUser {
                data = buildJsonObject {
                    put("hostel_id", hostelId)
                }
            }
            
            // 4. Update local cache
            currentUser = currentUser?.copy(hostelId = hostelId)
            
            println("DEBUG: Registration completed and persisted for hostel: $hostelId")
            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    override suspend fun switchActiveHostel(hostelId: String): Result<Unit> {
        return try {
            val userId = supabaseClient.auth.currentUserOrNull()?.id ?: throw Exception("Not logged in")
            
            // 1. Update active hostel manager
            activeHostelManager.setActiveHostelId(hostelId)

            // 2. Update profiles table
            try {
                supabaseClient.from("profiles").update(
                    mapOf("hostel_id" to hostelId)
                ) {
                    filter { eq("id", userId) }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            
            // 3. Update local cache
            currentUser = currentUser?.copy(hostelId = hostelId)
            
            println("DEBUG: Active hostel switched to: $hostelId")
            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    override suspend fun logout() {
        try {
            supabaseClient.auth.signOut()
            currentUser = null
        } catch (e: Exception) {
            // Ignored
            currentUser = null
        }
    }

    override suspend fun syncFcmToken() {
        try {
            val userId = supabaseClient.auth.currentUserOrNull()?.id ?: return
            val token = FirebaseMessaging.getInstance().token.await()
            
            val tokenData = mapOf(
                "user_id" to userId.toString(),
                "token" to token,
                "device_type" to "android"
            )
            
            supabaseClient.from("fcm_tokens").upsert(tokenData) {
                onConflict = "token"
            }
            println("DEBUG: FCM Token successfully synchronized with user: $userId")
        } catch (e: Exception) {
            println("ERROR: Failed to sync FCM token: ${e.message}")
            e.printStackTrace()
        }
    }
}

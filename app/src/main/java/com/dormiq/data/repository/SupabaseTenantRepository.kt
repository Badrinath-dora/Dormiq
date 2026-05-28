package com.dormiq.data.repository

import com.dormiq.domain.model.*
import com.dormiq.domain.repository.TenantRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import com.dormiq.data.manager.ActiveHostelManager
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SupabaseTenantRepository @Inject constructor(
    private val supabase: SupabaseClient,
    private val activeHostelManager: ActiveHostelManager
) : TenantRepository {

    private val _tenants = MutableStateFlow<List<Tenant>>(emptyList())

    override fun getTenants(hostelId: String): Flow<List<Tenant>> = flow {
        if (hostelId.isBlank()) {
            emit(emptyList())
            return@flow
        }
        while (true) {
            try {
                val response = supabase.postgrest["tenants"]
                    .select { filter { eq("hostel_id", hostelId) } }
                    .decodeList<Tenant>()
                _tenants.value = response
                emit(response)
            } catch (e: Exception) {
                if (e !is kotlinx.coroutines.CancellationException) {
                    e.printStackTrace()
                }
                emit(emptyList())
            }
            kotlinx.coroutines.delay(4000)
        }
    }

    override fun getTenantsByRoom(hostelId: String, roomNumber: String): Flow<List<Tenant>> = flow {
        if (hostelId.isBlank()) {
            emit(emptyList())
            return@flow
        }
        while (true) {
            try {
                val response = supabase.postgrest["tenants"]
                    .select { 
                        filter { 
                            eq("hostel_id", hostelId) 
                            eq("assigned_room_number", roomNumber)
                            eq("status", "active")
                        } 
                    }
                    .decodeList<Tenant>()
                emit(response)
            } catch (e: Exception) {
                if (e !is kotlinx.coroutines.CancellationException) {
                    e.printStackTrace()
                }
                emit(emptyList())
            }
            kotlinx.coroutines.delay(4000)
        }
    }

    override suspend fun getTenant(tenantId: String): Tenant? {
        return try {
            supabase.postgrest["tenants"]
                .select { filter { eq("id", tenantId) } }
                .decodeSingleOrNull<Tenant>()
        } catch (e: Exception) {
            if (e !is kotlinx.coroutines.CancellationException) {
                e.printStackTrace()
            }
            null
        }
    }

    override suspend fun getTenantByPhone(phone: String): Tenant? {
        return try {
            // Clean digits only
            val digitsOnly = phone.filter { it.isDigit() }
            val last10 = digitsOnly.takeLast(10)
            
            println("Searching for tenant with phone variations: $digitsOnly, $last10, +91$last10")
            
            // Try matching with both the full number and just the last 10 digits
            // We use a simpler select to bypass potential RLS complexities during the first lookup
            val response = supabase.postgrest["tenants"]
                .select {
                    filter {
                        or {
                            eq("phone", digitsOnly)
                            eq("phone", last10)
                            eq("phone", "+91$last10")
                        }
                    }
                }
            
            val tenants = response.decodeList<Tenant>()
            println("Found ${tenants.size} tenants for phone $phone")
            tenants.firstOrNull()
        } catch (e: Exception) {
            println("Error in getTenantByPhone: ${e.message}")
            if (e !is kotlinx.coroutines.CancellationException) {
                e.printStackTrace()
            }
            null
        }
    }

    override fun getTenantByUserId(userId: String): Flow<Tenant?> = flow {
        while (true) {
            try {
                val response = supabase.postgrest["tenants"]
                    .select { filter { eq("user_id", userId) } }
                    .decodeSingleOrNull<Tenant>()
                emit(response)
            } catch (e: Exception) {
                if (e !is kotlinx.coroutines.CancellationException) {
                    e.printStackTrace()
                }
                emit(null)
            }
            kotlinx.coroutines.delay(4000)
        }
    }

    override suspend fun createTenant(tenant: Tenant): Result<Tenant> {
        return try {
            val resolvedHostelId = if (tenant.hostelId.isNullOrBlank()) {
                activeHostelManager.getActiveHostelId() ?: throw Exception("No active hostel selected")
            } else {
                tenant.hostelId
            }
            val tenantToInsert = tenant.copy(hostelId = resolvedHostelId)
            val newTenant = supabase.postgrest["tenants"]
                .insert(tenantToInsert) {
                    select()
                }.decodeSingle<Tenant>()
            Result.success(newTenant)
        } catch (e: Exception) {
            if (e !is kotlinx.coroutines.CancellationException) {
                e.printStackTrace()
            }
            Result.failure(e)
        }
    }

    override suspend fun updateTenant(tenant: Tenant): Result<Unit> {
        return try {
            supabase.postgrest["tenants"]
                .update(tenant) {
                    filter { eq("id", tenant.id) }
                }
            Result.success(Unit)
        } catch (e: Exception) {
            if (e !is kotlinx.coroutines.CancellationException) {
                e.printStackTrace()
            }
            Result.failure(e)
        }
    }

    override suspend fun updateTenantUserId(tenantId: String, userId: String): Result<Unit> {
        return try {
            supabase.postgrest["tenants"]
                .update(mapOf("user_id" to userId)) {
                    filter { eq("id", tenantId) }
                }
            Result.success(Unit)
        } catch (e: Exception) {
            if (e !is kotlinx.coroutines.CancellationException) {
                e.printStackTrace()
            }
            Result.failure(e)
        }
    }

    override suspend fun assignBed(
        tenantId: String,
        bedId: String,
        roomId: String,
        hostelId: String,
        rentAmount: Double
    ): Result<TenantAllocation> {
        return try {
            val resolvedHostelId = if (hostelId.isBlank()) {
                activeHostelManager.getActiveHostelId() ?: throw Exception("No active hostel selected")
            } else {
                hostelId
            }

            // 0. Free the old bed if this is a re-assignment
            val currentAllocation = getCurrentAllocation(tenantId)
            if (currentAllocation != null) {
                supabase.postgrest["beds"].update(mapOf("status" to "vacant", "current_tenant_id" to null, "current_tenant_name" to null)) {
                    filter { eq("id", currentAllocation.bedId) }
                }
                supabase.postgrest["tenant_allocations"].update(mapOf("is_active" to false)) {
                    filter { eq("id", currentAllocation.id) }
                }
            }

            // 1. Get Room and Bed info for metadata
            val bed = supabase.postgrest["beds"]
                .select { filter { eq("id", bedId) } }
                .decodeSingle<com.dormiq.domain.model.Bed>()
            val room = supabase.postgrest["rooms"]
                .select { filter { eq("id", roomId) } }
                .decodeSingle<com.dormiq.domain.model.Room>()
            val floor = supabase.postgrest["floors"]
                .select { filter { eq("id", room.floorId) } }
                .decodeSingle<com.dormiq.domain.model.Floor>()
            val tenant = supabase.postgrest["tenants"]
                .select { filter { eq("id", tenantId) } }
                .decodeSingle<com.dormiq.domain.model.Tenant>()

            // 2. Create Allocation record
            val allocation = TenantAllocation(
                id = java.util.UUID.randomUUID().toString(),
                tenantId = tenantId,
                bedId = bedId,
                roomId = roomId,
                hostelId = resolvedHostelId,
                checkInDate = java.time.LocalDate.now().toString(),
                isActive = true
            )
            supabase.postgrest["tenant_allocations"].insert(allocation)

            // 3. Update Tenant record with metadata
            supabase.postgrest["tenants"].update(
                mapOf(
                    "assigned_bed_id" to bedId,
                    "assigned_bed_label" to bed.label,
                    "assigned_room_number" to room.roomNumber,
                    "assigned_floor_name" to floor.name,
                    "status" to "active"
                )
            ) { filter { eq("id", tenantId) } }

            // 4. Update Bed record
            supabase.postgrest["beds"].update(
                mapOf(
                    "status" to "occupied",
                    "current_tenant_id" to tenantId,
                    "current_tenant_name" to tenant.name
                )
            ) { filter { eq("id", bedId) } }

            // 5. Create active payment bill
            val currentMonth = java.time.format.DateTimeFormatter.ofPattern("MMMM yyyy").format(java.time.LocalDate.now())
            val dueDateStr = java.time.LocalDate.now().plusDays(7).toString() // due 7 days from now
            
            val initialPayment = RentPayment(
                id = java.util.UUID.randomUUID().toString(),
                hostelId = hostelId,
                tenantId = tenantId,
                tenantName = tenant.name,
                amount = rentAmount,
                paidAmount = 0.0,
                dueDate = dueDateStr,
                paidDate = null,
                month = currentMonth,
                status = PaymentStatus.PENDING,
                transactionId = null,
                roomNumber = room.roomNumber,
                floorName = floor.name
            )
            supabase.postgrest["payments"].insert<RentPayment>(initialPayment)

            Result.success(allocation)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    override suspend fun getAllocations(tenantId: String): List<TenantAllocation> {
        return try {
            supabase.postgrest["tenant_allocations"]
                .select { filter { eq("tenant_id", tenantId) } }
                .decodeList<TenantAllocation>()
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun getBedAllocations(bedId: String): List<TenantAllocation> {
        return try {
            supabase.postgrest["tenant_allocations"]
                .select { filter { eq("bed_id", bedId) } }
                .decodeList<TenantAllocation>()
                .sortedByDescending { it.createdAt }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun getCurrentAllocation(tenantId: String): TenantAllocation? {
        return try {
            supabase.postgrest["tenant_allocations"]
                .select { 
                    filter { 
                        eq("tenant_id", tenantId) 
                        eq("is_active", true)
                    } 
                }
                .decodeSingleOrNull<TenantAllocation>()
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun checkoutTenant(tenantId: String): Result<Unit> {
        return try {
            val allocation = getCurrentAllocation(tenantId)
            if (allocation != null) {
                // 1. Mark allocation inactive
                supabase.postgrest["tenant_allocations"].update(mapOf("is_active" to false, "check_out_date" to java.time.LocalDate.now().toString())) {
                    filter { eq("id", allocation.id) }
                }
                
                // 2. Mark bed vacant
                supabase.postgrest["beds"].update(mapOf("status" to "vacant", "current_tenant_id" to null, "current_tenant_name" to null)) {
                    filter { eq("id", allocation.bedId) }
                }
            }

            // 3. Mark tenant checked out
            supabase.postgrest["tenants"].update(mapOf(
                "status" to "checked_out", 
                "assigned_bed_id" to null, 
                "assigned_bed_label" to null,
                "assigned_room_number" to null, 
                "assigned_floor_name" to null
            )) {
                filter { eq("id", tenantId) }
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    override suspend fun deleteTenant(tenantId: String): Result<Unit> {
        return try {
            // 1. Check if the tenant is currently occupying a bed and free it
            val allocation = getCurrentAllocation(tenantId)
            if (allocation != null) {
                supabase.postgrest["beds"].update(mapOf("status" to "vacant", "current_tenant_id" to null, "current_tenant_name" to null)) {
                    filter { eq("id", allocation.bedId) }
                }
            }
            
            // 2. Delete the tenant
            supabase.postgrest["tenants"].delete {
                filter { eq("id", tenantId) }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }
}

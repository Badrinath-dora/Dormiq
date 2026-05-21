package com.dormiq.data.repository

import com.dormiq.domain.model.Hostel
import com.dormiq.domain.model.OccupancyStats
import com.dormiq.domain.repository.HostelRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SupabaseHostelRepository @Inject constructor(
    private val supabase: SupabaseClient
) : HostelRepository {

    private val _hostels = MutableStateFlow<List<Hostel>>(emptyList())

    override fun getHostels(): Flow<List<Hostel>> = flow {
        try {
            val response = supabase.postgrest["hostels"].select().decodeList<Hostel>()
            _hostels.value = response
            emit(response)
        } catch (e: Exception) {
            e.printStackTrace()
            // Fallback to cache if error
            emit(_hostels.value)
        }
    }

    override suspend fun getHostel(hostelId: String): Hostel? {
        return try {
            supabase.postgrest["hostels"]
                .select { filter { eq("id", hostelId) } }
                .decodeSingleOrNull<Hostel>()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    override suspend fun getHostelByOwner(ownerId: String): Result<Hostel> {
        return try {
            val hostel = supabase.postgrest["hostels"]
                .select { filter { eq("owner_id", ownerId) } }
                .decodeSingleOrNull<Hostel>()
            if (hostel != null) {
                Result.success(hostel)
            } else {
                Result.failure(Exception("Hostel not found for this owner"))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    override suspend fun createHostel(hostel: Hostel): Result<Hostel> {
        return try {
            val newHostel = supabase.postgrest["hostels"]
                .insert(hostel) {
                    select()
                }.decodeSingle<Hostel>()
            
            // Update cache
            _hostels.value = _hostels.value + newHostel
            Result.success(newHostel)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    override suspend fun updateHostel(hostel: Hostel): Result<Unit> {
        return try {
            supabase.postgrest["hostels"]
                .update(hostel) {
                    filter { eq("id", hostel.id) }
                }
            
            // Update cache
            _hostels.value = _hostels.value.map { if (it.id == hostel.id) hostel else it }
            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    override fun getStats(hostelId: String): Flow<OccupancyStats> = flow {
        try {
            // Fetch total counts from respective tables
            val floors = supabase.postgrest["floors"].select { filter { eq("hostel_id", hostelId) } }.decodeList<com.dormiq.domain.model.Floor>()
            val rooms = supabase.postgrest["rooms"].select { filter { eq("hostel_id", hostelId) } }.decodeList<com.dormiq.domain.model.Room>()
            val beds = supabase.postgrest["beds"].select { filter { eq("hostel_id", hostelId) } }.decodeList<com.dormiq.domain.model.Bed>()
            val tenants = supabase.postgrest["tenants"].select { filter { eq("hostel_id", hostelId) } }.decodeList<com.dormiq.domain.model.Tenant>()
            val issues = supabase.postgrest["issues"].select { 
                filter { 
                    eq("hostel_id", hostelId)
                    eq("status", "open")
                } 
            }.decodeList<com.dormiq.domain.model.Issue>()

            val activeBedIds = tenants.filter { it.status == "active" }.mapNotNull { it.assignedBedId }.toSet()
            
            val stats = OccupancyStats(
                totalBeds = beds.size,
                occupiedBeds = beds.count { activeBedIds.contains(it.id) || it.status == "occupied" && activeBedIds.contains(it.id) }.let { activeBedIds.size },
                vacantBeds = beds.count { !activeBedIds.contains(it.id) && it.status != "blocked" && it.status != "maintenance" },
                blockedBeds = beds.count { it.status == "blocked" },
                maintenanceBeds = beds.count { it.status == "maintenance" },
                totalFloors = floors.size,
                totalRooms = rooms.size,
                totalTenants = tenants.size,
                openIssues = issues.size
            )
            emit(stats)
        } catch (e: Exception) {
            e.printStackTrace()
            emit(OccupancyStats())
        }
    }
}

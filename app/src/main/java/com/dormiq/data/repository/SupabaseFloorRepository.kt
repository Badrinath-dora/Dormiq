package com.dormiq.data.repository

import com.dormiq.domain.model.Floor
import com.dormiq.domain.repository.FloorRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SupabaseFloorRepository @Inject constructor(
    private val supabase: SupabaseClient
) : FloorRepository {

    private val _floors = MutableStateFlow<List<Floor>>(emptyList())

    override fun getFloors(hostelId: String): Flow<List<Floor>> = flow {
        try {
            val response = supabase.postgrest["floors"]
                .select { filter { eq("hostel_id", hostelId) } }
                .decodeList<Floor>()

            // Fetch rooms and beds for the hostel to calculate stats dynamically
            val rooms = try {
                supabase.postgrest["rooms"]
                    .select { filter { eq("hostel_id", hostelId) } }
                    .decodeList<com.dormiq.domain.model.Room>()
            } catch (e: Exception) { emptyList() }
            
            val beds = try {
                supabase.postgrest["beds"]
                    .select { filter { eq("hostel_id", hostelId) } }
                    .decodeList<com.dormiq.domain.model.Bed>()
            } catch (e: Exception) { emptyList() }
            
            // Fetch tenants to ensure accurate occupancy counts
            val tenants = try {
                supabase.postgrest["tenants"]
                    .select { filter { eq("hostel_id", hostelId) } }
                    .decodeList<com.dormiq.domain.model.Tenant>()
            } catch (e: Exception) { emptyList() }
            
            val activeBedIds = tenants.filter { it.status == "active" }.mapNotNull { it.assignedBedId }.toSet()

            val updatedFloors = response.sortedBy { it.number }.mapIndexed { index, floor ->
                val floorRooms = rooms.filter { room ->
                    val belongsToThisFloor = when {
                        index == 0 -> {
                            room.roomNumber.startsWith("G", ignoreCase = true) ||
                            room.roomNumber.startsWith("0") ||
                            (room.roomNumber.firstOrNull()?.isDigit() == true && 
                             response.indices.none { fIdx -> fIdx > 0 && room.roomNumber.startsWith(fIdx.toString()) })
                        }
                        else -> {
                            room.roomNumber.startsWith(index.toString()) ||
                            room.roomNumber.startsWith("F$index", ignoreCase = true) ||
                            room.roomNumber.startsWith("Floor $index", ignoreCase = true)
                        }
                    }
                    belongsToThisFloor
                }
                val floorBeds = beds.filter { bed -> floorRooms.any { room -> room.id == bed.roomId } }
                
                floor.copy(
                    totalRooms = floorRooms.size,
                    totalBeds = floorBeds.size,
                    occupiedBeds = floorBeds.count { activeBedIds.contains(it.id) }
                )
            }

            _floors.value = updatedFloors
            emit(updatedFloors)
        } catch (e: Exception) {
            e.printStackTrace()
            emit(_floors.value.filter { it.hostelId == hostelId })
        }
    }

    override suspend fun createFloor(floor: Floor): Result<Floor> {
        return try {
            val newFloor = supabase.postgrest["floors"]
                .insert(floor) { select() }
                .decodeSingle<Floor>()
            _floors.value = _floors.value + newFloor
            Result.success(newFloor)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    override suspend fun updateFloor(floor: Floor): Result<Unit> {
        return try {
            supabase.postgrest["floors"].update(floor) { filter { eq("id", floor.id) } }
            _floors.value = _floors.value.map { if (it.id == floor.id) floor else it }
            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    override suspend fun deleteFloor(floorId: String): Result<Unit> {
        return try {
            supabase.postgrest["floors"].delete { filter { eq("id", floorId) } }
            _floors.value = _floors.value.filter { it.id != floorId }
            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }
}

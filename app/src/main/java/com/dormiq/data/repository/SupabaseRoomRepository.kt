package com.dormiq.data.repository

import com.dormiq.domain.model.Room
import com.dormiq.domain.repository.RoomRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SupabaseRoomRepository @Inject constructor(
    private val supabase: SupabaseClient
) : RoomRepository {

    private val _rooms = MutableStateFlow<List<Room>>(emptyList())

    override fun getRooms(hostelId: String): Flow<List<Room>> = flow {
        while (true) {
            try {
                val response = supabase.postgrest["rooms"]
                    .select { filter { eq("hostel_id", hostelId) } }
                    .decodeList<Room>()
                    
                val beds = try { supabase.postgrest["beds"].select { filter { eq("hostel_id", hostelId) } }.decodeList<com.dormiq.domain.model.Bed>() } catch (e: Exception) { emptyList() }
                val tenants = try { supabase.postgrest["tenants"].select { filter { eq("hostel_id", hostelId) } }.decodeList<com.dormiq.domain.model.Tenant>() } catch (e: Exception) { emptyList() }
                val activeBedIds = tenants.filter { it.status == "active" }.mapNotNull { it.assignedBedId }.toSet()
                
                val updatedRooms = response.map { room ->
                    val roomBeds = beds.filter { it.roomId == room.id }
                    room.copy(totalBeds = roomBeds.size, occupiedBeds = roomBeds.count { activeBedIds.contains(it.id) })
                }
                
                _rooms.value = updatedRooms
                emit(updatedRooms)
            } catch (e: Exception) {
                e.printStackTrace()
                emit(_rooms.value.filter { it.hostelId == hostelId })
            }
            kotlinx.coroutines.delay(4000)
        }
    }

    override fun getRoomsByFloor(floorId: String): Flow<List<Room>> = flow {
        while (true) {
            try {
                val response = supabase.postgrest["rooms"]
                    .select { filter { eq("floor_id", floorId) } }
                    .decodeList<Room>()
                
                val hostelId = response.firstOrNull()?.hostelId
                val updatedRooms = if (hostelId != null) {
                    val beds = try { supabase.postgrest["beds"].select { filter { eq("hostel_id", hostelId) } }.decodeList<com.dormiq.domain.model.Bed>() } catch (e: Exception) { emptyList() }
                    val tenants = try { supabase.postgrest["tenants"].select { filter { eq("hostel_id", hostelId) } }.decodeList<com.dormiq.domain.model.Tenant>() } catch (e: Exception) { emptyList() }
                    val activeBedIds = tenants.filter { it.status == "active" }.mapNotNull { it.assignedBedId }.toSet()
                    
                    val floor = try {
                        supabase.postgrest["floors"].select { filter { eq("id", floorId) } }.decodeSingleOrNull<com.dormiq.domain.model.Floor>()
                    } catch (e: Exception) { null }
                    
                    response.map { room ->
                        val roomBeds = beds.filter { it.roomId == room.id }
                        room.copy(
                            totalBeds = roomBeds.size, 
                            occupiedBeds = roomBeds.count { activeBedIds.contains(it.id) },
                            floorName = floor?.name
                        )
                    }
                } else response
                
                // Merge with local cache
                val newMap = _rooms.value.associateBy { it.id }.toMutableMap()
                updatedRooms.forEach { newMap[it.id] = it }
                _rooms.value = newMap.values.toList()
                
                emit(updatedRooms)
            } catch (e: Exception) {
                e.printStackTrace()
                emit(_rooms.value.filter { it.floorId == floorId })
            }
            kotlinx.coroutines.delay(4000)
        }
    }

    override suspend fun getRoom(roomId: String): Room? {
        return try {
            supabase.postgrest["rooms"]
                .select { filter { eq("id", roomId) } }
                .decodeSingleOrNull<Room>()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    override suspend fun createRoom(room: Room): Result<Room> {
        return try {
            val newRoom = supabase.postgrest["rooms"]
                .insert(room) { select() }
                .decodeSingle<Room>()
            _rooms.value = _rooms.value + newRoom
            Result.success(newRoom)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    override suspend fun updateRoom(room: Room): Result<Unit> {
        return try {
            supabase.postgrest["rooms"].update(room) { filter { eq("id", room.id) } }
            _rooms.value = _rooms.value.map { if (it.id == room.id) room else it }
            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    override suspend fun deleteRoom(roomId: String): Result<Unit> {
        return try {
            supabase.postgrest["rooms"].delete { filter { eq("id", roomId) } }
            _rooms.value = _rooms.value.filter { it.id != roomId }
            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }
}

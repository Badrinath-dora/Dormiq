package com.dormiq.data.repository

import com.dormiq.domain.model.Bed
import com.dormiq.domain.repository.BedRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SupabaseBedRepository @Inject constructor(
    private val supabase: SupabaseClient
) : BedRepository {

    private val _beds = MutableStateFlow<List<Bed>>(emptyList())

    override fun getBeds(roomId: String): Flow<List<Bed>> = flow {
        try {
            val response = supabase.postgrest["beds"]
                .select { filter { eq("room_id", roomId) } }
                .decodeList<Bed>()
            
            val newMap = _beds.value.associateBy { it.id }.toMutableMap()
            response.forEach { newMap[it.id] = it }
            _beds.value = newMap.values.toList()
            
            emit(response)
        } catch (e: Exception) {
            e.printStackTrace()
            emit(_beds.value.filter { it.roomId == roomId })
        }
    }

    override fun getVacantBeds(roomId: String): Flow<List<Bed>> = flow {
        try {
            val response = supabase.postgrest["beds"]
                .select { 
                    filter { 
                        eq("room_id", roomId)
                        eq("status", com.dormiq.domain.model.BedStatus.VACANT)
                    } 
                }
                .decodeList<Bed>()
            emit(response)
        } catch (e: Exception) {
            e.printStackTrace()
            emit(_beds.value.filter { it.roomId == roomId && it.status == com.dormiq.domain.model.BedStatus.VACANT })
        }
    }

    override suspend fun getBed(bedId: String): Bed? {
        return try {
            supabase.postgrest["beds"]
                .select { filter { eq("id", bedId) } }
                .decodeSingleOrNull<Bed>()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    override suspend fun createBed(bed: Bed): Result<Bed> {
        return try {
            val newBed = supabase.postgrest["beds"]
                .insert(bed) { select() }
                .decodeSingle<Bed>()
            _beds.value = _beds.value + newBed
            Result.success(newBed)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    override suspend fun updateBed(bed: Bed): Result<Unit> {
        return try {
            supabase.postgrest["beds"].update(bed) { filter { eq("id", bed.id) } }
            _beds.value = _beds.value.map { if (it.id == bed.id) bed else it }
            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    override suspend fun updateBedStatus(bedId: String, status: String): Result<Unit> {
        return try {
            supabase.postgrest["beds"]
                .update(mapOf("status" to status)) { filter { eq("id", bedId) } }
            _beds.value = _beds.value.map { if (it.id == bedId) it.copy(status = status) else it }
            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }
}

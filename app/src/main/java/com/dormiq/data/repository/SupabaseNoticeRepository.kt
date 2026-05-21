package com.dormiq.data.repository

import com.dormiq.domain.model.Notice
import com.dormiq.domain.repository.NoticeRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

@Singleton
class SupabaseNoticeRepository @Inject constructor(
    private val supabase: SupabaseClient
) : NoticeRepository {

    override fun getNotices(hostelId: String): Flow<List<Notice>> = flow {
        try {
            val response = supabase.postgrest["notices"]
                .select { filter { eq("hostel_id", hostelId) } }
                .decodeList<Notice>()
            emit(response)
        } catch (e: Exception) {
            e.printStackTrace()
            emit(emptyList())
        }
    }

    override suspend fun createNotice(notice: Notice): Result<Notice> {
        println("DEBUG: Creating Notice via JsonObject: $notice")
        return try {
            val noticeJson = kotlinx.serialization.json.buildJsonObject {
                put("id", notice.id)
                put("hostel_id", notice.hostelId)
                put("title", notice.title)
                put("body", notice.body)
                put("category", notice.category)
                put("target_audience", notice.targetAudience)
                put("target_floor_id", notice.targetFloorId)
                put("target_room_id", notice.targetRoomId)
                put("published_at", notice.publishedAt)
                put("created_by_id", notice.createdById)
                put("created_by_name", notice.createdByName)
                put("is_published", notice.isPublished)
            }
            
            supabase.postgrest["notices"]
                .insert(noticeJson)
            
            println("DEBUG: Notice created successfully (JSON): ${notice.id}")
            Result.success(notice)
        } catch (e: Exception) {
            println("DEBUG: Failed to create notice (JSON): ${e.message}")
            e.printStackTrace()
            Result.failure(e)
        }
    }

    override suspend fun updateNotice(notice: Notice): Result<Unit> {
        return try {
            supabase.postgrest["notices"]
                .update(notice) {
                    filter { eq("id", notice.id) }
                }
            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    override suspend fun deleteNotice(noticeId: String): Result<Unit> {
        return try {
            supabase.postgrest["notices"]
                .delete {
                    filter { eq("id", noticeId) }
                }
            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }
}

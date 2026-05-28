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

import io.ktor.client.request.post
import io.ktor.client.request.header
import io.ktor.client.request.setBody

@Singleton
class SupabaseNoticeRepository @Inject constructor(
    private val supabase: SupabaseClient
) : NoticeRepository {

    override fun getNotices(hostelId: String): Flow<List<Notice>> = flow {
        while (true) {
            try {
                val response = supabase.postgrest["notices"]
                    .select { filter { eq("hostel_id", hostelId) } }
                    .decodeList<Notice>()
                emit(response)
            } catch (e: Exception) {
                e.printStackTrace()
                emit(emptyList())
            }
            kotlinx.coroutines.delay(4000)
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
            
            // Trigger push notification directly to active tenants
            try {
                println("DEBUG: Directly triggering send-push-notification for notice: ${notice.title}")
                val response = supabase.httpClient.post("https://bbmzzjmiafpgymoocmni.supabase.co/functions/v1/send-push-notification") {
                    header("apikey", "sb_publishable_1EvvAXWG3kpcjYOaUk--GA_kb9eSqd6")
                    header("Authorization", "Bearer sb_publishable_1EvvAXWG3kpcjYOaUk--GA_kb9eSqd6")
                    header("Content-Type", "application/json")
                    setBody(
                        buildJsonObject {
                            put("event_type", "notice")
                            put("hostel_id", notice.hostelId)
                            put("title", notice.title)
                            put("body", notice.body)
                        }.toString()
                    )
                }
                println("DEBUG: Direct send-push-notification response status: ${response.status}")
            } catch (ex: Exception) {
                println("DEBUG: Failed to directly trigger send-push-notification: ${ex.message}")
                ex.printStackTrace()
            }
            
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

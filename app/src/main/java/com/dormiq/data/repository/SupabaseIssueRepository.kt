package com.dormiq.data.repository

import com.dormiq.domain.model.Issue
import com.dormiq.domain.model.IssueComment
import com.dormiq.domain.repository.IssueRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

import io.ktor.client.request.post
import io.ktor.client.request.header
import io.ktor.client.request.setBody
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

@Singleton
class SupabaseIssueRepository @Inject constructor(
    private val supabase: SupabaseClient
) : IssueRepository {

    override fun getIssues(hostelId: String): Flow<List<Issue>> = flow {
        while (true) {
            try {
                val response = supabase.postgrest["issues"]
                    .select { filter { eq("hostel_id", hostelId) } }
                    .decodeList<Issue>()
                emit(response)
            } catch (e: Exception) {
                e.printStackTrace()
                emit(emptyList())
            }
            kotlinx.coroutines.delay(4000)
        }
    }

    override fun getIssuesByTenant(tenantId: String): Flow<List<Issue>> = flow {
        while (true) {
            try {
                val response = supabase.postgrest["issues"]
                    .select { filter { eq("tenant_id", tenantId) } }
                    .decodeList<Issue>()
                emit(response)
            } catch (e: Exception) {
                e.printStackTrace()
                emit(emptyList())
            }
            kotlinx.coroutines.delay(4000)
        }
    }

    override suspend fun getIssue(issueId: String): Issue? {
        return try {
            supabase.postgrest["issues"]
                .select { filter { eq("id", issueId) } }
                .decodeSingleOrNull<Issue>()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    override suspend fun createIssue(issue: Issue): Result<Issue> {
        return try {
            val created = supabase.postgrest["issues"]
                .insert(issue) {
                    select()
                }.decodeSingle<Issue>()
            
            // Trigger push notification directly to the owner
            try {
                println("DEBUG: Directly triggering send-push-notification for new issue: ${issue.category}")
                val response = supabase.httpClient.post("https://bbmzzjmiafpgymoocmni.supabase.co/functions/v1/send-push-notification") {
                    header("apikey", "sb_publishable_1EvvAXWG3kpcjYOaUk--GA_kb9eSqd6")
                    header("Authorization", "Bearer sb_publishable_1EvvAXWG3kpcjYOaUk--GA_kb9eSqd6")
                    header("Content-Type", "application/json")
                    setBody(
                        buildJsonObject {
                            put("event_type", "issue_created")
                            put("hostel_id", created.hostelId)
                            put("title", "New Grievance Raised")
                            val roomStr = if (created.roomNumber != null) "Room ${created.roomNumber}" else "Complaint"
                            put("body", "$roomStr: ${created.description ?: created.category}")
                        }.toString()
                    )
                }
                println("DEBUG: Direct send-push-notification for new issue response status: ${response.status}")
            } catch (ex: Exception) {
                println("DEBUG: Failed to directly trigger new issue push notification: ${ex.message}")
                ex.printStackTrace()
            }
            
            Result.success(created)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    override suspend fun updateIssueStatus(issueId: String, status: String, notes: String): Result<Unit> {
        return try {
            // 1. Fetch issue details to obtain tenantId
            val issue = getIssue(issueId)
            
            supabase.postgrest["issues"]
                .update({
                    set("status", status)
                    set("technician_notes", notes)
                    set("updated_at", LocalDateTime.now().toString())
                }) {
                    filter { eq("id", issueId) }
                }
            
            // 2. Trigger push notification directly to the tenant
            if (issue != null) {
                try {
                    println("DEBUG: Directly triggering send-push-notification for issue status update: ${issue.id}")
                    val displayStatus = status.replace("_", " ").uppercase()
                    val response = supabase.httpClient.post("https://bbmzzjmiafpgymoocmni.supabase.co/functions/v1/send-push-notification") {
                        header("apikey", "sb_publishable_1EvvAXWG3kpcjYOaUk--GA_kb9eSqd6")
                        header("Authorization", "Bearer sb_publishable_1EvvAXWG3kpcjYOaUk--GA_kb9eSqd6")
                        header("Content-Type", "application/json")
                        setBody(
                            buildJsonObject {
                                put("event_type", "issue_updated")
                                put("tenant_id", issue.tenantId)
                                put("title", "Grievance Status Updated")
                                put("body", "Your complaint about '${issue.category}' has been marked as $displayStatus.")
                            }.toString()
                        )
                    }
                    println("DEBUG: Direct send-push-notification for issue status update response status: ${response.status}")
                } catch (ex: Exception) {
                    println("DEBUG: Failed to directly trigger status update push notification: ${ex.message}")
                    ex.printStackTrace()
                }
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    override fun getComments(issueId: String): Flow<List<IssueComment>> = flow {
        while (true) {
            try {
                val response = supabase.postgrest["issue_comments"]
                    .select { filter { eq("issue_id", issueId) } }
                    .decodeList<IssueComment>()
                emit(response)
            } catch (e: Exception) {
                e.printStackTrace()
                emit(emptyList())
            }
            kotlinx.coroutines.delay(4000)
        }
    }

    override suspend fun addComment(comment: IssueComment): Result<Unit> {
        return try {
            supabase.postgrest["issue_comments"]
                .insert(comment)
            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }
}

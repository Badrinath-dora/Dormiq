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

@Singleton
class SupabaseIssueRepository @Inject constructor(
    private val supabase: SupabaseClient
) : IssueRepository {

    override fun getIssues(hostelId: String): Flow<List<Issue>> = flow {
        try {
            val response = supabase.postgrest["issues"]
                .select { filter { eq("hostel_id", hostelId) } }
                .decodeList<Issue>()
            emit(response)
        } catch (e: Exception) {
            e.printStackTrace()
            emit(emptyList())
        }
    }

    override fun getIssuesByTenant(tenantId: String): Flow<List<Issue>> = flow {
        try {
            val response = supabase.postgrest["issues"]
                .select { filter { eq("tenant_id", tenantId) } }
                .decodeList<Issue>()
            emit(response)
        } catch (e: Exception) {
            e.printStackTrace()
            emit(emptyList())
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
            Result.success(created)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    override suspend fun updateIssueStatus(issueId: String, status: String, notes: String): Result<Unit> {
        return try {
            supabase.postgrest["issues"]
                .update({
                    set("status", status)
                    set("technician_notes", notes)
                    set("updated_at", LocalDateTime.now().toString())
                }) {
                    filter { eq("id", issueId) }
                }
            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    override fun getComments(issueId: String): Flow<List<IssueComment>> = flow {
        try {
            val response = supabase.postgrest["issue_comments"]
                .select { filter { eq("issue_id", issueId) } }
                .decodeList<IssueComment>()
            emit(response)
        } catch (e: Exception) {
            e.printStackTrace()
            emit(emptyList())
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

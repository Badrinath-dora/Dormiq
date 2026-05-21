package com.dormiq.data.repository

import com.dormiq.domain.model.*
import com.dormiq.domain.repository.PaymentRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SupabasePaymentRepository @Inject constructor(
    private val supabase: SupabaseClient
) : PaymentRepository {

    override fun getPayments(hostelId: String): Flow<List<RentPayment>> = flow {
        try {
            val response = supabase.postgrest["payments"]
                .select { 
                    filter { eq("hostel_id", hostelId) } 
                }
                .decodeList<RentPayment>()
            emit(response)
        } catch (e: Exception) {
            e.printStackTrace()
            emit(emptyList())
        }
    }

    override fun getTenantPayments(tenantId: String): Flow<List<RentPayment>> = flow {
        try {
            val response = supabase.postgrest["payments"]
                .select { 
                    filter { eq("tenant_id", tenantId) } 
                }
                .decodeList<RentPayment>()
            emit(response)
        } catch (e: Exception) {
            e.printStackTrace()
            emit(emptyList())
        }
    }

    override suspend fun getPaymentSummary(hostelId: String): Flow<PaymentSummary> = flow {
        try {
            val payments = supabase.postgrest["payments"]
                .select { filter { eq("hostel_id", hostelId) } }
                .decodeList<RentPayment>()
            
            val summary = PaymentSummary(
                totalExpected = payments.sumOf { it.amount },
                totalCollected = payments.sumOf { it.paidAmount },
                totalPending = payments.sumOf { it.amount - it.paidAmount },
                paidCount = payments.count { it.status == PaymentStatus.PAID },
                pendingCount = payments.count { it.status != PaymentStatus.PAID }
            )
            emit(summary)
        } catch (e: Exception) {
            e.printStackTrace()
            emit(PaymentSummary())
        }
    }

    override suspend fun recordPayment(payment: RentPayment): Result<RentPayment> {
        return try {
            val newPayment = supabase.postgrest["payments"]
                .insert(payment) { select() }
                .decodeSingle<RentPayment>()
            Result.success(newPayment)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    override suspend fun updatePayment(payment: RentPayment): Result<RentPayment> {
        return try {
            val updated = supabase.postgrest["payments"]
                .update(payment) {
                    filter { eq("id", payment.id) }
                    select()
                }
                .decodeSingle<RentPayment>()
            Result.success(updated)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }
}

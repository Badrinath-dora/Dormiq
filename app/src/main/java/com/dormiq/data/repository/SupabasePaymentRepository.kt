package com.dormiq.data.repository

import com.dormiq.domain.model.*
import com.dormiq.domain.repository.PaymentRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

import io.ktor.client.request.post
import io.ktor.client.request.header
import io.ktor.client.request.setBody
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

@Singleton
class SupabasePaymentRepository @Inject constructor(
    private val supabase: SupabaseClient
) : PaymentRepository {

    override fun getPayments(hostelId: String): Flow<List<RentPayment>> = flow {
        while (true) {
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
            kotlinx.coroutines.delay(4000)
        }
    }

    override fun getTenantPayments(tenantId: String): Flow<List<RentPayment>> = flow {
        while (true) {
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
            kotlinx.coroutines.delay(4000)
        }
    }

    override suspend fun getPaymentSummary(hostelId: String): Flow<PaymentSummary> = flow {
        while (true) {
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
            kotlinx.coroutines.delay(4000)
        }
    }

    override suspend fun recordPayment(payment: RentPayment): Result<RentPayment> {
        return try {
            val newPayment = supabase.postgrest["payments"]
                .insert(payment) { select() }
                .decodeSingle<RentPayment>()
            
            // Trigger push notification directly to tenant
            try {
                println("DEBUG: Directly triggering send-push-notification for new payment: ${payment.month}")
                val response = supabase.httpClient.post("https://bbmzzjmiafpgymoocmni.supabase.co/functions/v1/send-push-notification") {
                    header("apikey", "sb_publishable_1EvvAXWG3kpcjYOaUk--GA_kb9eSqd6")
                    header("Authorization", "Bearer sb_publishable_1EvvAXWG3kpcjYOaUk--GA_kb9eSqd6")
                    header("Content-Type", "application/json")
                    setBody(
                        buildJsonObject {
                            put("event_type", "payment_created")
                            put("tenant_id", newPayment.tenantId)
                            put("title", "New Rent Invoice Generated")
                            put("body", "A pending rent invoice of ₹${newPayment.amount} has been raised for ${newPayment.month}.")
                        }.toString()
                    )
                }
                println("DEBUG: Direct send-push-notification for new payment response status: ${response.status}")
            } catch (ex: Exception) {
                println("DEBUG: Failed to directly trigger new payment push notification: ${ex.message}")
                ex.printStackTrace()
            }
            
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
            
            // Trigger push notification directly to owner if payment is paid
            if (updated.status == PaymentStatus.PAID) {
                try {
                    println("DEBUG: Directly triggering send-push-notification for rent payment paid: ${updated.id}")
                    val response = supabase.httpClient.post("https://bbmzzjmiafpgymoocmni.supabase.co/functions/v1/send-push-notification") {
                        header("apikey", "sb_publishable_1EvvAXWG3kpcjYOaUk--GA_kb9eSqd6")
                        header("Authorization", "Bearer sb_publishable_1EvvAXWG3kpcjYOaUk--GA_kb9eSqd6")
                        header("Content-Type", "application/json")
                        setBody(
                            buildJsonObject {
                                put("event_type", "payment_paid")
                                put("hostel_id", updated.hostelId)
                                put("title", "Rent Payment Received")
                                put("body", "Tenant ${updated.tenantName} paid rent of ₹${updated.amount} for ${updated.month}.")
                            }.toString()
                        )
                    }
                    println("DEBUG: Direct send-push-notification for payment paid response status: ${response.status}")
                } catch (ex: Exception) {
                    println("DEBUG: Failed to directly trigger payment paid push notification: ${ex.message}")
                    ex.printStackTrace()
                }
            }
            
            Result.success(updated)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    override suspend fun sendPaymentReminder(payment: RentPayment): Result<Unit> {
        return try {
            println("DEBUG: Directly triggering send-push-notification for payment reminder: ${payment.id}")
            val response = supabase.httpClient.post("https://bbmzzjmiafpgymoocmni.supabase.co/functions/v1/send-push-notification") {
                header("apikey", "sb_publishable_1EvvAXWG3kpcjYOaUk--GA_kb9eSqd6")
                header("Authorization", "Bearer sb_publishable_1EvvAXWG3kpcjYOaUk--GA_kb9eSqd6")
                header("Content-Type", "application/json")
                setBody(
                    buildJsonObject {
                        put("event_type", "payment_created")
                        put("tenant_id", payment.tenantId)
                        put("title", "Rent Payment Reminder 🔔")
                        put("body", "Friendly reminder to pay outstanding rent of ₹${payment.amount - payment.paidAmount} for ${payment.month}.")
                    }.toString()
                )
            }
            println("DEBUG: Direct send-push-notification for payment reminder response status: ${response.status}")
            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }
}

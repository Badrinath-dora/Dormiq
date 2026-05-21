package com.dormiq.data.repository

import com.dormiq.domain.model.FoodMenu
import com.dormiq.domain.repository.FoodMenuRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SupabaseFoodMenuRepository @Inject constructor(
    private val supabase: SupabaseClient
) : FoodMenuRepository {

    private val _menu = MutableStateFlow<List<FoodMenu>>(emptyList())

    override fun getFoodMenu(hostelId: String): Flow<List<FoodMenu>> = flow {
        try {
            val response = supabase.postgrest["food_menu"]
                .select { filter { eq("hostel_id", hostelId) } }
                .decodeList<FoodMenu>()
            
            // Generate standard empty menu for any missing days of the week
            val days = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
            val completeMenu = days.map { day ->
                response.find { it.dayOfWeek.equals(day, ignoreCase = true) } ?: FoodMenu(
                    hostelId = hostelId,
                    dayOfWeek = day,
                    breakfast = "Not Scheduled",
                    lunch = "Not Scheduled",
                    snacks = "Not Scheduled",
                    dinner = "Not Scheduled"
                )
            }
            
            _menu.value = completeMenu
            emit(completeMenu)
        } catch (e: Exception) {
            e.printStackTrace()
            emit(_menu.value.filter { it.hostelId == hostelId })
        }
    }

    override suspend fun updateDailyMenu(menu: FoodMenu): Result<Unit> {
        return try {
            supabase.postgrest["food_menu"].upsert(menu) {
                select()
            }
            _menu.value = _menu.value.map { if (it.dayOfWeek.equals(menu.dayOfWeek, ignoreCase = true)) menu else it }
            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }
}

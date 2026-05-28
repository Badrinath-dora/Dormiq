package com.dormiq.domain.repository

import com.dormiq.domain.model.*
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val sessionStatus: kotlinx.coroutines.flow.Flow<io.github.jan.supabase.auth.status.SessionStatus>
    suspend fun getCurrentUser(): UserProfile?
    suspend fun isLoggedIn(): Boolean
    suspend fun sendOtp(phone: String, role: UserRole? = null): Result<Unit>
    suspend fun verifyOtp(phone: String, otp: String): Result<UserProfile>
    suspend fun loginWithPassword(userId: String, password: String): Result<UserProfile>
    suspend fun completeRegistration(hostelId: String): Result<Unit>
    suspend fun switchActiveHostel(hostelId: String): Result<Unit>
    suspend fun updateProfile(userId: String, name: String, email: String? = null, role: UserRole? = null): Result<Unit>
    suspend fun logout()
    suspend fun syncFcmToken()
}

interface HostelRepository {
    fun getHostels(): Flow<List<Hostel>>
    suspend fun getHostel(hostelId: String): Hostel?
    suspend fun getHostelByOwner(ownerId: String): Result<Hostel>
    fun getHostelsByOwner(ownerId: String): Flow<List<Hostel>>
    suspend fun createHostel(hostel: Hostel): Result<Hostel>
    suspend fun updateHostel(hostel: Hostel): Result<Unit>
    fun getStats(hostelId: String): Flow<OccupancyStats>
}

interface FloorRepository {
    fun getFloors(hostelId: String): Flow<List<Floor>>
    suspend fun createFloor(floor: Floor): Result<Floor>
    suspend fun updateFloor(floor: Floor): Result<Unit>
    suspend fun deleteFloor(floorId: String): Result<Unit>
}

interface RoomRepository {
    fun getRooms(hostelId: String): Flow<List<Room>>
    fun getRoomsByFloor(floorId: String): Flow<List<Room>>
    suspend fun getRoom(roomId: String): Room?
    suspend fun createRoom(room: Room): Result<Room>
    suspend fun updateRoom(room: Room): Result<Unit>
    suspend fun deleteRoom(roomId: String): Result<Unit>
}

interface BedRepository {
    fun getBeds(roomId: String): Flow<List<Bed>>
    fun getVacantBeds(roomId: String): Flow<List<Bed>>
    suspend fun getBed(bedId: String): Bed?
    suspend fun createBed(bed: Bed): Result<Bed>
    suspend fun updateBed(bed: Bed): Result<Unit>
    suspend fun updateBedStatus(bedId: String, status: String): Result<Unit>
}

interface TenantRepository {
    fun getTenants(hostelId: String): Flow<List<Tenant>>
    fun getTenantsByRoom(hostelId: String, roomNumber: String): Flow<List<Tenant>>
    suspend fun getTenant(tenantId: String): Tenant?
    suspend fun getTenantByPhone(phone: String): Tenant?
    fun getTenantByUserId(userId: String): Flow<Tenant?>
    suspend fun createTenant(tenant: Tenant): Result<Tenant>
    suspend fun updateTenant(tenant: Tenant): Result<Unit>
    suspend fun updateTenantUserId(tenantId: String, userId: String): Result<Unit>
    suspend fun assignBed(tenantId: String, bedId: String, roomId: String, hostelId: String, rentAmount: Double): Result<TenantAllocation>
    suspend fun getAllocations(tenantId: String): List<TenantAllocation>
    suspend fun getBedAllocations(bedId: String): List<TenantAllocation>
    suspend fun getCurrentAllocation(tenantId: String): TenantAllocation?
    suspend fun checkoutTenant(tenantId: String): Result<Unit>
    suspend fun deleteTenant(tenantId: String): Result<Unit>
}

interface IssueRepository {
    fun getIssues(hostelId: String): Flow<List<Issue>>
    fun getIssuesByTenant(tenantId: String): Flow<List<Issue>>
    suspend fun getIssue(issueId: String): Issue?
    suspend fun createIssue(issue: Issue): Result<Issue>
    suspend fun updateIssueStatus(issueId: String, status: String, notes: String = ""): Result<Unit>
    fun getComments(issueId: String): Flow<List<IssueComment>>
    suspend fun addComment(comment: IssueComment): Result<Unit>
}

interface NoticeRepository {
    fun getNotices(hostelId: String): Flow<List<Notice>>
    suspend fun createNotice(notice: Notice): Result<Notice>
    suspend fun updateNotice(notice: Notice): Result<Unit>
    suspend fun deleteNotice(noticeId: String): Result<Unit>
}

interface PaymentRepository {
    fun getPayments(hostelId: String): Flow<List<RentPayment>>
    fun getTenantPayments(tenantId: String): Flow<List<RentPayment>>
    suspend fun getPaymentSummary(hostelId: String): Flow<PaymentSummary>
    suspend fun recordPayment(payment: RentPayment): Result<RentPayment>
    suspend fun updatePayment(payment: RentPayment): Result<RentPayment>
    suspend fun sendPaymentReminder(payment: RentPayment): Result<Unit>
}

// ─── Gallery & Food Menu Repositories ──────────────────────────────────────────

interface GalleryRepository {
    fun getGalleryImages(hostelId: String): Flow<List<HostelGallery>>
    suspend fun addGalleryImage(gallery: HostelGallery): Result<HostelGallery>
    suspend fun deleteGalleryImage(imageId: String): Result<Unit>
    suspend fun uploadHostelImage(fileName: String, byteArray: ByteArray): String
}

interface FoodMenuRepository {
    fun getFoodMenu(hostelId: String): Flow<List<FoodMenu>>
    suspend fun updateDailyMenu(menu: FoodMenu): Result<Unit>
}

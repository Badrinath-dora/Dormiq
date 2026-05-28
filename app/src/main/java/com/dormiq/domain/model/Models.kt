package com.dormiq.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.Required
import java.util.UUID

// ─── Auth / User ───────────────────────────────────────────────────────────────

enum class UserRole { OWNER, TENANT, STAFF }

@Serializable
data class UserProfile(
    val id: String = "",
    val name: String = "",
    val phone: String = "",
    val email: String = "",
    val role: UserRole = UserRole.TENANT,
    @SerialName("avatar_url") val avatarUrl: String = "",
    @SerialName("hostel_id") val hostelId: String? = null,
    @SerialName("room_id") val roomId: String? = null,
    @SerialName("bed_id") val bedId: String? = null,
    @SerialName("room_number") val roomNumber: String? = null,
    @SerialName("bed_label") val bedLabel: String? = null
)

// ─── Hostel Structure ──────────────────────────────────────────────────────────

@Serializable
data class Hostel(
    val id: String = "",
    val name: String = "",
    val address: String = "",
    val type: String = "Mixed",     // Boys / Girls / Mixed
    @SerialName("owner_id") val ownerId: String = "",
    @SerialName("total_floors") val totalFloors: Int = 0,
    @SerialName("total_rooms") val totalRooms: Int = 0,
    @SerialName("total_beds") val totalBeds: Int = 0,
    val phone: String? = "",
    val email: String? = "",
    @SerialName("created_at") val createdAt: String? = ""
)

@Serializable
data class Floor(
    val id: String = "",
    @SerialName("hostel_id") val hostelId: String = "",
    val name: String = "",          // Ground Floor, 1st Floor, etc.
    val number: Int = 0,
    @SerialName("total_rooms") val totalRooms: Int = 0,
    @SerialName("total_beds") val totalBeds: Int = 0,
    @SerialName("occupied_beds") val occupiedBeds: Int = 0,
    @SerialName("created_at") val createdAt: String? = null
)

@Serializable
data class Room(
    val id: String = "",
    @SerialName("hostel_id") val hostelId: String = "",
    @SerialName("floor_id") val floorId: String = "",
    @SerialName("room_number") val roomNumber: String = "",
    val type: String = "Standard",  // Standard / Deluxe / Dormitory
    val capacity: Int = 2,
    @SerialName("bathroom_type") val bathroomType: String = "Attached", // Attached / Common
    val notes: String? = "",
    val status: String = RoomStatus.ACTIVE,
    @SerialName("total_beds") val totalBeds: Int = 0,
    @SerialName("occupied_beds") val occupiedBeds: Int = 0,
    @SerialName("floor_name") val floorName: String? = "",
    @SerialName("created_at") val createdAt: String? = ""
)

object RoomStatus {
    const val ACTIVE = "active"
    const val MAINTENANCE = "maintenance"
    const val INACTIVE = "inactive"
}

@Serializable
data class Bed(
    val id: String = "",
    @SerialName("hostel_id") val hostelId: String = "",
    @SerialName("room_id") val roomId: String = "",
    val label: String = "",         // Bed A, Upper Bunk, etc.
    val status: String? = BedStatus.VACANT,
    @SerialName("current_tenant_id") val currentTenantId: String? = null,
    @SerialName("current_tenant_name") val currentTenantName: String? = null,
    val price: Double? = 0.0,
    @SerialName("created_at") val createdAt: String? = ""
)

object BedStatus {
    const val VACANT = "vacant"
    const val OCCUPIED = "occupied"
    const val BLOCKED = "blocked"
    const val MAINTENANCE = "maintenance"
}

// ─── Tenant ────────────────────────────────────────────────────────────────────

@Serializable
data class Tenant(
    val id: String = "",
    @SerialName("hostel_id") val hostelId: String? = null,
    @SerialName("user_id") val userId: String? = null,
    val name: String = "",
    val phone: String = "",
    val email: String? = null,
    @SerialName("id_type") val idType: String? = "Aadhaar",
    @SerialName("id_number") val idNumber: String? = null,
    @SerialName("emergency_contact_name") val emergencyContactName: String? = null,
    @SerialName("emergency_contact_phone") val emergencyContactPhone: String? = null,
    @SerialName("joining_date") val joiningDate: String? = null,
    val status: String = TenantStatus.ACTIVE,
    @SerialName("avatar_url") val avatarUrl: String? = null,
    @SerialName("assigned_bed_id") val assignedBedId: String? = null,
    @SerialName("assigned_bed_label") val assignedBedLabel: String? = null,
    @SerialName("assigned_room_number") val assignedRoomNumber: String? = null,
    @SerialName("assigned_floor_name") val assignedFloorName: String? = null
)

object TenantStatus {
    const val ACTIVE = "active"
    const val CHECKED_OUT = "checked_out"
    const val PENDING = "pending"
}

@Serializable
data class TenantAllocation(
    val id: String = "",
    @SerialName("tenant_id") val tenantId: String = "",
    @SerialName("bed_id") val bedId: String = "",
    @SerialName("room_id") val roomId: String = "",
    @SerialName("hostel_id") val hostelId: String = "",
    @SerialName("check_in_date") val checkInDate: String = "",
    @SerialName("check_out_date") val checkOutDate: String = "",
    @SerialName("is_active") val isActive: Boolean = true,
    @SerialName("room_number") val roomNumber: String = "",
    @SerialName("bed_label") val bedLabel: String = "",
    @SerialName("floor_name") val floorName: String = "",
    @SerialName("created_at") val createdAt: String = ""
)

// ─── Issues / Complaints ───────────────────────────────────────────────────────

@Serializable
data class Issue(
    val id: String = "",
    @SerialName("hostel_id") val hostelId: String = "",
    @SerialName("room_id") val roomId: String? = null,
    @SerialName("tenant_id") val tenantId: String = "",
    @SerialName("reported_by_id") val reportedById: String = "",
    @SerialName("reporter_name") val reporterName: String = "",
    val category: String = IssueCategory.OTHER,
    val description: String? = "",
    val status: String = IssueStatus.OPEN,
    val priority: String = IssuePriority.MEDIUM,
    @SerialName("photo_url") val photoUrl: String? = null,
    @SerialName("technician_notes") val technicianNotes: String? = null,
    @SerialName("estimated_completion") val estimatedCompletion: String? = null,
    @SerialName("created_at") val createdAt: String = "",
    @SerialName("updated_at") val updatedAt: String = "",
    @SerialName("room_number") val roomNumber: String? = null,
    @SerialName("floor_name") val floorName: String? = null
)

object IssueCategory {
    const val ELECTRICITY = "electricity"
    const val PLUMBING = "plumbing"
    const val INTERNET = "internet"
    const val CLEANING = "cleaning"
    const val FURNITURE = "furniture"
    const val SECURITY = "security"
    const val OTHER = "other"

    val all = listOf(ELECTRICITY, PLUMBING, INTERNET, CLEANING, FURNITURE, SECURITY, OTHER)
}

object IssueStatus {
    const val OPEN = "open"
    const val IN_PROGRESS = "in_progress"
    const val RESOLVED = "resolved"
    const val CLOSED = "closed"
}

object IssuePriority {
    const val LOW = "low"
    const val MEDIUM = "medium"
    const val HIGH = "high"
    const val URGENT = "urgent"
}

@Serializable
data class IssueComment(
    val id: String = "",
    @SerialName("issue_id") val issueId: String = "",
    @SerialName("author_id") val authorId: String = "",
    @SerialName("author_name") val authorName: String = "",
    val comment: String = "",
    @SerialName("created_at") val createdAt: String = ""
)

// ─── Notices ───────────────────────────────────────────────────────────────────

@Serializable
data class Notice(
    val id: String = UUID.randomUUID().toString(),
    @SerialName("hostel_id") val hostelId: String,
    val title: String,
    val body: String,
    val category: String = NoticeCategory.GENERAL,
    @SerialName("target_audience") val targetAudience: String = NoticeAudience.ALL,
    @SerialName("target_floor_id") val targetFloorId: String? = null,
    @SerialName("target_room_id") val targetRoomId: String? = null,
    @SerialName("published_at") val publishedAt: String? = null,
    @SerialName("created_by_id") val createdById: String,
    @SerialName("created_by_name") val createdByName: String = "",
    @SerialName("is_published") val isPublished: Boolean = true
)

object NoticeCategory {
    const val GENERAL = "general"
    const val MAINTENANCE = "maintenance"
    const val REMINDER = "reminder"
    const val EMERGENCY = "emergency"
}

object NoticeAudience {
    const val ALL = "all"
    const val FLOOR = "floor"
    const val ROOM = "room"
}

// ─── Dashboard Stats ──────────────────────────────────────────────────────────

data class OccupancyStats(
    val totalBeds: Int = 0,
    val occupiedBeds: Int = 0,
    val vacantBeds: Int = 0,
    val blockedBeds: Int = 0,
    val maintenanceBeds: Int = 0,
    val openIssues: Int = 0,
    val totalTenants: Int = 0,
    val totalFloors: Int = 0,
    val totalRooms: Int = 0
) {
    val occupancyPercent: Float
        get() = if (totalBeds == 0) 0f else (occupiedBeds.toFloat() / totalBeds) * 100f
}
// ─── Payments ──────────────────────────────────────────────────────────────────

@Serializable
data class RentPayment(
    val id: String = "",
    @SerialName("hostel_id") val hostelId: String = "",
    @SerialName("tenant_id") val tenantId: String = "",
    @SerialName("tenant_name") val tenantName: String = "",
    val amount: Double = 0.0,
    @SerialName("paid_amount") val paidAmount: Double = 0.0,
    @SerialName("due_date") val dueDate: String = "",
    @SerialName("paid_date") val paidDate: String? = null,
    val month: String = "",
    val status: PaymentStatus = PaymentStatus.PENDING,
    @SerialName("transaction_id") val transactionId: String? = null,
    @SerialName("room_number") val roomNumber: String? = null,
    @SerialName("floor_name") val floorName: String? = null,
    @SerialName("receipt_generated") val receiptGenerated: Boolean = false
)

@Serializable
enum class PaymentStatus { PAID, PENDING, PARTIAL, OVERDUE }

@Serializable
data class PaymentSummary(
    val totalExpected: Double = 0.0,
    val totalCollected: Double = 0.0,
    val totalPending: Double = 0.0,
    val paidCount: Int = 0,
    val pendingCount: Int = 0
)

// ─── Gallery & Food Menu ────────────────────────────────────────────────────────

@Serializable
data class HostelGallery(
    val id: String = "",
    @SerialName("hostel_id") val hostelId: String = "",
    @SerialName("image_url") val imageUrl: String = "",
    val caption: String = "Hostel Image",
    @SerialName("created_at") val createdAt: String? = ""
)

@Serializable
data class FoodMenu(
    val id: String = "",
    @SerialName("hostel_id") val hostelId: String = "",
    @SerialName("day_of_week") val dayOfWeek: String = "",
    val breakfast: String = "Not Scheduled",
    val lunch: String = "Not Scheduled",
    val snacks: String = "Not Scheduled",
    val dinner: String = "Not Scheduled",
    @SerialName("updated_at") val updatedAt: String? = ""
)

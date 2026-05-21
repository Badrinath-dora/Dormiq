package com.dormiq.ui.navigation

// ─── Route Constants ──────────────────────────────────────────────────────────

object Routes {
    // Auth
    const val SPLASH = "splash"
    const val LOGIN = "login"
    const val OTP = "otp/{phone}?name={name}&email={email}&role={role}"
    fun otp(phone: String, name: String? = null, email: String? = null, role: String? = null): String {
        val base = "otp/$phone"
        val params = mutableListOf<String>()
        if (name != null) params.add("name=$name")
        if (email != null) params.add("email=$email")
        if (role != null) params.add("role=$role")
        return if (params.isEmpty()) base else "$base?${params.joinToString("&")}"
    }
    const val OWNER_REGISTRATION = "owner_registration"

    // Setup (Owner)
    const val CREATE_HOSTEL = "create_hostel"
    const val ADD_FLOORS = "add_floors/{hostelId}"
    fun addFloors(hostelId: String) = "add_floors/$hostelId"
    const val ADD_ROOMS = "add_rooms/{hostelId}/{floorId}"
    fun addRooms(hostelId: String, floorId: String) = "add_rooms/$hostelId/$floorId"
    const val ADD_BEDS = "add_beds/{hostelId}/{roomId}"
    fun addBeds(hostelId: String, roomId: String) = "add_beds/$hostelId/$roomId"
    const val REVIEW_STRUCTURE = "review_structure/{hostelId}"
    fun reviewStructure(hostelId: String) = "review_structure/$hostelId"

    // Owner Main
    const val OWNER_HOME = "owner_home"
    const val FLOOR_LIST = "floor_list/{hostelId}"
    fun floorList(hostelId: String) = "floor_list/$hostelId"
    const val ROOM_LIST = "room_list/{hostelId}/{floorId}"
    fun roomList(hostelId: String, floorId: String) = "room_list/$hostelId/$floorId"
    const val ROOM_DETAIL = "room_detail/{roomId}"
    fun roomDetail(roomId: String) = "room_detail/$roomId"
    const val BED_DETAIL = "bed_detail/{bedId}"
    fun bedDetail(bedId: String) = "bed_detail/$bedId"

    // Tenant Management
    const val TENANT_LIST = "tenant_list/{hostelId}"
    fun tenantList(hostelId: String) = "tenant_list/$hostelId"
    const val ADD_TENANT = "add_tenant/{hostelId}"
    fun addTenant(hostelId: String) = "add_tenant/$hostelId"
    const val ASSIGN_BED = "assign_bed/{tenantId}/{hostelId}"
    fun assignBed(tenantId: String, hostelId: String) = "assign_bed/$tenantId/$hostelId"
    const val TENANT_PROFILE = "tenant_profile/{tenantId}"
    fun tenantProfile(tenantId: String) = "tenant_profile/$tenantId"

    // Issues
    const val ISSUE_LIST = "issue_list/{hostelId}"
    fun issueList(hostelId: String) = "issue_list/$hostelId"
    const val REPORT_ISSUE = "report_issue/{hostelId}"
    fun reportIssue(hostelId: String) = "report_issue/$hostelId"
    const val ISSUE_DETAIL = "issue_detail/{issueId}"
    fun issueDetail(issueId: String) = "issue_detail/$issueId"
    const val UPDATE_ISSUE = "update_issue/{issueId}"
    fun updateIssue(issueId: String) = "update_issue/$issueId"

    // Notices
    const val NOTICE_BOARD = "notice_board/{hostelId}"
    fun noticeBoard(hostelId: String) = "notice_board/$hostelId"
    const val CREATE_NOTICE = "create_notice/{hostelId}"
    fun createNotice(hostelId: String) = "create_notice/$hostelId"

    // Tenant Home
    const val TENANT_HOME = "tenant_home"

    // Profile
    const val PROFILE = "profile"

    // Gallery & Food Menu
    const val HOSTEL_GALLERY = "hostel_gallery/{hostelId}/{isOwner}"
    fun hostelGallery(hostelId: String, isOwner: Boolean) = "hostel_gallery/$hostelId/$isOwner"

    const val FOOD_MENU = "food_menu/{hostelId}/{isOwner}"
    fun foodMenu(hostelId: String, isOwner: Boolean) = "food_menu/$hostelId/$isOwner"
}

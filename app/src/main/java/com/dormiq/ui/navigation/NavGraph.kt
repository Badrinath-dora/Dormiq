package com.dormiq.ui.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.*
import androidx.navigation.compose.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.activity.compose.BackHandler
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.dormiq.ui.screens.auth.*
import com.dormiq.ui.screens.issues.*
import com.dormiq.ui.screens.notices.*
import com.dormiq.ui.screens.owner.*
import com.dormiq.ui.screens.profile.*
import com.dormiq.ui.components.*
import com.dormiq.ui.viewmodel.AuthViewModel
import com.dormiq.ui.viewmodel.AuthState
import com.dormiq.ui.screens.setup.*
import com.dormiq.ui.screens.tenant.*
import com.dormiq.ui.screens.tenant_mgmt.*
import com.dormiq.ui.screens.payments.*
import com.dormiq.ui.theme.*
import com.dormiq.ui.viewmodel.*

// ─── Shared Enter/Exit transitions ───────────────────────────────────────────

val slideInFromRight: EnterTransition = slideInHorizontally(
    animationSpec = tween(300),
    initialOffsetX = { it }
) + fadeIn(tween(300))

val slideOutToLeft: ExitTransition = slideOutHorizontally(
    animationSpec = tween(300),
    targetOffsetX = { -it }
) + fadeOut(tween(200))

val slideInFromLeft: EnterTransition = slideInHorizontally(
    animationSpec = tween(300),
    initialOffsetX = { -it }
) + fadeIn(tween(300))

val slideOutToRight: ExitTransition = slideOutHorizontally(
    animationSpec = tween(300),
    targetOffsetX = { it }
) + fadeOut(tween(200))

// ─── Main Nav Graph ───────────────────────────────────────────────────────────

@Composable
fun DormiqNavGraph() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = hiltViewModel()
    val authState by authViewModel.authState.collectAsState()

    NavHost(
        navController = navController,
        startDestination = Routes.SPLASH,
        enterTransition = { slideInFromRight },
        exitTransition = { slideOutToLeft },
        popEnterTransition = { slideInFromLeft },
        popExitTransition = { slideOutToRight }
    ) {
        // ── Auth ──────────────────────────────────────────────────────────────
        composable(Routes.SPLASH) {
            SplashScreen(
                onNavigateToLogin = { navController.navigate(Routes.LOGIN) { popUpTo(Routes.SPLASH) { inclusive = true } } },
                onNavigateToOwnerHome = { navController.navigate(Routes.OWNER_HOME) { popUpTo(Routes.SPLASH) { inclusive = true } } },
                onNavigateToTenantHome = { navController.navigate(Routes.TENANT_HOME) { popUpTo(Routes.SPLASH) { inclusive = true } } }
            )
        }

        composable(Routes.LOGIN) {
            LaunchedEffect(authState) {
                if (authState is AuthState.Authenticated) {
                    val user = (authState as AuthState.Authenticated).user
                    val route = when {
                        user.role == com.dormiq.domain.model.UserRole.OWNER && (user.name == "User" || user.name.isBlank()) -> Routes.OWNER_REGISTRATION
                        user.role == com.dormiq.domain.model.UserRole.OWNER && user.hostelId == null -> "setup_graph"
                        user.role == com.dormiq.domain.model.UserRole.OWNER -> Routes.OWNER_HOME
                        else -> { Routes.TENANT_HOME }
                    }
                    navController.navigate(route) { popUpTo(Routes.LOGIN) { inclusive = true } }
                }
            }

            LoginScreen(
                authState = authState,
                onNavigateToOtp = { phone, roleStr -> 
                    val role = try { com.dormiq.domain.model.UserRole.valueOf(roleStr) } catch(e: Exception) { null }
                    authViewModel.sendOtp(phone, role)
                    navController.navigate(Routes.otp(phone, role = roleStr)) 
                },
                onTenantLogin = { id, pass ->
                    authViewModel.loginWithPassword(id, pass)
                },
                onNavigateToOwnerRegistration = { navController.navigate(Routes.OWNER_REGISTRATION) }
            )
        }

        composable(Routes.OWNER_REGISTRATION) {
            OwnerRegistrationScreen(
                onNavigateToOtp = { phone, name, email -> 
                    authViewModel.sendOtp(phone)
                    navController.navigate(Routes.otp(phone, name, email, role = "OWNER")) 
                },
                onRegistrationComplete = {
                    navController.navigate("setup_graph") {
                        popUpTo(Routes.OWNER_REGISTRATION) { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.OTP, arguments = listOf(
            navArgument("phone") { type = NavType.StringType },
            navArgument("name") { type = NavType.StringType; nullable = true; defaultValue = null },
            navArgument("email") { type = NavType.StringType; nullable = true; defaultValue = null },
            navArgument("role") { type = NavType.StringType; nullable = true; defaultValue = null }
        )) { entry ->
            val phone = entry.arguments?.getString("phone") ?: ""
            val name = entry.arguments?.getString("name")
            val email = entry.arguments?.getString("email")
            val roleStr = entry.arguments?.getString("role")
            val role = if (roleStr != null) com.dormiq.domain.model.UserRole.valueOf(roleStr) else null
            
            LaunchedEffect(authState) {
                if (authState is AuthState.Authenticated) {
                    val user = (authState as AuthState.Authenticated).user
                    val route = when {
                        user.role == com.dormiq.domain.model.UserRole.OWNER && (user.name == "User" || user.name.isBlank()) -> Routes.OWNER_REGISTRATION
                        user.role == com.dormiq.domain.model.UserRole.OWNER && user.hostelId == null -> "setup_graph"
                        user.role == com.dormiq.domain.model.UserRole.OWNER -> Routes.OWNER_HOME
                        else -> Routes.TENANT_HOME
                    }
                    navController.navigate(route) { 
                        popUpTo(Routes.LOGIN) { inclusive = true } 
                    }
                }
            }

            OtpScreen(
                phone = phone,
                authState = authState,
                onVerify = { otp -> authViewModel.verifyOtp(phone, otp, name, email, role) },
                onBack = { 
                    authViewModel.resetState()
                    navController.popBackStack() 
                }
            )
        }

        // ── Owner Setup (Shared ViewModel Graph) ─────────────────────────────
        navigation(startDestination = Routes.CREATE_HOSTEL, route = "setup_graph") {
            composable(Routes.CREATE_HOSTEL) { entry ->
                val setupGraphEntry = remember(entry) {
                    navController.getBackStackEntry("setup_graph")
                }
                val setupViewModel: SetupViewModel = hiltViewModel(setupGraphEntry)
                CreateHostelScreen(
                    viewModel = setupViewModel,
                    onNext = { hostelId -> navController.navigate(Routes.addFloors(hostelId)) },
                    onBack = { 
                        authViewModel.logout()
                        navController.navigate(Routes.LOGIN) { 
                            popUpTo(0) { inclusive = true } 
                        }
                    }
                )
            }
 
            composable(Routes.ADD_FLOORS, arguments = listOf(navArgument("hostelId") { type = NavType.StringType })) { entry ->
                val hostelId = entry.arguments?.getString("hostelId") ?: ""
                val setupGraphEntry = remember(entry) {
                    navController.getBackStackEntry("setup_graph")
                }
                val setupViewModel: SetupViewModel = hiltViewModel(setupGraphEntry)
                AddFloorsScreen(
                    hostelId = hostelId,
                    viewModel = setupViewModel,
                    onNext = { hId, floorId -> navController.navigate(Routes.addRooms(hId, floorId)) },
                    onBack = { navController.popBackStack() }
                )
            }
 
            composable(Routes.ADD_ROOMS, arguments = listOf(
                navArgument("hostelId") { type = NavType.StringType },
                navArgument("floorId") { type = NavType.StringType }
            )) { entry ->
                val hostelId = entry.arguments?.getString("hostelId") ?: ""
                val floorId = entry.arguments?.getString("floorId") ?: ""
                val setupGraphEntry = remember(entry) {
                    navController.getBackStackEntry("setup_graph")
                }
                val setupViewModel: SetupViewModel = hiltViewModel(setupGraphEntry)
                AddRoomsScreen(
                    hostelId = hostelId,
                    floorId = floorId,
                    viewModel = setupViewModel,
                    onNext = { hId, roomId -> navController.navigate(Routes.addBeds(hId, roomId)) },
                    onBack = { navController.popBackStack() }
                )
            }
 
            composable(Routes.ADD_BEDS, arguments = listOf(
                navArgument("hostelId") { type = NavType.StringType },
                navArgument("roomId") { type = NavType.StringType }
            )) { entry ->
                val hostelId = entry.arguments?.getString("hostelId") ?: ""
                val roomId = entry.arguments?.getString("roomId") ?: ""
                val setupGraphEntry = remember(entry) {
                    navController.getBackStackEntry("setup_graph")
                }
                val setupViewModel: SetupViewModel = hiltViewModel(setupGraphEntry)
                AddBedsScreen(
                    hostelId = hostelId,
                    roomId = roomId,
                    viewModel = setupViewModel,
                    onNext = { hId -> navController.navigate(Routes.reviewStructure(hId)) },
                    onBack = { navController.popBackStack() }
                )
            }
 
            composable(Routes.REVIEW_STRUCTURE, arguments = listOf(navArgument("hostelId") { type = NavType.StringType })) { entry ->
                val hostelId = entry.arguments?.getString("hostelId") ?: ""
                val setupGraphEntry = remember(entry) {
                    try { navController.getBackStackEntry("setup_graph") } catch (e: Exception) { null }
                }
                
                if (setupGraphEntry != null) {
                    val setupViewModel: SetupViewModel = hiltViewModel(setupGraphEntry)
                    ReviewStructureScreen(
                        hostelId = hostelId,
                        viewModel = setupViewModel,
                        onConfirm = {
                            authViewModel.completeRegistration(hostelId)
                            navController.navigate(Routes.OWNER_HOME) {
                                popUpTo("setup_graph") { inclusive = true }
                            }
                        },
                        onBack = { navController.popBackStack() },
                        onEditStep = { step ->
                            val route = when(step) {
                                1 -> Routes.CREATE_HOSTEL
                                2 -> Routes.addFloors(hostelId)
                                3 -> Routes.addRooms(hostelId, "temp")
                                4 -> Routes.addBeds(hostelId, "temp")
                                else -> Routes.CREATE_HOSTEL
                            }
                            navController.navigate(route) {
                                popUpTo(Routes.REVIEW_STRUCTURE) { inclusive = false }
                            }
                        }
                    )
                }
            }
        }

        // ── Owner Main ────────────────────────────────────────────────────────
        composable(Routes.OWNER_HOME) {
            OwnerBottomNavHost(
                onNavigateToSetup = { navController.navigate(Routes.CREATE_HOSTEL) },
                onLogout = {
                    authViewModel.logout()
                    navController.navigate(Routes.LOGIN) { 
                        popUpTo(0) { inclusive = true } 
                        launchSingleTop = true
                    }
                },
                onNavigateToGallery = { hostelId ->
                    navController.navigate(Routes.hostelGallery(hostelId, true))
                },
                onNavigateToFoodMenu = { hostelId ->
                    navController.navigate(Routes.foodMenu(hostelId, true))
                }
            )
        }

        // ── Tenant Main ────────────────────────────────────────────────────────
        composable(Routes.TENANT_HOME) {
            TenantBottomNavHost(
                onLogout = {
                    authViewModel.logout()
                    navController.navigate(Routes.LOGIN) { 
                        popUpTo(0) { inclusive = true } 
                        launchSingleTop = true
                    }
                },
                onNavigateToGallery = { hostelId ->
                    navController.navigate(Routes.hostelGallery(hostelId, false))
                },
                onNavigateToFoodMenu = { hostelId ->
                    navController.navigate(Routes.foodMenu(hostelId, false))
                }
            )
        }

        // ── Gallery & Food Menu ──────────────────────────────────────────────────
        composable(
            route = Routes.HOSTEL_GALLERY,
            arguments = listOf(
                navArgument("hostelId") { type = NavType.StringType },
                navArgument("isOwner") { type = NavType.BoolType }
            )
        ) { entry ->
            val hostelId = entry.arguments?.getString("hostelId") ?: ""
            val isOwner = entry.arguments?.getBoolean("isOwner") ?: false
            val galleryFoodViewModel: GalleryFoodViewModel = hiltViewModel()
            HostelGalleryScreen(
                hostelId = hostelId,
                isOwner = isOwner,
                onBack = { navController.popBackStack() },
                viewModel = galleryFoodViewModel
            )
        }

        composable(
            route = Routes.FOOD_MENU,
            arguments = listOf(
                navArgument("hostelId") { type = NavType.StringType },
                navArgument("isOwner") { type = NavType.BoolType }
            )
        ) { entry ->
            val hostelId = entry.arguments?.getString("hostelId") ?: ""
            val isOwner = entry.arguments?.getBoolean("isOwner") ?: false
            val galleryFoodViewModel: GalleryFoodViewModel = hiltViewModel()
            FoodMenuScreen(
                hostelId = hostelId,
                isOwner = isOwner,
                onBack = { navController.popBackStack() },
                viewModel = galleryFoodViewModel
            )
        }
    }
}

// ─── Owner Bottom Nav Host ────────────────────────────────────────────────────

data class OwnerNavItem(val label: String, val selectedIcon: ImageVector, val unselectedIcon: ImageVector, val route: String)

@Composable
fun OwnerBottomNavHost(
    onNavigateToSetup: () -> Unit,
    onLogout: () -> Unit,
    onNavigateToGallery: (String) -> Unit,
    onNavigateToFoodMenu: (String) -> Unit
) {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = hiltViewModel()
    val authState by authViewModel.authState.collectAsState()
    val items = listOf(
        OwnerNavItem("Dashboard", Icons.Filled.Dashboard, Icons.Outlined.Dashboard, "o_dashboard"),
        OwnerNavItem("Property", Icons.Filled.Apartment, Icons.Outlined.Apartment, "o_property"),
        OwnerNavItem("Tenants", Icons.Filled.People, Icons.Outlined.People, "o_tenants"),
        OwnerNavItem("Payments", Icons.Filled.Payments, Icons.Outlined.Payments, "o_payments"),
        OwnerNavItem("Profile", Icons.Filled.Person, Icons.Outlined.Person, "o_profile"),
    )

    val currentBackStack by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStack?.destination?.route

    // Handle system back button to always go to dashboard first
    BackHandler(enabled = currentRoute != "o_dashboard") {
        navController.navigate("o_dashboard") {
            popUpTo("o_dashboard") { inclusive = true }
        }
    }

    Scaffold(
        containerColor = DormiqNavy,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            PremiumBottomNav(
                items = items.map { PremiumNavItem(it.label, it.selectedIcon, it.unselectedIcon, it.route) },
                currentRoute = currentRoute ?: "",
                onNavigate = { route ->
                    if (route != currentRoute) {
                        navController.navigate(route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                accentColor = DormiqBlue
            )
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = "o_dashboard",
            modifier = Modifier.padding(bottom = padding.calculateBottomPadding() - 32.dp)
        ) {
            val user = (authState as? AuthState.Authenticated)?.user
            val currentHostelId = user?.hostelId ?: ""

            composable("o_dashboard") {
                OwnerDashboardScreen(
                    hostelId = currentHostelId,
                    onNavigateToFloorList = { navController.navigate("o_property") },
                    onNavigateToRoomList = { navController.navigate("o_property") },
                    onNavigateToTenantList = { navController.navigate("o_tenants") },
                    onNavigateToIssueList = { navController.navigate("o_issues") },
                    onNavigateToAddTenant = { navController.navigate("o_add_tenant") },
                    onNavigateToNoticeBoard = { navController.navigate("o_notices") },
                    onNavigateToRoomDetail = { roomId -> navController.navigate("o_room_detail/$roomId") },
                    onNavigateToBedDetail = { bedId -> navController.navigate("o_bed_detail/$bedId") },
                    onNavigateToTenantProfile = { tenantId -> navController.navigate("o_tenant_profile/$tenantId") },
                    onLogout = onLogout,
                    onNavigateToGallery = { onNavigateToGallery(currentHostelId) },
                    onNavigateToFoodMenu = { onNavigateToFoodMenu(currentHostelId) }
                )
            }

            composable("o_property") {
                FloorListScreen(
                    hostelId = currentHostelId,
                    onFloorClick = { floorId -> navController.navigate("o_room_list/$floorId") },
                    onBack = { navController.navigate("o_dashboard") }
                )
            }

            composable("o_room_list/{floorId}", arguments = listOf(navArgument("floorId") { type = NavType.StringType })) { entry ->
                val floorId = entry.arguments?.getString("floorId") ?: ""
                RoomListScreen(
                    hostelId = currentHostelId,
                    floorId = floorId,
                    onRoomClick = { roomId -> navController.navigate("o_room_detail/$roomId") },
                    onBack = { navController.popBackStack() }
                )
            }

            composable("o_room_detail/{roomId}", arguments = listOf(navArgument("roomId") { type = NavType.StringType })) { entry ->
                val roomId = entry.arguments?.getString("roomId") ?: ""
                RoomDetailScreen(
                    roomId = roomId,
                    isOwner = true,
                    onBedClick = { bedId -> navController.navigate("o_bed_detail/$bedId") },
                    onReportIssue = { navController.navigate("o_issues") },
                    onBack = { navController.popBackStack() }
                )
            }

            composable("o_bed_detail/{bedId}", arguments = listOf(navArgument("bedId") { type = NavType.StringType })) { entry ->
                val bedId = entry.arguments?.getString("bedId") ?: ""
                BedDetailScreen(
                    bedId = bedId,
                    onBack = { navController.popBackStack() },
                    onAssignTenant = { navController.navigate("o_tenants") }
                )
            }

            composable("o_tenants") {
                TenantListScreen(
                    hostelId = currentHostelId,
                    onTenantClick = { tenantId -> navController.navigate("o_tenant_profile/$tenantId") },
                    onAddTenant = { navController.navigate("o_add_tenant") },
                    onBack = { navController.navigate("o_dashboard") }
                )
            }

            composable("o_add_tenant") {
                AddTenantScreen(
                    hostelId = currentHostelId,
                    onSaved = { tenantId -> navController.navigate("o_assign_bed/$tenantId") },
                    onBack = { navController.popBackStack() }
                )
            }

            composable("o_assign_bed/{tenantId}", arguments = listOf(navArgument("tenantId") { type = NavType.StringType })) { entry ->
                val tenantId = entry.arguments?.getString("tenantId") ?: ""
                AssignBedScreen(
                    tenantId = tenantId,
                    hostelId = currentHostelId,
                    onAssigned = { navController.navigate("o_tenants") { popUpTo("o_tenants") { inclusive = true } } },
                    onBack = { navController.popBackStack() }
                )
            }

            composable("o_tenant_profile/{tenantId}", arguments = listOf(navArgument("tenantId") { type = NavType.StringType })) { entry ->
                val tenantId = entry.arguments?.getString("tenantId") ?: ""
                TenantProfileScreen(
                    tenantId = tenantId,
                    isOwner = true,
                    onEdit = { navController.navigate("o_edit_tenant/$tenantId") },
                    onAssignBed = { navController.navigate("o_assign_bed/$tenantId") },
                    onBack = { navController.popBackStack() }
                )
            }

            composable("o_edit_tenant/{tenantId}", arguments = listOf(navArgument("tenantId") { type = NavType.StringType })) { entry ->
                val tenantId = entry.arguments?.getString("tenantId") ?: ""
                EditTenantScreen(
                    tenantId = tenantId,
                    onSaved = { navController.popBackStack() },
                    onBack = { navController.popBackStack() }
                )
            }

            composable("o_issues") {
                IssueListScreen(
                    hostelId = currentHostelId,
                    isOwner = true,
                    onIssueClick = { issueId -> navController.navigate("o_issue_detail/$issueId") },
                    onReportIssue = { navController.navigate("o_issues") },
                    onBack = { navController.navigate("o_dashboard") }
                )
            }

            composable("o_payments") {
                OwnerPaymentManagementScreen(hostelId = currentHostelId)
            }

            composable("o_issue_detail/{issueId}", arguments = listOf(navArgument("issueId") { type = NavType.StringType })) { entry ->
                val issueId = entry.arguments?.getString("issueId") ?: ""
                IssueDetailScreen(
                    issueId = issueId,
                    isOwner = true,
                    onUpdateStatus = { navController.navigate("o_update_issue/$issueId") },
                    onBack = { navController.popBackStack() }
                )
            }

            composable("o_update_issue/{issueId}", arguments = listOf(navArgument("issueId") { type = NavType.StringType })) { entry ->
                val issueId = entry.arguments?.getString("issueId") ?: ""
                UpdateIssueStatusScreen(
                    issueId = issueId,
                    onUpdated = { navController.popBackStack() },
                    onBack = { navController.popBackStack() }
                )
            }

            composable("o_notices") {
                NoticeBoardScreen(
                    hostelId = currentHostelId,
                    isOwner = true,
                    onCreateNotice = { navController.navigate("o_create_notice") },
                    onBack = { navController.navigate("o_dashboard") }
                )
            }

            composable("o_create_notice") {
                CreateNoticeScreen(
                    hostelId = currentHostelId,
                    onPublished = { navController.popBackStack() },
                    onBack = { navController.popBackStack() }
                )
            }

            composable("o_profile") {
                ProfileSettingsScreen(
                    onLogout = onLogout,
                    onBack = { navController.popBackStack() },
                    onEditProfile = { navController.navigate("o_edit_profile") },
                    onNavigateToManageProperties = { navController.navigate("o_manage_properties") }
                )
            }

            composable("o_edit_profile") {
                EditProfileScreen(
                    onBack = { navController.popBackStack() },
                    onSuccess = { navController.popBackStack() }
                )
            }

            composable("o_manage_properties") {
                ManagePropertiesScreen(
                    onBack = { navController.popBackStack() },
                    onRegisterNewHostel = {
                        onNavigateToSetup()
                    },
                    onHostelSwitched = {
                        navController.navigate("o_dashboard") {
                            popUpTo("o_dashboard") { inclusive = true }
                        }
                    }
                )
            }
        }
    }
}

// ─── Tenant Bottom Nav Host ───────────────────────────────────────────────────

data class TenantNavItem(val label: String, val selectedIcon: ImageVector, val unselectedIcon: ImageVector, val route: String)

@Composable
fun TenantBottomNavHost(
    onLogout: () -> Unit,
    onNavigateToGallery: (String) -> Unit,
    onNavigateToFoodMenu: (String) -> Unit
) {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = hiltViewModel()
    val authState by authViewModel.authState.collectAsState()
    val user = (authState as? AuthState.Authenticated)?.user
    val currentHostelId = user?.hostelId ?: ""

    val items = listOf(
        TenantNavItem("Home", Icons.Filled.Home, Icons.Outlined.Home, "t_home"),
        TenantNavItem("My Room", Icons.Filled.Bed, Icons.Outlined.Bed, "t_my_room"),
        TenantNavItem("Payments", Icons.Filled.Payments, Icons.Outlined.Payments, "t_payments"),
        TenantNavItem("Issues", Icons.Filled.ReportProblem, Icons.Outlined.ReportProblem, "t_issues"),
        TenantNavItem("Profile", Icons.Filled.Person, Icons.Outlined.Person, "t_profile"),
    )

    val currentBackStack by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStack?.destination?.route

    // Handle system back button to always go to home first
    BackHandler(enabled = currentRoute != "t_home") {
        navController.navigate("t_home") {
            popUpTo("t_home") { inclusive = true }
        }
    }

    Scaffold(
        containerColor = DormiqNavy,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            PremiumBottomNav(
                items = items.map { PremiumNavItem(it.label, it.selectedIcon, it.unselectedIcon, it.route) },
                currentRoute = currentRoute ?: "",
                onNavigate = { route ->
                    if (route != currentRoute) {
                        navController.navigate(route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                accentColor = DormiqTeal
            )
        }
    ) { padding ->
        NavHost(navController = navController, startDestination = "t_home", modifier = Modifier.padding(bottom = padding.calculateBottomPadding())) {
            composable("t_home") {
                val tenantViewModel: TenantViewModel = hiltViewModel()
                val tenant by tenantViewModel.tenant.collectAsState()
                LaunchedEffect(user?.id) {
                    if (user != null) {
                        tenantViewModel.loadTenantData(user.id, user.phone)
                    }
                }
                TenantHomeScreen(
                    onNavigateToMyRoom = { navController.navigate("t_my_room") },
                    onNavigateToIssues = { navController.navigate("t_issues") },
                    onNavigateToNotices = { navController.navigate("t_notices") },
                    onNavigateToReportIssue = { navController.navigate("t_report_issue") },
                    onLogout = onLogout,
                    onNavigateToGallery = { onNavigateToGallery(currentHostelId.ifBlank { tenant?.hostelId ?: "" }) },
                    onNavigateToFoodMenu = { onNavigateToFoodMenu(currentHostelId.ifBlank { tenant?.hostelId ?: "" }) }
                )
            }

            composable("t_my_room") {
                TenantMyRoomScreen(
                    onReportIssue = { navController.navigate("t_report_issue") }
                )
            }

            composable("t_issues") {
                IssueListScreen(
                    hostelId = currentHostelId,
                    isOwner = false,
                    onIssueClick = { issueId -> navController.navigate("t_issue_detail/$issueId") },
                    onReportIssue = { navController.navigate("t_report_issue") },
                    onBack = { navController.popBackStack() }
                )
            }

            composable("t_report_issue") {
                ReportIssueScreen(
                    hostelId = currentHostelId,
                    onSubmitted = { navController.popBackStack() },
                    onBack = { navController.popBackStack() }
                )
            }

            composable("t_issue_detail/{issueId}", arguments = listOf(navArgument("issueId") { type = NavType.StringType })) { entry ->
                val issueId = entry.arguments?.getString("issueId") ?: ""
                IssueDetailScreen(
                    issueId = issueId,
                    isOwner = false,
                    onUpdateStatus = {},
                    onBack = { navController.popBackStack() }
                )
            }

            composable("t_payments") {
                TenantPaymentScreen(
                    onPayNow = { amount -> /* Razorpay integration here */ }
                )
            }

            composable("t_notices") {
                NoticeBoardScreen(
                    hostelId = currentHostelId,
                    isOwner = false,
                    onCreateNotice = {},
                    onBack = { navController.popBackStack() }
                )
            }

            composable("t_profile") {
                ProfileSettingsScreen(
                    onLogout = onLogout,
                    onBack = { navController.popBackStack() },
                    onEditProfile = { navController.navigate("t_edit_profile") }
                )
            }

            composable("t_edit_profile") {
                EditProfileScreen(
                    onBack = { navController.popBackStack() },
                    onSuccess = { navController.popBackStack() }
                )
            }
        }
    }
}

// ─── Premium Components ───────────────────────────────────────────────────────

data class PremiumNavItem(val label: String, val selectedIcon: ImageVector, val unselectedIcon: ImageVector, val route: String)

@Composable
fun PremiumBottomNav(
    items: List<PremiumNavItem>,
    currentRoute: String,
    onNavigate: (String) -> Unit,
    accentColor: Color
) {
    val middleIndex = items.size / 2
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(start = 8.dp, end = 8.dp, bottom = 8.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        // Custom Curved Background (Wave Style)
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(65.dp)
                .shadow(16.dp, shape = RoundedCornerShape(22.dp))
        ) {
            val width = size.width
            val height = size.height
            val curveWidth = 90.dp.toPx()
            val curveDepth = 40.dp.toPx()
            
            val path = Path().apply {
                moveTo(0f, 0f)
                
                // Left Wing
                lineTo(width / 2 - curveWidth / 2, 0f)
                
                // Deep Curve (Wave)
                cubicTo(
                    width / 2 - curveWidth / 4, 0f,
                    width / 2 - curveWidth / 4, curveDepth,
                    width / 2, curveDepth
                )
                cubicTo(
                    width / 2 + curveWidth / 4, curveDepth,
                    width / 2 + curveWidth / 4, 0f,
                    width / 2 + curveWidth / 2, 0f
                )
                
                // Right Wing
                lineTo(width, 0f)
                
                // Bottom and Sides
                lineTo(width, height)
                lineTo(0f, height)
                close()
            }
            
            drawPath(
                path = path,
                color = Color.White
            )
        }
        
        // Navigation Items
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.Bottom
        ) {
            items.forEachIndexed { index, item ->
                val isSelected = currentRoute == item.route
                
                if (index == middleIndex) {
                    // Elevated Center Button (FAB)
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .offset(y = (-15).dp)
                            .shadow(12.dp, CircleShape, ambientColor = accentColor, spotColor = accentColor)
                            .background(accentColor, CircleShape)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = { onNavigate(item.route) }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            item.selectedIcon,
                            contentDescription = item.label,
                            tint = Color.White,
                            modifier = Modifier.size(30.dp)
                        )
                    }
                } else {
                    // Standard Bottom Items
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(bottom = 10.dp)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = { onNavigate(item.route) }
                            ),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        val animatedScale by animateFloatAsState(if (isSelected) 1.25f else 1f)
                        
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .scale(animatedScale),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                if (isSelected) item.selectedIcon else item.unselectedIcon,
                                contentDescription = item.label,
                                tint = if (isSelected) accentColor else Color.Gray.copy(0.4f),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        
                        Text(
                            text = item.label,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isSelected) accentColor else Color.Gray.copy(0.6f),
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }
    }
}

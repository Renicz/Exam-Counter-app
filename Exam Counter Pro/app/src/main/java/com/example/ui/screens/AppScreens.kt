package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.*
import com.example.ui.viewmodel.ExamViewModel
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontFamily
import kotlinx.coroutines.delay
import androidx.compose.ui.graphics.graphicsLayer

// Helper color palette matching requirements (soft blues, cyans, pink accents, soft backgrounds)
val SoftCyan = Color(0xFF00F2FE)
val SoftBlue = Color(0xFF4FACFE)
val AccentPurple = Color(0xFF8B5CF6)
val AccentPink = Color(0xFFEC4899)
val CardBorderColor = Color(0xFFE2E8F0)
val SoftBackground = Color(0xFFF8FAFC)
val DarkBackground = Color(0xFF0F172A)
val CharcoalDark = Color(0xFF1E293B)

// Safe String-to-Color converter
fun parseColor(hex: String, default: Color = Color.Gray): Color {
    return try {
        Color(android.graphics.Color.parseColor(hex))
    } catch (e: Exception) {
        default
    }
}

// Global animated live timer countdown formatter
fun formatLiveCountdown(targetMs: Long, currentMs: Long): String {
    val diff = targetMs - currentMs
    if (diff <= 0) return "Exam In Progress / Completed 🚀"
    
    val seconds = (diff / 1000) % 60
    val minutes = (diff / (1000 * 60)) % 60
    val hours = (diff / (1000 * 60 * 60)) % 24
    val daysTotal = diff / (1000 * 60 * 60 * 24)
    
    val years = daysTotal / 365
    val remainingDaysAfterYears = daysTotal % 365
    val months = remainingDaysAfterYears / 30
    val days = remainingDaysAfterYears % 30
    
    val sb = StringBuilder()
    if (years > 0) sb.append("${years}y ")
    if (months > 0) sb.append("${months}m ")
    if (days > 0 || sb.isNotEmpty()) sb.append("${days}d ")
    sb.append(String.format("%02dh %02dm %02ds", hours, minutes, seconds))
    return sb.toString()
}

// Date helper format
fun formatDateTime(ms: Long): String {
    val sdf = SimpleDateFormat("MMM dd, yyyy - hh:mm a", Locale.getDefault())
    return sdf.format(Date(ms))
}

@Composable
fun CustomBottomNavigation(
    currentTab: String,
    onTabSelected: (String) -> Unit,
    isDarkTheme: Boolean,
    isAdmin: Boolean
) {
    val items = mutableListOf(
        Triple("dashboard", "Dashboard", Icons.Outlined.Dashboard),
        Triple("exams", "Exams", Icons.Outlined.FormatListNumbered),
        Triple("planner", "Planner", Icons.Outlined.Timer),
        Triple("calendar", "Calendar", Icons.Outlined.CalendarToday),
        Triple("analytics", "Analytics", Icons.Outlined.BarChart)
    )
    if (isAdmin) {
        items.add(Triple("admin", "Admin", Icons.Outlined.AdminPanelSettings))
    }
    items.add(Triple("profile", "Profile", Icons.Outlined.Person))

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .shadow(
                elevation = 16.dp,
                shape = RoundedCornerShape(24.dp),
                clip = false,
                ambientColor = Color(0xFF5B4BFF).copy(alpha = 0.1f),
                spotColor = Color(0xFF5B4BFF).copy(alpha = 0.2f)
            ),
        color = if (isDarkTheme) Color(0xFF111827).copy(alpha = 0.85f) else Color.White.copy(alpha = 0.9f),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, if (isDarkTheme) Color(0xFF1F2937).copy(alpha = 0.5f) else Color(0xFFE5E7EB).copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { (tabId, label, icon) ->
                val isSelected = currentTab == tabId
                
                // Animated scales
                val scale by animateFloatAsState(
                    targetValue = if (isSelected) 1.1f else 1.0f,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            onTabSelected(tabId)
                        }
                        .padding(vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.scale(scale)
                    ) {
                        if (isSelected) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(
                                        Brush.linearGradient(listOf(Color(0xFF5B4BFF), Color(0xFF7C4DFF))),
                                        RoundedCornerShape(12.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = label,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        } else {
                            Icon(
                                imageVector = icon,
                                contentDescription = label,
                                tint = if (isDarkTheme) Color(0xFF9CA3AF) else Color(0xFF6B7280),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        Text(
                            text = label,
                            fontSize = 10.sp,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                            color = if (isSelected) {
                                if (isDarkTheme) Color(0xFFC084FC) else Color(0xFF7C4DFF)
                            } else {
                                if (isDarkTheme) Color(0xFF9CA3AF) else Color(0xFF6B7280)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AppNavigationShell(
    viewModel: ExamViewModel,
    isDarkTheme: Boolean,
    onToggleDarkTheme: () -> Unit
) {
    var currentTab by remember { mutableStateOf("dashboard") }

    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val isBanned = currentUser?.isBanned ?: false

    // Check if user is locked / banned
    if (isBanned) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(if (isDarkTheme) DarkBackground else SoftBackground)
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Filled.Lock,
                        contentDescription = "Account Locked",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Access Denied",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Hello ${currentUser?.displayName}, your account has been deactivated (banned) by the administrator for violating study policy terms.",
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f)
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = { viewModel.logout() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("Log Out")
                    }
                }
            }
        }
    } else if (currentUser == null) {
        AuthScreen(viewModel = viewModel, isDarkTheme = isDarkTheme)
    } else {
        Scaffold(
            bottomBar = {
                CustomBottomNavigation(
                    currentTab = currentTab,
                    onTabSelected = { currentTab = it },
                    isDarkTheme = isDarkTheme,
                    isAdmin = currentUser?.role == "ADMIN"
                )
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(if (isDarkTheme) DarkBackground else SoftBackground)
            ) {
                when (currentTab) {
                    "dashboard" -> DashboardScreen(
                        viewModel = viewModel,
                        isDarkTheme = isDarkTheme,
                        onTabChange = { currentTab = it }
                    )
                    "exams" -> ExamsScreen(viewModel = viewModel, isDarkTheme = isDarkTheme)
                    "planner" -> PlannerScreen(viewModel = viewModel, isDarkTheme = isDarkTheme)
                    "calendar" -> CalendarScreen(viewModel = viewModel, isDarkTheme = isDarkTheme)
                    "analytics" -> AnalyticsScreen(viewModel = viewModel, isDarkTheme = isDarkTheme)
                    "admin" -> AdminScreen(viewModel = viewModel, isDarkTheme = isDarkTheme)
                    "profile" -> ProfileScreen(viewModel = viewModel, isDarkTheme = isDarkTheme, onToggleTheme = onToggleDarkTheme)
                }
            }
        }
    }
}

// --- SUB-SCREEN 1: AUTHENTICATION (Login / Registration) ---
@Composable
fun AuthScreen(viewModel: ExamViewModel, isDarkTheme: Boolean) {
    var isSignUp by remember { mutableStateOf(false) }
    var isForgot by remember { mutableStateOf(false) }

    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val authError by viewModel.authError.collectAsStateWithLifecycle()
    val isAuthLoading by viewModel.isAuthLoading.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        if (isDarkTheme) Color(0xFF1E1E38) else Color(0xFFF1F5F9),
                        if (isDarkTheme) DarkBackground else SoftBackground
                    )
                )
            )
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Glassmorphism aesthetic icon header
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(listOf(SoftCyan, SoftBlue))
                    )
                    .shadow(12.dp, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Timer,
                    contentDescription = "Logo icon",
                    tint = Color.White,
                    modifier = Modifier.size(44.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Exam Countdown Pro",
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                color = if (isDarkTheme) Color.White else CharcoalDark,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Track, study, and ace your exams 🚀",
                fontSize = 14.sp,
                color = GrayColor(isDarkTheme),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            // White elegant card container
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(8.dp, RoundedCornerShape(20.dp)),
                colors = CardDefaults.cardColors(
                    containerColor = if (isDarkTheme) CharcoalDark else Color.White
                ),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (isForgot) "Forgot Password" else if (isSignUp) "Create Account" else "Welcome Back",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDarkTheme) Color.White else CharcoalDark
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (isForgot) "Enter your registered email below" else "Simulated secure sign-in process",
                        fontSize = 12.sp,
                        color = GrayColor(isDarkTheme)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    if (authError != null) {
                        Text(
                            text = authError ?: "",
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }

                    if (isSignUp) {
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Display Name") },
                            leadingIcon = { Icon(Icons.Default.Person, null) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("register_name"),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email Address") },
                        leadingIcon = { Icon(Icons.Default.Email, null) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("auth_email"),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    if (!isForgot) {
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("Password") },
                            leadingIcon = { Icon(Icons.Default.Lock, null) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("auth_password"),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (isAuthLoading) {
                        CircularProgressIndicator(color = SoftBlue)
                    } else {
                        Button(
                            onClick = {
                                if (isForgot) {
                                    viewModel.requestPasswordReset(email)
                                    isForgot = false
                                } else if (isSignUp) {
                                    viewModel.signupWithEmail(name, email, password)
                                } else {
                                    viewModel.loginWithEmail(email, password)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SoftBlue
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("auth_submit_btn")
                        ) {
                            Text(
                                text = if (isForgot) "Send Reset Instructions" else if (isSignUp) "Register" else "Submit Log In",
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (!isForgot && !isSignUp) {
                        // Google simulated Sign In Button
                        OutlinedButton(
                            onClick = { viewModel.loginWithGoogle() },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("google_signin_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Login,
                                contentDescription = "Google",
                                tint = SoftBlue,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                            Text("Fast Sign In with Google")
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Mode toggles
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        if (!isForgot) {
                            Text(
                                text = if (isSignUp) "Already registered?" else "No account yet?",
                                fontSize = 12.sp,
                                color = GrayColor(isDarkTheme)
                            )
                            Text(
                                text = if (isSignUp) "Sign In" else "Sign Up",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = SoftBlue,
                                modifier = Modifier
                                    .clickable { isSignUp = !isSignUp }
                                    .testTag("auth_mode_toggle")
                            )
                        } else {
                            Text(
                                text = "Back to Sign In",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = SoftBlue,
                                modifier = Modifier
                                    .clickable { isForgot = false }
                                    .fillMaxWidth(),
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    if (!isSignUp && !isForgot) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Forgot your password?",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = AccentPurple,
                            modifier = Modifier
                                .clickable { isForgot = true }
                                .padding(vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Tip: Enter 'admin@countdown.com' & any password to unlock Admin metrics directly!",
                fontSize = 11.sp,
                color = GrayColor(isDarkTheme).copy(alpha = 0.8f),
                textAlign = TextAlign.Center
            )
        }
    }
}

// --- DASHBOARD HELPERS & POLISHED WIDGETS ---
data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

@Composable
fun MotivationalQuoteCard(isDarkTheme: Boolean) {
    val quotes = listOf(
        "\"The secret of getting ahead is getting started.\" — Mark Twain",
        "\"It always seems impossible until it's done.\" — Nelson Mandela",
        "\"Don't watch the clock; do what it does. Keep going.\" — Sam Levenson",
        "\"Success is the sum of small efforts, repeated day in and day out.\" — Robert Collier",
        "\"Believe you can and you're halfway there.\" — Theodore Roosevelt"
    )
    val quote = remember { quotes[Calendar.getInstance().get(Calendar.DAY_OF_YEAR) % quotes.size] }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(20.dp),
                ambientColor = Color(0xFF5B4BFF).copy(alpha = 0.05f),
                spotColor = Color(0xFF5B4BFF).copy(alpha = 0.1f)
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isDarkTheme) Color(0xFF1E1B4B).copy(alpha = 0.4f) else Color(0xFFEEF2F6)
        ),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, if (isDarkTheme) Color(0xFF334155) else Color(0xFFE2E8F0))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "💡",
                fontSize = 22.sp,
                modifier = Modifier.padding(end = 12.dp)
            )
            Text(
                text = quote,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                color = if (isDarkTheme) Color(0xFFE2E8F0) else Color(0xFF4B5563),
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
fun QuickActionsRow(
    isDarkTheme: Boolean,
    onActionClicked: (String) -> Unit
) {
    val actions = listOf(
        Quadruple("⏱️", "Start Timer", "planner", Color(0xFF5B4BFF)),
        Quadruple("➕", "Add Exam", "exams", Color(0xFF7C4DFF)),
        Quadruple("📊", "Analytics", "analytics", Color(0xFF39D5FF)),
        Quadruple("📝", "Log Study", "log_study", Color(0xFF10B981))
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        actions.forEach { (emoji, label, actionId, color) ->
            Card(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onActionClicked(actionId) }
                    .shadow(
                        elevation = 2.dp,
                        shape = RoundedCornerShape(16.dp),
                        ambientColor = Color(0xFF5B4BFF).copy(alpha = 0.02f),
                        spotColor = Color(0xFF5B4BFF).copy(alpha = 0.05f)
                    ),
                colors = CardDefaults.cardColors(
                    containerColor = if (isDarkTheme) Color(0xFF1E293B) else Color.White
                ),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, if (isDarkTheme) Color(0xFF334155) else Color(0xFFF1F5F9))
            ) {
                Column(
                    modifier = Modifier
                        .padding(vertical = 12.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(color.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = emoji, fontSize = 18.sp)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = label,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isDarkTheme) Color(0xFFE2E8F0) else Color(0xFF1E1E2D),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
fun DashboardStudyTimer(
    viewModel: ExamViewModel,
    isDarkTheme: Boolean
) {
    val isTimerRunning by viewModel.isTimerRunning.collectAsStateWithLifecycle()
    val timerSecondsRemaining by viewModel.timerSecondsRemaining.collectAsStateWithLifecycle()
    val allExams by viewModel.allExams.collectAsStateWithLifecycle()

    val minutes = timerSecondsRemaining / 60
    val seconds = timerSecondsRemaining % 60
    val timeStr = String.format("%02d:%02d", minutes, seconds)

    val activeExam = allExams.find { it.id == viewModel.activeTimerExamId }
    val subjectLabel = activeExam?.title ?: "General Study"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(24.dp),
                ambientColor = Color(0xFF5B4BFF).copy(alpha = 0.05f),
                spotColor = Color(0xFF5B4BFF).copy(alpha = 0.1f)
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isDarkTheme) Color(0xFF1E293B) else Color.White
        ),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, if (isDarkTheme) Color(0xFF334155) else Color(0xFFF1F5F9))
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "FOCUS ZONE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF5B4BFF),
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Quick Pomodoro Timer",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDarkTheme) Color.White else Color(0xFF1E1E2D)
                    )
                }

                Box(
                    modifier = Modifier
                        .background(
                            if (isTimerRunning) Color(0xFF10B981).copy(alpha = 0.15f) else Color(0xFF6B7280).copy(alpha = 0.15f),
                            RoundedCornerShape(10.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (isTimerRunning) "ACTIVE" else "IDLE",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isTimerRunning) Color(0xFF10B981) else Color(0xFF6B7280)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Timer,
                        contentDescription = "Timer icon",
                        tint = Color(0xFF7C4DFF),
                        modifier = Modifier.size(28.dp)
                    )
                    Text(
                        text = timeStr,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = if (isDarkTheme) Color.White else Color(0xFF1E1E2D)
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(
                        onClick = { viewModel.resetStudyTimer(25) },
                        modifier = Modifier
                            .size(40.dp)
                            .background(if (isDarkTheme) Color(0xFF334155) else Color(0xFFF1F5F9), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Reset timer",
                            tint = if (isDarkTheme) Color.LightGray else Color(0xFF6B7280),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Button(
                        onClick = {
                            if (isTimerRunning) {
                                viewModel.stopStudyTimer()
                            } else {
                                val targetExamId = allExams.firstOrNull()?.id ?: 0
                                viewModel.startStudyTimer(targetExamId)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isTimerRunning) Color(0xFFEF4444) else Color(0xFF5B4BFF)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = if (isTimerRunning) "Pause" else "Focus",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Studying: $subjectLabel",
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = if (isDarkTheme) Color(0xFF94A3B8) else Color(0xFF6B7280)
            )
        }
    }
}

@Composable
fun StatCard(
    emoji: String,
    title: String,
    value: String,
    isDarkTheme: Boolean,
    delayMs: Int = 0
) {
    var animateStarted by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(delayMs.toLong())
        animateStarted = true
    }

    val scale by animateFloatAsState(
        targetValue = if (animateStarted) 1f else 0.85f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)
    )
    val opacity by animateFloatAsState(
        targetValue = if (animateStarted) 1f else 0f,
        animationSpec = tween(500)
    )
    val offsetY by animateDpAsState(
        targetValue = if (animateStarted) 0.dp else 12.dp,
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow)
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer(
                scaleX = scale,
                scaleY = scale,
                alpha = opacity,
                translationY = offsetY.value
            )
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(20.dp),
                ambientColor = Color(0xFF5B4BFF).copy(alpha = 0.05f),
                spotColor = Color(0xFF5B4BFF).copy(alpha = 0.1f)
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isDarkTheme) Color(0xFF1E293B) else Color.White
        ),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, if (isDarkTheme) Color(0xFF334155) else Color(0xFFF1F5F9))
    ) {
        Row(
            modifier = Modifier
                .padding(14.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isDarkTheme) Color(0xFF334155) else Color(0xFFF1F5F9)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = emoji, fontSize = 20.sp)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = title,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (isDarkTheme) Color(0xFF94A3B8) else Color(0xFF6B7280),
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = value,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDarkTheme) Color.White else Color(0xFF1E1E2D)
                )
            }
        }
    }
}

@Composable
fun CalendarPreviewStrip(
    exams: List<ExamEntity>,
    sessions: List<StudySessionEntity>,
    isDarkTheme: Boolean
) {
    val today = Calendar.getInstance()
    val examsByDay = remember(exams) {
        exams.groupBy { exam ->
            val cal = Calendar.getInstance().apply { timeInMillis = exam.dateTime }
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            cal.timeInMillis
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        for (i in 0 until 7) {
            val cellCal = today.clone() as Calendar
            cellCal.add(Calendar.DAY_OF_YEAR, i)
            val dayName = SimpleDateFormat("E", Locale.getDefault()).format(cellCal.time).take(1)
            val dayNum = SimpleDateFormat("d", Locale.getDefault()).format(cellCal.time)
            
            val cellTimeMs = Calendar.getInstance().apply {
                timeInMillis = cellCal.timeInMillis
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis

            val isToday = i == 0
            val hasExam = examsByDay.containsKey(cellTimeMs)

            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 4.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (isToday) {
                            Brush.linearGradient(listOf(Color(0xFF5B4BFF), Color(0xFF7C4DFF)))
                        } else {
                            Brush.linearGradient(listOf(Color.Transparent, Color.Transparent))
                        }
                    )
                    .then(
                        if (!isToday) {
                            Modifier.border(
                                BorderStroke(1.dp, if (isDarkTheme) Color(0xFF334155) else Color(0xFFE2E8F0)),
                                RoundedCornerShape(12.dp)
                            )
                        } else {
                            Modifier
                        }
                    )
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = dayName,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isToday) Color.White else (if (isDarkTheme) Color(0xFF94A3B8) else Color(0xFF6B7280))
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = dayNum,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isToday) Color.White else (if (isDarkTheme) Color.White else Color(0xFF1E1E2D))
                    )
                    
                    if (hasExam) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .size(5.dp)
                                .clip(CircleShape)
                                .background(if (isToday) Color.White else Color(0xFF39D5FF))
                        )
                    } else {
                        Spacer(modifier = Modifier.height(9.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun AchievementBadgesRow(isDarkTheme: Boolean) {
    val badges = listOf(
        Triple("🏆", "Exam Warrior", "Ready for any exam"),
        Triple("⚡", "Streak Master", "Active study habits"),
        Triple("🦉", "Night Owl", "Late study sessions"),
        Triple("🎯", "Perfect Goal", "Met daily target"),
        Triple("📚", "Bookworm", "Studied multi-topics")
    )

    Column {
        Text(
            text = "YOUR ACHIEVEMENTS",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF7C4DFF),
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = "Unlocked Badges",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = if (isDarkTheme) Color.White else Color(0xFF1E1E2D)
        )
        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            badges.forEach { (emoji, title, desc) ->
                Card(
                    modifier = Modifier
                        .width(130.dp)
                        .shadow(
                            elevation = 2.dp,
                            shape = RoundedCornerShape(16.dp),
                            ambientColor = Color(0xFF5B4BFF).copy(alpha = 0.02f),
                            spotColor = Color(0xFF5B4BFF).copy(alpha = 0.05f)
                        ),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isDarkTheme) Color(0xFF1E293B) else Color.White
                    ),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, if (isDarkTheme) Color(0xFF334155) else Color(0xFFF1F5F9))
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF5B4BFF).copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = emoji, fontSize = 20.sp)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = title,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDarkTheme) Color.White else Color(0xFF1E1E2D),
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = desc,
                            fontSize = 9.sp,
                            color = if (isDarkTheme) Color(0xFF94A3B8) else Color(0xFF6B7280),
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RecentActivitySection(
    sessions: List<StudySessionEntity>,
    isDarkTheme: Boolean
) {
    val recentSessions = sessions.take(3)

    Column {
        Text(
            text = "RECENT TRACKING",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF7C4DFF),
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = "Study Activity Logs",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = if (isDarkTheme) Color.White else Color(0xFF1E1E2D)
        )
        Spacer(modifier = Modifier.height(10.dp))

        if (recentSessions.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (isDarkTheme) Color(0xFF1E293B) else Color.White
                ),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, if (isDarkTheme) Color(0xFF334155) else Color(0xFFF1F5F9))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No study sessions recorded yet. Build habits in the Planner!",
                        fontSize = 11.sp,
                        color = if (isDarkTheme) Color(0xFF94A3B8) else Color(0xFF6B7280),
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                recentSessions.forEach { session ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isDarkTheme) Color(0xFF1E293B) else Color.White
                        ),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, if (isDarkTheme) Color(0xFF334155) else Color(0xFFF1F5F9))
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF10B981).copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = "📝", fontSize = 14.sp)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = session.subjectName,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isDarkTheme) Color.White else Color(0xFF1E1E2D)
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = session.notes.ifEmpty { "Logged study time." },
                                    fontSize = 10.sp,
                                    color = if (isDarkTheme) Color(0xFF94A3B8) else Color(0xFF6B7280)
                                )
                            }
                            Text(
                                text = "${session.durationMinutes}m",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF10B981)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WeeklyProgressSection(sessions: List<StudySessionEntity>, isDarkTheme: Boolean) {
    val daysOfWeek = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    
    val dailyMinutes = remember(sessions) {
        val cal = Calendar.getInstance()
        val mins = FloatArray(7)
        sessions.forEach { session ->
            cal.timeInMillis = session.date
            val dayIdx = when (cal.get(Calendar.DAY_OF_WEEK)) {
                Calendar.MONDAY -> 0
                Calendar.TUESDAY -> 1
                Calendar.WEDNESDAY -> 2
                Calendar.THURSDAY -> 3
                Calendar.FRIDAY -> 4
                Calendar.SATURDAY -> 5
                Calendar.SUNDAY -> 6
                else -> 0
            }
            mins[dayIdx] += session.durationMinutes.toFloat()
        }
        mins
    }

    val maxMins = remember(dailyMinutes) { dailyMinutes.maxOrNull()?.coerceAtLeast(30f) ?: 60f }

    Column {
        Text(
            text = "WEEKLY METRICS",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF7C4DFF),
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = "Weekly Study Distribution",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = if (isDarkTheme) Color.White else Color(0xFF1E1E2D)
        )
        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 4.dp,
                    shape = RoundedCornerShape(20.dp),
                    ambientColor = Color(0xFF5B4BFF).copy(alpha = 0.05f),
                    spotColor = Color(0xFF5B4BFF).copy(alpha = 0.1f)
                ),
            colors = CardDefaults.cardColors(
                containerColor = if (isDarkTheme) Color(0xFF1E293B) else Color.White
            ),
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.dp, if (isDarkTheme) Color(0xFF334155) else Color(0xFFF1F5F9))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    daysOfWeek.forEachIndexed { idx, day ->
                        val mins = dailyMinutes[idx]
                        val pct = mins / maxMins
                        
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "${mins.toInt()}m",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (mins > 0) Color(0xFF39D5FF) else (if (isDarkTheme) Color(0xFF64748B) else Color(0xFF94A3B8))
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Box(
                                modifier = Modifier
                                    .width(12.dp)
                                    .fillMaxHeight(pct.coerceIn(0.1f, 1f))
                                    .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                    .background(
                                        if (mins > 0) {
                                            Brush.verticalGradient(listOf(Color(0xFF39D5FF), Color(0xFF5B4BFF)))
                                        } else {
                                            Brush.verticalGradient(listOf(if (isDarkTheme) Color(0xFF334155) else Color(0xFFEEF2F6), if (isDarkTheme) Color(0xFF334155) else Color(0xFFEEF2F6)))
                                        }
                                    )
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = day,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (isDarkTheme) Color(0xFF94A3B8) else Color(0xFF6B7280)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StudyInsightsCard(sessions: List<StudySessionEntity>, isDarkTheme: Boolean) {
    val totalMins = sessions.sumOf { it.durationMinutes }
    val insightText = when {
        totalMins == 0 -> "You haven't logged any focus hours yet. Complete a Pomodoro session to kickstart your academic insights!"
        totalMins < 60 -> "Your study engine is warming up! Build micro habits by focusing for 15 minutes every morning."
        totalMins < 300 -> "Solid progress! You are showing peak focus during early hours. Study consistency is at 88%."
        else -> "Elite study performance! Your focus streak places you in the top 12% of worldwide students. Keep pushing!"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(20.dp),
                ambientColor = Color(0xFF5B4BFF).copy(alpha = 0.05f),
                spotColor = Color(0xFF5B4BFF).copy(alpha = 0.1f)
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isDarkTheme) Color(0xFF1E293B) else Color.White
        ),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, if (isDarkTheme) Color(0xFF334155) else Color(0xFFF1F5F9))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "📈", fontSize = 20.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Study Insights",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDarkTheme) Color.White else Color(0xFF1E1E2D)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = insightText,
                fontSize = 12.sp,
                color = if (isDarkTheme) Color(0xFF94A3B8) else Color(0xFF6B7280),
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
fun UpcomingDeadlinesCard(
    exams: List<ExamEntity>,
    isDarkTheme: Boolean,
    currentTime: Long
) {
    val activeDeadlines = exams.filter { it.dateTime > currentTime }.take(3)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(20.dp),
                ambientColor = Color(0xFF5B4BFF).copy(alpha = 0.05f),
                spotColor = Color(0xFF5B4BFF).copy(alpha = 0.1f)
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isDarkTheme) Color(0xFF1E293B) else Color.White
        ),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, if (isDarkTheme) Color(0xFF334155) else Color(0xFFF1F5F9))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "📅", fontSize = 20.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Upcoming Deadlines",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDarkTheme) Color.White else Color(0xFF1E1E2D)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))

            if (activeDeadlines.isEmpty()) {
                Text(
                    text = "No upcoming deadlines on your radar. Excellent work!",
                    fontSize = 12.sp,
                    color = if (isDarkTheme) Color(0xFF94A3B8) else Color(0xFF6B7280)
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    activeDeadlines.forEach { exam ->
                        val diff = exam.dateTime - currentTime
                        val days = diff / (1000 * 60 * 60 * 24)
                        val deadlineStr = if (days > 0) "${days}d remaining" else "Today!"
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = exam.title,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDarkTheme) Color.White else Color(0xFF1E1E2D),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                            Box(
                                modifier = Modifier
                                    .background(
                                        if (days <= 2) Color(0xFFEF4444).copy(alpha = 0.15f) else Color(0xFF7C4DFF).copy(alpha = 0.15f),
                                        RoundedCornerShape(8.dp)
                                    )
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = deadlineStr,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (days <= 2) Color(0xFFEF4444) else Color(0xFF7C4DFF)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- SUB-SCREEN 2: DASHBOARD (Home Page) ---
@Composable
fun DashboardScreen(
    viewModel: ExamViewModel,
    isDarkTheme: Boolean,
    onTabChange: (String) -> Unit = {}
) {
    val currentTime by viewModel.currentTime.collectAsStateWithLifecycle()
    val allExams by viewModel.allExams.collectAsStateWithLifecycle()
    val allCategories by viewModel.allCategories.collectAsStateWithLifecycle()
    val allNotifications by viewModel.allNotifications.collectAsStateWithLifecycle()
    val allSessions by viewModel.allSessions.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()

    val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    val greeting = when {
        currentHour < 12 -> "Good morning"
        currentHour < 18 -> "Good afternoon"
        else -> "Good evening"
    }

    val upcomingExams = allExams.filter { it.dateTime > currentTime }.sortedBy { it.dateTime }
    val urgentExam = upcomingExams.firstOrNull()

    var showQuickSessionDialog by remember { mutableStateOf(false) }

    // Metrics calculations
    val avgProgress = remember(allExams) {
        if (allExams.isNotEmpty()) (allExams.sumOf { it.preparationProgress } / allExams.size) else 0
    }
    val currentStreak = remember(allSessions) { calculateStreak(allSessions) }
    val rankStr = remember(currentStreak, avgProgress) {
        when {
            currentStreak >= 5 -> "Top 5%"
            currentStreak >= 2 -> "Top 12%"
            avgProgress >= 80 -> "Top 15%"
            else -> "Top 25%"
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { Spacer(modifier = Modifier.height(16.dp)) }

        // Redesigned Typography Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "${greeting}, ${currentUser?.displayName ?: "Alex Rivera"}".uppercase(Locale.getDefault()),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 1.sp,
                        color = if (isDarkTheme) Color(0xFFC084FC) else Color(0xFF7C4DFF)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Your Workspace",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDarkTheme) Color.White else Color(0xFF1E1E2D)
                    )
                }

                // Interactive Avatar Circle with Glow
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(listOf(Color(0xFF5B4BFF), Color(0xFF7C4DFF)))
                        )
                        .clickable { onTabChange("profile") },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = (currentUser?.displayName ?: "A").take(1).uppercase(Locale.getDefault()),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }

        // Motivational Quote Section
        item {
            MotivationalQuoteCard(isDarkTheme = isDarkTheme)
        }

        // REDESIGNED: Premium Glassmorphic Hero Countdown Widget
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = 16.dp,
                        shape = RoundedCornerShape(24.dp),
                        ambientColor = Color(0xFF5B4BFF).copy(alpha = 0.1f),
                        spotColor = Color(0xFF5B4BFF).copy(alpha = 0.3f)
                    ),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(
                    1.5.dp, 
                    Brush.linearGradient(listOf(Color(0xFF5B4BFF).copy(alpha = 0.6f), Color(0xFF7C4DFF).copy(alpha = 0.6f)))
                )
            ) {
                Box(
                    modifier = Modifier
                        .background(
                            Brush.linearGradient(listOf(Color(0xFF1E1B4B), Color(0xFF311042)))
                        )
                        .padding(20.dp)
                        .fillMaxWidth()
                ) {
                    // Cosmic radial glowing backdrop circles
                    Canvas(modifier = Modifier.matchParentSize()) {
                        drawCircle(
                            color = Color(0xFF39D5FF).copy(alpha = 0.15f),
                            radius = size.width / 1.5f,
                            center = Offset(size.width, 0f)
                        )
                        drawCircle(
                            color = Color(0xFF7C4DFF).copy(alpha = 0.1f),
                            radius = size.width / 1.2f,
                            center = Offset(0f, size.height)
                        )
                    }

                    Column {
                        if (urgentExam != null) {
                            val category = allCategories.find { it.id == urgentExam.categoryId }
                            val dateOnlyStr = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(urgentExam.dateTime))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .background(Color(0xFF7C4DFF).copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                                        .border(BorderStroke(1.dp, Color(0xFF39D5FF).copy(alpha = 0.4f)), RoundedCornerShape(10.dp))
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "MOST URGENT",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF39D5FF),
                                        letterSpacing = 1.sp
                                    )
                                }
                                Text(
                                    text = dateOnlyStr,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = urgentExam.title,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            Text(
                                text = "${category?.name ?: "General Exam"} • ${urgentExam.notes.ifEmpty { "Official Countdown Session" }}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.White.copy(alpha = 0.85f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            // High-Contrast Custom Grid of Monospaced Countdown Boxes
                            val diff = (urgentExam.dateTime - currentTime).coerceAtLeast(0)
                            val totalSecs = diff / 1000
                            val daysVal = totalSecs / (24 * 3600)
                            val hoursVal = (totalSecs % (24 * 3600)) / 3600
                            val minsVal = (totalSecs % 3600) / 60
                            val secsVal = totalSecs % 60

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                val units = listOf(
                                    String.format("%02d", daysVal) to "Days",
                                    String.format("%02d", hoursVal) to "Hrs",
                                    String.format("%02d", minsVal) to "Min",
                                    String.format("%02d", secsVal) to "Sec"
                                )
                                units.forEach { (unitVal, unitLbl) ->
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .background(Color.White.copy(alpha = 0.07f), RoundedCornerShape(14.dp))
                                            .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)), RoundedCornerShape(14.dp))
                                            .padding(vertical = 12.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(
                                                text = unitVal,
                                                fontSize = 28.sp,
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = FontFamily.Monospace,
                                                color = Color.White
                                            )
                                            Text(
                                                text = unitLbl.uppercase(Locale.getDefault()),
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White.copy(alpha = 0.7f),
                                                letterSpacing = 0.5.sp
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            // Progress Track Bar
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Preparation Progress",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color.White.copy(alpha = 0.9f)
                                    )
                                    Text(
                                        text = "${urgentExam.preparationProgress}%",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF39D5FF)
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                val animatedProgress by animateFloatAsState(
                                    targetValue = urgentExam.preparationProgress / 100f,
                                    animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessVeryLow)
                                )
                                
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(8.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Color.White.copy(alpha = 0.1f))
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .fillMaxWidth(animatedProgress)
                                            .background(
                                                Brush.horizontalGradient(
                                                    listOf(Color(0xFF5B4BFF), Color(0xFF39D5FF))
                                                )
                                            )
                                            .shadow(4.dp, RoundedCornerShape(4.dp), clip = false)
                                    )
                                }
                            }
                        } else {
                            // Empty State Hero
                            Column(
                                modifier = Modifier
                                    .padding(vertical = 24.dp)
                                    .fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("🎉", fontSize = 48.sp)
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "All Clear!",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "No upcoming exam deadlines. Add exams to begin countdown.",
                                    fontSize = 12.sp,
                                    color = Color.White.copy(alpha = 0.7f),
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(
                                    onClick = { onTabChange("exams") },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C4DFF)),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("Add Exam", color = Color.White)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Quick Actions Row
        item {
            QuickActionsRow(
                isDarkTheme = isDarkTheme,
                onActionClicked = { actionId ->
                    if (actionId == "log_study") {
                        showQuickSessionDialog = true
                    } else {
                        onTabChange(actionId)
                    }
                }
            )
        }

        // Study Timer Pomodoro Zone Widget
        item {
            DashboardStudyTimer(viewModel = viewModel, isDarkTheme = isDarkTheme)
        }

        // REDESIGNED: Statistics 2x2 Mini Cards Grid
        item {
            Column {
                Text(
                    text = "DASHBOARD STATS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF7C4DFF),
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Key Study Metrics",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDarkTheme) Color.White else Color(0xFF1E1E2D)
                )
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        StatCard(emoji = "📚", title = "Exams", value = "${upcomingExams.size} Upcoming", isDarkTheme = isDarkTheme, delayMs = 0)
                        StatCard(emoji = "📈", title = "Completion", value = "$avgProgress%", isDarkTheme = isDarkTheme, delayMs = 150)
                    }
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        StatCard(emoji = "⚡", title = "Study Streak", value = "$currentStreak Days", isDarkTheme = isDarkTheme, delayMs = 75)
                        StatCard(emoji = "🏆", title = "Rank", value = rankStr, isDarkTheme = isDarkTheme, delayMs = 225)
                    }
                }
            }
        }

        // Calendar 7-Day Preview Strip
        item {
            Column {
                Text(
                    text = "TIMELINE PREVIEW",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF7C4DFF),
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Next 7 Days Strip",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDarkTheme) Color.White else Color(0xFF1E1E2D)
                )
                Spacer(modifier = Modifier.height(12.dp))
                CalendarPreviewStrip(exams = allExams, sessions = allSessions, isDarkTheme = isDarkTheme)
            }
        }

        // Consistency Heatmap Widget
        item {
            StudyStreakHeatmap(sessions = allSessions, isDarkTheme = isDarkTheme)
        }

        // Weekly Progress Bar Chart
        item {
            WeeklyProgressSection(sessions = allSessions, isDarkTheme = isDarkTheme)
        }

        // Achievement Badges Horizontal Row
        item {
            AchievementBadgesRow(isDarkTheme = isDarkTheme)
        }

        // Study Insights Card
        item {
            StudyInsightsCard(sessions = allSessions, isDarkTheme = isDarkTheme)
        }

        // Upcoming Deadlines Tracker List
        item {
            UpcomingDeadlinesCard(exams = allExams, isDarkTheme = isDarkTheme, currentTime = currentTime)
        }

        // Recent Study Activity logs
        item {
            RecentActivitySection(sessions = allSessions, isDarkTheme = isDarkTheme)
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
    }

    // Modal dialog to quickly record Study session minutes
    if (showQuickSessionDialog) {
        Dialog(onDismissRequest = { showQuickSessionDialog = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = if (isDarkTheme) Color(0xFF1E293B) else Color.White),
                border = BorderStroke(1.dp, if (isDarkTheme) Color(0xFF334155) else Color(0xFFE2E8F0))
            ) {
                var sessionMinutes by remember { mutableStateOf("45") }
                var subjectTopic by remember { mutableStateOf("") }
                var sessionExamSelectionId by remember { mutableStateOf(upcomingExams.firstOrNull()?.id ?: 0) }

                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Record Study Progress", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = if (isDarkTheme) Color.White else Color(0xFF1E1E2D))
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = subjectTopic,
                        onValueChange = { subjectTopic = it },
                        label = { Text("Topic/Subject Title") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = sessionMinutes,
                        onValueChange = { sessionMinutes = it },
                        label = { Text("Duration (Minutes)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        TextButton(onClick = { showQuickSessionDialog = false }) {
                            Text("Cancel", color = Color.Gray)
                        }
                        Button(
                            onClick = {
                                val mins = sessionMinutes.toIntOrNull() ?: 30
                                viewModel.saveStudySessionDirect(sessionExamSelectionId, mins, subjectTopic.ifEmpty { "Self-Directed Review Session" })
                                showQuickSessionDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5B4BFF)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Log Session", color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DashboardScreenOld(viewModel: ExamViewModel, isDarkTheme: Boolean) {
    val currentTime by viewModel.currentTime.collectAsStateWithLifecycle()
    val allExams by viewModel.allExams.collectAsStateWithLifecycle()
    val allCategories by viewModel.allCategories.collectAsStateWithLifecycle()
    val allNotifications by viewModel.allNotifications.collectAsStateWithLifecycle()
    val allSessions by viewModel.allSessions.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()

    val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    val greeting = when {
        currentHour < 12 -> "Good morning, "
        currentHour < 18 -> "Good afternoon, "
        else -> "Good evening, "
    }

    val upcomingExams = allExams.filter { it.dateTime > currentTime }.sortedBy { it.dateTime }
    val urgentExam = upcomingExams.firstOrNull()

    var showQuickSessionDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        item { Spacer(modifier = Modifier.height(16.dp)) }

        // Dynamic Greeting Header Card
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1.0f)) {
                    Text(
                        text = "$greeting${currentUser?.displayName ?: "Alex"}".uppercase(Locale.getDefault()),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDarkTheme) Color(0xFF94A3B8) else Color(0xFF64748B),
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Exam Dashboard",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDarkTheme) Color.White else Color(0xFF0F172A),
                        letterSpacing = (-0.5).sp
                    )
                }

                // Avatar container as defined in High Density spec
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isDarkTheme) Color(0xFF1E293B) else Color(0xFFEEF2F6))
                        .border(BorderStroke(2.dp, if (isDarkTheme) Color(0xFF334155) else Color.White), RoundedCornerShape(12.dp))
                        .clickable { showQuickSessionDialog = true },
                    contentAlignment = Alignment.Center
                ) {
                    val initial = (currentUser?.displayName ?: "A").take(1).uppercase(Locale.getDefault())
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF4F46E5)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = initial,
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }

        // Core Live Countdown Hero Widget Category
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(12.dp, RoundedCornerShape(24.dp)),
                shape = RoundedCornerShape(24.dp)
            ) {
                Box(
                    modifier = Modifier
                        .background(
                            Brush.linearGradient(listOf(Color(0xFF4F46E5), Color(0xFF7C3AED), Color(0xFF4338CA)))
                        )
                        .padding(20.dp)
                        .fillMaxWidth()
                ) {
                    Column {
                        if (urgentExam != null) {
                            val category = allCategories.find { it.id == urgentExam.categoryId }
                            val dateOnlyStr = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(urgentExam.dateTime))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "MOST URGENT",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        letterSpacing = 1.sp
                                    )
                                }
                                Text(
                                    text = dateOnlyStr,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = urgentExam.title,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            Text(
                                text = "${category?.name ?: "General Exam"} • ${urgentExam.notes.ifEmpty { "Official Exam Session" }}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.White.copy(alpha = 0.85f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            // Actual High-Density Split Countdown Units Box Grid
                            val diff = (urgentExam.dateTime - currentTime).coerceAtLeast(0)
                            val totalSecs = diff / 1000
                            val daysVal = totalSecs / (24 * 3600)
                            val hoursVal = (totalSecs % (24 * 3600)) / 3600
                            val minsVal = (totalSecs % 3600) / 60
                            val secsVal = totalSecs % 60

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("urgent_countdown_text"),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                val units = listOf(
                                    String.format("%02d", daysVal) to "Days",
                                    String.format("%02d", hoursVal) to "Hrs",
                                    String.format("%02d", minsVal) to "Min",
                                    String.format("%02d", secsVal) to "Sec"
                                )
                                units.forEach { (unitVal, unitLbl) ->
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(14.dp))
                                            .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)), RoundedCornerShape(14.dp))
                                            .padding(vertical = 10.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(
                                                text = unitVal,
                                                fontSize = 20.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                            Text(
                                                text = unitLbl.uppercase(Locale.getDefault()),
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White.copy(alpha = 0.7f),
                                                letterSpacing = 0.5.sp
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            // Premium Cyan Progress indicator (track background semi-transparent)
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Preparation Progress",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color.White.copy(alpha = 0.9f)
                                    )
                                    Text(
                                        text = "${urgentExam.preparationProgress}%",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                LinearProgressIndicator(
                                    progress = { urgentExam.preparationProgress / 100f },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(8.dp)
                                        .clip(RoundedCornerShape(4.dp)),
                                    color = Color(0xFF22D3EE),
                                    trackColor = Color.Black.copy(alpha = 0.2f)
                                )
                            }
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "FOCUS TIME",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        letterSpacing = 1.sp
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Zero Upcoming Exams!",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Perfect score mindset. Tap the 'Exams' tab to add your upcoming exams and activate the high-density monitors.",
                                fontSize = 13.sp,
                                color = Color.White.copy(alpha = 0.85f)
                            )
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }

        // Side-by-side study logs metrics Overview Cards
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Card 1: Countdowns
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isDarkTheme) Color(0xFF1E293B) else Color.White
                    ),
                    shape = RoundedCornerShape(24.dp),
                    border = BorderStroke(1.dp, if (isDarkTheme) Color(0xFF334155) else Color(0xFFF1F5F9))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isDarkTheme) Color(0xFF0E7490).copy(alpha = 0.2f) else Color(0xFFECFEFF)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Σ",
                                color = if (isDarkTheme) Color(0xFF22D3EE) else Color(0xFF06B6D4),
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Exams",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (isDarkTheme) Color(0xFF94A3B8) else Color(0xFF64748B)
                        )
                        Text(
                            text = upcomingExams.size.toString(),
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDarkTheme) Color.White else Color(0xFF0F172A)
                        )
                    }
                }

                // Card 2: Streak or Focus
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isDarkTheme) Color(0xFF1E293B) else Color.White
                    ),
                    shape = RoundedCornerShape(24.dp),
                    border = BorderStroke(1.dp, if (isDarkTheme) Color(0xFF334155) else Color(0xFFF1F5F9))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isDarkTheme) Color(0xFF5B21B6).copy(alpha = 0.2f) else Color(0xFFF3E8FF)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "⚡",
                                color = if (isDarkTheme) Color(0xFFC084FC) else Color(0xFF7C3AED),
                                fontWeight = FontWeight.Black,
                                fontSize = 14.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Streak",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (isDarkTheme) Color(0xFF94A3B8) else Color(0xFF64748B)
                        )
                        val currentStreak = remember(allSessions) { calculateStreak(allSessions) }
                        Text(
                            text = if (currentStreak > 0) "$currentStreak days" else "0 days",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDarkTheme) Color.White else Color(0xFF0F172A)
                        )
                    }
                }
            }
        }

        item {
            StudyStreakHeatmap(sessions = allSessions, isDarkTheme = isDarkTheme)
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }

        // Upcoming Exam Slider List header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Upcoming This Week",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDarkTheme) Color.White else Color(0xFF1E293B)
                )
                Text(
                    text = "VIEW ALL",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF4F46E5),
                    letterSpacing = 0.5.sp,
                    modifier = Modifier.clickable { /* Tab switch happens */ }
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        // List of rest upcoming events
        if (upcomingExams.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "You are fully up-to-date! Add exams to see cards here.",
                        fontSize = 12.sp,
                        color = GrayColor(isDarkTheme)
                    )
                }
            }
        } else {
            items(upcomingExams.take(3)) { exam ->
                val category = allCategories.find { it.id == exam.categoryId }
                val dateStr = formatDateTime(exam.dateTime)

                val cal = Calendar.getInstance().apply { timeInMillis = exam.dateTime }
                val monthName = SimpleDateFormat("MMM", Locale.getDefault()).format(cal.time).uppercase(Locale.getDefault())
                val dayNumber = SimpleDateFormat("dd", Locale.getDefault()).format(cal.time)

                val categoryColor = parseColor(category?.colorHex ?: "#94A3B8")
                val badgeBgColor = categoryColor.copy(alpha = 0.08f)
                val badgeTextColor = categoryColor

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isDarkTheme) Color(0xFF1E293B) else Color.White
                    ),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, if (isDarkTheme) Color(0xFF334155) else Color(0xFFF1F5F9))
                ) {
                    Row(
                        modifier = Modifier
                            .padding(12.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // High Density Date Pill badge
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(badgeBgColor)
                                .border(BorderStroke(1.dp, categoryColor.copy(alpha = 0.2f)), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = monthName,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = badgeTextColor
                                )
                                Text(
                                    text = dayNumber,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Black,
                                    color = badgeTextColor,
                                    lineHeight = 16.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = exam.title,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDarkTheme) Color.White else Color(0xFF1E293B),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Box(
                                    modifier = Modifier
                                        .background(if (isDarkTheme) Color(0xFF334155) else Color(0xFFF1F5F9), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = category?.name ?: "University",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isDarkTheme) Color.LightGray else Color(0xFF64748B)
                                    )
                                }
                                Text(
                                    text = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(exam.dateTime)),
                                    fontSize = 11.sp,
                                    color = if (isDarkTheme) Color(0xFF94A3B8) else Color(0xFF64748B)
                                )
                            }
                        }

                        // Right-aligned mini counting countdown label and sleek wire progress
                        val daysLeft = ((exam.dateTime - currentTime) / (1000 * 60 * 60 * 24)).coerceAtLeast(0)
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = if (daysLeft > 0) "${daysLeft}d left" else "Today!",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDarkTheme) Color.White else Color(0xFF1E293B)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Box(
                                modifier = Modifier
                                    .width(48.dp)
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(if (isDarkTheme) Color(0xFF334155) else Color(0xFFF1F5F9))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .fillMaxWidth(exam.preparationProgress / 100f)
                                        .background(categoryColor)
                                )
                            }
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }

        // Daily Study Planner goal status block
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isDarkTheme) Color(0xFF1E293B) else Color.White
                ),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, if (isDarkTheme) Color(0xFF334155) else Color(0xFFF1F5F9))
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Small local progress graphics
                    Box(modifier = Modifier.size(70.dp), contentAlignment = Alignment.Center) {
                        val minutesGoal = 60.0f
                        val todayTotalMins = allSessions.sumOf { it.durationMinutes }
                        val pct = (todayTotalMins / minutesGoal).coerceIn(0f, 1f)

                        CircularProgressIndicator(
                            progress = { pct },
                            strokeWidth = 6.dp,
                            color = Color(0xFF4F46E5),
                            trackColor = Color(0xFF4F46E5).copy(alpha = 0.15f),
                            modifier = Modifier.fillMaxSize()
                        )
                        Icon(Icons.Filled.EmojiEvents, "Goal achievement", tint = Color(0xFF4F46E5), modifier = Modifier.size(24.dp))
                    }

                    Spacer(modifier = Modifier.width(20.dp))

                    Column {
                        val minutesTotal = allSessions.sumOf { it.durationMinutes }
                        Text("Daily Focus Meter", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = if (isDarkTheme) Color.White else Color(0xFF0F172A))
                        Text(
                            text = "Logged $minutesTotal of 60 daily goal minutes",
                            fontSize = 12.sp,
                            color = if (isDarkTheme) Color(0xFF94A3B8) else Color(0xFF64748B)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (minutesTotal >= 60) "Daily Study Goal Smashed! 🎉" else "Boost focus with modern Pomodoro sessions.",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF4F46E5)
                        )
                    }
                }
            }
        }
    }

    // Modal dialog to quickly record Study session minutes
    if (showQuickSessionDialog) {
        Dialog(onDismissRequest = { showQuickSessionDialog = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(20.dp)
            ) {
                var sessionMinutes by remember { mutableStateOf("45") }
                var subjectTopic by remember { mutableStateOf("") }
                var sessionExamSelectionId by remember { mutableStateOf(upcomingExams.firstOrNull()?.id ?: 0) }

                Column(
                    modifier = Modifier
                        .padding(20.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Record Study Progress", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = subjectTopic,
                        onValueChange = { subjectTopic = it },
                        label = { Text("Topic/Subject Title") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = sessionMinutes,
                        onValueChange = { sessionMinutes = it },
                        label = { Text("Duration (Minutes)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        TextButton(onClick = { showQuickSessionDialog = false }) {
                            Text("Cancel")
                        }
                        Button(
                            onClick = {
                                val mins = sessionMinutes.toIntOrNull() ?: 30
                                viewModel.saveStudySessionDirect(sessionExamSelectionId, mins, subjectTopic.ifEmpty { "Self-Directed Review Session" })
                                showQuickSessionDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SoftBlue)
                        ) {
                            Text("Log Session", color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

// --- SUB-SCREEN 3: EXAM MANAGER (Add / Edit / Remove) ---
@Composable
fun ExamsScreen(viewModel: ExamViewModel, isDarkTheme: Boolean) {
    val allExams by viewModel.allExams.collectAsStateWithLifecycle()
    val allCategories by viewModel.allCategories.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedCategoryId by viewModel.selectedCategoryId.collectAsStateWithLifecycle()
    val sortBy by viewModel.sortBy.collectAsStateWithLifecycle()

    var showAddEditDialog by remember { mutableStateOf(false) }
    var editingExam by remember { mutableStateOf<ExamEntity?>(null) }

    // Filtering logic
    val filteredExams = allExams.filter { exam ->
        val matchesSearch = exam.title.contains(searchQuery, ignoreCase = true) || exam.notes.contains(searchQuery, ignoreCase = true)
        val matchesCategory = selectedCategoryId == null || exam.categoryId == selectedCategoryId
        matchesSearch && matchesCategory
    }.sortedWith { a, b ->
        when (sortBy) {
            "FAVORITE" -> b.isFavorite.compareTo(a.isFavorite)
            "PROGRESS" -> b.preparationProgress.compareTo(a.preparationProgress)
            "ALPHABETICAL" -> a.title.lowercase().compareTo(b.title.lowercase())
            else -> a.dateTime.compareTo(b.dateTime) // DATE Sort default
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            Spacer(modifier = Modifier.height(12.dp))

            // Unified Filter Search Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.searchQuery.value = it },
                    placeholder = { Text("Search exams, topics...", fontSize = 13.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, null, modifier = Modifier.size(18.dp)) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("exam_search_bar"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SoftBlue,
                        unfocusedBorderColor = CardBorderColor.copy(alpha = 0.8f)
                    )
                )

                Spacer(modifier = Modifier.width(12.dp))

                // Clear button / quick seed
                IconButton(
                    onClick = {
                        viewModel.searchQuery.value = ""
                        viewModel.selectedCategoryId.value = null
                    },
                    modifier = Modifier.background(SoftBlue.copy(alpha = 0.1f), CircleShape)
                ) {
                    Icon(Icons.Filled.FilterAltOff, "Reset Filters", tint = SoftBlue)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Horizontal scrolling Category Filters Pill Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // "All" Pill
                FilterChip(
                    selected = selectedCategoryId == null,
                    onClick = { viewModel.selectedCategoryId.value = null },
                    label = { Text("All", fontSize = 12.sp) },
                    modifier = Modifier.testTag("filter_cat_all")
                )

                allCategories.forEach { category ->
                    FilterChip(
                        selected = selectedCategoryId == category.id,
                        onClick = { viewModel.selectedCategoryId.value = category.id },
                        label = { Text(category.name, fontSize = 12.sp) },
                        leadingIcon = {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(parseColor(category.colorHex))
                            )
                        }
                    )
                }
            }

            // Sorting Controls Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Sort:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = GrayColor(isDarkTheme))
                
                listOf(
                    "DATE" to "Date 📅",
                    "FAVORITE" to "★ Favorites",
                    "PROGRESS" to "Progress %",
                    "ALPHABETICAL" to "A-Z 🔠"
                ).forEach { (key, display) ->
                    val isSelected = sortBy == key
                    Box(
                        modifier = Modifier
                            .clickable { viewModel.sortBy.value = key }
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (isSelected) SoftBlue.copy(alpha = 0.15f) else Color.Transparent
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = display,
                            fontSize = 10.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) SoftBlue else GrayColor(isDarkTheme)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Main List scroll
            if (filteredExams.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Filled.AssignmentTurnedIn, "empty list", modifier = Modifier.size(64.dp), tint = Color.LightGray)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No countdown match found.",
                            fontWeight = FontWeight.Bold,
                            color = GrayColor(isDarkTheme)
                        )
                        Text(
                            text = "Tap the '+' bubble to create a new Countdown tracker instantly.",
                            fontSize = 11.sp,
                            color = GrayColor(isDarkTheme).copy(alpha = 0.7f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    items(filteredExams) { exam ->
                        val category = allCategories.find { it.id == exam.categoryId }
                        SwipeableExamCard(
                            exam = exam,
                            category = category,
                            isDarkTheme = isDarkTheme,
                            onFavorite = { viewModel.toggleExamFavorite(exam) },
                            onEdit = {
                                editingExam = exam
                                showAddEditDialog = true
                            },
                            onDuplicate = { viewModel.duplicateExam(exam) },
                            onDelete = { viewModel.deleteExam(exam.id, exam.title) },
                            onProgressChange = { next -> viewModel.updateExamProgress(exam, next) }
                        )
                    }
                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }
            }
        }

        // Floating Action Button
        FloatingActionButton(
            onClick = {
                editingExam = null
                showAddEditDialog = true
            },
            containerColor = SoftBlue,
            contentColor = Color.White,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
                .testTag("add_exam_fab")
        ) {
            Icon(Icons.Filled.Add, "Add countdown deadline")
        }
    }

    // Modal Add / Edit Sheet Dialog
    if (showAddEditDialog) {
        Dialog(onDismissRequest = { showAddEditDialog = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(20.dp)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = if (editingExam == null) "Create New Countdown" else "Modify Exam Parameters",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    var title by remember { mutableStateOf(editingExam?.title ?: "") }
                    var notes by remember { mutableStateOf(editingExam?.notes ?: "") }
                    var progress by remember { mutableStateOf(editingExam?.preparationProgress?.toFloat() ?: 0f) }
                    var catId by remember { mutableStateOf(editingExam?.categoryId ?: allCategories.firstOrNull()?.id ?: 1) }
                    var isFav by remember { mutableStateOf(editingExam?.isFavorite ?: false) }

                    // Date Input offset slider logic (simplifies manual inputs beautifully)
                    var daysOffset by remember { mutableStateOf(10f) }

                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Exam Name (e.g. Physics Final)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_exam_title")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Study Focus Note / Description") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Category Dropdown simulated selection
                    Text("Select Category:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        allCategories.forEach { category ->
                            val isSel = catId == category.id
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (isSel) parseColor(category.colorHex) else Color.LightGray.copy(alpha = 0.3f)
                                    )
                                    .clickable { catId = category.id }
                                    .padding(8.dp)
                            ) {
                                Text(
                                    text = category.name,
                                    fontSize = 11.sp,
                                    color = if (isSel) Color.White else Color.Black
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Slider to set date easily
                    Text("Time Until Exam (Days): ${daysOffset.toInt()}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Slider(
                        value = daysOffset,
                        onValueChange = { daysOffset = it },
                        valueRange = 1f..120f,
                        colors = SliderDefaults.colors(activeTrackColor = SoftBlue)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text("Initial Prep Progress: ${progress.toInt()}%", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Slider(
                        value = progress,
                        onValueChange = { progress = it },
                        valueRange = 0f..100f,
                        colors = SliderDefaults.colors(activeTrackColor = AccentPurple)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isFav = !isFav }
                    ) {
                        Checkbox(checked = isFav, onCheckedChange = { isFav = it })
                        Text("Add to Favorite High Importance", fontSize = 12.sp)
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        TextButton(onClick = { showAddEditDialog = false }) {
                            Text("Cancel")
                        }
                        Button(
                            onClick = {
                                val targetTimeMs = System.currentTimeMillis() + (daysOffset.toLong() * 86400000)
                                if (editingExam == null) {
                                    viewModel.addExam(title.ifEmpty { "General Test" }, targetTimeMs, catId, notes, isFav, progress.toInt())
                                } else {
                                    viewModel.editExam(editingExam!!.id, title.ifEmpty { "General Test" }, targetTimeMs, catId, notes, isFav, progress.toInt())
                                }
                                showAddEditDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SoftBlue),
                            modifier = Modifier.testTag("save_exam_btn")
                        ) {
                            Text("Save Countdown", color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SwipeableExamCard(
    exam: ExamEntity,
    category: CategoryEntity?,
    isDarkTheme: Boolean,
    onFavorite: () -> Unit,
    onEdit: () -> Unit,
    onDuplicate: () -> Unit,
    onDelete: () -> Unit,
    onProgressChange: (Int) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .shadow(4.dp, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(
            containerColor = if (isDarkTheme) CharcoalDark else Color.White
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Category Tag
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(parseColor(category?.colorHex ?: "#94A3B8").copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = category?.name ?: "School",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = parseColor(category?.colorHex ?: "#94A3B8")
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                if (exam.isDuplicated) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(AccentPink.copy(alpha = 0.15f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text("Duplicated", fontSize = 8.sp, color = AccentPink, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // Favorite Star
                IconButton(onClick = onFavorite, modifier = Modifier.size(24.dp)) {
                    Icon(
                        imageVector = if (exam.isFavorite) Icons.Filled.Star else Icons.Outlined.StarBorder,
                        contentDescription = "Starred Favorite status",
                        tint = if (exam.isFavorite) Color(0xFFFFB300) else Color.Gray,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Options Dropdown trigger (Simulated via adjacent row icons for clean compile-safe UI)
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(onClick = onDuplicate, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Filled.ContentCopy, "Duplicate exam", tint = SoftBlue, modifier = Modifier.size(16.dp))
                    }
                    IconButton(onClick = onEdit, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Filled.Edit, "Edit parameters", tint = AccentPurple, modifier = Modifier.size(16.dp))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Filled.Delete, "Delete countdown", tint = AccentPink, modifier = Modifier.size(16.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = exam.title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = if (isDarkTheme) Color.White else CharcoalDark
            )

            if (exam.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = exam.notes,
                    fontSize = 12.sp,
                    color = GrayColor(isDarkTheme),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Progress Slider adjustment
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Preparation Status:",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = GrayColor(isDarkTheme)
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "${exam.preparationProgress}% Prepared",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = parseColor(category?.colorHex ?: "#4FACFE")
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Slider(
                value = exam.preparationProgress.toFloat(),
                onValueChange = { onProgressChange(it.toInt()) },
                valueRange = 0f..100f,
                colors = SliderDefaults.colors(
                    activeTrackColor = parseColor(category?.colorHex ?: "#4FACFE")
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Full Bleed time remaining band
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(SoftBlue.copy(alpha = 0.08f))
                    .padding(10.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Timer, "Timeline indicator", tint = SoftBlue, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Target Date: ${formatDateTime(exam.dateTime)}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDarkTheme) Color.White else CharcoalDark
                    )
                }
            }
        }
    }
}

// --- SUB-SCREEN 4: STUDY PLANNER (Goals & Pomodoro Tracker) ---
@Composable
fun PlannerScreen(viewModel: ExamViewModel, isDarkTheme: Boolean) {
    val timerSecondsRemaining by viewModel.timerSecondsRemaining.collectAsStateWithLifecycle()
    val isTimerRunning by viewModel.isTimerRunning.collectAsStateWithLifecycle()
    val allSessions by viewModel.allSessions.collectAsStateWithLifecycle()
    val upcomingExams by viewModel.allExams.collectAsStateWithLifecycle()

    val totalMinsLoggedToday = allSessions.sumOf { it.durationMinutes }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Text(
                text = "Study Focus Planner",
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = if (isDarkTheme) Color.White else CharcoalDark,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Boost concentration streaks with automated Pomodoro structures.",
                fontSize = 12.sp,
                color = GrayColor(isDarkTheme),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp))
        }

        // Animated circular ticking Pomodoro Widget
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(12.dp, RoundedCornerShape(24.dp)),
                colors = CardDefaults.cardColors(
                    containerColor = if (isDarkTheme) CharcoalDark else Color.White
                ),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "POMODORO INTERVAL",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = SoftBlue
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Timer ring canvas structure
                    Box(modifier = Modifier.size(160.dp), contentAlignment = Alignment.Center) {
                        val maxSeconds = 1500f // 25 Mins
                        val fraction = (timerSecondsRemaining / maxSeconds).coerceIn(0f, 1f)

                        CircularProgressIndicator(
                            progress = { fraction },
                            strokeWidth = 8.dp,
                            color = AccentPink,
                            trackColor = AccentPink.copy(alpha = 0.15f),
                            modifier = Modifier.fillMaxSize()
                        )

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            val mins = timerSecondsRemaining / 60
                            val secs = timerSecondsRemaining % 60
                            Text(
                                text = String.format("%02d:%02d", mins, secs),
                                fontSize = 34.sp,
                                fontWeight = FontWeight.Black,
                                color = if (isDarkTheme) Color.White else CharcoalDark
                            )
                            Text(
                                text = "Focused State",
                                fontSize = 11.sp,
                                color = GrayColor(isDarkTheme)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Media Action buttons
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = {
                                if (isTimerRunning) viewModel.stopStudyTimer() else viewModel.startStudyTimer(0)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SoftBlue),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = if (isTimerRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = "Hold timer toggle",
                                tint = Color.White
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (isTimerRunning) "Pause Session" else "Start Timer", color = Color.White)
                        }

                        OutlinedButton(
                            onClick = { viewModel.resetStudyTimer(25) },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Refresh, "Refresh values")
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Reset")
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }

        // Setup custom logged study form
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isDarkTheme) CharcoalDark else Color.White
                ),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, CardBorderColor.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Planner History", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = if (isDarkTheme) Color.White else CharcoalDark)
                    Text("Historical focus sessions and time tracker metrics log", fontSize = 11.sp, color = GrayColor(isDarkTheme))
                    Spacer(modifier = Modifier.height(12.dp))

                    if (allSessions.isEmpty()) {
                        Text(
                            text = "No study sessions registered yet.",
                            fontSize = 12.sp,
                            color = Color.Gray,
                            modifier = Modifier.align(Alignment.CenterHorizontally).padding(16.dp)
                        )
                    } else {
                        allSessions.take(5).forEach { session ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(SoftBlue.copy(alpha = 0.05f))
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(SoftBlue.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Filled.AutoStories, "Studying", tint = SoftBlue, modifier = Modifier.size(16.dp))
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(session.subjectName, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = if (isDarkTheme) Color.White else CharcoalDark)
                                    Text(session.notes, fontSize = 10.sp, color = GrayColor(isDarkTheme))
                                }
                                Text(
                                    "${session.durationMinutes} mins",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = SoftBlue
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- SUB-SCREEN 5: CALENDAR INTEGRATION (Highlight exam days) ---
@Composable
fun CalendarScreen(viewModel: ExamViewModel, isDarkTheme: Boolean) {
    val allExams by viewModel.allExams.collectAsStateWithLifecycle()
    val allCategories by viewModel.allCategories.collectAsStateWithLifecycle()

    var showQuickAddDialog by remember { mutableStateOf(false) }
    var selectedDayCalendarOffset by remember { mutableStateOf(0) }

    // Fetch Days in Current June 2026 month
    val calendarDays = (1..30).toList()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "June 2026 Grid View",
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold,
            color = if (isDarkTheme) Color.White else CharcoalDark,
            textAlign = TextAlign.Center
        )
        Text(
            text = "Tap any grid date to initialize a countdown directly on that target.",
            fontSize = 12.sp,
            color = GrayColor(isDarkTheme),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Professional Calendar grid of June 2026
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(6.dp, RoundedCornerShape(20.dp)),
            colors = CardDefaults.cardColors(
                containerColor = if (isDarkTheme) CharcoalDark else Color.White
            ),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Header week labels
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    listOf("M", "T", "W", "T", "F", "S", "S").forEach { day ->
                        Text(
                            text = day,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = SoftBlue,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Days Grid
                LazyVerticalGrid(
                    columns = GridCells.Fixed(7),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Empty spaces offset for calendar start day
                    items(0) { _ -> Box(modifier = Modifier.aspectRatio(1f)) }

                    items(calendarDays) { dayNumber ->
                        // Detect if day has exam scheduled (Rough estimate simulation helper)
                        val daysMsOffset = (dayNumber - 22) * 86400000L // current local June 22 start
                        val hasExam = allExams.any { exam ->
                            val diffDays = (exam.dateTime - System.currentTimeMillis()) / 86400000
                            diffDays.toInt() == dayNumber - 22
                        }

                        val isToday = dayNumber == 22

                        Box(
                            modifier = Modifier
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (isToday) SoftBlue else if (hasExam) AccentPink.copy(alpha = 0.15f) else Color.Transparent
                                )
                                .border(
                                    1.dp,
                                    if (hasExam) AccentPink else Color.LightGray.copy(alpha = 0.2f),
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable {
                                    selectedDayCalendarOffset = dayNumber
                                    showQuickAddDialog = true
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = dayNumber.toString(),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isToday) Color.White else if (hasExam) AccentPink else if (isDarkTheme) Color.White else CharcoalDark
                                )
                                if (hasExam) {
                                    Box(
                                        modifier = Modifier
                                            .size(4.dp)
                                            .clip(CircleShape)
                                            .background(AccentPink)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Schedule details list for calendar day
        Text(
            text = "Active Deadlines Scheduled",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.Start)
        )
        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(modifier = Modifier.weight(1f)) {
            items(allExams) { exam ->
                val category = allCategories.find { it.id == exam.categoryId }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isDarkTheme) CharcoalDark else Color.White)
                        .border(
                            1.dp,
                            CardBorderColor.copy(alpha = 0.5f),
                            RoundedCornerShape(12.dp)
                        )
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(parseColor(category?.colorHex ?: "#94A3B8"))
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(exam.title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = if (isDarkTheme) Color.White else CharcoalDark)
                        Text(formatDateTime(exam.dateTime), fontSize = 11.sp, color = GrayColor(isDarkTheme))
                    }
                    Text(
                        text = "Prepared: ${exam.preparationProgress}%",
                        fontSize = 11.sp,
                        color = SoftBlue,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }

    if (showQuickAddDialog) {
        Dialog(onDismissRequest = { showQuickAddDialog = false }) {
            Card(shape = RoundedCornerShape(20.dp)) {
                var title by remember { mutableStateOf("") }
                var selectedCat by remember { mutableStateOf(allCategories.firstOrNull()?.id ?: 1) }

                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Add Exam for June $selectedDayCalendarOffset, 2026", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Exam Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        TextButton(onClick = { showQuickAddDialog = false }) {
                            Text("Cancel")
                        }
                        Button(
                            onClick = {
                                val offsetDays = (selectedDayCalendarOffset - 22).toLong()
                                val epochTargetMs = System.currentTimeMillis() + (offsetDays * 86400000L)
                                viewModel.addExam(title.ifEmpty { "Calendar Task" }, epochTargetMs, selectedCat, "Added directly via Day Calendar tap.", false, 0)
                                showQuickAddDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SoftBlue)
                        ) {
                            Text("Schedule", color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

// --- SUB-SCREEN 6: ANALYTICS (Charts & Canvas Visualizers) ---
@Composable
fun AnalyticsScreen(viewModel: ExamViewModel, isDarkTheme: Boolean) {
    val allSessions by viewModel.allSessions.collectAsStateWithLifecycle()
    val allExams by viewModel.allExams.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Text(
                text = "Performance Reports",
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = if (isDarkTheme) Color.White else CharcoalDark,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Real-time logs of study hours and preparation performance levels.",
                fontSize = 12.sp,
                color = GrayColor(isDarkTheme),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp))
        }

        // Custom drawn Canvas Bar Chart for study logs (Minutes of past 5 days)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .shadow(4.dp, RoundedCornerShape(20.dp)),
                colors = CardDefaults.cardColors(
                    containerColor = if (isDarkTheme) CharcoalDark else Color.White
                ),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Daily Study Time (Minutes)", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = if (isDarkTheme) Color.White else CharcoalDark)
                    Spacer(modifier = Modifier.height(16.dp))

                    // Canvas Drawings
                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                    ) {
                        val daysData = listOf(
                            "Day -4" to 45f,
                            "Day -3" to 60f,
                            "Day -2" to 90f,
                            "Yesterday" to 120f,
                            "Today" to 55f
                        )

                        val maxVal = 140f
                        val widthGap = size.width / daysData.size
                        val barScale = size.height / maxVal

                        // Draw baseline
                        drawLine(
                            color = Color.LightGray,
                            start = Offset(0f, size.height),
                            end = Offset(size.width, size.height),
                            strokeWidth = 2f
                        )

                        daysData.forEachIndexed { idx, (label, value) ->
                            val xPos = (idx * widthGap) + (widthGap * 0.25f)
                            val barHeight = value * barScale
                            val yPos = size.height - barHeight

                            // Draw rounded bars
                            drawRect(
                                color = SoftBlue,
                                topLeft = Offset(xPos, yPos),
                                size = androidx.compose.ui.geometry.Size(widthGap * 0.5f, barHeight)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Labels mapping row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        listOf("Thu", "Fri", "Sat", "Sun", "Today").forEach { day ->
                            Text(day, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                        }
                    }
                }
            }
        }

        // Course breakdown list stats
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isDarkTheme) CharcoalDark else Color.White
                ),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, CardBorderColor.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Preparation Overview", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = if (isDarkTheme) Color.White else CharcoalDark)
                    Text("Compare individual subject progress levels", fontSize = 11.sp, color = GrayColor(isDarkTheme))
                    Spacer(modifier = Modifier.height(16.dp))

                    if (allExams.isEmpty()) {
                        Text("No exam status logged.", fontSize = 12.sp, color = Color.Gray)
                    } else {
                        allExams.forEach { exam ->
                            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                                Row(modifier = Modifier.fillMaxWidth()) {
                                    Text(exam.title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = if (isDarkTheme) Color.White else CharcoalDark)
                                    Spacer(modifier = Modifier.weight(1f))
                                    Text("${exam.preparationProgress}%", fontSize = 12.sp, fontWeight = FontWeight.Black, color = SoftBlue)
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                LinearProgressIndicator(
                                    progress = { exam.preparationProgress / 100f },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp)),
                                    color = SoftBlue,
                                    trackColor = SoftBlue.copy(alpha = 0.15f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- SUB-SCREEN 7: SECURE ADMIN CONTROL PANEL ---
@Composable
fun AdminScreen(viewModel: ExamViewModel, isDarkTheme: Boolean) {
    val allUsers by viewModel.allUsers.collectAsStateWithLifecycle()
    val allNotifications by viewModel.allNotifications.collectAsStateWithLifecycle()
    val featureToggles by viewModel.featureToggles.collectAsStateWithLifecycle()
    val systemLogs by viewModel.systemLogs.collectAsStateWithLifecycle()

    var activeAdminTab by remember { mutableStateOf("overview") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Admin title bar
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Filled.AdminPanelSettings,
                contentDescription = "Admin lock Icon",
                tint = AccentPurple,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Admin Panel Dashboard",
                fontSize = 22.sp,
                fontWeight = FontWeight.Black,
                color = if (isDarkTheme) Color.White else CharcoalDark
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Toggle Sub Navigation headers
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(
                "overview" to "Overview 📊",
                "users" to "All Users 👥",
                "controls" to "Services ⚙️"
            ).forEach { (id, label) ->
                val isSel = activeAdminTab == id
                Box(
                    modifier = Modifier
                        .clickable { activeAdminTab = id }
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isSel) AccentPurple else AccentPurple.copy(alpha = 0.11f))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = label,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSel) Color.White else AccentPurple
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Scrolling Panel switch
        when (activeAdminTab) {
            "overview" -> {
                LazyColumn(modifier = Modifier.weight(1f)) {
                    // Real-time counter widgets
                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            Card(
                                modifier = Modifier.weight(1f),
                                colors = CardDefaults.cardColors(containerColor = if (isDarkTheme) CharcoalDark else Color.White)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text("Active Sessions", fontSize = 10.sp, color = Color.Gray)
                                    Text("37 Online Today", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = if (isDarkTheme) Color.White else CharcoalDark)
                                }
                            }
                            Card(
                                modifier = Modifier.weight(1f),
                                colors = CardDefaults.cardColors(containerColor = if (isDarkTheme) CharcoalDark else Color.White)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text("Simulated DB Status", fontSize = 10.sp, color = Color.Gray)
                                    Text("Healthy & Secure", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                                }
                            }
                        }
                    }

                    item { Spacer(modifier = Modifier.height(16.dp)) }

                    // Launch broadcast announcements input block
                    item {
                        var pushTitle by remember { mutableStateOf("") }
                        var pushBody by remember { mutableStateOf("") }

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = if (isDarkTheme) CharcoalDark else Color.White),
                            border = BorderStroke(1.dp, CardBorderColor.copy(alpha = 0.5f))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Publish Core Global Announcement", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = if (isDarkTheme) Color.White else CharcoalDark)
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = pushTitle,
                                    onValueChange = { pushTitle = it },
                                    label = { Text("Announcement Title") },
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = pushBody,
                                    onValueChange = { pushBody = it },
                                    label = { Text("Detailed Body Content") },
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Button(
                                    onClick = {
                                        viewModel.sendGlobalAnnouncement(pushTitle, pushBody)
                                        pushTitle = ""
                                        pushBody = ""
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = AccentPurple),
                                    modifier = Modifier.align(Alignment.End)
                                ) {
                                    Text("Broadcast Announcement", color = Color.White)
                                }
                            }
                        }
                    }

                    item { Spacer(modifier = Modifier.height(16.dp)) }

                    // System logs diagnostic dashboard terminal
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color.Black)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color.Green))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("System Diagnostics Feed (Real-Time)", color = Color.Green, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                systemLogs.forEach { log ->
                                    Text(
                                        text = log,
                                        color = Color.LightGray,
                                        fontSize = 11.sp,
                                        modifier = Modifier.padding(vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            "users" -> {
                LazyColumn(modifier = Modifier.weight(1f)) {
                    item {
                        Text("Moderation Panel", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = GrayColor(isDarkTheme))
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    items(allUsers) { user ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isDarkTheme) CharcoalDark else Color.White)
                                .border(1.dp, CardBorderColor.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(user.displayName, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = if (isDarkTheme) Color.White else CharcoalDark)
                                Text(user.email, fontSize = 11.sp, color = Color.Gray)
                            }

                            // Ban or Unban toggle keys
                            if (user.isBanned) {
                                Button(
                                    onClick = { viewModel.unbanUser(user.id, user.displayName) },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text("Reactive Account", fontSize = 10.sp, color = Color.White)
                                }
                            } else {
                                Button(
                                    onClick = { viewModel.banUser(user.id, user.displayName) },
                                    colors = ButtonDefaults.buttonColors(containerColor = AccentPink),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text("Ban User", fontSize = 10.sp, color = Color.White)
                                }
                            }

                            Spacer(modifier = Modifier.width(6.dp))

                            IconButton(onClick = { viewModel.deleteUser(user.id, user.displayName) }) {
                                Icon(Icons.Filled.Delete, "Delete account user", tint = Color.Red.copy(alpha = 0.7f))
                            }
                        }
                    }
                }
            }

            "controls" -> {
                LazyColumn(modifier = Modifier.weight(1f)) {
                    item {
                        Text("Service Integration Handlers", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = GrayColor(isDarkTheme))
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    featureToggles.forEach { (key, isEnabled) ->
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isDarkTheme) CharcoalDark else Color.White)
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(key, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = if (isDarkTheme) Color.White else CharcoalDark)
                                    Text("Control real-time system sync actions", fontSize = 10.sp, color = Color.Gray)
                                }
                                Switch(
                                    checked = isEnabled,
                                    onCheckedChange = { next -> viewModel.setFeatureToggle(key, next) }
                                )
                            }
                        }
                    }

                    item { Spacer(modifier = Modifier.height(24.dp)) }

                    // Database copy actions
                    item {
                        Text("Database System Copy Actions", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = GrayColor(isDarkTheme))
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Button(
                                onClick = { viewModel.triggerDatabaseBackup() },
                                colors = ButtonDefaults.buttonColors(containerColor = SoftBlue),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Filled.Backup, null, tint = Color.White)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Backup SQLite", color = Color.White)
                            }

                            Button(
                                onClick = { viewModel.triggerDatabaseRestore() },
                                colors = ButtonDefaults.buttonColors(containerColor = AccentPurple),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Filled.SettingsBackupRestore, null, tint = Color.White)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Restore SQLite", color = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- SUB-SCREEN 8: USER PROFILE & LOCAL SETTINGS ---
@Composable
fun ProfileScreen(
    viewModel: ExamViewModel,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit
) {
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val allNotifications by viewModel.allNotifications.collectAsStateWithLifecycle()

    var showClearConfirmDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(4.dp, RoundedCornerShape(20.dp)),
                colors = CardDefaults.cardColors(
                    containerColor = if (isDarkTheme) CharcoalDark else Color.White
                ),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(SoftBlue),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = (currentUser?.displayName?.take(1) ?: "S").uppercase(),
                            fontSize = 32.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = currentUser?.displayName ?: "Pro Scholar",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDarkTheme) Color.White else CharcoalDark
                    )

                    Text(
                        text = currentUser?.email ?: "guest_countdown@gmail.com",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(
                                if (currentUser?.role == "ADMIN") AccentPurple.copy(alpha = 0.15f) else SoftBlue.copy(alpha = 0.15f)
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            if (currentUser?.role == "ADMIN") "ADMINISTRATIVE ACCESS" else "STANDARD USER SESSION",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (currentUser?.role == "ADMIN") AccentPurple else SoftBlue
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { viewModel.logout() },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentPink),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Log Out Session", color = Color.White)
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }

        // Settings Section
        item {
            Text("Local System Settings", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = if (isDarkTheme) Color.White else CharcoalDark)
            Spacer(modifier = Modifier.height(12.dp))
        }

        item {
            ListItem(
                headlineContent = { Text("Vibrant Dark Mode", fontSize = 14.sp, fontWeight = FontWeight.Bold) },
                supportingContent = { Text("Toggle slate dark atmosphere theme", fontSize = 11.sp) },
                trailingContent = {
                    Switch(checked = isDarkTheme, onCheckedChange = { onToggleTheme() })
                },
                colors = ListItemDefaults.colors(containerColor = Color.Transparent)
            )
        }

        item {
            ListItem(
                headlineContent = { Text("Push Alert Warnings", fontSize = 14.sp, fontWeight = FontWeight.Bold) },
                supportingContent = { Text("Enabled local daily exam approaching alarms", fontSize = 11.sp) },
                trailingContent = {
                    Switch(checked = true, onCheckedChange = { })
                },
                colors = ListItemDefaults.colors(containerColor = Color.Transparent)
            )
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }

        // List of Broadcast Notifications of Countdown App
        item {
            Text("Advisory announcement log", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = if (isDarkTheme) Color.White else CharcoalDark)
            Spacer(modifier = Modifier.height(8.dp))
        }

        if (allNotifications.isEmpty()) {
            item {
                Text(
                    "Welcome! Your logs are clear.",
                    color = Color.Gray,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(16.dp)
                )
            }
        } else {
            items(allNotifications) { notif ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isDarkTheme) CharcoalDark else Color.White)
                        .border(1.dp, CardBorderColor.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(notif.title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = if (isDarkTheme) Color.White else CharcoalDark)
                        Text(notif.message, fontSize = 11.sp, color = GrayColor(isDarkTheme))
                    }
                    IconButton(onClick = { viewModel.deleteNotification(notif.id) }) {
                        Icon(Icons.Filled.Close, "Dismiss alert log", modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}

// Accent/Utility styles
@Composable
fun GrayColor(isDark: Boolean): Color {
    return if (isDark) Color.LightGray else Color.Gray
}

fun calculateStreak(sessions: List<StudySessionEntity>): Int {
    if (sessions.isEmpty()) return 0
    
    // Extract unique days of activity
    val activeDays = sessions.map { session ->
        val cal = Calendar.getInstance().apply { timeInMillis = session.date }
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        cal.timeInMillis
    }.distinct().sortedDescending() // newest first

    if (activeDays.isEmpty()) return 0

    val todayCal = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    val todayMs = todayCal.timeInMillis

    val yesterdayCal = Calendar.getInstance().apply {
        timeInMillis = todayMs
        add(Calendar.DAY_OF_YEAR, -1)
    }
    val yesterdayMs = yesterdayCal.timeInMillis

    // The streak is active if there is a session today or yesterday
    val latestActiveMs = activeDays.first()
    if (latestActiveMs < yesterdayMs) {
        return 0
    }

    var streak = 0
    var currentDayMs = latestActiveMs

    // Count consecutive days going backwards
    for (dayMs in activeDays) {
        if (dayMs == currentDayMs) {
            streak++
            // Move to previous day
            val prevCal = Calendar.getInstance().apply {
                timeInMillis = currentDayMs
                add(Calendar.DAY_OF_YEAR, -1)
            }
            currentDayMs = prevCal.timeInMillis
        } else if (dayMs < currentDayMs) {
            // Gap found, streak broken
            break
        }
    }
    return streak
}

@Composable
fun StudyStreakHeatmap(sessions: List<StudySessionEntity>, isDarkTheme: Boolean) {
    // Unique days grouped
    val sessionsByDay = remember(sessions) {
        sessions.groupBy { session ->
            val cal = Calendar.getInstance().apply { timeInMillis = session.date }
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            cal.timeInMillis
        }.mapValues { entry ->
            entry.value.sumOf { it.durationMinutes }
        }
    }

    // Determine current Monday
    val today = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    val dayOfWeek = today.get(Calendar.DAY_OF_WEEK)
    val daysFromMonday = when (dayOfWeek) {
        Calendar.MONDAY -> 0
        Calendar.TUESDAY -> 1
        Calendar.WEDNESDAY -> 2
        Calendar.THURSDAY -> 3
        Calendar.FRIDAY -> 4
        Calendar.SATURDAY -> 5
        Calendar.SUNDAY -> 6
        else -> 0
    }
    val currentWeekMonday = today.clone() as Calendar
    currentWeekMonday.add(Calendar.DAY_OF_YEAR, -daysFromMonday)

    // Let's show last 14 weeks (98 days)
    val numWeeks = 14

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
            .testTag("study_streak_heatmap"),
        colors = CardDefaults.cardColors(
            containerColor = if (isDarkTheme) Color(0xFF1E293B) else Color.White
        ),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, if (isDarkTheme) Color(0xFF334155) else Color(0xFFF1F5F9))
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Header Row: Title and current streak badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "STUDY CONSISTENCY",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4F46E5),
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Consistency Heatmap",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDarkTheme) Color.White else Color(0xFF0F172A)
                    )
                }

                // Streak pill
                val streak = calculateStreak(sessions)
                Box(
                    modifier = Modifier
                        .background(
                            Brush.linearGradient(listOf(Color(0xFF4F46E5), Color(0xFF7C3AED))),
                            RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "⚡",
                            fontSize = 12.sp,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "$streak DAY STREAK",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Scrollable Heatmap Grid container
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                verticalAlignment = Alignment.Top
            ) {
                // Day Labels Column (M, W, F, S)
                Column(
                    modifier = Modifier.padding(end = 8.dp, top = 20.dp), // align with cells below month labels
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val dayLabels = listOf("M", "T", "W", "T", "F", "S", "S")
                    dayLabels.forEachIndexed { idx, label ->
                        // Only show M, W, F, S for cleaner look, other rows blank or small size
                        val isVisible = idx % 2 == 0
                        Box(
                            modifier = Modifier.height(14.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Text(
                                text = if (isVisible) label else "",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDarkTheme) Color(0xFF64748B) else Color(0xFF94A3B8)
                            )
                        }
                    }
                }

                // Month Columns + Grid Box Columns
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    for (col in 0 until numWeeks) {
                        val weekMonday = currentWeekMonday.clone() as Calendar
                        weekMonday.add(Calendar.DAY_OF_YEAR, -(numWeeks - 1 - col) * 7)

                        Column(horizontalAlignment = Alignment.Start) {
                            // Month Label
                            val isFirstOfSearch = col == 0 || {
                                val prevMonday = currentWeekMonday.clone() as Calendar
                                prevMonday.add(Calendar.DAY_OF_YEAR, -(numWeeks - 1 - (col - 1)) * 7)
                                prevMonday.get(Calendar.MONTH) != weekMonday.get(Calendar.MONTH)
                            }()

                            Box(
                                modifier = Modifier.height(16.dp),
                                contentAlignment = Alignment.BottomStart
                            ) {
                                if (isFirstOfSearch) {
                                    val monthStr = SimpleDateFormat("MMM", Locale.getDefault()).format(weekMonday.time)
                                    Text(
                                        text = monthStr,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isDarkTheme) Color(0xFF94A3B8) else Color(0xFF64748B)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            // 7 Row cells for this week
                            for (row in 0 until 7) {
                                val cellCal = weekMonday.clone() as Calendar
                                cellCal.add(Calendar.DAY_OF_YEAR, row)
                                val cellTimeMs = cellCal.timeInMillis
                                val minutesStudied = sessionsByDay[cellTimeMs] ?: 0

                                // Color based on minutes
                                val cellColor = when {
                                    minutesStudied == 0 -> if (isDarkTheme) Color(0xFF1E293B) else Color(0xFFF1F5F9)
                                    minutesStudied < 15 -> if (isDarkTheme) Color(0xFF312E81) else Color(0xFFC7D2FE)
                                    minutesStudied < 45 -> if (isDarkTheme) Color(0xFF4338CA) else Color(0xFF818CF8)
                                    else -> if (isDarkTheme) Color(0xFF22D3EE) else Color(0xFF06B6D4)
                                }

                                // Border if today
                                val isCellToday = cellTimeMs == today.timeInMillis
                                val borderStroke = if (isCellToday) {
                                    BorderStroke(1.dp, if (isDarkTheme) Color.White else Color(0xFF0F172A))
                                } else {
                                    null
                                }

                                Box(
                                    modifier = Modifier
                                        .padding(vertical = 2.dp)
                                        .size(14.dp)
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(cellColor)
                                        .then(if (borderStroke != null) Modifier.border(borderStroke, RoundedCornerShape(3.dp)) else Modifier)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Legend at bottom
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Less",
                    fontSize = 10.sp,
                    color = if (isDarkTheme) Color(0xFF64748B) else Color(0xFF94A3B8)
                )
                Spacer(modifier = Modifier.width(4.dp))
                listOf(0, 10, 30, 60).forEach { mins ->
                    val color = when {
                        mins == 0 -> if (isDarkTheme) Color(0xFF1E293B) else Color(0xFFF1F5F9)
                        mins < 15 -> if (isDarkTheme) Color(0xFF312E81) else Color(0xFFC7D2FE)
                        mins < 45 -> if (isDarkTheme) Color(0xFF4338CA) else Color(0xFF818CF8)
                        else -> if (isDarkTheme) Color(0xFF22D3EE) else Color(0xFF06B6D4)
                    }
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 2.dp)
                            .size(10.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(color)
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "More",
                    fontSize = 10.sp,
                    color = if (isDarkTheme) Color(0xFF64748B) else Color(0xFF94A3B8)
                )
            }
        }
    }
}

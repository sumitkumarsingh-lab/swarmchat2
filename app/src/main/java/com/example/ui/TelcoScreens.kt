package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ChatMessage
import kotlinx.coroutines.launch

// Geometric Balance Design Color Suite (Material 3 Baseline Integration)
val GeoBackground = Color(0xFFF6F2F7)       // Light Lavender/Slate Grey backing
val GeoOnBackground = Color(0xFF1C1B1F)     // Dark charcoal text
val GeoSecondaryText = Color(0xFF49454F)    // Standard body slate gray text
val GeoPrimaryPurple = Color(0xFF6750A4)    // Active command violet tone
val GeoPurpleContainer = Color(0xFFEADDFF)  // Light violet highlights
val GeoOnPurpleContainer = Color(0xFF21005D)// Dark purple text decoration
val GeoCardBackground = Color(0xFFFEF7FF)   // Container card shade
val GeoOutlineBorder = Color(0xFFCAC4D0)    // High contrast divider outline
val GeoWhite = Color(0xFFFFFFFF)

/**
 * Root orchestrator routing to Login, Register or Swarm Chat Dashboard based on active session state.
 */
@Composable
fun TelcoAppContent(viewModel: TelcoViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(GeoBackground)
    ) {
        Crossfade(targetState = currentScreen, label = "ScreenTransition") { screen ->
            when (screen) {
                AppScreen.LOGIN -> LoginScreen(viewModel)
                AppScreen.REGISTRATION -> RegistrationScreen(viewModel)
                AppScreen.DASHBOARD -> DashboardScreen(viewModel)
            }
        }

        // Floating global error notification bar - styled with Material 3 warning theme tones
        errorMessage?.let { errorText ->
            Snackbar(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
                    .windowInsetsPadding(WindowInsets.navigationBars),
                action = {
                    TextButton(onClick = { viewModel.clearError() }) {
                        Text("Dismiss", color = Color(0xFFFFB4AB))
                    }
                },
                containerColor = Color(0xFF410E0B), // Material 3 deep red error background
                contentColor = Color(0xFFF9DEDC)
            ) {
                Text(errorText, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

/**
 * 1. Login/Authentication Screen supporting fast diagnostic account search
 */
@Composable
fun LoginScreen(viewModel: TelcoViewModel) {
    val searchParam by viewModel.loginSearchParam.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isHealthy by viewModel.isHealthy.collectAsState()
    val baseUrl by viewModel.baseUrlState.collectAsState()

    var showConfig by remember { mutableStateOf(false) }
    var tempUrl by remember(baseUrl) { mutableStateOf(baseUrl) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
            .statusBarsPadding()
            .navigationBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Branding Banner
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(GeoPrimaryPurple)
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Share,
                contentDescription = "Swarm Logo",
                tint = GeoWhite,
                modifier = Modifier.size(36.dp)
            )
        }
        
        Text(
            text = "MultiAgent CXM",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 0.5.sp,
                color = GeoOnBackground
            ),
            modifier = Modifier.padding(top = 16.dp),
            textAlign = TextAlign.Center
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
        ) {
            Text(
                text = "Resolution,Retention & Registration",
                fontSize = 10.sp,
                color = GeoPrimaryPurple,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.5.sp
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "• DES",
                fontSize = 10.sp,
                color = GeoSecondaryText,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }

        // API Connection status chip
        Surface(
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, if (isHealthy) Color(0xFFB2F5EA) else Color(0xFFFFC9C9)),
            color = if (isHealthy) Color(0xFFE6FFFA) else Color(0xFFFFF5F5),
            modifier = Modifier
                .padding(bottom = 24.dp)
                .clickable { viewModel.pingServer() }
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(if (isHealthy) Color(0xFF319795) else Color(0xFFE53E3E))
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isHealthy) "Welcome to AI driven CXM" else "GATEWAY UNREACHABLE (TAP TO RETRY)",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (isHealthy) Color(0xFF234E52) else Color(0xFF742A2A)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Refresh",
                    tint = if (isHealthy) Color(0xFF234E52) else Color(0xFF742A2A),
                    modifier = Modifier.size(12.dp)
                )
            }
        }

        // Credentials Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = GeoCardBackground),
            shape = RoundedCornerShape(28.dp), // Distinct Geometric M3 Shape
            border = BorderStroke(1.dp, GeoOutlineBorder),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp) // Flat bordered geometric style
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    text = "Login to Chat",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = GeoOnBackground,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                OutlinedTextField(
                    value = searchParam,
                    onValueChange = { viewModel.loginSearchParam.value = it },
                    label = { Text("Enter your name/4 digit ID", color = GeoSecondaryText) },
                    placeholder = { Text("e.g., Sumit Kumar or ACC123", color = Color(0xFF938F99)) },
                    leadingIcon = { Icon(Icons.Default.AccountCircle, "User Icon", tint = GeoPrimaryPurple) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = GeoOnBackground,
                        unfocusedTextColor = GeoOnBackground,
                        focusedBorderColor = GeoPrimaryPurple,
                        unfocusedBorderColor = GeoOutlineBorder,
                        focusedLabelColor = GeoPrimaryPurple,
                        unfocusedLabelColor = GeoSecondaryText
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("login_input"),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { viewModel.loginUser() })
                )

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = { viewModel.loginUser() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .testTag("login_submit_btn"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GeoPrimaryPurple,
                        contentColor = GeoWhite
                    ),
                    enabled = !isLoading,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = GeoWhite, strokeWidth = 2.dp)
                    } else {
                        Text(
                          text = "Start Swarm",
                          fontWeight = FontWeight.Bold,
                          fontSize = 15.sp,
                          letterSpacing = 0.1.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Quick register redirection button - Clean rounded geometric border button
        OutlinedButton(
            onClick = { viewModel.navigateTo(AppScreen.REGISTRATION) },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .testTag("go_to_registration_btn"),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = GeoOnPurpleContainer),
            border = BorderStroke(1.dp, GeoPrimaryPurple.copy(alpha = 0.5f)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(Icons.Default.Person, contentDescription = "New Account", modifier = Modifier.size(18.dp), tint = GeoPrimaryPurple)
            Spacer(modifier = Modifier.width(8.dp))
            Text("New User Registration", fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Collapsible Advanced Config Options
        Row(
            modifier = Modifier
                .clickable { showConfig = !showConfig }
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Config Icon",
                tint = GeoSecondaryText,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Device Configuration & IP Swaps",
                color = GeoSecondaryText,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
            Icon(
                imageVector = if (showConfig) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = "Expand Indicator",
                tint = GeoSecondaryText,
                modifier = Modifier.size(16.dp)
            )
        }

        AnimatedVisibility(
            visible = showConfig,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
                colors = CardDefaults.cardColors(containerColor = GeoCardBackground),
                border = BorderStroke(1.dp, GeoOutlineBorder),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "FastAPI Target Gateway Address",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = GeoOnBackground,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    Text(
                        text = "Customize the endpoint so Android Retrofit can reach Render cloud server instances.",
                        fontSize = 11.sp,
                        color = GeoSecondaryText,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    OutlinedTextField(
                        value = tempUrl,
                        onValueChange = { tempUrl = it },
                        label = { Text("Base Gateway URL", color = GeoSecondaryText) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = GeoOnBackground,
                            unfocusedTextColor = GeoOnBackground,
                            focusedBorderColor = GeoPrimaryPurple,
                            unfocusedBorderColor = GeoOutlineBorder
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(
                            onClick = {
                                tempUrl = "https://telco-swarm-backend.onrender.com/"
                                viewModel.updateBaseUrl("https://telco-swarm-backend.onrender.com/")
                            }
                        ) {
                            Text("Reset Localhost", color = GeoPrimaryPurple, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = { viewModel.updateBaseUrl(tempUrl) },
                            colors = ButtonDefaults.buttonColors(containerColor = GeoPrimaryPurple),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Save Address", color = GeoWhite, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

/**
 * Helper import for scrolling columns.
 */
@Composable
fun rememberScrollState() = androidx.compose.foundation.rememberScrollState()

/**
 * 2. New User Registration view for corporate profile generation
 */
@Composable
fun RegistrationScreen(viewModel: TelcoViewModel) {
    val name by viewModel.regName.collectAsState()
    val phone by viewModel.regPhone.collectAsState()
    val email by viewModel.regEmail.collectAsState()
    val planTier by viewModel.regPlanTier.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    val tiers = listOf("Platinum", "Gold", "Silver")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
            .statusBarsPadding()
            .navigationBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .align(Alignment.Start)
                .clickable { viewModel.navigateTo(AppScreen.LOGIN) }
                .padding(bottom = 24.dp)
        ) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = GeoPrimaryPurple)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Back to Authenticate", color = GeoPrimaryPurple, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }

        Text(
            text = "Create Profile",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.ExtraBold,
                color = GeoOnBackground
            )
        )

        Text(
            text = "Register clients into the Global Enterprise Vault to spin up high-speed multi-agent workflows.",
            style = MaterialTheme.typography.bodyMedium,
            color = GeoSecondaryText,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 4.dp, bottom = 28.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = GeoCardBackground),
            shape = RoundedCornerShape(28.dp),
            border = BorderStroke(1.dp, GeoOutlineBorder)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                
                // Name Input
                OutlinedTextField(
                    value = name,
                    onValueChange = { viewModel.regName.value = it },
                    label = { Text("Full Name", color = GeoSecondaryText) },
                    placeholder = { Text("e.g. Sumit Kumar", color = Color(0xFF938F99)) },
                    leadingIcon = { Icon(Icons.Default.Person, "Name", tint = GeoPrimaryPurple) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = GeoOnBackground,
                        unfocusedTextColor = GeoOnBackground,
                        focusedBorderColor = GeoPrimaryPurple,
                        unfocusedBorderColor = GeoOutlineBorder,
                        focusedLabelColor = GeoPrimaryPurple,
                        unfocusedLabelColor = GeoSecondaryText
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("reg_name_input")
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Phone Input
                OutlinedTextField(
                    value = phone,
                    onValueChange = { viewModel.regPhone.value = it },
                    label = { Text("Phone Number", color = GeoSecondaryText) },
                    placeholder = { Text("e.g. 9876543210", color = Color(0xFF938F99)) },
                    leadingIcon = { Icon(Icons.Default.Phone, "Phone", tint = GeoPrimaryPurple) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = GeoOnBackground,
                        unfocusedTextColor = GeoOnBackground,
                        focusedBorderColor = GeoPrimaryPurple,
                        unfocusedBorderColor = GeoOutlineBorder,
                        focusedLabelColor = GeoPrimaryPurple,
                        unfocusedLabelColor = GeoSecondaryText
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("reg_phone_input")
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Email Input
                OutlinedTextField(
                    value = email,
                    onValueChange = { viewModel.regEmail.value = it },
                    label = { Text("Email Address", color = GeoSecondaryText) },
                    placeholder = { Text("e.g. sumit@example.com", color = Color(0xFF938F99)) },
                    leadingIcon = { Icon(Icons.Default.Email, "Email", tint = GeoPrimaryPurple) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = GeoOnBackground,
                        unfocusedTextColor = GeoOnBackground,
                        focusedBorderColor = GeoPrimaryPurple,
                        unfocusedBorderColor = GeoOutlineBorder,
                        focusedLabelColor = GeoPrimaryPurple,
                        unfocusedLabelColor = GeoSecondaryText
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("reg_email_input")
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Plan Tier SELECTOR
                Text(
                    text = "Subscription Service Tier",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = GeoSecondaryText,
                    modifier = Modifier.padding(bottom = 8.dp),
                    letterSpacing = 0.5.sp
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    tiers.forEach { tier ->
                        val isSelected = planTier == tier
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) GeoPurpleContainer else GeoCardBackground)
                                .border(
                                    border = BorderStroke(
                                        width = if (isSelected) 2.dp else 1.dp,
                                        color = if (isSelected) GeoPrimaryPurple else GeoOutlineBorder
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable { viewModel.regPlanTier.value = tier }
                                .testTag("tier_option_$tier"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = tier,
                                color = if (isSelected) GeoOnPurpleContainer else GeoSecondaryText,
                                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                                fontSize = 13.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                Button(
                    onClick = { viewModel.registerUser() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .testTag("reg_submit_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = GeoPrimaryPurple, contentColor = GeoWhite),
                    enabled = !isLoading,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = GeoWhite, strokeWidth = 2.dp)
                    } else {
                        Text("Register Profile & Launch Workspace", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                }
            }
        }
    }
}

/**
 * 3. Primary Swarm Analytics Dashboard and Active chatbot panel
 */
@Composable
fun DashboardScreen(viewModel: TelcoViewModel) {
    val user by viewModel.currentUser.collectAsState()
    val activeAgent by viewModel.activeAgent.collectAsState()
    val isHealthy by viewModel.isHealthy.collectAsState()
    val latencyText by viewModel.latencyText.collectAsState()
    val opsCount by viewModel.operationsCount.collectAsState()
    val chatHistory by viewModel.chatMessages.collectAsState()
    val chatText by viewModel.chatInputText.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    val coroutineScope = rememberCoroutineScope()
    val chatListState = rememberLazyListState()
    val keyboardController = LocalSoftwareKeyboardController.current

    // Scroll chat to the base whenever new message appends
    LaunchedEffect(chatHistory.size) {
        if (chatHistory.isNotEmpty()) {
            coroutineScope.launch {
                chatListState.animateScrollToItem(chatHistory.size - 1)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // Upper Dashboard Title Header
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = GeoCardBackground),
            shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp),
            border = BorderStroke(1.dp, GeoOutlineBorder),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Swarm Control Monitor",
                            fontWeight = FontWeight.ExtraBold,
                            color = GeoOnBackground,
                            fontSize = 20.sp
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 2.dp)
                        ) {
                            Text(
                                text = user?.name ?: "Guest Support",
                                fontWeight = FontWeight.Bold,
                                color = GeoPrimaryPurple,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            // User Plan subscription badge
                            user?.let { u ->
                                Surface(
                                    color = when (u.tier.lowercase()) {
                                        "platinum" -> Color(0xFFEADDFF) // light purple
                                        "gold" -> Color(0xFFFEF08A)     // soft yellow
                                        else -> Color(0xFFE2E8F0)       // soft gray
                                    },
                                    border = BorderStroke(
                                        width = 1.dp,
                                        color = when (u.tier.lowercase()) {
                                            "platinum" -> Color(0xFFD0BCFF)
                                            "gold" -> Color(0xFFFDE047)
                                            else -> Color(0xFFCBD5E1)
                                        }
                                    ),
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier.padding(horizontal = 2.dp)
                                ) {
                                    Text(
                                        text = u.tier.uppercase(),
                                        fontWeight = FontWeight.Black,
                                        fontSize = 10.sp,
                                        color = when (u.tier.lowercase()) {
                                            "platinum" -> Color(0xFF21005D)
                                            "gold" -> Color(0xFF713F12)
                                            else -> Color(0xFF334155)
                                        },
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Logout trigger
                    IconButton(
                        onClick = { viewModel.logout() },
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFF9DEDC))
                            .border(BorderStroke(1.dp, Color(0xFFF2B8B5)), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Log out",
                            tint = Color(0xFF601410)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Horizontally grouped activity metrics (Perfect match to the provided design grid specs)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Gateway Health Metric Box
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(GeoWhite)
                            .border(BorderStroke(1.dp, GeoOutlineBorder), RoundedCornerShape(16.dp))
                            .clickable { viewModel.pingServer() }
                            .padding(12.dp)
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(if (isHealthy) Color(0xFF319795) else Color(0xFFE53E3E))
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("SYSTEM HEALTH", color = GeoSecondaryText, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (isHealthy) "Online" else "Offline",
                                color = if (isHealthy) Color(0xFF234E52) else Color(0xFF742A2A),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }

                    // Response Speed Delay Box
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(GeoWhite)
                            .border(BorderStroke(1.dp, GeoOutlineBorder), RoundedCornerShape(16.dp))
                            .padding(12.dp)
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Info, "Latency info", tint = GeoPrimaryPurple, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("LATENCY SPEED", color = GeoSecondaryText, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = latencyText,
                                color = GeoOnBackground,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }

                    // Operations Metrics Box
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(GeoWhite)
                            .border(BorderStroke(1.dp, GeoOutlineBorder), RoundedCornerShape(16.dp))
                            .padding(12.dp)
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.List, "Operations count", tint = Color(0xFFB69DF8), modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("OPERATIONS", color = GeoSecondaryText, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "$opsCount runs",
                                color = GeoOnBackground,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Active Agent Status Information Area
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            colors = CardDefaults.cardColors(containerColor = GeoPurpleContainer),
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.dp, Color(0xFFD0BCFF)),
            elevation = CardDefaults.cardElevation(0.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Agent specific dynamic visual avatar mapping with clean geometric outlines
                val agentColor = when (activeAgent.lowercase().trim()) {
                    "triage supervisor" -> Color(0xFF005DB4)  // Royal Sky
                    "technical support" -> Color(0xFF8F4E00)  // Deep Amber
                    "sales specialist" -> Color(0xFF006E3A)   // Emerald Green
                    "loyalty squad" -> Color(0xFF8C0094)      // Orchid Pink
                    else -> GeoSecondaryText
                }
                
                val agentIcon = when (activeAgent.lowercase().trim()) {
                    "triage supervisor" -> Icons.Default.Face
                    "technical support" -> Icons.Default.Build
                    "sales specialist" -> Icons.Default.ShoppingCart
                    "loyalty squad" -> Icons.Default.Star
                    else -> Icons.Default.Info
                }

                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(GeoWhite)
                        .border(BorderStroke(1.5.dp, agentColor), RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = agentIcon, contentDescription = "Agent", tint = agentColor, modifier = Modifier.size(24.dp))
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "ACTIVE SWARM HANDLER",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            color = GeoOnPurpleContainer.copy(alpha = 0.7f),
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(if (isLoading) Color(0xFFFB923C) else Color(0xFF22C55E))
                        )
                    }
                    Text(
                        text = activeAgent,
                        color = GeoOnPurpleContainer,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = when (activeAgent.lowercase().trim()) {
                            "triage supervisor" -> "Analyzing requirements to route to specialists."
                            "technical support" -> "Fixing connection issue and submitting ServiceNow logs."
                            "sales specialist" -> "Providing guidance on enterprise cloud contracts."
                            "loyalty squad" -> "Applying loyalty adjustments up to 20% discount."
                            else -> "Awaiting operator conversational parameters..."
                        },
                        fontSize = 11.sp,
                        color = GeoOnPurpleContainer.copy(alpha = 0.8f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Large Chat conversation screen logs - Styled with geometric clean borders & background
        Surface(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(24.dp))
                .border(BorderStroke(1.dp, GeoOutlineBorder), RoundedCornerShape(24.dp)),
            color = GeoWhite
        ) {
            if (chatHistory.isEmpty()) {
                // Empty support state with clean modern graphical elements
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(GeoPurpleContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Empty message log icon",
                            tint = GeoPrimaryPurple,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Conversational Stream Offline",
                        fontWeight = FontWeight.Bold,
                        color = GeoOnBackground,
                        fontSize = 16.sp
                    )
                    Text(
                        text = "Initialize communication variables instantly down below to start conversational multi-agent evaluation.",
                        color = GeoSecondaryText,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Button(
                        onClick = {
                            viewModel.chatInputText.value = "Hello! I have some questions regarding my network connection today."
                            viewModel.sendChatMessage()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = GeoPrimaryPurple),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.testTag("start_chat_now_quick_btn")
                    ) {
                        Text("Start Chat Session", fontWeight = FontWeight.Bold, color = GeoWhite)
                    }
                }
            } else {
                LazyColumn(
                    state = chatListState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(chatHistory) { message ->
                        val isUser = message.role.lowercase() == "user"
                        ChatBubbleItem(message, isUser)
                    }
                }
            }
        }

        // Bottom text message entry container (clean geometric border styling)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = chatText,
                onValueChange = { viewModel.chatInputText.value = it },
                placeholder = { Text("Ask technical support or ask sales info...", color = Color(0xFF938F99)) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = GeoOnBackground,
                    unfocusedTextColor = GeoOnBackground,
                    focusedBorderColor = GeoPrimaryPurple,
                    unfocusedBorderColor = GeoOutlineBorder,
                    focusedContainerColor = GeoCardBackground,
                    unfocusedContainerColor = GeoCardBackground
                ),
                shape = RoundedCornerShape(16.dp),
                maxLines = 4,
                modifier = Modifier
                    .weight(1f)
                    .testTag("chat_bottom_input"),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = {
                    viewModel.sendChatMessage()
                    keyboardController?.hide()
                })
            )

            IconButton(
                onClick = {
                    viewModel.sendChatMessage()
                    keyboardController?.hide()
                },
                enabled = chatText.isNotBlank() && !isLoading,
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (chatText.isNotBlank()) GeoPrimaryPurple else GeoOutlineBorder.copy(alpha = 0.5f))
                    .testTag("chat_bottom_send_btn")
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = GeoWhite, strokeWidth = 2.dp)
                } else {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Send",
                        tint = if (chatText.isNotBlank()) GeoWhite else GeoSecondaryText
                    )
                }
            }
        }

        // Dynamic Loading Stripe
        if (isLoading && chatHistory.isNotEmpty()) {
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth().height(3.dp),
                color = GeoPrimaryPurple,
                trackColor = GeoPurpleContainer
            )
        }
    }
}

/**
 * Clean helper function converting standard pixels to DP for exact line scaling in Compose Canvas loops.
 */
@Composable
fun Int.pxToDp() = with(androidx.compose.ui.platform.LocalDensity.current) { this@pxToDp.toDp() }

/**
 * Support chat chat bubbles item bubble
 */
@Composable
fun ChatBubbleItem(message: ChatMessage, isUser: Boolean) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        contentAlignment = if (isUser) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Card(
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isUser) 16.dp else 4.dp,
                bottomEnd = if (isUser) 4.dp else 16.dp
            ),
            colors = CardDefaults.cardColors(
                containerColor = if (isUser) GeoPurpleContainer else GeoCardBackground
            ),
            border = BorderStroke(1.dp, if (isUser) Color(0xFFD0BCFF) else GeoOutlineBorder),
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = if (isUser) "You" else "Swarm Agent",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (isUser) GeoOnPurpleContainer else GeoPrimaryPurple,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = message.content,
                    color = if (isUser) GeoOnPurpleContainer else GeoOnBackground,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
            }
        }
    }
}

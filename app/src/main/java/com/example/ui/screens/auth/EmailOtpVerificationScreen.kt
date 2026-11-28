package com.example.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.MarkEmailRead
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.EmailOtpService
import com.example.core.OtpSendResult
import com.example.core.OtpVerifyResult
import com.example.ui.theme.Gold500
import com.example.ui.theme.Navy700
import com.example.ui.theme.Navy800
import com.example.ui.theme.Navy900
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun EmailOtpVerificationScreen(
    email: String,
    isBootstrappedAdmin: Boolean,
    onVerified: () -> Unit,
    onSignOut: () -> Unit,
    autoSendOnLaunch: Boolean = true,
    sendOtp: suspend () -> OtpSendResult = { EmailOtpService.sendLoginOtp() },
    verifyOtp: suspend (String) -> OtpVerifyResult = { EmailOtpService.verifyLoginOtp(it) }
) {
    val scope = rememberCoroutineScope()
    val maskedEmail = remember(email) { EmailOtpService.maskEmail(email) }

    var codeInput by remember { mutableStateOf("") }
    var isSending by remember { mutableStateOf(false) }
    var isVerifying by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var cooldownSeconds by remember { mutableIntStateOf(0) }
    var isFunctionNotDeployed by remember { mutableStateOf(false) }

    suspend fun triggerSendOtp() {
        isSending = true
        errorMessage = null
        isFunctionNotDeployed = false
        when (val res = sendOtp()) {
            is OtpSendResult.Sent -> {
                statusMessage = "আপনার ইমেইলে ($maskedEmail) ৫ সংখ্যার ভেরিফিকেশন কোড পাঠানো হয়েছে।"
                cooldownSeconds = 60
            }
            is OtpSendResult.Cooldown -> {
                statusMessage = "আগে পাঠানো কোডটি এখনো সক্রিয় আছে।"
                errorMessage = res.message
                cooldownSeconds = res.cooldownSeconds
            }
            is OtpSendResult.ServiceUnavailable -> {
                isFunctionNotDeployed = res.isFunctionNotDeployed
                errorMessage = res.message
            }
            is OtpSendResult.Error -> {
                errorMessage = res.message
            }
        }
        isSending = false
    }

    LaunchedEffect(email, autoSendOnLaunch) {
        if (autoSendOnLaunch && email.isNotBlank()) {
            triggerSendOtp()
        }
    }

    LaunchedEffect(cooldownSeconds) {
        if (cooldownSeconds > 0) {
            delay(1000L)
            cooldownSeconds -= 1
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 500.dp)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(Brush.linearGradient(listOf(Navy900, Navy700)))
                    .padding(24.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        color = Gold500.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.size(64.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.MarkEmailRead,
                                contentDescription = "Email OTP Verification",
                                tint = Gold500,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                    Text(
                        text = "ইমেইল ভেরিফিকেশন (৫ সংখ্যা)",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = if (maskedEmail.isNotBlank()) {
                            "লগইন সম্পন্ন করতে $maskedEmail ঠিকানায় পাঠানো ৫ সংখ্যার কোড লিখুন"
                        } else {
                            "লগইন সম্পন্ন করতে আপনার ইমেইলে পাঠানো ৫ সংখ্যার কোড লিখুন"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.88f),
                        textAlign = TextAlign.Center
                    )
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (!statusMessage.isNullOrBlank()) {
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = statusMessage.orEmpty(),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }

                    if (!errorMessage.isNullOrBlank()) {
                        Surface(
                            color = MaterialTheme.colorScheme.errorContainer,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("email_otp_error_banner")
                        ) {
                            Text(
                                text = errorMessage.orEmpty(),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }

                    OutlinedTextField(
                        value = codeInput,
                        onValueChange = { next ->
                            val digitsOnly = next.filter { it.isDigit() }.take(5)
                            codeInput = digitsOnly
                        },
                        label = { Text("৫ সংখ্যার ভেরিফিকেশন কোড") },
                        placeholder = { Text("12345") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        textStyle = MaterialTheme.typography.headlineSmall.copy(
                            letterSpacing = 6.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("email_otp_input")
                    )

                    Button(
                        onClick = {
                            scope.launch {
                                isVerifying = true
                                errorMessage = null
                                when (val res = verifyOtp(codeInput)) {
                                    is OtpVerifyResult.Verified -> {
                                        onVerified()
                                    }
                                    is OtpVerifyResult.InvalidCode -> {
                                        errorMessage = res.message
                                    }
                                    is OtpVerifyResult.ServiceUnavailable -> {
                                        isFunctionNotDeployed = res.isFunctionNotDeployed
                                        errorMessage = res.message
                                    }
                                    is OtpVerifyResult.Error -> {
                                        errorMessage = res.message
                                    }
                                }
                                isVerifying = false
                            }
                        },
                        enabled = codeInput.length == 5 && !isVerifying && !isSending,
                        colors = ButtonDefaults.buttonColors(containerColor = Navy800),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .minimumInteractiveComponentSize()
                            .testTag("email_otp_verify_button")
                    ) {
                        if (isVerifying) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("যাচাই করা হচ্ছে...", color = Color.White)
                        } else {
                            Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = Gold500)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("কোড যাচাই করে প্রবেশ করুন", color = Color.White)
                        }
                    }

                    OutlinedButton(
                        onClick = {
                            scope.launch { triggerSendOtp() }
                        },
                        enabled = !isSending && !isVerifying && cooldownSeconds == 0,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .minimumInteractiveComponentSize()
                            .testTag("email_otp_resend_button")
                    ) {
                        if (isSending) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("কোড পাঠানো হচ্ছে...")
                        } else {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                if (cooldownSeconds > 0) {
                                    "আবার কোড পাঠান (${cooldownSeconds}s)"
                                } else {
                                    "ইমেইলে ৫ সংখ্যার কোড পাঠান"
                                }
                            )
                        }
                    }

                    if (isFunctionNotDeployed && isBootstrappedAdmin) {
                        Surface(
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Shield,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = "অ্যাডমিন সেটআপ নোটিশ",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Text(
                                    text = "Cloud Functions-এ sendLoginOtp ও verifyLoginOtp ডিপ্লয় না হওয়া পর্যন্ত শুধুমাত্র অ্যাডমিন প্রবেশ করে ড্যাশবোর্ড থেকে নীতি নিয়ন্ত্রণ করতে পারবেন।",
                                    style = MaterialTheme.typography.bodySmall
                                )
                                OutlinedButton(
                                    onClick = onVerified,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .minimumInteractiveComponentSize()
                                        .testTag("email_otp_admin_bypass_button")
                                ) {
                                    Text("আপাতত অ্যাডমিন হিসেবে প্রবেশ করুন")
                                }
                            }
                        }
                    }

                    TextButton(
                        onClick = onSignOut,
                        modifier = Modifier
                            .fillMaxWidth()
                            .minimumInteractiveComponentSize()
                            .testTag("email_otp_signout_button")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("অন্য Google অ্যাকাউন্ট ব্যবহার করুন / লগআউট")
                    }
                }
            }
        }
    }
}

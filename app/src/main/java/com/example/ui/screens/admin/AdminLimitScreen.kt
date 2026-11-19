package com.example.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.model.AiLimitConfig
import com.example.data.model.AiLimitPurchaseRequest
import com.example.data.model.UiState
import com.example.ui.components.EmptyStateView
import com.example.ui.components.ErrorStateCard
import com.example.ui.components.HonoursTopBar
import com.example.ui.components.LoadingStateBox
import com.example.ui.components.SectionHeader
import com.example.ui.theme.CrimsonError
import com.example.ui.theme.Emerald600

@Composable
fun AdminAiLimitScreen(
    config: AiLimitConfig,
    purchaseRequestsState: UiState<List<AiLimitPurchaseRequest>>,
    onBack: () -> Unit,
    onSaveConfig: (AiLimitConfig) -> Unit,
    onReviewRequest: (requestId: String, approve: Boolean, adminNote: String) -> Unit
) {
    var freeLimitText by remember(config.freeLimit) { mutableStateOf(config.freeLimit.toString()) }
    var paymentPhone by remember(config.paymentPhone) { mutableStateOf(config.paymentPhone) }
    var paymentMethods by remember(config.paymentMethods) { mutableStateOf(config.paymentMethods) }
    var paymentNote by remember(config.paymentNote) { mutableStateOf(config.paymentNote) }

    var p1Credits by remember(config.pack1Credits) { mutableStateOf(config.pack1Credits.toString()) }
    var p1Price by remember(config.pack1PriceBdt) { mutableStateOf(config.pack1PriceBdt.toString()) }
    var p2Credits by remember(config.pack2Credits) { mutableStateOf(config.pack2Credits.toString()) }
    var p2Price by remember(config.pack2PriceBdt) { mutableStateOf(config.pack2PriceBdt.toString()) }
    var p3Credits by remember(config.pack3Credits) { mutableStateOf(config.pack3Credits.toString()) }
    var p3Price by remember(config.pack3PriceBdt) { mutableStateOf(config.pack3PriceBdt.toString()) }

    var statusFilter by remember { mutableStateOf("pending") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        HonoursTopBar(
            title = "AI লিমিট ও পেমেন্ট ম্যানেজমেন্ট",
            subtitle = "Free Limit Config & Purchase Approvals",
            onBackClick = onBack
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 720.dp)
                .align(Alignment.CenterHorizontally),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        SectionHeader(
                            title = "ফ্রী লিমিট ও পেমেন্ট সেটিংস",
                            subtitle = "শিক্ষার্থীদের ফ্রী লিমিট, পেমেন্ট নম্বর ও প্যাকেজের দাম নির্ধারণ করুন"
                        )
                        OutlinedTextField(
                            value = freeLimitText,
                            onValueChange = { freeLimitText = it.filter { ch -> ch.isDigit() } },
                            label = { Text("ফ্রী AI লিমিট (ডিফল্ট: 30)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = paymentPhone,
                            onValueChange = { paymentPhone = it },
                            label = { Text("পেমেন্ট রিসিভ নম্বর (bKash/Nagad/Rocket)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = paymentMethods,
                            onValueChange = { paymentMethods = it },
                            label = { Text("পেমেন্ট মাধ্যম সমূহ") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = paymentNote,
                            onValueChange = { paymentNote = it },
                            label = { Text("পেমেন্ট নির্দেশনা") },
                            maxLines = 3,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Text("প্যাকেজ ১–৩ (ক্রেডিট ও মূল্য টাকা)", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = p1Credits,
                                onValueChange = { p1Credits = it.filter(Char::isDigit) },
                                label = { Text("প্যাক ১ ক্রেডিট") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = p1Price,
                                onValueChange = { p1Price = it.filter(Char::isDigit) },
                                label = { Text("দাম (৳)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = p2Credits,
                                onValueChange = { p2Credits = it.filter(Char::isDigit) },
                                label = { Text("প্যাক ২ ক্রেডিট") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = p2Price,
                                onValueChange = { p2Price = it.filter(Char::isDigit) },
                                label = { Text("দাম (৳)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = p3Credits,
                                onValueChange = { p3Credits = it.filter(Char::isDigit) },
                                label = { Text("প্যাক ৩ ক্রেডিট") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = p3Price,
                                onValueChange = { p3Price = it.filter(Char::isDigit) },
                                label = { Text("দাম (৳)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Button(
                            onClick = {
                                onSaveConfig(
                                    AiLimitConfig(
                                        freeLimit = freeLimitText.toLongOrNull() ?: 30L,
                                        paymentPhone = paymentPhone,
                                        paymentMethods = paymentMethods,
                                        paymentNote = paymentNote,
                                        pack1Credits = p1Credits.toLongOrNull() ?: 50L,
                                        pack1PriceBdt = p1Price.toLongOrNull() ?: 50L,
                                        pack2Credits = p2Credits.toLongOrNull() ?: 150L,
                                        pack2PriceBdt = p2Price.toLongOrNull() ?: 120L,
                                        pack3Credits = p3Credits.toLongOrNull() ?: 500L,
                                        pack3PriceBdt = p3Price.toLongOrNull() ?: 300L
                                    )
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .minimumInteractiveComponentSize()
                                .testTag("admin_save_limit_config_button")
                        ) {
                            Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("লিমিট ও প্যাকেজ সেটিংস সংরক্ষণ করুন")
                        }
                    }
                }
            }

            item {
                SectionHeader(title = "শিক্ষার্থীদের লিমিট কেনার রিকোয়েস্ট")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("pending" to "অপেক্ষমাণ", "approved" to "অনুমোদিত", "rejected" to "বাতিল", "all" to "সব")
                        .forEach { (key, label) ->
                            FilterChip(
                                selected = statusFilter == key,
                                onClick = { statusFilter = key },
                                label = { Text(label) }
                            )
                        }
                }
            }

            when (purchaseRequestsState) {
                is UiState.Loading -> item { LoadingStateBox("পেমেন্ট রিকোয়েস্ট লোড হচ্ছে...") }
                is UiState.Error -> item { ErrorStateCard(purchaseRequestsState.message) }
                is UiState.Success -> {
                    val filtered = if (statusFilter == "all") {
                        purchaseRequestsState.data
                    } else {
                        purchaseRequestsState.data.filter { it.status == statusFilter }
                    }
                    if (filtered.isEmpty()) {
                        item {
                            EmptyStateView(
                                icon = Icons.Default.AddShoppingCart,
                                title = "কোনো রিকোয়েস্ট পাওয়া যায়নি",
                                message = "শিক্ষার্থীরা লিমিট কেনার জন্য পেমেন্ট সাবমিট করলে এখানে দেখা যাবে।"
                            )
                        }
                    } else {
                        items(filtered, key = { it.id }) { req ->
                            var adminNoteInput by remember(req.id) { mutableStateOf(req.adminNote) }
                            val statusColor = when (req.status) {
                                "approved" -> Emerald600
                                "rejected" -> CrimsonError
                                else -> MaterialTheme.colorScheme.primary
                            }
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Column(
                                    modifier = Modifier.padding(14.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "${req.userName.ifBlank { "Student" }} (${req.userEmail})",
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = "${req.packageTitle} • ৳${req.priceBdt} (+${req.creditsAmount} ক্রেডিট)",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.primary,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                        Surface(
                                            color = statusColor.copy(alpha = 0.14f),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text(
                                                text = req.status.uppercase(),
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = statusColor,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                    }

                                    Text(
                                        text = "মাধ্যম: ${req.paymentMethod} | প্রেরক: ${req.senderNumber} | TrxID: ${req.transactionId}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium
                                    )

                                    if (req.status == "pending") {
                                        OutlinedTextField(
                                            value = adminNoteInput,
                                            onValueChange = { adminNoteInput = it },
                                            label = { Text("অ্যাডমিন নোট (ঐচ্ছিক)") },
                                            singleLine = true,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            Button(
                                                onClick = { onReviewRequest(req.id, true, adminNoteInput) },
                                                colors = ButtonDefaults.buttonColors(containerColor = Emerald600),
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .minimumInteractiveComponentSize()
                                                    .testTag("approve_purchase_${req.id}")
                                            ) {
                                                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text("অনুমোদন (+${req.creditsAmount})")
                                            }
                                            OutlinedButton(
                                                onClick = { onReviewRequest(req.id, false, adminNoteInput) },
                                                colors = ButtonDefaults.outlinedButtonColors(contentColor = CrimsonError),
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .minimumInteractiveComponentSize()
                                                    .testTag("reject_purchase_${req.id}")
                                            ) {
                                                Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text("বাতিল")
                                            }
                                        }
                                    } else if (req.adminNote.isNotBlank()) {
                                        Text(
                                            text = "অ্যাডমিন নোট: ${req.adminNote}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                UiState.Idle -> {}
            }
        }
    }
}

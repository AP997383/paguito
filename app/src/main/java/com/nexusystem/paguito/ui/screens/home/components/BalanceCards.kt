package com.nexusystem.paguito.ui.screens.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import com.nexusystem.paguito.data.local.entity.AbonosDelMes
import com.nexusystem.paguito.domain.data.DeudoresSummary
import com.nexusystem.paguito.utils.formatAsCurrency

@Composable
fun BalanceCarousel(
    pagosList: List<AbonosDelMes>,
    sumary1: DeudoresSummary,
    showBalance: Boolean,
    onToggleBalance: () -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { 1 })

    Column(modifier = Modifier.fillMaxWidth()) {
        HorizontalPager(
            state = pagerState,
            contentPadding = PaddingValues(horizontal = 18.dp),
            pageSpacing = 5.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            MainBalanceCard(
                title = "Saldo Pendiente Total",
                amount = formatAsCurrency(sumary1.sumaTotalMontos ?: 0.0f),
                count1 = sumary1.totalDeudores.toString(),
                label1 = "Clientes",
                count2 = "12",
                label2 = "Vencidos",
                backgroundColor = Color(0xFF1A73E8),
                icon = Icons.Outlined.AccountBalanceWallet,
                showBalance = showBalance,
                onToggleBalance = onToggleBalance
            )
        }

        Row(
            Modifier
                .height(24.dp)
                .fillMaxWidth()
                .padding(top = 12.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .padding(2.dp)
                    .clip(CircleShape)
                    .background(Color.DarkGray)
                    .size(8.dp)
            )
        }
    }
}

@Composable
fun MainBalanceCard(
    title: String,
    amount: String,
    decimals: String = "",
    count1: String,
    label1: String,
    count2: String,
    label2: String,
    backgroundColor: Color,
    icon: ImageVector,
    showBalance: Boolean,
    onToggleBalance: () -> Unit
) {
    val displayAmount = if (showBalance) amount else "$***.**"
    val displayDecimals = if (showBalance) decimals else ""
    val buttonText = if (showBalance) "Ocultar balance" else "Mostrar balance"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.12f),
                modifier = Modifier
                    .size(150.dp)
                    .align(Alignment.TopEnd)
                    .offset(x = 40.dp, y = (-30).dp)
            )

            Column(modifier = Modifier.padding(24.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(title, color = Color.White.copy(alpha = 0.9f), fontSize = 14.sp)

                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            "Actualizado",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = buildAnnotatedString {
                        withStyle(SpanStyle(fontSize = 32.sp, fontWeight = FontWeight.ExtraBold)) {
                            append(displayAmount)
                        }
                        withStyle(SpanStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold)) {
                            append(displayDecimals)
                        }
                    },
                    color = Color.White
                )

                Spacer(modifier = Modifier.weight(1f))
                HorizontalDivider(color = Color.White.copy(alpha = 0.2f))
                Spacer(modifier = Modifier.height(12.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    InfoItem(Icons.Default.Group, count1, label1)

                    Spacer(modifier = Modifier.width(16.dp))

                    VerticalDivider(
                        modifier = Modifier.height(14.dp),
                        color = Color.White.copy(alpha = 0.4f)
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(5.dp))
                            .background(Color.White.copy(alpha = 0.2f))
                            .clickable { onToggleBalance() }
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = buttonText,
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun InfoItem(
    icon: ImageVector,
    count: String,
    label: String
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = Color.White, modifier = Modifier.size(14.dp))

        Spacer(modifier = Modifier.width(6.dp))

        Text(
            text = buildAnnotatedString {
                withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                    append(count)
                }
                append(" $label")
            },
            color = Color.White,
            fontSize = 12.sp
        )
    }
}
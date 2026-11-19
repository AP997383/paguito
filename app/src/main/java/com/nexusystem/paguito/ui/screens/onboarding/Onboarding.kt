package com.nexusystem.paguito.ui.screens.onboarding

import android.animation.ValueAnimator
import androidx.activity.compose.BackHandler
import androidx.annotation.StringRes
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.nexusystem.paguito.R
import com.nexusystem.paguito.ui.components.paguitoBackground
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue

internal enum class IntroScene { Overview, Clients, Products, Growth }
private data class IntroPage(@StringRes val category: Int, @StringRes val title: Int, @StringRes val accent: Int,
    @StringRes val description: Int, @StringRes val detail: Int, val scene: IntroScene)

private val introPages = listOf(
    IntroPage(R.string.intro_category_overview, R.string.intro_title_overview, R.string.intro_accent_overview, R.string.intro_description_overview, R.string.intro_detail_overview, IntroScene.Overview),
    IntroPage(R.string.intro_category_clients, R.string.intro_title_clients, R.string.intro_accent_clients, R.string.intro_description_clients, R.string.intro_detail_clients, IntroScene.Clients),
    IntroPage(R.string.intro_category_products, R.string.intro_title_products, R.string.intro_accent_products, R.string.intro_description_products, R.string.intro_detail_products, IntroScene.Products),
    IntroPage(R.string.intro_category_growth, R.string.intro_title_growth, R.string.intro_accent_growth, R.string.intro_description_growth, R.string.intro_detail_growth, IntroScene.Growth)
)

@Composable
fun OnboardingScreen(onFinish: () -> Unit) {
    val pager = rememberPagerState(pageCount = { introPages.size })
    val scope = rememberCoroutineScope()
    var finishing by rememberSaveable { mutableStateOf(false) }
    val complete = { if (!finishing) { finishing = true; onFinish() } }
    fun goTo(page: Int) { scope.launch {
        if (ValueAnimator.areAnimatorsEnabled()) pager.animateScrollToPage(page, animationSpec = tween(420, easing = FastOutSlowInEasing))
        else pager.scrollToPage(page)
    } }
    BackHandler(enabled = pager.currentPage > 0 && !finishing) { goTo(pager.currentPage - 1) }
    val colors = MaterialTheme.colorScheme
    Column(Modifier.fillMaxSize().paguitoBackground().testTag("onboarding-screen")) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(color = colors.primaryContainer, shape = MaterialTheme.shapes.medium) {
                Image(painterResource(R.drawable.abonia_a), null, Modifier.padding(9.dp).size(27.dp))
            }
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text("Paguito", style = MaterialTheme.typography.titleLarge)
                Text(stringResource(R.string.intro_brand_tagline), style = MaterialTheme.typography.labelSmall, color = colors.onSurfaceVariant)
            }
            TextButton(onClick = complete, enabled = !finishing, modifier = Modifier.testTag("onboarding-skip")) { Text(stringResource(R.string.intro_skip)) }
        }

        HorizontalPager(pager, modifier = Modifier.fillMaxWidth().weight(1f).testTag("onboarding-pager"), key = { it }) { index ->
            val page = introPages[index]
            BoxWithConstraints(Modifier.fillMaxSize().graphicsLayer {
                val distance = ((pager.currentPage - index) + pager.currentPageOffsetFraction).absoluteValue.coerceIn(0f, 1f)
                alpha = 1f - distance * 0.45f
                scaleX = 1f - distance * 0.035f; scaleY = scaleX
                translationY = distance * 12.dp.toPx()
            }) {
                val wide = maxWidth >= 650.dp && maxWidth > maxHeight
                if (wide) {
                    Row(Modifier.fillMaxSize().padding(horizontal = 36.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(36.dp)) {
                        OnboardingArtwork(page.scene, pager.currentPage == index, Modifier.weight(1f).heightIn(max = 360.dp).fillMaxHeight())
                        Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) { IntroCopy(page) }
                    }
                } else {
                    val artHeight = (maxHeight * 0.48f).coerceIn(150.dp, 300.dp)
                    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 24.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
                        OnboardingArtwork(page.scene, pager.currentPage == index, Modifier.fillMaxWidth().height(artHeight))
                        IntroCopy(page)
                    }
                }
            }
        }

        Column(Modifier.widthIn(max = 600.dp).fillMaxWidth().align(Alignment.CenterHorizontally).navigationBarsPadding().padding(horizontal = 24.dp).padding(bottom = 16.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                if (pager.currentPage > 0) IconButton(onClick = { goTo(pager.currentPage - 1) }, enabled = !finishing, modifier = Modifier.testTag("onboarding-previous")) {
                    Icon(Icons.AutoMirrored.Outlined.ArrowBack, stringResource(R.string.intro_previous))
                } else Spacer(Modifier.size(48.dp))
                Row(Modifier.selectableGroup(), verticalAlignment = Alignment.CenterVertically) {
                    introPages.indices.forEach { index ->
                        val selected = pager.currentPage == index
                        val width by animateDpAsState(if (selected) 24.dp else 7.dp, label = "intro-page-dot")
                        val label = stringResource(R.string.intro_page_label, index + 1, introPages.size)
                        Box(Modifier.size(40.dp).selectable(selected, enabled = !finishing, role = Role.Tab, onClick = { goTo(index) })
                            .semantics { contentDescription = label }.testTag("onboarding-dot-$index"), contentAlignment = Alignment.Center) {
                            Box(Modifier.height(7.dp).width(width).background(if (selected) colors.primary else colors.outlineVariant, CircleShape))
                        }
                    }
                }
                Text("${pager.currentPage + 1} / ${introPages.size}", style = MaterialTheme.typography.labelMedium, color = colors.onSurfaceVariant, modifier = Modifier.testTag("onboarding-page-count"))
            }
            Button(onClick = { if (pager.currentPage == introPages.lastIndex) complete() else goTo(pager.currentPage + 1) },
                enabled = !finishing && !pager.isScrollInProgress, shape = MaterialTheme.shapes.large,
                modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp).testTag("onboarding-next")) {
                Text(stringResource(if (pager.currentPage == introPages.lastIndex) R.string.intro_start else R.string.intro_next))
                Spacer(Modifier.width(10.dp)); Icon(if (pager.currentPage == introPages.lastIndex) Icons.Outlined.Check else Icons.AutoMirrored.Outlined.ArrowForward, null, Modifier.size(20.dp))
            }
        }
    }
}

@Composable
private fun IntroCopy(page: IntroPage) {
    val colors = MaterialTheme.colorScheme
    Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
        Text(stringResource(page.category).uppercase(), color = colors.primary, style = MaterialTheme.typography.labelMedium)
        Column {
            Text(stringResource(page.title), style = MaterialTheme.typography.displaySmall)
            Text(stringResource(page.accent), style = MaterialTheme.typography.displaySmall, color = colors.primary)
        }
        Text(stringResource(page.description), style = MaterialTheme.typography.bodyLarge, color = colors.onSurfaceVariant)
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 2.dp, bottom = 8.dp)) {
            Icon(Icons.Outlined.Check, null, tint = colors.primary, modifier = Modifier.size(16.dp))
            Text(stringResource(page.detail), style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
        }
    }
}

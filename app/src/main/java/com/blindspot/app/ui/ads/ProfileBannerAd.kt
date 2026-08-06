package com.blindspot.app.ui.ads

import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.viewinterop.AndroidView
import com.blindspot.app.ui.components.aurora.AuroraCard
import com.google.android.libraries.ads.mobile.sdk.banner.AdSize
import com.google.android.libraries.ads.mobile.sdk.banner.AdView
import com.google.android.libraries.ads.mobile.sdk.banner.BannerAd
import com.google.android.libraries.ads.mobile.sdk.banner.BannerAdRequest
import com.google.android.libraries.ads.mobile.sdk.common.AdLoadCallback
import com.google.android.libraries.ads.mobile.sdk.common.LoadAdError

/** Banner ad shown at the bottom of the Profile tab with a fixed height of one third of the screen. */
@Composable
fun ProfileBannerAd(modifier: Modifier = Modifier) {
    AuroraCard(modifier = modifier.fillMaxWidth()) {
        BoxWithConstraints(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center,
        ) {
            val activity = LocalActivity.current
            val adContext = activity ?: LocalContext.current
            val adView = remember { AdView(adContext) }
            val isPreviewMode = LocalInspectionMode.current
            val widthPx = with(LocalDensity.current) { maxWidth.roundToPx() }
            val heightPx = LocalWindowInfo.current.containerSize.height / 3
            val heightDp = with(LocalDensity.current) { heightPx.toDp() }

            LaunchedEffect(adContext, widthPx, heightPx) {
                if (isPreviewMode || widthPx <= 0 || heightPx <= 0) return@LaunchedEffect
                val adRequest = BannerAdRequest.Builder(
                    AdsConfig.BANNER_AD_UNIT_ID,
                    AdSize(widthPx, heightPx),
                ).build()
                adView.loadAd(
                    adRequest,
                    object : AdLoadCallback<BannerAd> {
                        override fun onAdLoaded(ad: BannerAd) = Unit

                        override fun onAdFailedToLoad(adError: LoadAdError) = Unit
                    },
                )
            }

            DisposableEffect(adView) {
                onDispose { adView.destroy() }
            }

            AndroidView(
                factory = { adView },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(heightDp),
            )
        }
    }
}

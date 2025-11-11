package com.simats.insulinbuddy

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.billingclient.api.*
import com.simats.insulinbuddy.R
import com.google.android.material.button.MaterialButton

class SubscriptionActivity : AppCompatActivity(), PurchasesUpdatedListener {

    private lateinit var btnSubscribe: MaterialButton
    private lateinit var btnSkipForNow: MaterialButton
    private lateinit var billingClient: BillingClient
    private var productDetails: ProductDetails? = null

    companion object {
        private const val TAG = "SubscriptionActivity"
        private const val SUBSCRIPTION_SKU = "insulinbuddy_premium_subscription"
        private const val TEST_SUBSCRIPTION_SKU = "android.test.purchased" // for testing
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_subscription)

        addDebugInformation()
        initializeViews()
        setupBillingClient()
        setupClickListeners()
    }

    private fun addDebugInformation() {
        Log.d(TAG, "=== DEBUG INFORMATION ===")
        Log.d(TAG, "Package name: $packageName")
        try {
            val packageInfo = packageManager.getPackageInfo(packageName, 0)
            Log.d(TAG, "Version code: ${packageInfo.longVersionCode}")
            Log.d(TAG, "Version name: ${packageInfo.versionName}")
        } catch (e: Exception) {
            Log.w(TAG, "Unable to get package info: ${e.message}")
        }
        Log.d(TAG, "Product ID: $SUBSCRIPTION_SKU")
        Log.d(TAG, "Test Product ID: $TEST_SUBSCRIPTION_SKU")
        Log.d(TAG, "=========================")
    }

    private fun initializeViews() {
        btnSubscribe = findViewById(R.id.btnSubscribe)
        btnSkipForNow = findViewById(R.id.btnSkipForNow)
    }

    private fun setupBillingClient() {
        billingClient = BillingClient.newBuilder(this)
            .setListener(this)
            .enablePendingPurchases(
                PendingPurchasesParams.newBuilder()
                    .enableOneTimeProducts()
                    .build()
            )
            .build()

        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(p0: BillingResult) {
                if (p0.responseCode == BillingClient.BillingResponseCode.OK) {
                    Log.d(TAG, "Billing setup finished successfully")
                    querySubscriptionDetails()
                } else {
                    Log.e(TAG, "Billing setup failed: ${p0.debugMessage}")
                }
            }

            override fun onBillingServiceDisconnected() {
                Log.d(TAG, "Billing service disconnected")
            }
        })
    }

    private fun querySubscriptionDetails() {
        querySpecificProduct(SUBSCRIPTION_SKU, BillingClient.ProductType.SUBS) { success ->
            if (!success) {
                Log.w(TAG, "Real subscription not found, trying test products...")
                querySpecificProduct(TEST_SUBSCRIPTION_SKU, BillingClient.ProductType.INAPP) { testSuccess ->
                    if (!testSuccess) showNoProductsAvailable()
                }
            }
        }
    }

    private fun querySpecificProduct(productId: String, productType: String, callback: (Boolean) -> Unit) {
        val productList = listOf(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(productId)
                .setProductType(productType)
                .build()
        )

        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(productList)
            .build()

        billingClient.queryProductDetailsAsync(params) { billingResult, result ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                val detailsList = result.productDetailsList ?: emptyList()
                if (detailsList.isNotEmpty()) {
                    productDetails = detailsList[0]
                    Log.d(TAG, "Product details retrieved successfully for: $productId")

                    // Log subscription offers safely
                    if (productType == BillingClient.ProductType.SUBS) {
                        productDetails?.subscriptionOfferDetails?.let { offers ->
                            Log.d(TAG, "Available subscription offers count: ${offers.count()}")
                            offers.withIndex().forEach { (index, offer) ->
                                Log.d(
                                    TAG,
                                    "Offer $index → basePlanId=${offer.basePlanId}, offerToken=${offer.offerToken}"
                                )
                            }
                        } ?: Log.w(TAG, "No subscription offers found")
                    }
                    callback(true)
                } else {
                    Log.e(TAG, "No product details found for: $productId")
                    callback(false)
                }
            } else {
                Log.e(TAG, "Failed to query product details for $productId: ${billingResult.debugMessage}")
                callback(false)
            }
        }
    }

    private fun showNoProductsAvailable() {
        runOnUiThread {
            Toast.makeText(
                this,
                "No subscription products available. Check Play Console setup.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun setupClickListeners() {
        btnSkipForNow.setOnClickListener {
            startActivity(Intent(this, WelcomeActivity::class.java))
            finish()
        }

        btnSubscribe.setOnClickListener {
            launchSubscriptionFlow()
        }
    }

    private fun launchSubscriptionFlow() {
        if (!billingClient.isReady) {
            Toast.makeText(this, "Billing service not ready. Try again.", Toast.LENGTH_SHORT).show()
            return
        }

        val details = productDetails ?: run {
            Toast.makeText(this, "Subscription not available. Try again.", Toast.LENGTH_SHORT).show()
            return
        }

        val productDetailsParamsList = if (details.productType == BillingClient.ProductType.SUBS) {
            val offers = details.subscriptionOfferDetails
            if (offers.isNullOrEmpty()) {
                Toast.makeText(this, "No subscription offers available", Toast.LENGTH_SHORT).show()
                return
            }

            val selectedOffer = offers[0]
            listOf(
                BillingFlowParams.ProductDetailsParams.newBuilder()
                    .setProductDetails(details)
                    .setOfferToken(selectedOffer.offerToken)
                    .build()
            )
        } else {
            listOf(
                BillingFlowParams.ProductDetailsParams.newBuilder()
                    .setProductDetails(details)
                    .build()
            )
        }

        val billingFlowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(productDetailsParamsList)
            .build()

        val billingResult = billingClient.launchBillingFlow(this, billingFlowParams)
        if (billingResult.responseCode != BillingClient.BillingResponseCode.OK) {
            Toast.makeText(
                this,
                "Failed to start subscription: ${billingResult.debugMessage}",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: MutableList<Purchase>?) {
        when (billingResult.responseCode) {
            BillingClient.BillingResponseCode.OK -> purchases?.forEach { handlePurchase(it) }
            BillingClient.BillingResponseCode.USER_CANCELED ->
                Toast.makeText(this, "Purchase canceled", Toast.LENGTH_SHORT).show()
            BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> {
                Toast.makeText(this, "You already have an active subscription", Toast.LENGTH_SHORT).show()
                navigateToMain()
            }
            BillingClient.BillingResponseCode.ITEM_UNAVAILABLE ->
                Toast.makeText(this, "Subscription unavailable. Try Play Store build.", Toast.LENGTH_LONG).show()
            BillingClient.BillingResponseCode.DEVELOPER_ERROR ->
                Toast.makeText(this, "Configuration error. Check Play Console setup.", Toast.LENGTH_LONG).show()
            BillingClient.BillingResponseCode.SERVICE_UNAVAILABLE ->
                Toast.makeText(this, "Google Play services unavailable. Try again later.", Toast.LENGTH_SHORT).show()
            else ->
                Toast.makeText(this, "Purchase failed: ${billingResult.debugMessage}", Toast.LENGTH_LONG).show()
        }
    }

    private fun handlePurchase(purchase: Purchase) {
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
            if (!purchase.isAcknowledged) {
                billingClient.acknowledgePurchase(
                    AcknowledgePurchaseParams.newBuilder()
                        .setPurchaseToken(purchase.purchaseToken)
                        .build()
                ) { result ->
                    if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                        onSubscriptionSuccess()
                    }
                }
            } else {
                onSubscriptionSuccess()
            }
        }
    }

    private fun onSubscriptionSuccess() {
        Toast.makeText(this, "Subscription successful! Welcome to Premium!", Toast.LENGTH_LONG).show()
        getSharedPreferences("subscription_prefs", MODE_PRIVATE).edit().apply {
            putBoolean("is_premium_user", true)
            putLong("subscription_time", System.currentTimeMillis())
            apply()
        }
        startActivity(Intent(this, WelcomeActivity::class.java))
        finish()
    }

    private fun navigateToMain() {
        startActivity(Intent(this, WelcomeActivity::class.java))
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::billingClient.isInitialized) billingClient.endConnection()
    }
}

package com.superwall.sdk.store.transactions

import LogLevel
import LogScope
import Logger
import com.superwall.sdk.Superwall
import com.superwall.sdk.analytics.SessionEventsManager
import com.superwall.sdk.analytics.internal.track
import com.superwall.sdk.analytics.internal.trackable.InternalSuperwallEvent
import com.superwall.sdk.delegate.PurchaseResult
import com.superwall.sdk.dependencies.TransactionVerifierFactory
import com.superwall.sdk.misc.ActivityLifecycleTracker
import com.superwall.sdk.paywall.presentation.internal.dismiss
import com.superwall.sdk.paywall.presentation.internal.state.PaywallResult
import com.superwall.sdk.paywall.vc.PaywallViewController
import com.superwall.sdk.paywall.vc.delegate.PaywallLoadingState
import com.superwall.sdk.store.StoreKitManager
import com.superwall.sdk.store.abstractions.product.StoreProduct
import com.superwall.sdk.store.abstractions.transactions.StoreTransactionType
import kotlinx.coroutines.*

class TransactionManager(
    private val storeKitManager: StoreKitManager,
    private val sessionEventsManager: SessionEventsManager,
    private val activityLifecycleTracker: ActivityLifecycleTracker,
    private val factory: TransactionVerifierFactory
) {
    private var lastPaywallViewController: PaywallViewController? = null

    suspend fun purchase(productId: String, paywallViewController: PaywallViewController) {
        print("purchase, $productId")
        val product = storeKitManager.productsById[productId] ?: return

        val activity = activityLifecycleTracker.getCurrentActivity() ?: return
        println("currentActivity: $activity")

        prepareToStartTransaction(product, paywallViewController)

        val result = storeKitManager.purchaseController.purchase(
            activity,
            product.rawStoreProduct.skuDetails
        )

        when (result) {
            is PurchaseResult.Purchased -> {
                didPurchase(product, paywallViewController)
            }
            is PurchaseResult.Failed -> {
                when (val outcome = TransactionErrorLogic.handle(result.error)) {
                    is TransactionErrorLogic.Cancelled -> trackCancelled(
                        product,
                        paywallViewController
                    )

                    is TransactionErrorLogic.PresentAlert -> presentAlert(
                        Error(result.error),
                        product,
                        paywallViewController
                    )
                }
            }
            is PurchaseResult.Pending -> {
                handlePendingTransaction(paywallViewController)
            }
            is PurchaseResult.Cancelled -> {
                trackCancelled(product, paywallViewController)
            }
        }
    }

    private suspend fun prepareToStartTransaction(
        product: StoreProduct,
        paywallViewController: PaywallViewController
    ) {
        GlobalScope.launch(Dispatchers.Default) {
            Logger.debug(
                LogLevel.debug,
                LogScope.paywallTransactions,
                "Transaction Purchasing",
                mapOf("paywall_vc" to paywallViewController),
                null
            )
        }

        val paywallInfo = paywallViewController.info

        sessionEventsManager.triggerSession.trackBeginTransaction(product)
        val trackedEvent = InternalSuperwallEvent.Transaction(
            InternalSuperwallEvent.Transaction.State.Start(product),
            paywallInfo,
            product,
            null
        )
        Superwall.instance.track(trackedEvent)

        withContext(Dispatchers.Main) {
            paywallViewController.loadingState = PaywallLoadingState.LoadingPurchase()
        }

        lastPaywallViewController = paywallViewController
    }

    // ... Remaining functions translated in a similar fashion ...
    private suspend fun didPurchase(
        product: StoreProduct,
        paywallViewController: PaywallViewController
    ) {
        GlobalScope.launch(Dispatchers.Default) {
            Logger.debug(
                LogLevel.debug,
                LogScope.paywallTransactions,
                "Transaction Succeeded",
                mapOf(
                    "product_id" to product.productIdentifier,
                    "paywall_vc" to paywallViewController
                ),
                null
            )
        }

        val transactionVerifier = factory.makeTransactionVerifier()
        val transaction = transactionVerifier.getAndValidateLatestTransaction(
            product.productIdentifier
        )

        transaction?.let {
            sessionEventsManager.enqueue(it)
        }

        storeKitManager.loadPurchasedProducts()

        trackTransactionDidSucceed(transaction, product)

        if (Superwall.instance.options.paywalls.automaticallyDismiss) {
            Superwall.instance.dismiss(
                paywallViewController,
                PaywallResult.Purchased(product.productIdentifier)
            )
        }
    }

    private suspend fun trackCancelled(
        product: StoreProduct,
        paywallViewController: PaywallViewController
    ) {
        GlobalScope.launch(Dispatchers.Default) {
            Logger.debug(
                LogLevel.debug,
                LogScope.paywallTransactions,
                "Transaction Abandoned",
                mapOf(
                    "product_id" to product.productIdentifier,
                    "paywall_vc" to paywallViewController
                ),
                null
            )
        }

        val paywallInfo = paywallViewController.info
        val trackedEvent = InternalSuperwallEvent.Transaction(
            InternalSuperwallEvent.Transaction.State.Abandon(product),
            paywallInfo,
            product,
            null
        )
        Superwall.instance.track(trackedEvent)
        sessionEventsManager.triggerSession.trackTransactionAbandon()

        withContext(Dispatchers.Main) {
            paywallViewController.loadingState = PaywallLoadingState.Ready()
        }
    }

    private suspend fun handlePendingTransaction(paywallViewController: PaywallViewController) {
        GlobalScope.launch(Dispatchers.Default) {
            Logger.debug(
                LogLevel.debug,
                LogScope.paywallTransactions,
                "Transaction Pending",
                mapOf("paywall_vc" to paywallViewController),
                null
            )
        }

        val paywallInfo = paywallViewController.info

        val trackedEvent = InternalSuperwallEvent.Transaction(
            InternalSuperwallEvent.Transaction.State.Fail(TransactionError.Pending("Needs parental approval")),
            paywallInfo,
            null,
            null
        )
        Superwall.instance.track(trackedEvent)
        sessionEventsManager.triggerSession.trackPendingTransaction()

        paywallViewController.presentAlert(
            "Waiting for Approval",
            "Thank you! This purchase is pending approval from your parent. Please try again once it is approved."
        )
    }

    private suspend fun presentAlert(
        error: Error,
        product: StoreProduct,
        paywallViewController: PaywallViewController
    ) {
        GlobalScope.launch(Dispatchers.Default) {
            Logger.debug(
                LogLevel.debug,
                LogScope.paywallTransactions,
                "Transaction Error",
                mapOf(
                    "product_id" to product.productIdentifier,
                    "paywall_vc" to paywallViewController
                ),
                error
            )
        }

        val paywallInfo = paywallViewController.info

        val trackedEvent = InternalSuperwallEvent.Transaction(
            InternalSuperwallEvent.Transaction.State.Fail(
                TransactionError.Failure(
                    error.message ?: "", product
                )
            ),
            paywallInfo,
            product,
            null
        )
        Superwall.instance.track(trackedEvent)
        sessionEventsManager.triggerSession.trackTransactionError()


        paywallViewController.presentAlert(
            "An error occurred",
            error.message ?: "Unknown error"
        )
    }

    // ... and so on for the other methods ...
    suspend fun trackTransactionDidSucceed(
        transaction: StoreTransactionType?,
        product: StoreProduct
    ) {
        val paywallViewController = lastPaywallViewController ?: return

        val paywallShowingFreeTrial = paywallViewController.paywall.isFreeTrialAvailable == true
        val didStartFreeTrial = product.hasFreeTrial && paywallShowingFreeTrial

        val paywallInfo = paywallViewController.info

        transaction?.let {
            sessionEventsManager.triggerSession.trackTransactionSucceeded(
                withId = it.storeTransactionId,
                forProduct = product,
                isFreeTrialAvailable = didStartFreeTrial
            )
        }

        val trackedEvent = InternalSuperwallEvent.Transaction(
            InternalSuperwallEvent.Transaction.State.Complete(product, transaction),
            paywallInfo,
            product,
            transaction
        )
        Superwall.instance.track(trackedEvent)

        if (product.subscriptionPeriod == null) {
            val nonRecurringEvent = InternalSuperwallEvent.NonRecurringProductPurchase(
                paywallInfo,
                product
            )
            Superwall.instance.track(nonRecurringEvent)
        }

        if (didStartFreeTrial) {
            val freeTrialEvent = InternalSuperwallEvent.FreeTrialStart(paywallInfo, product)
            Superwall.instance.track(freeTrialEvent)

        // SW-2214
        // https://linear.app/superwall/issue/SW-2214/%5Bandroid%5D-%5Bv2%5D-add-back-local-notifications
        // val notifications = paywallInfo.localNotifications.filter { it.type == NotificationType.TRIAL_STARTED }
        // NotificationScheduler.scheduleNotifications(notifications)
        } else {
            val subscriptionEvent =
                InternalSuperwallEvent.SubscriptionStart(paywallInfo, product)
            Superwall.instance.track(subscriptionEvent)
        }

        lastPaywallViewController = null
    }
}

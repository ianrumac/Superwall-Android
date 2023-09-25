package com.superwall.superapp

import com.superwall.sdk.Superwall
import com.superwall.sdk.delegate.SubscriptionStatus
import com.superwall.sdk.identity.identify
import com.superwall.sdk.identity.setUserAttributes
import com.superwall.sdk.models.paywall.PaywallProducts
import com.superwall.sdk.paywall.presentation.internal.dismiss
import kotlinx.coroutines.delay

class UITestHandler {
    companion object {
        var test0Info = UITestInfo(
            0,
            "Uses the identify function. Should see the name 'Jack' in the paywall."
        )
        suspend fun test0() {
            // TODO: The name doesn't display
            Superwall.instance.identify(userId = "test0")
            Superwall.instance.setUserAttributes(attributes = mapOf("first_name" to "Jack"))
            Superwall.instance.register(event = "present_data")
        }

        var test1Info = UITestInfo(
            1,
            "Uses the identify function. Should see the name 'Kate' in the paywall."
        )
        suspend fun test1() {
            // TODO: The name doesn't display
            // Set identity
            Superwall.instance.identify(userId = "test1a")
            Superwall.instance.setUserAttributes(mapOf("first_name" to "Jack"))

            // Set new identity
            Superwall.instance.identify(userId = "test1b")
            Superwall.instance.setUserAttributes(mapOf("first_name" to "Kate"))
            Superwall.instance.register(event = "present_data")
        }

        var test2Info = UITestInfo(
            2,
            "Calls `reset()`. No first name should be displayed."
        )
        suspend fun test2() {
            // TODO: The name doesn't get set to begin with so isn't an accurate test.
            // Set identity
            Superwall.instance.identify(userId = "test2")
            Superwall.instance.setUserAttributes(mapOf("first_name" to "Jack"))

            Superwall.instance.reset()
            Superwall.instance.register(event = "present_data")
        }

        var test3Info = UITestInfo(
            3,
            "Calls `reset()` multiple times. No first name should be displayed."
        )
        suspend fun test3() {
            // Set identity
            Superwall.instance.identify(userId = "test3")
            Superwall.instance.setUserAttributes(mapOf("first_name" to "Jack"))

            Superwall.instance.reset()
            Superwall.instance.reset()
            Superwall.instance.register(event = "present_data")
        }

        var test4Info = UITestInfo(
            4,
            "This paywall will open with a video playing that shows a 0 in the video at" +
                    "t0 and a 2 in the video at t2. It will close after 4 seconds. A new paywall " +
                    "will be presented 1 second after close. This paywall should have a video " +
                    "playing and should be started from the beginning with a 0 on the screen. "
        )

        suspend fun test4() {
            // Present the paywall.
            Superwall.instance.register(event = "present_video")

            // Dismiss after 4 seconds
            delay(4000)
            Superwall.instance.dismiss()

            // Present again after 1 second
            delay(1000)
            Superwall.instance.register(event = "present_video")
        }

        var test5Info = UITestInfo(
            5,
            "Show paywall with override products. Paywall should appear with 2 products:" +
                    "1 monthly at \$12.99 and 1 annual at \$99.99."
        )

        suspend fun test5() {
            // TODO: Need to get some products from google play console and substitute in.
        }

        var test6Info = UITestInfo(
            6,
            "Paywall should appear with 2 products: 1 monthly at \$4.99 and 1 annual at" +
                    " \$29.99."
        )
        suspend fun test6() {
            // TODO: This doesn't have the products that it should have - need to add to
            //  google play console
            Superwall.instance.register(event = "present_products")
        }

        var test7Info = UITestInfo(
            7,
            "Adds a user attribute to verify rule on `present_and_rule_user` presents: " +
                    "user.should_display == true and user.some_value > 12. Then dismisses and removes " +
                    "those attributes. Make sure it's not presented."
        )
        suspend fun test7() {
            // TODO: This crashes with no rule match
            Superwall.instance.identify(userId = "test7")
            Superwall.instance.setUserAttributes(
                mapOf(
                    "first_name" to "Charlie",
                    "should_display" to true,
                    "some_value" to 14
                )
            )
            Superwall.instance.register(event = "present_and_rule_user")

            delay(8000)
            Superwall.instance.dismiss()

            // Remove those attributes.
            Superwall.instance.setUserAttributes(
                mapOf(
                    "should_display" to null,
                    "some_value" to null
                )
            )
            Superwall.instance.register(event = "present_and_rule_user")
        }

        var test8Info = UITestInfo(
            8,
            "Adds a user attribute to verify rule on `present_and_rule_user`. Verify it" +
                    " DOES NOT present: user.should_display == true and user.some_value > 12"
        )
        suspend fun test8() {
            // TODO: Crashes on no rule match
            Superwall.instance.identify(userId = "test7")
            Superwall.instance.setUserAttributes(
                mapOf(
                    "first_name" to "Charlie",
                    "should_display" to true,
                    "some_value" to 12
                )
            )
            Superwall.instance.register(event = "present_and_rule_user")
        }

        var test9Info = UITestInfo(
            9,
            "Sets subs status to active, paywall should present regardless of this," +
                    " then it sets the status back to inactive."
        )
        suspend fun test9() {
            Superwall.instance.setSubscriptionStatus(SubscriptionStatus.ACTIVE)
            Superwall.instance.register(event = "present_always")
            Superwall.instance.setSubscriptionStatus(SubscriptionStatus.INACTIVE)
        }

        var test10Info = UITestInfo(
            10,
            "Paywall should appear with 2 products: 1 monthly at \$4.99 and 1 annual at " +
                    "\$29.99. After dismiss, paywall should be presented again with override " +
                    "products: 1 monthly at \$12.99 and 1 annual at \$99.99. After dismiss, paywall " +
                    "should be presented again with no override products. After dismiss, paywall " +
                    "should be presented one last time with no override products."
        )
        suspend fun  test10() {
            // TODO: Product substitution
        }

        var test11Info = UITestInfo(
            11,
            "Paywall should present with the name Claire. Then it should dismiss after" +
                    "8 seconds and present again without any name. Then it should present again" +
                    " with the name Sawyer."
        )
        suspend fun test11() {
            // TODO: USer attributes not set
            Superwall.instance.setUserAttributes(mapOf("first_name" to "Claire" ))
            Superwall.instance.register(event = "present_data")

            delay(8000)

            // Dismiss any view controllers
            Superwall.instance.dismiss()

            delay(2000)

            Superwall.instance.setUserAttributes(mapOf("first_name" to null))
            Superwall.instance.register(event = "present_data")

            delay(8000)

            // Dismiss any view controllers
            Superwall.instance.dismiss()

            delay(2000)

            Superwall.instance.setUserAttributes(mapOf("first_name" to "Sawyer"))
            Superwall.instance.register(event = "present_data")
        }

        var test12Info = UITestInfo(
            12,
            "Test trigger: off. Paywall shouldn't present."
        )
        suspend fun test12() {
            Superwall.instance.register(event = "keep_this_trigger_off")
        }

        var test13Info = UITestInfo(
            13,
            "Test trigger: not in the dashboard. Paywall shouldn't present."
        )
        suspend fun test13() {
            Superwall.instance.register(event = "i_just_made_this_up_and_it_dne")
        }

        var test14Info = UITestInfo(
            14,
            "Presents the paywall and then dismisses after 8 seconds. The paywall shouldn't " +
                    "display based on a paywall_close event."
        )
        suspend fun test14() {
            // Show a paywall
            Superwall.instance.register(event = "present_always")

            delay(8000)

            // Dismiss any view controllers
            Superwall.instance.dismiss()
        }

        var test15Info = UITestInfo(
            15,
            "Clusterfucks by Jake™. One paywall should present, then it should disappear" +
                    " then another paywall should present and disappear."
        )
        suspend fun test15() {
            // TODO: Stop multiple paywalls from being presented at a time
            Superwall.instance.register(event = "present_always")
            Superwall.instance.register(
                event = "present_always",
                params = mapOf("some_param_1" to "hello")
            )
            Superwall.instance.register(event = "present_always")

            delay(8000)

            // Dismiss any view controllers
            Superwall.instance.dismiss()

            delay(2000)

            Superwall.instance.register(event = "present_always")
            Superwall.instance.identify(userId = "1111")
            Superwall.instance.register(event = "present_always")

            delay(8000)

            // Dismiss any view controllers
            Superwall.instance.dismiss()

            // TODO: Add handler to register
//
//        var handler = PaywallPresentationHandler()
//
//        var experimentId = ""
//        handler.onPresent { info in
//                experimentId = info.experiment?.id ?? ""
//            Superwall.instance.register(event = "present_always")
//        }
//        Superwall.instance.register(event = "present_always", handler = handler)
        }

        var test16Info = UITestInfo(
            16,
            "Present an alert on Superwall.presentedViewController from the onPresent" +
                    " callback"
        )
        suspend fun test16() {
            // TODO: Can't do this without a handler in register
        }

        var test17Info = UITestInfo(
            17,
            "Clusterfucks by Jake™. This presents a paywall with the name Jack. Then it " +
                    "dismisses after 8s. Then another paywall will present with no name. Then" +
                    " the paywall will dismiss after 8s and one more paywall will display."
        )
        suspend fun test17() {
            Superwall.instance.identify(userId = "test0")
            Superwall.instance.setUserAttributes(mapOf("first_name" to "Jack"))
            Superwall.instance.register(event = "present_data")

            delay(8000)

            // Dismiss any view controllers
            Superwall.instance.dismiss()

            delay(2000)

            // Set identity
            Superwall.instance.identify(userId = "test2")
            Superwall.instance.setUserAttributes(mapOf("first_name" to "Jack"))

            // Reset the user identity
            Superwall.instance.reset()

            Superwall.instance.register(event = "present_data")

            delay(8000)

            // Dismiss any view controllers
            Superwall.instance.dismiss()

            delay(2000)

            // Present paywall
            Superwall.instance.register(event = "present_always")
            Superwall.instance.register(
                event = "present_always",
                params = mapOf("some_param_1" to "hello")
            )
            Superwall.instance.register(event = "present_always")
        }

        var test18Info = UITestInfo(
            18,
            "Open In-App Safari view controller from manually presented paywall"
        )
        suspend fun test18() {
            // TODO: Needs getPaywall
        }

        var test19Info = UITestInfo(
            19,
            "Clusterfucks by Jake™. Ths presents a paywall with no name. Then it dismisses" +
                    " after 8s. Then it presents again with no name, dismisses, and finally presents " +
                    "with the name Kate."
        )
        suspend fun test19() {
            // Set identity
            Superwall.instance.identify(userId = "test19a")
            Superwall.instance.setUserAttributes(mapOf("first_name" to "Jack"))

            Superwall.instance.reset()
            Superwall.instance.reset()
            Superwall.instance.register(event = "present_data")

            delay(8000)

            // Dismiss any view controllers
            Superwall.instance.dismiss()

            delay(2000)

            // TODO: Implement getPresentationResult
            // Superwall.instance.getPresentationResult(forEvent = "present_and_rule_user")

            delay(8000)

            // Dismiss any view controllers
            Superwall.instance.dismiss()

            delay(2000)

            // Show a paywall
            Superwall.instance.register(event = "present_always")

            delay(8000)

            // Dismiss any view controllers
            Superwall.instance.dismiss()

            delay(2000)

            // Set identity
            Superwall.instance.identify(userId = "test19b")
            Superwall.instance.setUserAttributes(mapOf("first_name" to "Jack"))

            // Set new identity
            Superwall.instance.identify(userId = "test19c")
            Superwall.instance.setUserAttributes(mapOf("first_name" to "Kate"))
            Superwall.instance.register(event = "present_data")
        }

        var test20Info = UITestInfo(
            20,
            "Verify that external URLs can be opened in native Safari from paywall. When" +
                    " the paywall opens, tap button 2."
        )
        suspend fun test20() {
            // Present paywall with URLs
            Superwall.instance.register(event = "present_urls")

            // Need to manually tap the button here
        }

        var test21Info = UITestInfo(
            21,
            "Present the paywall and manually purchase. After 12 seconds, it'll try to " +
                    "present the paywall again. The paywall shouldn't present."
        )
        suspend fun test21() {
            Superwall.instance.register(event = "present_data")

            // Manually purchase here
            delay(12000)

            // Try to present paywall again
            Superwall.instance.register(event = "present_data")
        }

        var test22Info = UITestInfo(
            22,
            "Track an event shortly after another one is beginning to present. The " +
                    "session should not be cancelled out."
        )
        suspend fun test22() {
            // TODO: This is skipped in the iOS SDK for now
        }

        var test23Info = UITestInfo(
            23,
            "Case: Unsubscribed user, register event without a gating handler\n" +
                    "Result: paywall should display"
        )
        suspend fun test23() {
            // Register event
            Superwall.instance.register(event = "register_nongated_paywall")
        }

        var test24Info = UITestInfo(
            24,
            "Case: Subscribed user, register event without a gating handler\n" +
                    "Result: paywall should NOT display. Resets subscription status to inactive " +
                    "4s later."
        )
        suspend fun test24() {
            // Set user as subscribed
            Superwall.instance.setSubscriptionStatus(SubscriptionStatus.ACTIVE)

            // Register event - paywall shouldn't appear.
            Superwall.instance.register(event = "register_nongated_paywall")

            delay(4000)
            Superwall.instance.setSubscriptionStatus(SubscriptionStatus.INACTIVE)
        }

        var test25Info = UITestInfo(
            25,
            "Present the paywall and make a purchase. After 12s it'll try to present a " +
                    "paywall again. It shouldn't present. These register calls don't have a feature gate."
        )
        suspend fun test25() {
            Superwall.instance.register(event = "register_nongated_paywall")

            // Manually purchase

            delay(12000)

            // Try to present paywall again
            Superwall.instance.register(event = "register_nongated_paywall")
        }

        // TODO: Test 25-32 require stuff that we don't have rn

        var test33Info = UITestInfo(
            33,
            "Calls identify twice with the same ID before presenting a paywall"
        )
        suspend fun test33() {
            // Set identity
            Superwall.instance.identify(userId = "test33")
            Superwall.instance.identify(userId = "test33")

            Superwall.instance.register(event = "present_data")
        }

        var test34Info = UITestInfo(
            34,
            "Call reset 8s after a paywall is presented – should not cause a crash."
        )
        suspend fun test34() {
            Superwall.instance.register(event = "present_data")

            delay(8000)

            // Call reset while it is still on screen
            Superwall.instance.reset()
        }

        // TODO: Test 35-48 require either getPaywall or a feature block

        var test62Info = UITestInfo(
            62,
            "Verify that an invalid URL like `#` doesn't crash the app. Manually tap on" +
                    "the \"Open in-app #\" button."
        )
        suspend fun test62() {
            // Present paywall with URLs
            Superwall.instance.register(event = "present_urls")

            // Need to manually tap on the URL button
        }

        // TODO: Test 63 - 71 require getPaywall, feature block, delegate, and surveys.


        var test72Info = UITestInfo(
            72,
            "Check that calling identify restores the seed value. This is async and " +
                    "dependent on config so needs to sleep after calling identify."
        )
        suspend fun test72() {
            // TODO: This fails to have the same userId after resetting and identifying.
            Superwall.instance.identify(userId = "abc")

            delay(1000)

            var seedHolder = Superwall.instance.getUserAttributes()
            println(seedHolder)

            Superwall.instance.reset()

            Superwall.instance.identify(userId = "abc")

            delay(1000)

            seedHolder = Superwall.instance.getUserAttributes()

            println(seedHolder)
        }
    }
}
package com.timeline.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import platform.Foundation.NSCharacterSet
import platform.Foundation.NSString
import platform.Foundation.NSURL
import platform.Foundation.URLQueryAllowedCharacterSet
import platform.Foundation.stringByAddingPercentEncodingWithAllowedCharacters
import platform.UIKit.UIApplication

@Composable
actual fun rememberEmailLauncher(): (to: String, subject: String, body: String) -> Unit {
    return remember {
        { to, subject, body ->
            val nsSubject = subject as NSString
            val nsBody = body as NSString

            val encodedSubject = nsSubject.stringByAddingPercentEncodingWithAllowedCharacters(
                NSCharacterSet.URLQueryAllowedCharacterSet
            ) ?: subject

            val encodedBody = nsBody.stringByAddingPercentEncodingWithAllowedCharacters(
                NSCharacterSet.URLQueryAllowedCharacterSet
            ) ?: body

            val urlString = "mailto:$to?subject=$encodedSubject&body=$encodedBody"
            val url = NSURL.URLWithString(urlString)

            if (url != null && UIApplication.sharedApplication.canOpenURL(url)) {
                UIApplication.sharedApplication.openURL(url)
            }
        }
    }
}
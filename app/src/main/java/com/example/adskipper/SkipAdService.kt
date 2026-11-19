package com.example.adskipper

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.graphics.Rect
import android.os.SystemClock
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

class SkipAdService : AccessibilityService() {

    private var lastClickMs = 0L

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event?.packageName != YOUTUBE_PKG) return
        val now = SystemClock.uptimeMillis()
        if (now - lastClickMs < 700) return
        val root = rootInActiveWindow ?: return
        val node = findSkipNode(root) ?: return
        if (clickNodeOrParent(node)) {
            lastClickMs = now
            Log.d(TAG, "Clicked skip button")
        }
    }

    private fun findSkipNode(root: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        root.findAccessibilityNodeInfosByViewId("$YOUTUBE_PKG:id/skip_ad_button")
            .firstOrNull { it.isVisibleToUser }
            ?.let { return it }
        for (label in LABELS) {
            root.findAccessibilityNodeInfosByText(label)
                .firstOrNull { it.isVisibleToUser && isExactLabel(it) }
                ?.let { return it }
        }
        return null
    }

    private fun isExactLabel(n: AccessibilityNodeInfo): Boolean {
        val t = n.text?.toString()?.trim()?.lowercase()
        val d = n.contentDescription?.toString()?.trim()?.lowercase()
        return t in LABELS || d in LABELS
    }

    private fun clickNodeOrParent(start: AccessibilityNodeInfo): Boolean {
        var n: AccessibilityNodeInfo? = start
        var depth = 0
        while (n != null && depth < 5) {
            if (n.isClickable && n.performAction(AccessibilityNodeInfo.ACTION_CLICK)) return true
            n = n.parent
            depth++
        }
        return tapCenter(start)
    }

    private fun tapCenter(node: AccessibilityNodeInfo): Boolean {
        val r = Rect()
        node.getBoundsInScreen(r)
        if (r.isEmpty) return false
        val path = Path().apply { moveTo(r.exactCenterX(), r.exactCenterY()) }
        val stroke = GestureDescription.StrokeDescription(path, 0, 50)
        return dispatchGesture(GestureDescription.Builder().addStroke(stroke).build(), null, null)
    }

    override fun onInterrupt() {}

    companion object {
        private const val TAG = "SkipAdService"
        private const val YOUTUBE_PKG = "com.google.android.youtube"
        private val LABELS = setOf("skip ads", "skip ad", "skip")
    }
}

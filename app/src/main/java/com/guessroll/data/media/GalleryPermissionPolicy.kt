package com.guessroll.data.media

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat

object GalleryPermissionPolicy {
    fun requiredPermissions(sdkInt: Int = Build.VERSION.SDK_INT): Array<String> {
        return when {
            sdkInt >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE -> arrayOf(
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED,
            )
            sdkInt >= Build.VERSION_CODES.TIRAMISU -> arrayOf(Manifest.permission.READ_MEDIA_IMAGES)
            sdkInt >= Build.VERSION_CODES.M -> arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
            else -> emptyArray()
        }
    }

    fun hasImageAccess(context: Context): Boolean {
        return when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE ->
                hasPermission(context, Manifest.permission.READ_MEDIA_IMAGES) ||
                    hasPermission(context, Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED)
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU ->
                hasPermission(context, Manifest.permission.READ_MEDIA_IMAGES)
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ->
                hasPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE)
            else -> true
        }
    }

    fun isAccessGranted(grants: Map<String, Boolean>): Boolean {
        return grants.any { (_, granted) -> granted }
    }

    private fun hasPermission(context: Context, permission: String): Boolean {
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
    }
}

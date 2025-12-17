package com.ion.app.core.util

import com.ion.app.BuildConfig.BASE_URL

fun buildImageUrl(path: String?): String? {
    if (path.isNullOrBlank()) return null
    return BASE_URL.trimEnd('/') + path
}
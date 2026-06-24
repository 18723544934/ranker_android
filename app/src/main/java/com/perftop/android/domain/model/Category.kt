package com.perftop.android.domain.model

enum class Category(val displayName: String) {
    PC_CPU("PC CPU"),
    PC_GPU("PC GPU"),
    MOBILE_CPU("手机 CPU"),
    MOBILE_GPU("手机 GPU");

    companion object {
        fun fromString(value: String): Category? {
            return values().find { it.name == value }
        }
    }
}

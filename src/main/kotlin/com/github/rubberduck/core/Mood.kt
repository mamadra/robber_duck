package com.github.rubberduck.core

enum class Mood {
    IDLE,
    HAPPY,
    PANIC,
    NOD;

    val animated: Boolean
        get() = this == PANIC || this == NOD
}

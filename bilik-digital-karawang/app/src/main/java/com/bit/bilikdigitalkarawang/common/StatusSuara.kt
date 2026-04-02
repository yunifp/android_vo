package com.bit.bilikdigitalkarawang.common

enum class StatusSuara(val id: Int) {
    SAH(1),
    TIDAK_SAH(2),
    ABSTAIN(3);

    companion object {
        fun fromId(id: Int): StatusSuara? = values().find { it.id == id }
    }
}

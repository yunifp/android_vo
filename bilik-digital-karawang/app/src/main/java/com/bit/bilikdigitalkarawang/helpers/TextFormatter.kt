package com.bit.bilikdigitalkarawang.helpers

fun Long.formatNumber(): String {
    return "%,d".format(this).replace(",", ".")
}

fun Int.formatNumber(): String {
    return this.toLong().formatNumber()
}
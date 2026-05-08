package com.aquib.pricepulse.core.common.util

fun Double.toFormattedPrice(decimals: Int = 2): String = "%.${decimals}f".format(this)

fun Double.toFormattedPercent(decimals: Int = 2): String = "${"%.${decimals}f".format(this)}%"

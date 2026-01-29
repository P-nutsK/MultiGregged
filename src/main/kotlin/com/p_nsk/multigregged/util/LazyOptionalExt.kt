package com.p_nsk.multigregged.util

import net.minecraftforge.common.util.LazyOptional

fun <T> LazyOptional<T>.orNull(): T? =
    if (isPresent) orElseThrow(::IllegalStateException) else null

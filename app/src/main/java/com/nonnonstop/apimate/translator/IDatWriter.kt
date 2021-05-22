package com.nonnonstop.apimate.translator

import java.io.Closeable

interface IDatWriter : Closeable {
    fun open(info: IDatInfo)
    fun write(str: String)
}
package com.nonnonstop.apimate.translator

interface IDatTranslator {
    fun translate(info: IDatInfo, writer: IDatWriter): Boolean
}
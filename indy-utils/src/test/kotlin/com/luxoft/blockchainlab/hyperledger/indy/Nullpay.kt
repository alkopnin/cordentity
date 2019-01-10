package com.luxoft.blockchainlab.hyperledger.indy

import com.sun.jna.Library
import com.sun.jna.Native
import com.sun.jna.NativeLibrary


interface Nullpay: Library {
    companion object {
        val JNA_LIBRARY_NAME = "nullpay"
        val JNA_NATIVE_LIB = NativeLibrary.getInstance(JNA_LIBRARY_NAME)
        val INSTANCE: Nullpay = Native.loadLibrary(JNA_LIBRARY_NAME, Nullpay::class.java)
    }

    fun nullpay_init(): Int
}
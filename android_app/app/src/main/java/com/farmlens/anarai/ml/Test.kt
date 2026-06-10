package com.farmlens.anarai.ml

import org.tensorflow.lite.Interpreter

fun test() {
    val methods = Interpreter::class.java.methods
    for (m in methods) {
        println(m.name)
    }
}

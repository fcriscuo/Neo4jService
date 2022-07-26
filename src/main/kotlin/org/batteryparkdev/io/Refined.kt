package org.batteryparkdev.io

interface Refined<in T> {
    abstract fun isValid(value: T) : Boolean
}
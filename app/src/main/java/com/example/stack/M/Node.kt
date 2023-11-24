package com.example.stack.M

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class Dependency(val nodes: List<Node>, val computation: (List<Double>) -> Double)

data class Node(val order: Int, val initialValue: Double = 0.0) { // Node class with reactive properties
    var value by mutableStateOf(initialValue)
    private val _valueFlow = MutableStateFlow<Double>(initialValue)
    val valueFlow: StateFlow<Double> = _valueFlow
    var dependency: Dependency? = null

    fun updateValue(newValue: Double) {
        value = newValue
        _valueFlow.value = newValue
    }

    fun setFormula(inputNodes: List<Node>, computation: (List<Double>) -> Double, scope: CoroutineScope) {
        this.dependency = Dependency(inputNodes, computation)

        combine(*inputNodes.map { it.valueFlow }.toTypedArray()) { values ->
            computation.invoke(values.toList()) // apply the formula
        }.onEach { newValue: Double ->
            updateValue(newValue) // update the value of the node itself
        }.launchIn(scope)
    }
}
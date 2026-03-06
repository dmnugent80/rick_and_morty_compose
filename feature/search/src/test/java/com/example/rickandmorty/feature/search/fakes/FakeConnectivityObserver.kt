package com.example.rickandmorty.feature.search.fakes

import com.example.rickandmorty.core.domain.network.ConnectivityObserver
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeConnectivityObserver : ConnectivityObserver {
    private val _isOnline = MutableStateFlow(true)
    override val isOnline: StateFlow<Boolean> = _isOnline.asStateFlow()

    fun setOnline(online: Boolean) {
        _isOnline.value = online
    }
}

package com.example.rickandmorty.core.domain.network

import kotlinx.coroutines.flow.StateFlow

interface ConnectivityObserver {
    val isOnline: StateFlow<Boolean>
}

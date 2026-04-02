package com.bit.bilikdigitalkarawang.common

sealed interface CommonStatus {
    object Idle : CommonStatus
    object Loading : CommonStatus
    object Success : CommonStatus
    object Error : CommonStatus
}
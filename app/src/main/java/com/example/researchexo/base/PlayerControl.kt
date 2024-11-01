package com.example.researchexo.base

import androidx.media3.common.Player

interface PlayerControl {
    fun attachPlayer(player: Player)
    fun detachPlayer()
    fun updateState()
}
package com.example.healthocr.pages.acceptWindows

import com.example.healthocr.db.SessionInfo

sealed class AcceptWindow{
    data object None: AcceptWindow()
    data class DeleteSession(val session: SessionInfo): AcceptWindow()

}
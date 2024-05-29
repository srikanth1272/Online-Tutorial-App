package com.multipurposeapp.adapters

import live.videosdk.rtc.android.Participant

interface IVideoClicked {
    fun onVideoItemClicked(participant: Participant)
}
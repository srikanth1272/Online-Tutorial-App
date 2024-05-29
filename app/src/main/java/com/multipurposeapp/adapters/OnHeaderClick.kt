package com.multipurposeapp.adapters

import com.multipurposeapp.data.HeaderModel

interface OnHeaderClick {
    fun onClick(headerName:String)
    fun onLongClick(headerModel:HeaderModel)
}
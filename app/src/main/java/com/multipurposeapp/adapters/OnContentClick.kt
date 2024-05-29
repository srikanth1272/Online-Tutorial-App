package com.multipurposeapp.adapters

import com.multipurposeapp.data.ContentModel

interface OnContentClick {
    fun onClick(contentName:String)
    fun onLongClick(contentModel:ContentModel)
}
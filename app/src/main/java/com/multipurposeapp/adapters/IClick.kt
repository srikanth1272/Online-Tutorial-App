package com.multipurposeapp.adapters

import com.multipurposeapp.data.ContentModel
import com.multipurposeapp.data.InstructorModel

interface IClick {
    fun youtube(link:String)
    fun chat(instructorModel:InstructorModel)
    fun video(instructorModel:InstructorModel)
    fun onLongClick(instructorModel:InstructorModel)
}
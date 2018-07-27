package com.example.ozturkse.x.ui.adapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.example.ozturkse.x.R
import java.io.File

class ImagesAdapter(
        private val context: Context,
        private val imagesList: List<File>
) : RecyclerView.Adapter<ImageViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ImageViewHolder(LayoutInflater
            .from(parent.context)
            .inflate(R.layout.row_image, parent, false))

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) = holder.bindView(imagesList[position])

    override fun getItemCount() = imagesList.size
}
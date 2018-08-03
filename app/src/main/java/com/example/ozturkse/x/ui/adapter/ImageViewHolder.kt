package com.example.ozturkse.x.ui.adapter

import android.support.v7.widget.RecyclerView
import android.view.View
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.row_image.view.*
import java.io.File

class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    fun bindView(file: File){
        Picasso.get()
                .load(file)
                .fit()
                .centerCrop()
                .into(itemView.row_imageview_image)
    }


}
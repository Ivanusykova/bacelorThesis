package com.example.bak_python

import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import java.io.File

class ImageAdapter(
    private var imageFiles: List<File>,
    private val onClick: (File) -> Unit
) : RecyclerView.Adapter<ImageAdapter.ImageViewHolder>() {

    class ImageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.findViewById(R.id.imageItem)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.adapter_image, parent, false)
        return ImageViewHolder(view)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val file = imageFiles[position]
        val bitmap = BitmapFactory.decodeFile(file.absolutePath)
        holder.imageView.setImageBitmap(bitmap)
        holder.imageView.setOnClickListener {
            onClick(file)
        }
    }

    override fun getItemCount(): Int = imageFiles.size

    fun updateData(newFiles: List<File>) {
        imageFiles = newFiles
        notifyDataSetChanged()
    }
}


package com.example.mytravelers.Adapter

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.mytravelers.Activity.PackageDetailsActivity
import com.example.mytravelers.Modal.PackageData
import com.example.mytravelers.R
import com.example.mytravelers.databinding.ActivityDashboardBinding

class PackageAdapter(
    private var packageList: List<PackageData>
) : RecyclerView.Adapter<PackageAdapter.PackageViewHolder>() {

    inner class PackageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.imgfirstimage)
        val placeTextView: TextView = itemView.findViewById(R.id.txtPlace)
        val descriptionTextView: TextView = itemView.findViewById(R.id.txtDescription)
        val daysTextView: TextView = itemView.findViewById(R.id.txtDays)
        val priceTextView: TextView = itemView.findViewById(R.id.txtPrice)
        val llPackageDetails: LinearLayout = itemView.findViewById(R.id.llPackageDetails)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PackageViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.packge_item, parent, false)
        return PackageViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: PackageViewHolder, position: Int) {
        val currentItem = packageList[position]
        Glide.with(holder.itemView.context)
            .load(currentItem.images.firstOrNull()) // Load the first image URL, if available
            .placeholder(R.drawable.baseline_broken_image_24)
            .into(holder.imageView)
        holder.placeTextView.text = currentItem.place
        holder.descriptionTextView.text = currentItem.description
        holder.daysTextView.text = "${currentItem.days} Day's"
        holder.priceTextView.text = "₹ ${currentItem.price}"

        with(holder){
            llPackageDetails.setOnClickListener {
                var i = Intent(holder.itemView.context, PackageDetailsActivity::class.java)
                i.putExtra("place",packageList[position].place)
                i.putExtra("mobile",packageList[position].phone)
                i.putExtra("allImages", packageList[position].images.toTypedArray())



                i.putExtra("Price","₹ "+packageList[position].price+"/-")


                i.putExtra("day",packageList[position].days)
                i.putExtra("description",packageList[position].description)
                Log.e("TAG", "onBindViewHolder: "+packageList[position].images)
                holder.itemView.context.startActivity(i)

            }
        }

    }


    override fun getItemCount(): Int {
        return packageList.size
    }
}

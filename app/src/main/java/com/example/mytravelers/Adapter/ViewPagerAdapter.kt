package com.example.mytravelers.Adapter

import DepthPageTransformer
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.bumptech.glide.Glide
import com.example.mytravelers.R
import java.util.*

class ViewPagerAdapter(var context: Context, var ListViewPager: Array<String>?, private val viewPager: ViewPager) : PagerAdapter() {

    private var timer: Timer? = null
    private val handler = Handler(Looper.getMainLooper())


    override fun getCount(): Int {
        return ListViewPager?.size ?: 0
    }

    override fun isViewFromObject(view: View, objects: Any): Boolean {
        return view == objects
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val view = LayoutInflater.from(context).inflate(R.layout.view_pager_item, container, false)
        val imageView: ImageView = view.findViewById(R.id.imgSwiper)

        Glide.with(context).load(ListViewPager?.get(position)).into(imageView) // Load image from the provided URL

        container.addView(view)

        // Apply the depth page transformation to the ViewPager
        viewPager.setPageTransformer(true, DepthPageTransformer())

        return view
    }

    override fun destroyItem(container: ViewGroup, position: Int, objects: Any) {
        container.removeView(objects as View?)
    }

    fun startAutoSlide(viewPager: ViewPager, delayMillis: Long = 2500, periodMillis: Long = 2500) {
        timer = Timer()
        timer?.schedule(object : TimerTask() {
            override fun run() {
                handler.post {
                    val nextPage = (viewPager.currentItem + 1) % getCount()
                    viewPager.setCurrentItem(nextPage, true)
                }
            }
        }, delayMillis, periodMillis) // Delay 3000ms, repeat every 3000ms
    }


    fun stopAutoSlide() {
        timer?.cancel()
        timer = null
    }
}

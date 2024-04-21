package com.aacharya.prashant.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.aacharya.prashant.common.GridSpacingItemDecoration
import com.aacharya.prashant.R
import com.aacharya.prashant.databinding.ActivityMainBinding
import com.aacharya.prashant.common.loadUrl
import com.aacharya.prashant.common.showToast
import com.aacharya.prashant.viewmodel.ImagesViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap

class MainActivity : AppCompatActivity() {

    private val viewModel: ImagesViewModel by viewModels()
    lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupRecyclerView(binding.recyclerView)
        observeEvents()
        viewModel.loadImages()

    }

    private fun setupRecyclerView(recyclerView: RecyclerView) {
        with(recyclerView) {
            layoutManager = GridLayoutManager(context, 3)
            adapter = ImageAdapter().apply {
                setHasStableIds(true)
            }

            setHasFixedSize(true)
            addItemDecoration(GridSpacingItemDecoration(3, 10, true))
        }
    }

    private fun observeEvents() {
        lifecycleScope.launch(Dispatchers.Main) {
            viewModel.imageLoadingEventsStateFlow.collect { event ->
                when (event) {
                    is ImagesViewModel.ImageLoadingEvents.ImagesLoaded -> onImageUrlsLoaded(event.images)

                    is ImagesViewModel.ImageLoadingEvents.ImageLoadingFailure -> onImageUrlsLoadFailed(
                        event.reason
                    )
                }
            }
        }
    }

    private fun onImageUrlsLoaded(urls: List<String>) {
        (binding.recyclerView.adapter as ImageAdapter).submitList(urls)
    }

    private fun onImageUrlsLoadFailed(reason: String) {
        (binding.recyclerView.adapter as ImageAdapter).submitList(List(30) { "" })
        showToast("Something went wrong, make sure internet is active")
//        Snackbar.make(binding.root, reason, Snackbar.LENGTH_LONG).setAnchorView(binding.root).setAction(
//            "Retry"
//        ) { viewModel.loadImages() }.show()
    }
}


class ImageAdapter : ListAdapter<String, ImageAdapter.ImageViewHolder>(diffCallback) {

    val jobs: ConcurrentHashMap<Int, Job> = ConcurrentHashMap()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_image, parent, false)
        return ImageViewHolder(view)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val imageUrl = getItem(position)
        holder.bind(imageUrl)
    }

    override fun getItemId(position: Int): Long {
        return getItem(position).hashCode().toLong()
    }

    inner class ImageViewHolder(private val v: View) : RecyclerView.ViewHolder(v) {
        fun bind(url: String) {
            loadUrl(
                v.findViewById<ImageView>(R.id.imageView), url,
            ) { job ->
                jobs[layoutPosition] = job
            }
        }
    }

    override fun onViewDetachedFromWindow(holder: ImageViewHolder) {
        super.onViewDetachedFromWindow(holder)
        jobs[holder.layoutPosition]?.cancel()
    }

    companion object DIFF {
        val diffCallback = object : DiffUtil.ItemCallback<String>() {
            override fun areItemsTheSame(oldItem: String, newItem: String) = oldItem == newItem
            override fun areContentsTheSame(oldItem: String, newItem: String) = oldItem == newItem
        }
    }
}

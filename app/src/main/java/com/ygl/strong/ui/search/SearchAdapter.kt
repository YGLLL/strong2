package com.ygl.strong.ui.search

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ygl.strong.R
import com.ygl.strong.http.dto.ReplyDto
import com.ygl.strong.http.dto.SearchDto

/**
 * Created by ygl-gpd
 * Created date:2023/5/3 23:46
 **/
class SearchAdapter(val mData:MutableList<SearchDto.SearchDataResultData>,val mItemClick:(pos:Int)->Unit) : RecyclerView.Adapter<SearchAdapter.ReplyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReplyViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_search,parent,false)
        return ReplyViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReplyViewHolder, position: Int) {
        holder.mIv?.let {
            Glide.with(it).load("https:${mData[position].pic}").into(it)
        }
        holder.mTvTitle?.text = mData[position].title
        holder.mLL?.setOnClickListener {
            mItemClick.invoke(position)
        }
    }

    override fun getItemCount(): Int {
        return mData.size
    }

    class ReplyViewHolder : RecyclerView.ViewHolder {

        open var mIv:ImageView? = null
        open var mTvTitle:TextView? = null
        open var mLL:LinearLayout? = null

        constructor(itemView: View):super(itemView){
            mIv = itemView.findViewById(R.id.iv)
            mTvTitle = itemView.findViewById(R.id.tv_title)
            mLL = itemView.findViewById(R.id.ll)
        }
    }
}
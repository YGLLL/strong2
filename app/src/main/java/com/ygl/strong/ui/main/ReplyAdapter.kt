package com.ygl.strong.ui.main

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ygl.strong.R
import com.ygl.strong.http.dto.ReplyDto

class ReplyAdapter(val mData:MutableList<ReplyDto.RepliesClass>) : RecyclerView.Adapter<ReplyAdapter.ReplyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReplyViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_reply,parent,false)
        return ReplyViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReplyViewHolder, position: Int) {
        holder.mTvUname?.text = mData[position].member?.uname
        holder.mTvContent?.text = mData[position].content?.message
    }

    override fun getItemCount(): Int {
        return mData.size
    }

    class ReplyViewHolder : RecyclerView.ViewHolder {

        open var mTvUname:TextView? = null
        open var mTvContent:TextView? = null

        constructor(itemView: View):super(itemView){
            mTvUname = itemView.findViewById(R.id.tv_uname)
            mTvContent = itemView.findViewById(R.id.tv_reply_content)
        }
    }
}
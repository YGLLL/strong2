package com.ygl.strong.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.ygl.strong.R
import com.ygl.strong.http.Api
import com.ygl.strong.http.dto.ReplyDto
import com.ygl.strong.widget.LoadingDialog
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ReplyFragment(val mAid:String) : BottomSheetDialogFragment() {

    protected var mLoading: LoadingDialog? = null
    private var mRoot:View? = null
    private var mData:MutableList<ReplyDto.RepliesClass> = mutableListOf()
    private var mAdapter:ReplyAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mLoading = LoadingDialog(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mRoot = inflater.inflate(R.layout.reply_fragment, container, false)
        return mRoot
    }

    override fun onStart() {
        super.onStart()
        showLoading()
        Api.BILIBILI.getReplys(pn = "1", oid = mAid).enqueue(object :
            Callback<ReplyDto> {
            override fun onResponse(call: Call<ReplyDto>, response: Response<ReplyDto>) {
                dismissLoading()
                val body = response.body()
                if (body!=null && body.code == "0" && body.data?.replies?.isNotEmpty() == true){
                    showReply(body)
                }
            }

            override fun onFailure(call: Call<ReplyDto>, t: Throwable) {
                dismissLoading()
            }

        })

    }

    override fun onDestroy() {
        super.onDestroy()
        mLoading = null
    }

    private fun showReply(body: ReplyDto) {
        body.data?.replies?.let {
            val rv = mRoot?.findViewById<RecyclerView>(R.id.rv)
            mData.clear()
            mData.addAll(it)
            mAdapter = ReplyAdapter(mData)
            rv?.layoutManager = LinearLayoutManager(context)
            rv?.adapter = mAdapter
        }
    }

    protected fun showLoading() {
        activity?.runOnUiThread { showLoading(true) }
    }
    protected fun showLoading(cancelable:Boolean) {
        if (mLoading != null && mLoading?.isShowing != true) {
            activity?.runOnUiThread { mLoading?.show(cancelable) }
        }
    }

    protected fun showLoading(cancelable:Boolean,text:String) {
        if (mLoading != null && mLoading?.isShowing != true) {
            activity?.runOnUiThread { mLoading?.show(cancelable,text) }
        }
    }

    protected fun dismissLoading() {
        if (mLoading != null && mLoading?.isShowing != false) {
            activity?.runOnUiThread { mLoading?.dismiss() }
        }
    }
}
package com.ygl.strong.ui.search

import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ygl.strong.R
import com.ygl.strong.base.BaseActivity
import com.ygl.strong.db.DB
import com.ygl.strong.db.bean.VideoDetail
import com.ygl.strong.http.Api
import com.ygl.strong.http.dto.PagelistDto
import com.ygl.strong.http.dto.SearchDto
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SearchActivity : BaseActivity() {

    var mLLEdit:LinearLayout? = null
    var mRv:RecyclerView? = null
    private var mData:MutableList<SearchDto.SearchDataResultData> = mutableListOf()
    private var mAdapter: SearchAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)
        setControlBarTransparent()

        mLLEdit = findViewById(R.id.ll_edit)
        mRv = findViewById(R.id.rv)
        mRv?.layoutManager = LinearLayoutManager(this)
        mAdapter = SearchAdapter(mData,mOnItemClick)
        mRv?.adapter = mAdapter
        val et = findViewById<EditText>(R.id.et)
        val btn = findViewById<Button>(R.id.btn)
        btn.setOnClickListener {
            val keyword = et.text.toString()
            if (!TextUtils.isEmpty(keyword)){
                search(keyword)
            }
        }
    }

    private val mOnItemClick:(pos:Int)->Unit = { pos->

    }

    private fun search(keyword: String) {
        showLoading()
        Api.BILIBILI.search(keyword = keyword).enqueue(object : Callback<SearchDto>{
            override fun onResponse(call: Call<SearchDto>, response: Response<SearchDto>) {
                val body = response.body()
                if (body == null || body.code != "0"){
                    dismissLoading()
                    showToast("body error")
                    return@onResponse
                }
                var searchDataResultData:MutableList<SearchDto.SearchDataResultData> = mutableListOf()
                body?.data?.result?.forEach { resultBean->
                    if (resultBean.result_type == "video"){
                        searchDataResultData.clear()
                        resultBean.data?.let { data->
                            searchDataResultData.addAll(data)
                        }
                    }
                }
                if (searchDataResultData.size>0){
                    dismissLoading()
                    showResult()
                    mData.clear()
                    mData.addAll(searchDataResultData)
                    mAdapter?.notifyDataSetChanged()
                }else{
                    dismissLoading()
                    showToast("error")
                }
            }

            override fun onFailure(call: Call<SearchDto>, t: Throwable) {
                dismissLoading()
                showToast(t.toString())
            }
        })
    }

//    private fun loopAddData(index:Int, loopList: MutableList<SearchDto.SearchDataResultData>, end: ((msg: String) -> Unit)?){
//        Api.BILIBILI.pagelist(bvid = loopList[index].bvid).enqueue(object :Callback<PagelistDto>{
//            override fun onResponse(call: Call<PagelistDto>, response: Response<PagelistDto>) {
//                val body = response.body()
//
//                body?.data?.get(0)?.let {
//                    val bean = loopList[index]
//                    val videoDetail = VideoDetail()
//                    videoDetail.aid = bean.aid
//                    videoDetail.bvid = bean.bvid
//                    videoDetail.cid = it.cid
//                    videoDetail.title = bean.title
//                    videoDetail.reply = "0"
//
//                    if (DB.isNewVideo(videoDetail)){
//                        videoDetail.save()
//                    }
//                }
//
//                val nextIndex = index+1
//                if ((index+1) == loopList.size){
//                    end?.invoke("SUCCESS")
//                }else{
//                    loopAddData(nextIndex,loopList,end)
//                }
//            }
//
//            override fun onFailure(call: Call<PagelistDto>, t: Throwable) {
//                end?.invoke("fail:${t.toString()}")
//            }
//        })
//    }

    private fun showResult(){
        mLLEdit?.visibility = View.GONE
        mRv?.visibility = View.VISIBLE
    }
    private fun showEdit(){
        mLLEdit?.visibility = View.VISIBLE
        mRv?.visibility = View.GONE
    }

    override fun onBackPressed() {
        if (mRv?.isVisible == true){
            showEdit()
        }else{
            super.onBackPressed()
        }
    }
}
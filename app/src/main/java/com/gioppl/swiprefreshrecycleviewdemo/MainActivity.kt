package com.gioppl.swiprefreshrecycleviewdemo

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.util.*

class MainActivity : AppCompatActivity() {
    private val mList = ArrayList<MyAdapt.ClassifyButtonInfo>();
    private var rv: RecyclerView? = null;
    private var mAdapt: MyAdapt? = null
    var refreshable_view: RefreshableViewList? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        for (i in 0..50)
            mList.add(MyAdapt.ClassifyButtonInfo(i))
        refreshable_view = findViewById(R.id.refreshable_view) as RefreshableViewList
        rv = findViewById(R.id.rv_main)
        val layoutManager = LinearLayoutManager(this)
        rv!!.layoutManager = layoutManager
        rv!!.setHasFixedSize(true)
        mAdapt = MyAdapt(mList, this,object : MyAdapt.ClickBack{
            override fun back(position: Int) {

            }
        })
        rv!!.adapter = mAdapt
        rv!!.itemAnimator = DefaultItemAnimator()

        refreshable_view!!.setOnRefreshListener(1,object : RefreshableViewList.RefreshCallBack{
            override fun onRefresh() {
                Thread(Runnable {
                    Thread.sleep(3000)
                    refreshable_view!!.finishRefresh()
                }).start()

            }

            override fun onFinished() {

            }

        })
    }
}
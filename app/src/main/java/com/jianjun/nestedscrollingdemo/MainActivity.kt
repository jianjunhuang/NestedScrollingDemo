package com.jianjun.nestedscrollingdemo

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val recyclerView = findViewById<RecyclerView>(R.id.recycler)
        recyclerView.run {
            adapter = TestAdapter()
            layoutManager = LinearLayoutManager(this@MainActivity)
        }
        val refreshLayout = findViewById<SwipeRefreshLayout>(R.id.refresh_layout)
        refreshLayout.setOnRefreshListener {
            refreshLayout.postDelayed({
                refreshLayout.isRefreshing = false
            }, 1000)

        }
    }

    class TestAdapter() : RecyclerView.Adapter<ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(
                LayoutInflater.from(parent.context).inflate(R.layout.item_text, parent, false)
            )
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        }

        override fun getItemCount(): Int {
            return 50
        }

    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    }
}
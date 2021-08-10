package com.jianjun.nestedscrollingdemo

import android.app.Activity
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class NestedActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nested)
        val recyclerView = findViewById<RecyclerView>(R.id.recycler)
        recyclerView.run {
            adapter = TestAdapter()
            layoutManager = LinearLayoutManager(this@NestedActivity)
        }
    }
}
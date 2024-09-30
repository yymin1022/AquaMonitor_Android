package com.yong.aquamonitor.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.yong.aquamonitor.R

class DetailFragment: Fragment() {
    private var recyclerDetail: RecyclerView? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val layoutInflater = inflater.inflate(R.layout.fragment_detail, container, false)
        recyclerDetail = layoutInflater.findViewById(R.id.main_detail_recycler)

        return layoutInflater
    }
}
package com.yong.aquamonitor.fragment

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import com.yong.aquamonitor.R
import com.yong.aquamonitor.activity.ConnectActivity
import com.yong.aquamonitor.activity.ProfileEditActivity

class ProfileFragment: Fragment() {
    private var btnBluetooth: ConstraintLayout? = null
    private var btnEdit: ConstraintLayout? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val layoutInflater = inflater.inflate(R.layout.fragment_profile, container, false)
        btnBluetooth = layoutInflater.findViewById(R.id.main_profile_btn_bluetooth)
        btnEdit = layoutInflater.findViewById(R.id.main_profile_btn_edit)

        btnBluetooth!!.setOnClickListener(btnListener)
        btnEdit!!.setOnClickListener(btnListener)

        return layoutInflater
    }

    private val btnListener = View.OnClickListener { view ->
        when(view.id) {
            R.id.main_profile_btn_bluetooth -> {
                startActivity(Intent(requireActivity(), ConnectActivity::class.java))
            }

            R.id.main_profile_btn_edit -> {
                startActivity(Intent(requireActivity(), ProfileEditActivity::class.java))
            }
        }
    }
}
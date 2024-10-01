package com.yong.aquamonitor.fragment

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.yong.aquamonitor.R
import com.yong.aquamonitor.activity.ConnectActivity
import com.yong.aquamonitor.activity.ProfileEditActivity
import com.yong.aquamonitor.util.PreferenceUtil

class ProfileFragment: Fragment() {
    private var btnBluetooth: ConstraintLayout? = null
    private var btnEdit: ConstraintLayout? = null
    private var tvName: TextView? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val layoutInflater = inflater.inflate(R.layout.fragment_profile, container, false)
        btnBluetooth = layoutInflater.findViewById(R.id.main_profile_btn_bluetooth)
        btnEdit = layoutInflater.findViewById(R.id.main_profile_btn_edit)
        tvName = layoutInflater.findViewById(R.id.main_profile_name)

        btnBluetooth!!.setOnClickListener(btnListener)
        btnEdit!!.setOnClickListener(btnListener)

        return layoutInflater
    }

    override fun onResume() {
        super.onResume()

        tvName!!.text = PreferenceUtil.getProfileName(requireActivity())
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
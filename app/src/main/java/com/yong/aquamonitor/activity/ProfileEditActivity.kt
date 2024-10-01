package com.yong.aquamonitor.activity

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.RadioGroup.OnCheckedChangeListener
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.yong.aquamonitor.R
import com.yong.aquamonitor.util.PreferenceUtil

class ProfileEditActivity : AppCompatActivity() {
    private var btnSave: Button? = null
    private var chkDiet: CheckBox? = null
    private var chkLactation: CheckBox? = null
    private var inputAge: EditText? = null
    private var inputCalorie: EditText? = null
    private var inputName: EditText? = null
    private var radioGender: RadioGroup? = null

    private var isDiet = false
    private var isLactation = false
    private var isMale = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_profile_edit)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.profile_edit)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        btnSave = findViewById(R.id.profile_edit_btn_save)
        chkDiet = findViewById(R.id.profile_edit_check_diet)
        chkLactation = findViewById(R.id.profile_edit_check_lactation)
        inputAge = findViewById(R.id.profile_edit_input_age)
        inputCalorie = findViewById(R.id.profile_edit_input_diet_calorie)
        inputName = findViewById(R.id.profile_edit_input_name)
        radioGender = findViewById(R.id.profile_edit_radio_gender)

        btnSave!!.setOnClickListener(btnListener)
        chkDiet!!.setOnClickListener(chkListener)
        chkLactation!!.setOnClickListener(chkListener)
        radioGender!!.setOnCheckedChangeListener(radioListener)
    }

    private val btnListener = View.OnClickListener { view ->
        when(view.id) {
            R.id.profile_edit_btn_save -> {
                if(inputAge!!.text.isNullOrEmpty() || inputName!!.text.isNullOrEmpty()) {
                    return@OnClickListener
                }

                val age = inputAge!!.text.toString().toInt()
                var target: Int
                if(age <= 8) {
                    target = 800
                } else if(age <= 11) {
                    target = 900
                } else if(age <= 14) {
                    target = if(isMale) 1100 else 900
                } else if(age <= 18) {
                    target = if(isMale) 1200 else 900
                } else if(age <= 29) {
                    target = if(isMale) 1200 else 1000
                } else if(age <= 49) {
                    target = if(isMale) 1200 else 1000
                } else if(age <= 64) {
                    target = 1000
                } else if(age <= 74) {
                    target = if(isMale) 1000 else 900
                } else {
                    target = if(isMale) 1100 else 1000
                }

                if(!isMale && isLactation) {
                    target += 200
                }

                if(!isLactation && isDiet && !inputCalorie!!.text.isNullOrEmpty()) {
                    target -= (inputCalorie!!.text.toString().toInt() * 0.53).toInt()
                }

                PreferenceUtil.saveProfileData(target, inputName!!.text.toString(), applicationContext)
                finish()
            }
        }
    }

    private val chkListener = View.OnClickListener { view ->
        if(view.id == R.id.profile_edit_check_diet) {
            isDiet = (view as CheckBox).isChecked
            if(isDiet) {
                inputCalorie!!.isEnabled = true
            } else {
                inputCalorie!!.isEnabled = false
            }
        } else if(view.id == R.id.profile_edit_check_lactation) {
            isLactation = (view as CheckBox).isChecked
            if(isLactation) {
                chkDiet!!.isChecked = false
                chkDiet!!.isEnabled = false
                inputCalorie!!.isEnabled = false
            } else {
                chkDiet!!.isEnabled = true
            }
        }
    }

    private val radioListener = OnCheckedChangeListener { group, checked ->
        isMale = (checked == R.id.profile_edit_radio_gender_male)
        if(isMale) {
            chkLactation!!.isChecked = false
            chkLactation!!.isEnabled = false
        } else {
            chkLactation!!.isEnabled = true
        }
    }
}
package com.example.exam1

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {
    // Views
    private lateinit var edtMSSV: TextInputEditText
    private lateinit var edtFullName: TextInputEditText
    private lateinit var rgGender: RadioGroup
    private lateinit var rbMale: RadioButton
    private lateinit var rbFemale: RadioButton
    private lateinit var edtEmail: TextInputEditText
    private lateinit var edtPhone: TextInputEditText
    private lateinit var edtBirthday: TextInputEditText
    private lateinit var spinnerProvince: Spinner
    private lateinit var spinnerDistrict: Spinner
    private lateinit var spinnerWard: Spinner
    private lateinit var cbSports: CheckBox
    private lateinit var cbMovies: CheckBox
    private lateinit var cbMusic: CheckBox
    private lateinit var cbAgreement: CheckBox
    private lateinit var btnSubmit: Button

    // Helper objects
    private lateinit var addressHelper: AddressHelper
    private var currentProvince: String = ""
    private var currentDistrict: String = ""
    private val calendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initializeViews()
        setupDatePicker()
        setupAddress()
        setupSubmitButton()
    }

    private fun initializeViews() {
        // Ánh xạ các view từ layout
        edtMSSV = findViewById(R.id.edtMSSV)
        edtFullName = findViewById(R.id.edtFullName)
        rgGender = findViewById(R.id.rgGender)
        rbMale = findViewById(R.id.rbMale)
        rbFemale = findViewById(R.id.rbFemale)
        edtEmail = findViewById(R.id.edtEmail)
        edtPhone = findViewById(R.id.edtPhone)
        edtBirthday = findViewById(R.id.edtBirthday)
        spinnerProvince = findViewById(R.id.spinnerProvince)
        spinnerDistrict = findViewById(R.id.spinnerDistrict)
        spinnerWard = findViewById(R.id.spinnerWard)
        cbSports = findViewById(R.id.cbSports)
        cbMovies = findViewById(R.id.cbMovies)
        cbMusic = findViewById(R.id.cbMusic)
        cbAgreement = findViewById(R.id.cbAgreement)
        btnSubmit = findViewById(R.id.btnSubmit)
    }

    private fun setupDatePicker() {
        val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month)
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            updateDateInView()
        }

        edtBirthday.setOnClickListener {
            DatePickerDialog(
                this,
                dateSetListener,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
    }

    private fun updateDateInView() {
        val format = "dd/MM/yyyy"
        val sdf = SimpleDateFormat(format, Locale.getDefault())
        edtBirthday.setText(sdf.format(calendar.time))
    }

    private fun setupAddress() {
        // Khởi tạo AddressHelper
        addressHelper = AddressHelper(resources)

        // Setup Province Spinner
        val provinces = addressHelper.getProvinces()
        val provinceAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, provinces)
        provinceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerProvince.adapter = provinceAdapter

        // Setup District Spinner (ban đầu rỗng)
        val districtAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, mutableListOf<String>())
        districtAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerDistrict.adapter = districtAdapter

        // Setup Ward Spinner (ban đầu rỗng)
        val wardAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, mutableListOf<String>())
        wardAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerWard.adapter = wardAdapter

        // Xử lý sự kiện chọn tỉnh/thành
        spinnerProvince.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                currentProvince = provinces[position]
                updateDistricts()
                // Clear phường/xã khi đổi tỉnh/thành
                (spinnerWard.adapter as ArrayAdapter<String>).clear()
                (spinnerWard.adapter as ArrayAdapter<String>).notifyDataSetChanged()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Không làm gì
            }
        }

        // Xử lý sự kiện chọn quận/huyện
        spinnerDistrict.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                currentDistrict = (parent?.getItemAtPosition(position) as? String) ?: ""
                updateWards()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Không làm gì
            }
        }
    }

    private fun updateDistricts() {
        val districts = addressHelper.getDistricts(currentProvince)
        val adapter = spinnerDistrict.adapter as ArrayAdapter<String>
        adapter.clear()
        adapter.addAll(districts)
        adapter.notifyDataSetChanged()
    }

    private fun updateWards() {
        val wards = addressHelper.getWards(currentProvince, currentDistrict)
        val adapter = spinnerWard.adapter as ArrayAdapter<String>
        adapter.clear()
        adapter.addAll(wards)
        adapter.notifyDataSetChanged()
    }

    private fun setupSubmitButton() {
        btnSubmit.setOnClickListener {
            if (validateForm()) {
                val formData = collectFormData()
                showSuccessDialog(formData)
            }
        }
    }

    private fun validateForm(): Boolean {
        var isValid = true
        val errorMessage = StringBuilder()

        // Validate MSSV
        if (edtMSSV.text.isNullOrEmpty()) {
            errorMessage.append("- Vui lòng nhập MSSV\n")
            isValid = false
        }

        // Validate Họ tên
        if (edtFullName.text.isNullOrEmpty()) {
            errorMessage.append("- Vui lòng nhập họ tên\n")
            isValid = false
        }

        // Validate Giới tính
        if (rgGender.checkedRadioButtonId == -1) {
            errorMessage.append("- Vui lòng chọn giới tính\n")
            isValid = false
        }

        // Validate Email
        if (edtEmail.text.isNullOrEmpty()) {
            errorMessage.append("- Vui lòng nhập email\n")
            isValid = false
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(edtEmail.text.toString()).matches()) {
            errorMessage.append("- Email không hợp lệ\n")
            isValid = false
        }

        // Validate Số điện thoại
        if (edtPhone.text.isNullOrEmpty()) {
            errorMessage.append("- Vui lòng nhập số điện thoại\n")
            isValid = false
        } else if (!android.util.Patterns.PHONE.matcher(edtPhone.text.toString()).matches()) {
            errorMessage.append("- Số điện thoại không hợp lệ\n")
            isValid = false
        }

        // Validate Ngày sinh
        if (edtBirthday.text.isNullOrEmpty()) {
            errorMessage.append("- Vui lòng chọn ngày sinh\n")
            isValid = false
        }

        // Validate địa chỉ
        if (currentProvince.isEmpty() || currentDistrict.isEmpty() || spinnerWard.selectedItem == null) {
            errorMessage.append("- Vui lòng chọn đầy đủ địa chỉ\n")
            isValid = false
        }

        // Validate Sở thích (ít nhất một sở thích)
        if (!cbSports.isChecked && !cbMovies.isChecked && !cbMusic.isChecked) {
            errorMessage.append("- Vui lòng chọn ít nhất một sở thích\n")
            isValid = false
        }

        // Validate Đồng ý điều khoản
        if (!cbAgreement.isChecked) {
            errorMessage.append("- Vui lòng đồng ý với điều khoản\n")
            isValid = false
        }

        if (!isValid) {
            Toast.makeText(this, errorMessage.toString(), Toast.LENGTH_LONG).show()
        }

        return isValid
    }

    private fun collectFormData(): String {
        val gender = when (rgGender.checkedRadioButtonId) {
            R.id.rbMale -> "Nam"
            R.id.rbFemale -> "Nữ"
            else -> ""
        }

        val interests = mutableListOf<String>()
        if (cbSports.isChecked) interests.add("Thể thao")
        if (cbMovies.isChecked) interests.add("Điện ảnh")
        if (cbMusic.isChecked) interests.add("Âm nhạc")

        return """
            MSSV: ${edtMSSV.text}
            Họ tên: ${edtFullName.text}
            Giới tính: $gender
            Email: ${edtEmail.text}
            Số điện thoại: ${edtPhone.text}
            Ngày sinh: ${edtBirthday.text}
            Địa chỉ: ${spinnerWard.selectedItem}, $currentDistrict, $currentProvince
            Sở thích: ${interests.joinToString(", ")}
        """.trimIndent()
    }

    private fun showSuccessDialog(formData: String) {
        android.app.AlertDialog.Builder(this)
            .setTitle("Đăng ký thành công")
            .setMessage(formData)
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
                resetForm()
            }
            .show()
    }

    private fun resetForm() {
        edtMSSV.text?.clear()
        edtFullName.text?.clear()
        rgGender.clearCheck()
        edtEmail.text?.clear()
        edtPhone.text?.clear()
        edtBirthday.text?.clear()
        spinnerProvince.setSelection(0)
        cbSports.isChecked = false
        cbMovies.isChecked = false
        cbMusic.isChecked = false
        cbAgreement.isChecked = false
    }
}
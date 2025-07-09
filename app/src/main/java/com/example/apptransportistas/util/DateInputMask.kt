package com.example.apptransportistas.util

import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText

class DateInputMask(private val input: EditText) {

    private var isUpdating = false

    init {
        input.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (isUpdating || s == null) return

                val clean = s.toString().replace(Regex("[^\\d]"), "")
                val formatted = StringBuilder()
                isUpdating = true

                for (i in clean.indices) {
                    if (i == 2 || i == 4) formatted.append("/")
                    if (i < 8) formatted.append(clean[i])
                }

                input.setText(formatted.toString())
                input.setSelection(formatted.length.coerceAtMost(input.text.length))
                isUpdating = false
            }
        })
    }
}


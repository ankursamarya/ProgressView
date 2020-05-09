package com.ankursamarya.progressview

import android.os.Bundle
import android.text.InputFilter
import android.text.Spanned
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.ankursamarya.progressviewlib.ProgressView


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initViews()

    }

    private fun initViews() {
        val progressView = findViewById<ProgressView>(R.id.progressView);

        val progressInput = findViewById<EditText>(R.id.progressInput);
        progressInput.filters = arrayOf<InputFilter>(InputFilterMinMax("0", "100"))

        val animateButton = findViewById<Button>(R.id.animButton);
        animateButton.setOnClickListener {
            if (progressInput.text.isEmpty()) {
                Toast.makeText(
                    applicationContext,
                    getString(R.string.please_enter_progress),
                    Toast.LENGTH_LONG
                ).show()
                return@setOnClickListener
            }
            progressView.setProgress(progressInput.text.toString().toInt())
        }
    }

    private class InputFilterMinMax : InputFilter {
        private var min: Int
        private var max: Int

        constructor(min: Int, max: Int) {
            this.min = min
            this.max = max
        }

        constructor(min: String, max: String) {
            this.min = min.toInt()
            this.max = max.toInt()
        }

        override fun filter(
            source: CharSequence,
            start: Int,
            end: Int,
            dest: Spanned,
            dstart: Int,
            dend: Int
        ): CharSequence? {
            try {
                val input = (dest.toString() + source.toString()).toInt()
                if (isInRange(min, max, input)) return null
            } catch (nfe: NumberFormatException) {
            }
            return ""
        }

        private fun isInRange(a: Int, b: Int, c: Int): Boolean {
            return if (b > a) c >= a && c <= b else c >= b && c <= a
        }
    }
}

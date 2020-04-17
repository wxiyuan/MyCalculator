package com.dev.mycalculator

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import java.text.NumberFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initClickListener()
    }

    private fun initClickListener() {
        text_clear.setOnClickListener { view -> onClick(view) }
        text_back.setOnClickListener { view -> onClick(view) }
        text_divide.setOnClickListener { view -> onClick(view) }
        text_multiply.setOnClickListener { view -> onClick(view) }
        text_minus.setOnClickListener { view -> onClick(view) }
        text_add.setOnClickListener { view -> onClick(view) }
        text_equal.setOnClickListener { view -> onClick(view) }
        text_percent.setOnClickListener { view -> onClick(view) }
        text_dot.setOnClickListener { view -> onClick(view) }
        text_0.setOnClickListener { view -> onClick(view) }
        text_1.setOnClickListener { view -> onClick(view) }
        text_2.setOnClickListener { view -> onClick(view) }
        text_3.setOnClickListener { view -> onClick(view) }
        text_4.setOnClickListener { view -> onClick(view) }
        text_5.setOnClickListener { view -> onClick(view) }
        text_6.setOnClickListener { view -> onClick(view) }
        text_7.setOnClickListener { view -> onClick(view) }
        text_8.setOnClickListener { view -> onClick(view) }
        text_9.setOnClickListener { view -> onClick(view) }
    }

    private fun onClick(view : View) {
        var shouldAppend = false
        when(view) {
            // Clear button clicked, reset text to "0"
            text_clear -> {
                history_text.text = ""
                result_text.text = "0"
            }
            // Backspace button clicked, remove the last char
            text_back -> onBackspaceClick()
            // Equal button clicked, calculate result and display it
            text_equal -> onEqualClick()
            // +-*/ button click
            text_add,
            text_minus,
            text_multiply,
            text_divide -> shouldAppend = onOperatorClick(view as TextView)
            // Percent button clicked
            text_percent -> shouldAppend = onPercentClick()
            // Dot button clicked
            text_dot -> shouldAppend = onDotClick()
            // Number(0-9) button clicked
            else -> {
                shouldAppend = true
            }
        }
        if (shouldAppend) {
            val input = (view as TextView).text
            val result = result_text.text
            if (isCleared() && input != ".") {
                result_text.text = input
            } else {
                setResultWithScroll(result.toString().plus(input))
            }
        }
    }

    private fun setResultWithScroll(result : String) {
        result_text.text = result
        scroll_view.post {
            scroll_view?.fullScroll(View.FOCUS_DOWN)
        }
    }

    private fun onBackspaceClick() {
        if (isCleared()) {
            return
        }
        result_text.text = with(result_text.text) {
            when {
                length == 1 -> "0"
                ',' == get(length - 2) -> substring(0, length - 2)
                else -> substring(0, length - 1)
            }
        }
    }

    private fun onOperatorClick(view : TextView) : Boolean {
        if (isCleared() && view.text != "-") {
            return false
        }
        if (result_text.text.toString().matches(Regex(".*[-+×÷.]"))) {
            return false
        }
        return true
    }

    private fun onPercentClick() : Boolean {
        if (isCleared()) {
            return false
        }
        return result_text.text.matches(Regex(".*[0-9]$"))
    }

    private fun onDotClick() : Boolean {
        return result_text.text.matches(Regex(".*[0-9]$"))
    }

    private fun isCleared() : Boolean {
        return "0" == result_text.text || result_text.text.isEmpty()
    }

    private fun onEqualClick() {
        if (isCleared()) {
            return
        }
        if (Regex("[-+×÷.]").matches(result_text.text.last().toString())) {
            Toast.makeText(this, "Expression error!", Toast.LENGTH_SHORT).show()
            return
        }
        history_text.text = result_text.text.toString().plus("=")
        var expression = result_text.text.toString()
            .replace("%", "÷100")
            .replace("-", "+-")
            .replace(",", "").let {
                if (it.startsWith("+")) it.substring(1, it.length) else it
            }
        expression = performMultiplyAndDivide(expression)
        expression = performAdd(expression)
        setResultWithScroll(formatResult(expression))
    }

    private fun performMultiplyAndDivide(expression: String) : String {
        return performCalculation(expression, "-?(\\d+)(\\.?\\d*)[×÷]-?(\\d+)(\\.?\\d*)")
    }

    private fun performAdd(expression: String) : String {
        return performCalculation(expression, "-?(\\d+)(\\.?\\d*)\\+-?(\\d+)(\\.?\\d*)")
    }

    private fun performCalculation(expression: String, pattern: String) : String {
        var newExpression = expression
        var temp = Regex(pattern).find(newExpression) // 3*3 6/2 1+2 48-90
        while (temp != null) {
            newExpression = newExpression.replace(temp.value, performSingleCalculation(temp.value))
            temp = Regex(pattern).find(newExpression)
        }
        return newExpression
    }

    private fun performSingleCalculation(singleExpression : String) : String {
        var singleResult = 0.0
        when {
            singleExpression.matches(Regex("-?(\\d+)(\\.?\\d*)\\+-?(\\d+)(\\.?\\d*)")) -> {
                singleResult = singleExpression.let {
                    val numList = it.split("+")
                    numList[0].toDouble() + numList[1].toDouble()
                }
            }
            singleExpression.matches(Regex("-?(\\d+)(\\.?\\d*)×-?(\\d+)(\\.?\\d*)")) -> {
                singleResult = singleExpression.let {
                    val numList = it.split("×")
                    numList[0].toDouble() * numList[1].toDouble()
                }
            }
            singleExpression.matches(Regex("-?(\\d+)(\\.?\\d*)÷-?(\\d+)(\\.?\\d*)")) -> {
                singleResult = singleExpression.let {
                    val numList = it.split("÷")
                    numList[0].toDouble() / numList[1].toDouble()
                }
            }
        }
        return singleResult.toString()
    }

    private fun formatResult(resultString : String) : String {
        val numberFormat = NumberFormat.getInstance(Locale.getDefault()).apply {
            maximumFractionDigits = 16 // Same as double
        }
        return numberFormat.format(resultString.toDouble()).toString()
    }
}

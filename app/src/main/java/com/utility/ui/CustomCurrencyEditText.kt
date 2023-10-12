package com.utility.ui

import android.telephony.PhoneNumberUtils.formatNumber
import android.text.Editable
import android.text.InputFilter
import android.text.Spanned
import android.text.TextWatcher
import android.widget.EditText
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.utility.extensions.Amount
import com.utility.extensions.formatCurrencyBigDecimal
import com.utility.extensions.formatCurrencyNoSymbol
import com.utility.extensions.toSafeAmount
import java.lang.ref.WeakReference
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.regex.Matcher
import java.util.regex.Pattern

typealias BeforeTextChanged = (s: CharSequence, start: Int, count: Int, after: Int) -> Unit
typealias OnTextChanged = (s: CharSequence, start: Int, before: Int, count: Int) -> Unit
typealias AfterTextChanged = (editable: Editable) -> Unit

class CustomTextWatcher(
    private val beforeTextChanged: BeforeTextChanged? = null,
    private val onTextChanged: OnTextChanged? = null,
    private val afterTextChanged: AfterTextChanged? = null,
) : TextWatcher {
    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
        beforeTextChanged?.invoke(s, start, count, after)
    }

    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        onTextChanged?.invoke(s, start, before, count)
    }

    override fun afterTextChanged(editable: Editable) {
        afterTextChanged?.invoke(editable)
    }
}

class CustomCurrencyEditText(editText: EditText?) : TextWatcher {
    private val editTextWeakReference: WeakReference<EditText?> = WeakReference<EditText?>(editText)
    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
    }

    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
    }

    override fun afterTextChanged(editable: Editable) {
        val editText: EditText = editTextWeakReference.get() ?: return
        val s = editable.toString()
        if (s.isEmpty()) return
        editText.removeTextChangedListener(this)

        val cleanString = s.replace("[₦,.]".toRegex(), "")
        val parsed: BigDecimal = BigDecimal(cleanString).setScale(2, RoundingMode.FLOOR)
            .divide(BigDecimal(100), RoundingMode.FLOOR)
        val formatted: String = formatCurrencyBigDecimal(parsed)
        editText.setText(formatted)
        editText.setSelection(formatted.length)
        editText.addTextChangedListener(this)
    }

}

class CustomAmountEditTextLifeCycleAware(editText: EditText?,
                                         private val lifecyle: Lifecycle,
                                         private val allowDecimal: Boolean = false,
                                         private val action: (value: Double) -> Unit = {}): TextWatcher, LifecycleObserver{
    private val editTextWeakReference: WeakReference<EditText?> = WeakReference<EditText?>(editText)
    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
    }

    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        println("inside data")
    }

    override fun afterTextChanged(editable: Editable) {
        if(allowDecimal)
            assignView2(editable)
        else
            assignView(editable)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    private fun assignView(editable: Editable){
        val editText: EditText = editTextWeakReference.get() ?: return
        val s = editable.toString()
        if (s.isEmpty()){
            setCallback(0.0)
            return
        }
        editText.removeTextChangedListener(this)

        val cleanString = s.replace("[₦,.]".toRegex(), "")
        val parsed: BigDecimal = BigDecimal(cleanString).setScale(2, RoundingMode.FLOOR)

        val formatted: String = formatCurrencyNoSymbol(parsed.toDouble()).takeWhile { t -> t != '.' }

        editText.setText(formatted)
        editText.setSelection(formatted.length)
        editText.addTextChangedListener(this)

        setCallback(cleanString.toSafeAmount())
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    private fun assignView2(editable: Editable){
        val editText: EditText = editTextWeakReference.get() ?: return
        val s = editable.toString()
        if (s.isEmpty()){
            setCallback(0.0)
            return
        }
        editText.removeTextChangedListener(this)

        val cleanString = s.replace("[₦,]".toRegex(), "")
        val formatted = formatNumber(cleanString)

        editText.setText(formatted)
        editText.setSelection(formatted.length)
        editText.addTextChangedListener(this)

        setCallback(formatted.replace("[₦,]".toRegex(), "").toSafeAmount())
    }


    private fun setCallback(s: Amount){
        if(lifecyle.currentState.isAtLeast(Lifecycle.State.RESUMED))
            action(s)
    }

}

class DecimalDigitsInputFilter(digitsBeforeZero: Int, digitsAfterZero: Int) :
    InputFilter {
    private var mPattern: Pattern = Pattern.compile("[0-9]{0," + (digitsBeforeZero - 1) + "}+((\\.[0-9]{0," + (digitsAfterZero - 1) + "})?)||(\\.)?")
    override fun filter(
        source: CharSequence,
        start: Int,
        end: Int,
        dest: Spanned,
        dstart: Int,
        dend: Int
    ): CharSequence? {
        val matcher: Matcher = mPattern.matcher(dest)
        return if (!matcher.matches()) "" else null
    }

}
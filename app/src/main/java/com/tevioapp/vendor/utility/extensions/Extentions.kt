package com.tevioapp.vendor.utility.extensions

import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.TextPaint
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.text.style.StrikethroughSpan
import android.text.style.StyleSpan
import android.text.style.TypefaceSpan
import android.text.style.UnderlineSpan
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.text.DecimalFormat


fun Location.getLatLng(): LatLng {
    return LatLng(latitude, longitude)
}

fun Location.getLatLngObject(): JSONObject {
    return JSONObject().apply {
        put("latitude", latitude)
        put("longitude", longitude)
    }
}

fun <T> T.toApplicationJsonBody(): RequestBody {
    val json = when (this) {
        is JSONObject -> this.toString() // If it's a JSONObject, directly convert it to a string
        else -> Gson().toJson(this) // For other data classes, use Gson to convert it to a JSON string
    }
    return json.toRequestBody("application/json".toMediaTypeOrNull()) // Convert JSON string to RequestBody
}

@Deprecated("Use setMultiSpan with span Config instead")
fun String.setMultiSpan(
    color: Int? = null,
    bold: Boolean = false,
    underLine: Boolean = false,
    strikeThrough: Boolean = false,
    onClick: (() -> Unit)? = null
): SpannableStringBuilder {
  return  setMultiSpan(SpanConfig(
        color = color,
        bold = bold,
        underline = underLine,
        strikeThrough = strikeThrough,
        onClick = onClick
    ))
}


fun <T> Intent?.findParcelData(name: String?, clazz: Class<T>): T? {
    if (this == null) return null
    return this.extras?.findParcelData(name, clazz)
}

fun <T> Intent?.findParcelDataList(name: String?, clazz: Class<T>): List<T>? {
    if (this == null) return null
    return this.extras?.findParcelDataList(name, clazz)
}

fun <T> Bundle?.findParcelData(name: String?, clazz: Class<T>): T? {
    return try {
        if (this == null || name.isNullOrEmpty()) return null
        this.classLoader = clazz.classLoader
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            this.getParcelable(name, clazz)
        } else {
            @Suppress("DEPRECATION")
            this.getParcelable(name) as? T
        }
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}


fun <T> Bundle?.findParcelDataList(name: String?, clazz: Class<T>): List<T>? {
    if (this == null) return null
    return try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            this.getParcelableArrayList(name, clazz)
        } else this.getParcelableArrayList(name)
    } catch (e: Exception) {
        null
    }
}


fun Double?.formatMeterToKm(): String? {
    if (this == null) return null
    val meter = this.toInt()
    if (meter < 100) {
        return buildString {
            append(meter)
            append(" m")
        }
    }
    val distanceInKilometers = this / 1000.0
    return DecimalFormat("#,##,##0.#").format(distanceInKilometers) + " km"
}

inline fun <reified T> String?.toListOf(): List<T> {
    val data = this.orEmpty()
    return try {
        if (data.isNotEmpty()) {
            val type = TypeToken.getParameterized(List::class.java, T::class.java).type
            Gson().fromJson(data, type)
        } else {
            emptyList()
        }
    } catch (e: JsonSyntaxException) {
        println("Error deserializing JSON: ${e.localizedMessage}")
        emptyList()
    }
}

fun LatLng?.asCommaSeparatedString(): String? {
    if (this == null) return null
    return buildString {
        append(latitude)
        append(",")
        append(longitude)
    }
}

fun String?.asLatLng(): LatLng? {
    val txtArray = orEmpty().split(",")
    val lat = txtArray.getOrNull(0)?.toDoubleOrNull()
    val lng = txtArray.getOrNull(1)?.toDoubleOrNull()
    return if (lat != null && lng != null) {
        LatLng(lat, lng)
    } else null
}


inline fun <reified T> String?.fromJsonToList(): List<T> {
    val list = mutableListOf<T>()
    try {
        if (this.isNullOrEmpty().not()) {
            val type = object : TypeToken<List<T>>() {}.type
            list.addAll(Gson().fromJson(this, type))
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return list
}

fun String.capitalizeFirstLetter(): String {
    return this.replaceFirstChar {
        if (it.isLowerCase()) it.titlecase() else it.toString()
    }
}

inline fun <reified T> T.toJsonObject(): JSONObject {
    val gson = Gson()
    return JSONObject(gson.toJson(this))
}

fun String?.maskLastChars(visibleCharLength: Int = 4, maskChar: Char = '*'): String {
    val input = this.orEmpty()
    if (input.length <= visibleCharLength) return input
    val maskLength = input.length - visibleCharLength
    return maskChar.toString().repeat(maskLength) + input.takeLast(visibleCharLength)
}

fun LifecycleOwner.getContext(): Context? {
    return when (this) {
        is Context -> this                  // Activity
        is Fragment -> this.context         // Fragment (nullable)
        else -> null
    }
}

/**
 * Small config holder to reduce number of function parameters.
 */
data class SpanConfig(
    val color: Int? = null,
    val bold: Boolean = false,
    val underline: Boolean = false,
    val strikeThrough: Boolean = false,
    val typeface: Typeface? = null,
    val onClick: (() -> Unit)? = null
)

/**
 * Extension that applies multiple spans using a single config object.
 * Params: config, start, end -> total parameters <= 3.
 */
fun String.setMultiSpan(
    config: SpanConfig = SpanConfig(),
    start: Int = 0,
    end: Int = this.length
): SpannableStringBuilder {
    if (isEmpty()) return SpannableStringBuilder(this)

    val safeStart = start.coerceIn(0, this.length)
    val safeEnd = end.coerceIn(safeStart, this.length)

    val spannable = SpannableStringBuilder(this)
    if (safeStart >= safeEnd) return spannable

    config.color?.let {
        spannable.setSpan(
            ForegroundColorSpan(it),
            safeStart, safeEnd,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
    }

    if (config.bold) {
        spannable.setSpan(
            StyleSpan(Typeface.BOLD),
            safeStart, safeEnd,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
    }

    if (config.underline) {
        spannable.setSpan(
            UnderlineSpan(),
            safeStart, safeEnd,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
    }

    if (config.strikeThrough) {
        spannable.setSpan(
            StrikethroughSpan(),
            safeStart, safeEnd,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
    }

    config.typeface?.let { tf ->
        spannable.setSpan(
            object : TypefaceSpan("") {
                override fun updateDrawState(ds: TextPaint) = applyTypeface(ds, tf)
                override fun updateMeasureState(ds: TextPaint) = applyTypeface(ds, tf)
                private fun applyTypeface(paint: TextPaint, tf: Typeface) {
                    val oldStyle = paint.typeface?.style ?: 0
                    val fake = oldStyle and tf.style.inv()
                    if (fake and Typeface.BOLD != 0) paint.isFakeBoldText = true
                    if (fake and Typeface.ITALIC != 0) paint.textSkewX = -0.25f
                    paint.typeface = tf
                }
            },
            safeStart, safeEnd,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
    }

    config.onClick?.let { click ->
        spannable.setSpan(object : ClickableSpan() {
            override fun onClick(widget: View) = click()
            override fun updateDrawState(ds: TextPaint) {
                // keep underline only if explicitly requested in config
                ds.isUnderlineText = config.underline
            }
        }, safeStart, safeEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
    }

    return spannable
}



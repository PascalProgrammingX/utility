package com.utility.extensions

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import androidx.biometric.BiometricManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.ResponseBody
import java.io.ByteArrayOutputStream
import java.math.BigDecimal
import java.text.NumberFormat

typealias Amount = Double

// Check if a string is numeric
fun isNumeric(toCheck: String): Boolean {
    val regex = "-?\\d+(\\.\\d+)?".toRegex()
    return toCheck.matches(regex)
}

fun String.capitalizeFirstCharOnly(): String {
    if (isEmpty()) return this
    return this[0].toUpperCase() + substring(1).toLowerCase()
}

fun Any?.toSafeAmount(): Amount{
    return try{
        val cleanString = this?.toString()?.replace("[$₦,]".toRegex(), "")
        cleanString?.toDouble() ?: 0.0
    }catch (ex: Exception){
        ex.printStackTrace()
        0.0
    }
}

//Replace char
fun replaceChars(input: String, length:Int): String {
    val replacement = "#".repeat(length) // Create a string of "#" with a length of 15
    return replacement + input.substring(length)
}

fun formatCurrencyBigDecimal(amt: BigDecimal?): String{
    val ft = NumberFormat.getCurrencyInstance()
    val amount = amt ?: 0.0
    return "₦${ft.format(amount).substring(1)}"
}

fun formatCurrencyNoSymbol(amt: Double?): String{
    val ft = NumberFormat.getCurrencyInstance()
    val amount = amt ?: 0.0
    return if(amount < 0)
        "-${ft.format(amount).substring(2)}"
    else
        ft.format(amount).substring(1)
}

fun formatCurrencyDollarSymbol(amt: Amount?): String{
    val ft = NumberFormat.getCurrencyInstance()
    val amount = amt ?: 0.0
    //return ft.format(amount)
    return if(amount < 0)
        "$-${ft.format(amount).substring(2)}"
    else
        "$${ft.format(amount).substring(1)}"
}


/**
 * Convert a base64 image string to a bitmap.
 */
fun base64StringToBitmap(encodedImage:String): Bitmap {
    val decodedString: ByteArray = Base64.decode(encodedImage, Base64.DEFAULT)
    // First, decode the dimensions of the image without loading the full bitmap into memory
    val options = BitmapFactory.Options().apply {
        inJustDecodeBounds = true
        BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size, this)

        // Calculate the sample size based on the requested dimensions
        inSampleSize = calculateInSampleSize(outWidth, outHeight, 200, 200)

        // Decode the bitmap with the calculated sample size
        inJustDecodeBounds = false
    }
    return BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size, options)
}
fun calculateInSampleSize(width: Int, height: Int, reqWidth: Int, reqHeight: Int): Int {
    var inSampleSize = 1
    if (height > reqHeight || width > reqWidth) {
        val halfHeight = height / 2
        val halfWidth = width / 2

        while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
            inSampleSize *= 2
        }
    }
    return inSampleSize
}


/**
 * This function takes an image path and converts image to Base64 String.
 * @param path
 * @return Base64 Image
 */
fun imageToBase64(image: Bitmap): String {
    val baos = ByteArrayOutputStream()
    image.compress(Bitmap.CompressFormat.JPEG, 60, baos) // bm is the bitmap object
    val b = baos.toByteArray()
    return Base64.encodeToString(b, Base64.DEFAULT)
}

// This function is useful when you use getContent to pick image from gallery which returns a uri.
fun decodeBitmapFromUri(uri: Uri, cr: ContentResolver): Bitmap? {
    val inputStream = cr.openInputStream(uri)
    return BitmapFactory.decodeStream(inputStream)
}


//Check if device has fingerprint support
fun hasFingerPrint(context: Context):Boolean {
    var status = false
    val biometricManager = BiometricManager.from(context)
    BiometricManager.Authenticators.BIOMETRIC_STRONG
    when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG or
            BiometricManager.Authenticators.BIOMETRIC_WEAK)) {
        BiometricManager.BIOMETRIC_SUCCESS -> {
            status = true
        }
        BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
            status = false
        }
        BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE ->{
            status = false
        }
        BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
            status = false
        }
        BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED -> {
            status = false
        }
        BiometricManager.BIOMETRIC_ERROR_UNSUPPORTED -> {
            status = false
        }
        BiometricManager.BIOMETRIC_STATUS_UNKNOWN -> {
            status = false
        }
    }
    return status
}



inline fun <reified T> ResponseBody?.fromJson(): T? {
    if (this == null) return null

    val gson = Gson()
    val jsonString = this.string()

    return gson.fromJson(jsonString, object : TypeToken<T>() {}.type)
}


//Convert JSON response to a data classs
fun <T>genericClassCast(value: Any?, baseClass: Class<T>): T?{
    return if(value != null){
        try{
            val gson = Gson()
            gson.fromJson(gson.toJson(value),baseClass)
        }catch (ex: Exception){
            ex.printStackTrace()
            null
        }

    }else
        null
}

//Convert Json array to data class
fun <T> genericClassListCast(value: Any?, baseclass: Class<T>): List<T?>?{
    return try{
        val gson = Gson()
        val type = TypeToken.getParameterized(List::class.java, baseclass).type
        gson.fromJson<List<T>>(gson.toJson(value), type)
    }catch (ex: Exception){
        ex.printStackTrace()
        null
    }
}
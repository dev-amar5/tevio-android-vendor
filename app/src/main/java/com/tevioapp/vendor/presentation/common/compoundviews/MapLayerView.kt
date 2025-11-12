package com.tevioapp.vendor.presentation.common.compoundviews

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import com.google.android.gms.maps.GoogleMap
import com.tevioapp.vendor.R
import com.tevioapp.vendor.injection.helper.BaseEntryPoint
import com.tevioapp.vendor.utility.popups.OptionPopupWindow
import dagger.hilt.android.EntryPointAccessors

class MapLayerView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : AppCompatImageView(context, attrs, defStyleAttr) {

    private var _googleMap: GoogleMap? = null
    private val baseEntryPoint: BaseEntryPoint by lazy {
        EntryPointAccessors.fromApplication(
            context.applicationContext, BaseEntryPoint::class.java
        )
    }

    init {
        setImageResource(R.drawable.ic_map_layer)
        setOnClickListener {
            _googleMap?.let { map ->
                val pairList = arrayListOf(
                    Pair("Normal", GoogleMap.MAP_TYPE_NORMAL),
                    Pair("Satellite", GoogleMap.MAP_TYPE_SATELLITE),
                    Pair("Terrain", GoogleMap.MAP_TYPE_TERRAIN),
                    Pair("Hybrid", GoogleMap.MAP_TYPE_HYBRID),
                )
                OptionPopupWindow(
                    context = context,
                    dataList = pairList,
                    getTitle = { bean -> bean.first },
                    isSelected = { bean -> map.mapType == bean.second },
                    onOptionClick = { bean, _ ->
                        baseEntryPoint.getSharePref().setGoogleMapType(bean.second)
                        refreshView()
                    }).showBelowLeft(this)
            }
        }
    }

    fun refreshView() {
        _googleMap?.apply {
            mapType = baseEntryPoint.getSharePref().getGoogleMapType()
            val mapType = _googleMap?.mapType ?: GoogleMap.MAP_TYPE_NORMAL
            if (mapType == GoogleMap.MAP_TYPE_NORMAL) {
                backgroundTintList = null
                setColorFilter(ContextCompat.getColor(context, R.color.black))
            } else {
                ViewCompat.setBackgroundTintList(
                    this@MapLayerView,
                    ColorStateList.valueOf(ContextCompat.getColor(context, R.color.orange))
                )
                setColorFilter(Color.WHITE)
            }
        }
    }

    fun setGoogleMap(googleMap: GoogleMap) {
        _googleMap = googleMap
        refreshView()
    }

}
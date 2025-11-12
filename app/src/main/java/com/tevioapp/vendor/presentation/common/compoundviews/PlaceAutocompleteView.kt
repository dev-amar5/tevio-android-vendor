package com.tevioapp.vendor.presentation.common.compoundviews

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.FrameLayout
import androidx.core.view.isVisible
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.gson.Gson
import com.tevioapp.vendor.R
import com.tevioapp.vendor.data.common.UserLocation
import com.tevioapp.vendor.databinding.HolderPlaceItemBinding
import com.tevioapp.vendor.databinding.ViewPlaceAutoCompleteBinding
import com.tevioapp.vendor.presentation.BaseApp
import com.tevioapp.vendor.presentation.common.base.BaseViewModel
import com.tevioapp.vendor.presentation.common.base.adapter.QuickAdapter
import com.tevioapp.vendor.utility.extensions.getLatLng
import com.tevioapp.vendor.utility.extensions.setVerticalSpacingDecorator
import com.tevioapp.vendor.utility.extensions.toListOf
import com.tevioapp.vendor.utility.log.Logger
import com.tevioapp.vendor.utility.rx.RxSearch


class PlaceAutocompleteView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {
    private val etSearch: EditText
    private lateinit var rxSearch: RxSearch
    private val binding =
        ViewPlaceAutoCompleteBinding.inflate(LayoutInflater.from(context), this, true)
    private var placesClient: PlacesClient? = null

    private val predictionsAdapter =
        QuickAdapter<AutocompletePrediction>(context, getBinding = { parent, _ ->
            HolderPlaceItemBinding.inflate(LayoutInflater.from(context), parent, false)
        }, onItemClick = { _, _, bean, _ ->
            rxSearch.clearFocus()
            binding.rvList.isVisible = false
            fetchPlaceDetails(bean.placeId)
        })

    private var onPlaceSelected: ((UserLocation) -> Boolean)? = null

    init {
        etSearch = binding.etSearch
    }


    fun activateAutoCompleteView(
        viewModel: BaseViewModel, onPlaceSelected: (UserLocation) -> Boolean
    ) {
        placesClient = Places.createClient(context)
        this.onPlaceSelected = onPlaceSelected
        rxSearch = RxSearch(viewModel, etSearch) {
            updateResults(it)
        }
    }

    init {
        binding.rvList.setVerticalSpacingDecorator(R.color.line_dual)
        binding.rvList.adapter = predictionsAdapter
        binding.rvList.isVisible = false
        etSearch.setHint(R.string.search_for_area_street_name)
    }

    private fun updateResults(query: String) {
        if (isInEditMode) return  // Skip if in edit mode
        val request = FindAutocompletePredictionsRequest.builder().setQuery(query)
        BaseApp.instance.apply {
            request.setCountries(getCountryCode())
            lastLocation?.let {
                request.setOrigin(it.getLatLng())
            }
        }
        placesClient?.findAutocompletePredictions(request.build())
            ?.addOnSuccessListener { response ->
                binding.rvList.isVisible = true
                val animate = predictionsAdapter.itemCount == 0
                predictionsAdapter.setItemList(response.autocompletePredictions)
                if (animate) binding.rvList.scheduleLayoutAnimation()
            }?.addOnFailureListener { exception ->
                exception.printStackTrace()
                Logger.e("Error: ${exception.message}")
                binding.rvList.isVisible = false
            }
    }

    private fun fetchPlaceDetails(placeId: String) {
        if (isInEditMode) return  // Skip if in edit mode
        val placeFields = listOf(Place.Field.LAT_LNG)
        val request = FetchPlaceRequest.newInstance(placeId, placeFields)
        placesClient?.fetchPlace(request)?.addOnSuccessListener { response ->
            response.place.location?.let { latLng ->
                val userLocation = UserLocation.fromLatLng(context, latLng) ?: return@let
                saveRecentSearch(userLocation)
                onPlaceSelected?.invoke(userLocation)?.let {
                    if (it) {
                        rxSearch.setText(null, false)
                    } else {
                        rxSearch.setText(userLocation.title, false)
                    }
                }
            }
        }?.addOnFailureListener { exception ->
            exception.printStackTrace()
            Logger.e("Error: ${exception.message}")
        }
    }

    var onFocusChange: ((Boolean) -> Unit)? = null
        set(value) {
            field = value
            etSearch.setOnFocusChangeListener { _, hasFocus ->
                value?.invoke(hasFocus)
            }
        }

    fun resetSearch(): Boolean {
        if (etSearch.hasFocus() || rxSearch.getText().isNotEmpty()) {
            rxSearch.clearFocus()
            rxSearch.setText(null)
            binding.rvList.isVisible = false
            return true
        }
        return false
    }

    fun enableSearch() {
        etSearch.requestFocus()
    }

    fun getSearchView(): EditText {
        return etSearch
    }

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(context.packageName, MODE_PRIVATE)

    private fun saveRecentSearch(location: UserLocation) {
        val recentSearches = getRecentSearches().toMutableList()
        recentSearches.removeAll { it.title == location.title }
        recentSearches.add(0, location)
        val jsonString = Gson().toJson(recentSearches.take(10))
        sharedPreferences.edit().putString("recent_search", jsonString).apply()
    }

    fun getRecentSearches(): List<UserLocation> {
        return sharedPreferences.getString("recent_search", null).toListOf<UserLocation>()
    }


}

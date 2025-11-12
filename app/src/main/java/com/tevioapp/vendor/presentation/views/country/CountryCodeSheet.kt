package com.tevioapp.vendor.presentation.views.country

import androidx.fragment.app.viewModels
import androidx.room.Entity
import com.google.gson.annotations.SerializedName
import com.tevioapp.vendor.R
import com.tevioapp.vendor.databinding.ItemCountryCodeBinding
import com.tevioapp.vendor.databinding.SheetCountryCodeBinding
import com.tevioapp.vendor.presentation.common.base.BaseViewModel
import com.tevioapp.vendor.presentation.common.base.adapter.QuickAdapter
import com.tevioapp.vendor.presentation.common.base.sheet.BaseBottomSheet
import com.tevioapp.vendor.utility.rx.RxSearch
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CountryCodeSheet(private val onSelect: (CountryCode) -> Unit) :
    BaseBottomSheet<SheetCountryCodeBinding>() {
    private val viewModel: CountryCodeSheetVM by viewModels()
    private lateinit var adapter: QuickAdapter<CountryCode>
    private lateinit var rxSearch: RxSearch
    override fun getLayoutResource(): Int {
        return R.layout.sheet_country_code
    }

    override fun getViewModel(): BaseViewModel {
        return viewModel
    }

    override fun onSheetCreated(binding: SheetCountryCodeBinding) {
        rxSearch = RxSearch(viewModel, binding.etSearch) {
            viewModel.getCountryList(it)
        }
        adapter = QuickAdapter(requireContext(), getBinding = { parent, _ ->
            ItemCountryCodeBinding.inflate(layoutInflater, parent, false)
        }, onItemClick = { _, _, bean, _ ->
            dismiss()
            onSelect.invoke(bean)
        }, onBindView = { b, _, _ ->
            (b as ItemCountryCodeBinding).searchedText = rxSearch.getText()
        })
        adapter.addToRecyclerView(binding.rvOne)
        viewModel.onClick.observe(viewLifecycleOwner) {
            when (it.id) {
                R.id.iv_cross -> {
                    dismiss()
                }
            }
        }
        viewModel.obrCountryCode.observe(viewLifecycleOwner) { list ->
            adapter.setItemList(list)
        }
        viewModel.getCountryList()
    }

    override fun onStart() {
        super.onStart()
        extendToFullHeight()
    }
}

@Entity(tableName = "tb_country", primaryKeys = ["code"])
data class CountryCode(
    @SerializedName("code") val code: String,
    @SerializedName("dial_code") val dialCode: String,
    @SerializedName("flag") val flag: String?,
    @SerializedName("name") val name: String?,
    @SerializedName("min_length") val minLength: Int?,
    @SerializedName("max_length") val maxLength: Int?
) {
    fun getFlagUrl(): String {
        return "https://flagcdn.com/48x36/${code.lowercase()}.png"
    }

    fun getDialCodeWithOutSign(): String {
        return dialCode.removePrefix("+")
    }

}
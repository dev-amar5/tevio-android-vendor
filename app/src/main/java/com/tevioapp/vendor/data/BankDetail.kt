package com.tevioapp.vendor.data


import com.google.gson.annotations.SerializedName

data class BankDetail(
    @SerializedName("account_holders_name")
    var accountHoldersName: String?,
    @SerializedName("account_number")
    var accountNumber: String?,
    @SerializedName("account_type")
    var accountType: String?,
    @SerializedName("bank_name")
    var bankName: String?,
    @SerializedName("branch_location")
    var branchLocation: String?,
    @SerializedName("branch_name")
    var branchName: String?,
    @SerializedName("destination_branch_code")
    var destinationBranchCode: Any?,
    @SerializedName("id")
    var id: String?,
    @SerializedName("payout_info_id")
    var payoutInfoId: String?,
    @SerializedName("routing_number")
    var routingNumber: String?,
    @SerializedName("swift_code")
    var swiftCode: String?
)
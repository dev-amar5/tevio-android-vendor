package com.tevioapp.vendor.data


import com.google.gson.annotations.SerializedName

data class DocumentResponse(
    @SerializedName("identity_proof") var identityProofList: List<IdentityProof>?,
    @SerializedName("vehicle_details") var vehicleInfo: VehicleInfo?
)
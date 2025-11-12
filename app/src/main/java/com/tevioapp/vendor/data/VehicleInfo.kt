package com.tevioapp.vendor.data


import com.google.gson.annotations.SerializedName

data class VehicleInfo(
    @SerializedName("id") var id: String?,
    @SerializedName("vehicle_name") var vehicleName: String?,
    @SerializedName("license_plate_number") var licensePlateNumber: String?,
    @SerializedName("motor_insurance_card") var motorInsuranceCard: String?,
    @SerializedName("motor_insurance_expiry_date") var motorInsuranceExpiryDate: String?,
    @SerializedName("motor_insurance_number") var motorInsuranceNumber: String?,
    @SerializedName("vehicle_fitness") var vehicleFitness: String?,
    @SerializedName("vehicle_type") var vehicleType: String?
)
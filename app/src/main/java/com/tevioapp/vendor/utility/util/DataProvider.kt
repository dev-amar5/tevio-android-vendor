package com.tevioapp.vendor.utility.util

import android.content.Context
import android.location.Location
import androidx.annotation.CheckResult
import com.tevioapp.vendor.data.local.CourierStatus
import com.tevioapp.vendor.presentation.BaseApp
import com.tevioapp.vendor.utility.Enums
import dagger.hilt.android.qualifiers.ApplicationContext
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.BehaviorSubject
import java.util.Optional
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.jvm.optionals.getOrNull

@Singleton
class DataProvider @Inject constructor(
    @ApplicationContext val context: Context
) {
    var courierStatus: CourierStatus? = null

    // Subjects always have default values
    private val subjectLocation = BehaviorSubject.createDefault(Optional.empty<Location>())
    private val subjectActiveOrder = BehaviorSubject.createDefault(emptyList<String>())

    /** Emit user location */
    fun emitLocation(location: Location?) {
        subjectLocation.onNext(Optional.ofNullable(location))
    }

    /** Add or remove a single active order */
    fun emitActiveOrder(orderId: String, isActive: Boolean) {
        val updated = subjectActiveOrder.value.orEmpty().toMutableSet()
        if (isActive) updated.add(orderId) else updated.remove(orderId)
        subjectActiveOrder.onNext(updated.toList())
    }

    /** Replace all active orders */
    fun emitActiveOrders(newList: List<String>) {
        subjectActiveOrder.onNext(newList.distinct())
    }

    /** Returns true if there is at least one active order */
    fun hasActiveOrder(): Boolean = subjectActiveOrder.value.orEmpty().isNotEmpty()

    /** Subscribe to active orders */
    @CheckResult
    fun subscribeToActiveOrders(
        onActiveOrdersChange: (List<String>) -> Unit
    ): Disposable =
        subjectActiveOrder.startWith(subjectActiveOrder.value.orEmpty()) // emit initial list
            .observeOn(AndroidSchedulers.mainThread()) // everything on UI thread
            .subscribe(onActiveOrdersChange)

    /** Subscribe to user location */
    @CheckResult
    fun subscribeToUserLocation(
        onLocationChange: (Location?) -> Unit
    ): Disposable = subjectLocation.startWith(Optional.ofNullable(BaseApp.instance.lastLocation))
        .observeOn(AndroidSchedulers.mainThread()) // everything on UI thread
        .subscribe { item -> onLocationChange(item.getOrNull()) }

    /** Vehicle types */
    fun getVehicleTypes(): List<Pair<String, String>> = listOf(
        Enums.VEHICLE_TYPE_MOTORCYCLE to "Motorcycle",
        Enums.VEHICLE_TYPE_CAR to "Car",
        Enums.VEHICLE_TYPE_BICYCLE to "Bicycle"
    )

    /** Payment methods */
    fun getPaymentMethodTypes(): List<Pair<String, String>> = listOf(
        Enums.PAYMENT_METHOD_BANK to "Bank", Enums.PAYMENT_METHOD_MOBILE_MONEY to "Mobile Money"
    )

    /** Network types */
    fun getNetworkTypes(): List<Pair<String, String>> = listOf(
        Enums.NETWORK_MTN to "MTN Money",
        Enums.NETWORK_AIRTEL to "Airtel",
        Enums.NETWORK_TIGO to "Tigo",
        Enums.NETWORK_VODAFONE to "Vodafone",
        Enums.NETWORK_OTHER to "Other"
    )

    /** Network types */
    fun getRoles(): List<Pair<String, String>> = listOf(
        Enums.ROLE_ADMIN to "Admin",
        Enums.ROLE_CUSTOMER to "Customer",
        Enums.ROLE_COURIER to "Courier",
        Enums.ROLE_MERCHANT to "Merchant"
    )
}

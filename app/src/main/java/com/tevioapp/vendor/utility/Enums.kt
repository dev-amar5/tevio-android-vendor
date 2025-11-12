package com.tevioapp.vendor.utility

object Enums {
    const val ROLE_ADMIN = "0"
    const val ROLE_CUSTOMER = "1"
    const val ROLE_COURIER = "2"
    const val ROLE_MERCHANT = "3"


    const val REGISTRATION_STATUS_PENDING = "pending"
    const val REGISTRATION_STATUS_IN_PROCESS = "in-process"
    const val REGISTRATION_STATUS_APPROVED = "approved"
    const val REGISTRATION_STATUS_REJECTED = "rejected"
    const val REGISTRATION_STATUS_SUBMITTED = "submitted"

    const val DOCUMENT_TYPE_DL = "2"
    const val DOCUMENT_TYPE_NATIONAL_ID = "1"
    const val DOCUMENT_TYPE_OTHER = "3"

    const val RIDER_STATUS_ONLINE = "1"
    const val RIDER_STATUS_OFFLINE = "2"

    const val GENDER_MALE = "1"
    const val GENDER_FEMALE = "2"

    const val TOKEN_TYPE_TEMP = "temp_token"
    const val TOKEN_TYPE_BEARER = "bearer_token"

    const val PAYMENT_METHOD_BANK = "1"
    const val PAYMENT_METHOD_MOBILE_MONEY = "2"


    const val NETWORK_MTN = "1"
    const val NETWORK_AIRTEL = "2"
    const val NETWORK_TIGO = "3"
    const val NETWORK_VODAFONE = "4"
    const val NETWORK_OTHER = "5"


    const val VEHICLE_TYPE_MOTORCYCLE = "1"
    const val VEHICLE_TYPE_CAR = "2"
    const val VEHICLE_TYPE_BICYCLE = "3"


    const val TICKET_STATUS_SUBMITTED = "0"

    const val ORDER_STATUS_PENDING = "0"
    const val ORDER_STATUS_CONFIRMED = "1"
    const val ORDER_STATUS_IN_PROCESS = "2" // preparing / in-process
    const val ORDER_STATUS_PROCESSED = "3" // prepared

    const val ORDER_STATUS_PACKED = "4"
    const val ORDER_STATUS_PICKED_UP = "5"
    const val ORDER_STATUS_OUT_FOR_DELIVERY = "6"
    const val ORDER_STATUS_DELIVERED = "7"

    const val ORDER_STATUS_CANCELLED = "8"
    const val ORDER_STATUS_REJECTED = "9"

    const val ORDER_STATUS_REFUND_INITIATED = "10"
    const val ORDER_STATUS_REFUND_IN_PROCESS = "11"
    const val ORDER_STATUS_REFUND_PROCESSED = "12"


    const val COURIER_STATUS_ASSIGNED = "1"
    const val COURIER_STATUS_GOING_TO_PICKUP = "2"
    const val COURIER_STATUS_ARRIVED_AT_PICKUP = "3"
    const val COURIER_STATUS_PICKED_UP = "4"
    const val COURIER_STATUS_OUT_FOR_DELIVERY = "5"
    const val COURIER_STATUS_ARRIVED_AT_DESTINATION = "6"
    const val COURIER_STATUS_WAITING_AT_DESTINATION = "7"
    const val COURIER_STATUS_DELIVERED = "8"


    const val CART_TYPE_FOOD_DELIVERY = "food"
    const val CART_TYPE_DROP_IT = "drop-it"
    const val CART_TYPE_GROCERY = "grocery"
    const val CART_TYPE_GROUP_ORDER = "group-order"

    const val CHAT_MESSAGE_TEXT = "0"
    const val CHAT_MESSAGE_IMAGE = "1"

    const val AUDIO_ROLE_CALLER = "caller"
    const val AUDIO_ROLE_RECEIVER = "receiver"

    const val CALL_VISIBLE = "call-visible"
    const val CALL_NOT_ANSWERED = "call-not-answered"
    const val CALL_DECLINED = "call-declined"
    const val CALL_ENDED = "call-ended"

}
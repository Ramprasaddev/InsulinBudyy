package com.saveetha.insulinbuddy.utils

object ApiConstants {
    // Base URL for your backend
    const val BASE_URL = "https://606tr6vg-80.inc1.devtunnels.ms/INSULIN/"

    // APIs
    const val SAVE_API = "${BASE_URL}save_entry.php"
    const val PREDICT_API = "${BASE_URL}get_prediction.php"

     // (formula flow, if you keep it)
        const val ML_PREDICT  = BASE_URL + "ml_predict.php"         // <-- NEW
    }



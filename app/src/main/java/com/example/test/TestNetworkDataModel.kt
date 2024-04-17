package com.example.test

import com.google.gson.annotations.SerializedName

data class TestNetworkDataModel(
    @SerializedName("address")
    var address:String

) {

    override fun toString(): String {
        return "TestNetworkDataModel(address='$address')"
    }

}
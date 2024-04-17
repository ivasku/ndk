package com.example.test

import com.google.gson.annotations.SerializedName

data class TestNewtorkResponseModel(
    @SerializedName("nat")
    var nat:String

) {

    override fun toString(): String {
        return "TestNetworkDataModel(address='$nat')"
    }

}
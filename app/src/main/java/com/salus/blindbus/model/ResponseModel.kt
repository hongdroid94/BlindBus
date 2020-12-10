package com.salus.blindbus.model

import com.google.gson.annotations.SerializedName

/**
 * Search API 를 통해 데이터를 받기위해 데이터 클래스를 정의하였다.
 * 수신받는 데이터 중 사용하려고 하는 필드들만 구현해도 상관 없다.
 */

class ResponseModel {
//    @SerializedName("total_count")
//    val totalCount: Int = 0
//
//    @SerializedName("items")
//    val items:List<RepoModel> = listOf()

    @SerializedName("success")
    val success : Boolean = false

    @SerializedName("userID")
    val userID: String = ""

    @SerializedName("userPassword")
    val userPassword: String = ""
}
package com.salus.blindbus.database.api

import com.salus.blindbus.database.creator.RetrofitCreator
import com.salus.blindbus.database.model.ResponseModel
import io.reactivex.Observable
import retrofit2.http.*

/**
 * API 클래스 정의..
 * 레트로핏을 통해 수신받는 데이터의 형태를 스트림 형태로 사용하기 위해서
 * Reactive를 활용하여 Observable 형태로 수신 받는다..
 * API 호출의 편의를 위해서 companion object 를 정의함.
 */

class MainApi {
    interface MainApiImpl {
        @FormUrlEncoded
        @POST("/salus_api/register.php")
        fun postRegister(@FieldMap map: HashMap<String, String>): Observable<ResponseModel>

        @FormUrlEncoded
        @POST("/salus_api/login.php")
        fun postLogin(@FieldMap map: HashMap<String, String>): Observable<ResponseModel>
    }

    companion object {
        fun postRegister(map: HashMap<String, String>): Observable<ResponseModel> {
            return RetrofitCreator.create(MainApiImpl::class.java).postRegister(map)
        }

        fun postLogin(map: HashMap<String, String>): Observable<ResponseModel> {
            return RetrofitCreator.create(MainApiImpl::class.java).postLogin(map)
        }
    }
}
package com.salus.blindbus.api

import com.salus.blindbus.creator.RetrofitCreator
import com.salus.blindbus.model.ResponseModel
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
        @GET("/search/repositories")
        fun getRepoList(@Query("q") query: String): Observable<ResponseModel>
    }

    interface TestApiImpl {
        @FormUrlEncoded
        @POST("/salus_api/register.php")
        fun postRegister(@FieldMap map: HashMap<String, String>): Observable<ResponseModel>
    }

    companion object {
        fun getRepoList(query: String): Observable<ResponseModel> {
            return RetrofitCreator.create(MainApiImpl::class.java).getRepoList(query)
        }

        fun postRegister(map: HashMap<String, String>): Observable<ResponseModel> {
            return RetrofitCreator.create(TestApiImpl::class.java).postRegister(map)
        }
    }
}
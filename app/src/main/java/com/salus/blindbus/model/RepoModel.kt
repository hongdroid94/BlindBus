package com.salus.blindbus.model

import com.google.gson.annotations.SerializedName

/**
 * Search API 를 통해 데이터를 받기위해 데이터 클래스를 정의하였다.
 * 수신받는 데이터 중 사용하려고 하는 필드들만 구현해도 상관 없다.
 */

/**
    API 완료된것 ::
    로그인, 회원가입, 아이디중복검사, 이메일중복검사
    ( 모든 HTTP Request -> POST )
    URL 및 파라미터
    http://3.34.61.169/salus_api/register.php
    $user_id = $_POST["userID"];
    $user_pw = $_POST["userPassword"];
    $user_name = $_POST["userName"];
    $user_age = $_POST["userAge"]; // int
    $user_gender = $_POST["userGender"];
    $user_type = $_POST["userType"];
    $parent_id = $_POST["parentID"];
    $child_id = $_POST["childID"];
    $email_address = $_POST["emailAddress"];
    $email_verify = $_POST["emailVerify"];
    $phone_number= $_POST["phoneNumber"];
    $fcm_token = $_POST["fcm_token"];
    $social_login_name = $_POST["social_login_name"];
    $beacon_uuid = $_POST["beacon_uuid"];
    http://3.34.61.169/salus_api/login.php
    $userID = $_POST["userID"];
    $userPassword = $_POST["userPassword"];
    http://3.34.61.169/salus_api/user_id_verify.php
    $userID = $_POST["userID"];
    http://3.34.61.169/salus_api/user_email_verify.php
    $emailAddress = $_POST["email_address"];
 */
class RepoModel {
//    @SerializedName("id")
//    val id: Long = 0
//
//    @SerializedName("name")
//    val name: String = ""
//
//    @SerializedName("full_name")
//    val fullName: String = ""
    @SerializedName("userID")
    val userID : String = ""

    @SerializedName("userPassword")
    val userPassword : String = ""

    @SerializedName("emailVerify")
    val emailVerify : String = ""

}

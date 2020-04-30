package com.example.w3d3_rxjavaretrofitexample.network

import com.example.w3d3_rxjavaretrofitexample.network.model.Price
import com.example.w3d3_rxjavaretrofitexample.network.model.Ticket

import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {

    @GET("airline-tickets-price.php")
    fun priceSeats(
        @Query("flight_number") flight_number: String,
        @Query("from") from: String,
        @Query("to") to: String
    ): Single<Price>


    @GET("airline-tickets.php")
    fun tickets(   @Query("from") from: String,
                   @Query("to") to: String): Single<MutableList<Ticket>>

}
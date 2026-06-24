package com.perftop.android.data.remote.api

import com.perftop.android.data.remote.dto.CompareResponse
import com.perftop.android.data.remote.dto.FilterOptionsResponse
import com.perftop.android.data.remote.dto.HardwareDetailResponse
import com.perftop.android.data.remote.dto.HardwareListResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface PerfTopApi {

    @GET("hardwares")
    suspend fun getHardwares(
        @Query("category") category: String,
        @Query("sort_by") sortBy: String = "overall_score",
        @Query("order") order: String = "desc",
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 20,
        @Query("brand") brand: String? = null,
        @Query("architecture") architecture: String? = null,
        @Query("core_min") coreMin: Int? = null,
        @Query("core_max") coreMax: Int? = null,
        @Query("year_min") yearMin: Int? = null,
        @Query("year_max") yearMax: Int? = null
    ): HardwareListResponse

    @GET("hardwares/{id}")
    suspend fun getHardwareDetail(
        @Path("id") id: Int
    ): HardwareDetailResponse

    @GET("hardwares/compare")
    suspend fun getCompareData(
        @Query("ids") ids: String
    ): CompareResponse

    @GET("hardwares/search")
    suspend fun searchHardwares(
        @Query("q") query: String,
        @Query("category") category: String? = null,
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 20
    ): HardwareListResponse

    @GET("meta/filters")
    suspend fun getFilterOptions(
        @Query("category") category: String
    ): FilterOptionsResponse

    @GET("hardwares/export/all")
    suspend fun exportAllData(): HardwareListResponse
}

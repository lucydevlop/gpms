package io.glnt.gpms.common.api

import io.glnt.gpms.common.api.Pagination
import io.glnt.gpms.common.api.ResultCode
import io.glnt.gpms.common.api.calculatePage
import io.glnt.gpms.common.api.calculatePagesNumber
import kotlinx.serialization.Serializable

@Serializable
data class PaginationResult<T>(
    override val page: Int,
    val pagesNumber: Int,
    val data: List<T>,
    override val size: Int,
    val total: Long,
//    val isLast: Boolean,
//    val isFirst: Boolean,
    var code : Any? = ResultCode.SUCCESS.getCode()
) : Pagination

fun <T> emptyPaginationResult() = PaginationResult<T>(0, 0, emptyList(), 0, ResultCode.VALIDATE_FAILED.getCode())

fun <T> List<T>.createPaginationResult(
    pagination: Pagination,
    commonObjectsNumber: Long
) = PaginationResult(
    pagination.page,
    calculatePagesNumber(
        commonObjectsNumber,
        pagination.size
    ),
    this,
    pagination.size,
    getListSize(commonObjectsNumber)
)

fun <T> List<T>.createPaginationResult(
    firstIndex: Int,
    commonObjectsNumber: Long,
    totlaSize: Long
) = PaginationResult(
    calculatePage(firstIndex, size),
    calculatePagesNumber(
        commonObjectsNumber,
        size
    ),
    this,
    size,
    getListSize(commonObjectsNumber)
)

fun <T> Pair<Long, List<T>>.createPaginationResult(
    pagination: Pagination
) = second.createPaginationResult(pagination, first)
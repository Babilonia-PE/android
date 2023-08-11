package com.babilonia.domain.model

/** Created by Renso Contreras on 14/07/2021.
 * rensocontreras91@gmail.com
 * Lima, Peru.
 **/

data class Pagination(
    val currentPage: Int,
    val perPage: Int,
    val totalPages: Int)
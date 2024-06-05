package models

import enums.DaysOfWeek

data class Movie (
    val moviesName: String,
    val code: String,
    val startDate: String,
    val endDate: String,
    val schedule: List<DaysOfWeek>,
    val duration: Int,
    val showTime: List<ShowTime>
)
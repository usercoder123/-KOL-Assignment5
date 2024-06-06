package models

data class Theater(
    val theaterName: String,
    val address: String,
    val contact: String,
    val movies: ArrayList<Movie>,
    val price: List<Price>,
    val rooms: List<Room>
)
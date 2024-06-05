package dao

import models.Movie

interface TheaterDAO {
    fun showNowShowingMovies(showMovies: (List<Movie>) -> Unit)
    fun showIncomingMovies(showMovies: (List<Movie>) -> Unit)
    fun showAvailableMovies(showMovies: (List<Movie>) -> Unit)
}
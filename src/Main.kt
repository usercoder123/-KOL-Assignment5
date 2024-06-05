import enums.*
import dao.TheaterDAO
import models.Movie
import models.ShowTime
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.ArrayList
import kotlin.system.exitProcess


val movie1 = Movie(
    moviesName = "Inception",
    code = "INC123",
    startDate = "04/06/2024",
    endDate = "15/06/2024",
    schedule = listOf(DaysOfWeek.MONDAY, DaysOfWeek.WEDNESDAY, DaysOfWeek.FRIDAY),
    duration = 148,
    showTime = listOf(
        ShowTime("14:30", "2D", "Room 1"),
        ShowTime("19:45", "3D", "Room 2")
    )
)

val movie2 = Movie(
    moviesName = "The Matrix",
    code = "MTR456",
    startDate = "15/06/2024",
    endDate = "25/06/2024",
    schedule = listOf(DaysOfWeek.TUESDAY, DaysOfWeek.THURSDAY, DaysOfWeek.SATURDAY),
    duration = 136,
    showTime = listOf(
        ShowTime("16:00", "2D", "Room 3"),
        ShowTime("20:30", "IMAX", "Room 4")
    )
)

val movie3 = Movie(
    moviesName = "Avatar",
    code = "AVT789",
    startDate = "03/06/2024",
    endDate = "12/06/2024",
    schedule = listOf(DaysOfWeek.THURSDAY, DaysOfWeek.FRIDAY, DaysOfWeek.SUNDAY),
    duration = 162,
    showTime = listOf(
        ShowTime("13:00", "2D", "Room 5"),
        ShowTime("18:30", "3D", "Room 6")
    )
)

val movies = arrayListOf(movie1, movie2, movie3)


fun main() {
    homePage()
}

fun homePage(){
    val theaterDAO = object : TheaterDAO {
        override fun showNowShowingMovies(showMovies: (List<Movie>) -> Unit) {
            showMovies(movies.getNowShowingMovies())
        }

        override fun showIncomingMovies(showMovies: (List<Movie>) -> Unit) {
            val incomingMovies = movies.filter { !it.isMovieStarted() }
            showMovies(incomingMovies)
        }

        override fun showAvailableMovies(showMovies: (List<Movie>) -> Unit) {
            val availableMovies = movies.getNowShowingMovies().filter { it.isTodayInSchedule() }
            showMovies(availableMovies)
        }
    }

    /*  Movie information requirements
          -	Movie’s name
          -	Movie’s code
          -	Start date
          -	Duration
          -	Sort ascending by start_date
    */
    val showMovie: (List<Movie>) -> Unit = { movies ->
        movies.sortedBy { it.startDate }
            .forEach { println("- " + it.moviesName + ", " + it.code + ", " + it.startDate + ", " + it.duration) }
        println()
    }

    //Home page
    println("========================================")
    println("Welcome to -- BHD Movie Theater --")
    println("Address: 123 A Street")
    println("Contact: @24XXXXXXX")
    println("========================================\n\n")
    println("Please choose below option to continue:")
    println("1. View now showing movies")
    println("2. View coming soon movies")
    println("3. View available movies by data")
    println("4. View now showing movies")
    println("5. Exit")
    println("========================================\n\n")
    print("Option: ")
    val option = readlnOrNull()
    while (true) {
        when (option) {
            "1" -> {
                println("Now showing movies:")
                theaterDAO.showNowShowingMovies(showMovie)
            }

            "2" -> {
                println("Coming soon movies:")
                theaterDAO.showIncomingMovies(showMovie)
            }

            "3" -> {
                println("Available movies:")
                theaterDAO.showAvailableMovies(showMovie)
            }

            "4" -> bookTicket()

            "5" -> exitProcess(1)

            else -> println("invalid")
        }
    }
}

fun bookTicket() {
    print("Input code: ")
    val codeInput = readlnOrNull()
    if (!checkMovieCode(codeInput)) {
        homePage()
    } else {
        val targetMovie = codeInput?.let { movies.getMovieByCode(it) }
        if (targetMovie != null) {
            val scheduleList = targetMovie.getMovieShowingSchedule()
            scheduleList.forEach { println("${it.key}. ${it.value}") }
            println("0. Exit")
            println("========================================\n\n")
            print("Your option: ")
            val dateChoice = inputDateChoice(scheduleList)
//            println(dateChoice)
            println("Please select the show time that you want to book ticket:")
            targetMovie.showShowTime()
        }
    }
}

fun Movie.showShowTime(){
    this.showTime.forEach {
        
        println(" Time " + it.time + " - " + "Type: " + it.type)
    }
}

fun inputDateChoice(scheduleList: MutableMap<Int, String>): String? {
    val choice = readlnOrNull()
    if (choice?.let { validateDateChoice(it, scheduleList) } == true) {
        if(choice.toInt() != 0){
            return scheduleList[choice.toInt()]
        }
        else{
            homePage()
            return null
        }
    }
    print("Invalid input! Please try again: ")
    return inputDateChoice(scheduleList)
}

fun validateDateChoice(choice: String, scheduleList: MutableMap<Int, String>): Boolean =
    (choice.matches("^[0-9]*".toRegex()) && choice.toInt() in 0..scheduleList.size)

fun Movie.getMovieShowingSchedule(): MutableMap<Int, String> {
    val result = mutableMapOf<Int, String>()
    if (this.isNowShowing() && this.isTodayInSchedule()) {
        val currentDate = getCurrentDate()
            .toDate(DateFormat.DAY_MONTH_YEAR.formatter)
        val end = this.endDate.toDate(DateFormat.DAY_MONTH_YEAR.formatter)
        var i = 0
        var date = currentDate
        while (date <= end) {
            if (this.isWeekDayInSchedule(date)) {
                i++
                result[i] = date.format(DateTimeFormatter.ofPattern(DateFormat.DAY_MONTH_YEAR.formatter))
                    .toString()
            }
            date = date.plusDays(1)
            if (i == 7) {
                break
            }
        }
    }
    return result
}

fun checkMovieCode(codeInput: String?): Boolean {
    val movie = codeInput?.let { movies.getMovieByCode(it) }
    return !((movie == null) || (!movie.isMovieStarted()) || (movie.isMovieExpired()))
}

fun ArrayList<Movie>.getMovieByCode(code: String): Movie? {
    val result = this.filter { it.code.lowercase() == code.lowercase() }
    return if (result.isEmpty()) null else result[0]
}

fun Movie.isTodayInSchedule(): Boolean {
    val currentDate = Calendar.getInstance()
    val currentWeekDay = currentDate.get(Calendar.DAY_OF_WEEK)
    this.schedule.forEach { d ->
        if (currentWeekDay == d.index) {
            return true
        }
    }
    return false
}

fun Movie.isWeekDayInSchedule(date: LocalDate): Boolean {
    this.schedule.forEach {
        if (it.index == date.dayOfWeek.value) {
            return true
        }
    }
    return false
}

fun Movie.isMovieStarted(): Boolean {
    val currentDate = getCurrentDate()
    return this.startDate < currentDate
}

fun Movie.isMovieExpired(): Boolean {
    val currentDate = getCurrentDate()
    return this.endDate < currentDate
}

fun Movie.isNowShowing(): Boolean {
    val currentDate = getCurrentDate()
    return currentDate in (this.startDate..this.endDate)
}

fun ArrayList<Movie>.getNowShowingMovies(): List<Movie> {
    return this.filter { it.isNowShowing() }
}

fun Calendar.getTimeStringByFormat(format: String): String {
    val formatter = SimpleDateFormat(format)
    return formatter.format(this.time)
}

fun String.toDate(formatter: String): LocalDate {
    val dateFormatter = DateTimeFormatter.ofPattern(formatter).withLocale(Locale.getDefault())
    return LocalDate.parse(this, dateFormatter)
}

fun getCurrentDate() = Calendar.getInstance().getTimeStringByFormat(DateFormat.DAY_MONTH_YEAR.formatter)
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import enums.*
import dao.TheaterDAO
import models.Movie
import models.ShowTime
import models.Theater
import java.io.File
import java.lang.reflect.Type
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.ArrayList
import kotlin.system.exitProcess


//val movie1 = Movie(
//    moviesName = "Inception",
//    code = "INC123",
//    startDate = "04/06/2024",
//    endDate = "15/06/2024",
//    schedule = listOf(DaysOfWeek.MONDAY, DaysOfWeek.WEDNESDAY, DaysOfWeek.FRIDAY),
//    duration = 148,
//    showTime = listOf(
//        ShowTime("14:30", "2D", "Room 1"),
//        ShowTime("19:45", "3D", "Room 2")
//    )
//)
//
//val movie2 = Movie(
//    moviesName = "The Matrix",
//    code = "MTR456",
//    startDate = "15/06/2024",
//    endDate = "25/06/2024",
//    schedule = listOf(DaysOfWeek.TUESDAY, DaysOfWeek.THURSDAY, DaysOfWeek.SATURDAY),
//    duration = 136,
//    showTime = listOf(
//        ShowTime("16:00", "2D", "Room 3"),
//        ShowTime("20:30", "IMAX", "Room 4")
//    )
//)
//
//val movie3 = Movie(
//    moviesName = "Avatar",
//    code = "AVT789",
//    startDate = "03/06/2024",
//    endDate = "12/06/2024",
//    schedule = listOf(DaysOfWeek.THURSDAY, DaysOfWeek.FRIDAY, DaysOfWeek.SUNDAY),
//    duration = 162,
//    showTime = listOf(
//        ShowTime("13:00", "2D", "Room 5"),
//        ShowTime("18:30", "3D", "Room 6")
//    )
//)
// val movies = arrayListOf(movie1, movie2, movie3)

val jsonFile = File("resources/data.json")
val jsonString = jsonFile.readText()
val gson = Gson()
val theaterListType: Type = object : TypeToken<ArrayList<Theater>>() {}.type
val theaters: ArrayList<Theater> = gson.fromJson(jsonString, theaterListType)


val slots: Array<Array<String>> = arrayOf(
    arrayOf("A1", "A2", "A3", "A4", "A5", "A6", "A7", "A8", "A9"),
    arrayOf("B1", "B2", "B3", "B4", "B5", "B6", "B7", "B8", "B9"),
    arrayOf("C1", "C2", "C3", "C4", "C5", "C6", "C7", "C8", "C9"),
    arrayOf("D1", "D2", "D3", "D4", "D5", "D6", "D7", "D8", "D9"),
)


fun main() {
    homePage(theaters[0])
//    movies.forEach{ println(it) }
}

fun homePage(theater: Theater) {
    val theaterDAO = object : TheaterDAO {
        override fun showNowShowingMovies(showMovies: (List<Movie>) -> Unit) {
            showMovies(theater.movies.getNowShowingMovies())
        }

        override fun showIncomingMovies(showMovies: (List<Movie>) -> Unit) {
            val incomingMovies = theater.movies.filter { !it.isMovieStarted() }
            showMovies(incomingMovies)
        }

        override fun showAvailableMovies(showMovies: (List<Movie>) -> Unit) {
            val availableMovies = theater.movies.getNowShowingMovies().filter { it.isTodayInSchedule() }
            showMovies(availableMovies)
        }

        override fun bookTicket() {
            print("Input code: ")
            val codeInput = readlnOrNull()
            if (!checkMovieCode(theater.movies, codeInput)) {
                homePage(theater)
            } else {
                val targetMovie = codeInput?.let { theater.movies.getMovieByCode(it) }
                if (targetMovie != null) {
                    val scheduleList = targetMovie.getMovieShowingSchedule()
                    scheduleList.forEach { println("${it.key}. ${it.value}") }
                    println("0. Exit")
                    println("========================================\n\n")
                    print("Your option: ")
                    val dateChoice = inputDateChoice(theater, scheduleList)
                    println("Please select the show time that you want to book ticket:")
                    val timeList = targetMovie.getShowTime(dateChoice)
                    timeList.showShowTime()
                    print("Your option: ")
                    val timeChoice = inputTimeChoice(timeList)
                    if (timeChoice != null) {
                        val slotWithTypes = theater.mapSlotWithData(timeChoice)
                        println("           =======<<<<SCREEN>>>>=======\n")
                        println("       [A1, A2, A3, A4, A5, A6, A7, A8, A9]")
                        println("       [B1, 82, 83, 84, 85, 86, 87, 88, 89]")
                        println("       [C1, C2, C3, C4, C5, C6, C7, C8, C9]")
                        println("       [D1, D2, D3, D4, D5, D6, D7, D8, D9]")
                        print("\n\nVip slot: ")
                        val vipSlots = slotWithTypes.getVIPRooms()
                        vipSlots.forEach { print(it.key + " ") }
                        println("\n\nPlease select slot: ")
                        print("Your option (0 to exit): ")
                        val slot = theater.inputSlot(slotWithTypes)

                        println("Enter your info: ")
                        print("- Name: ")
                        val customer: String = inputCustomerInfo(enums.Regex.CUSTOMER_NAME.formatter)
                        print("- Phone number: ")
                        val phone: String = inputCustomerInfo(enums.Regex.PHONE_NUMBER.formatter)

                        println("========================================")
                        println("========================================")
                        println("-- Movies TICKET --")
                        println(theater.theaterName)
                        println("Address: " + theater.address)
                        println("Contact: " + theater.contact)
                        println("========================================")
                        println("Customer: $customer")
                        println("Phone number: $phone")
                        println("========================================")
                        println("Movie: " + targetMovie.moviesName)
                        println("Movie Type: " + timeChoice.type)
                        println("Affected date: $dateChoice")
                        println("Show Time: " + timeChoice.time)
                        print("Slot: $slot")
                        var price = theater.getRawPrice(timeChoice.type)
                        // for VIP slots
                        if (vipSlots.contains(slot)) {
                            print(" - VIP")
                            price += price * 20 / 100
                        }
                        println("\nPrice: $price")
                        println("========================================")
                        println("Hope you enjoy this movie.\n\n")
                        println("Press Enter key to continue...")
                        val enterKey = readln()
                        homePage(theater)
                    }
                }
            }
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

    //Menu
    println("========================================")
    println("Welcome to -- " + theater.theaterName + " --")
    println("Address: " + theater.address)
    println("Contact: " + theater.contact)
    println("========================================\n\n")
    println("Please choose below option to continue:")
    println("1. View now showing movies")
    println("2. View coming soon movies")
    println("3. View available movies by data")
    println("4. Book ticket")
    println("5. Exit")
    println("========================================\n\n")
    print("Option: ")
    val option = readlnOrNull()
    when (option) {
        "1" -> {
            println("Now showing movies:")
            theaterDAO.showNowShowingMovies(showMovie)
            homePage(theater)
        }

        "2" -> {
            println("Coming soon movies:")
            theaterDAO.showIncomingMovies(showMovie)
            homePage(theater)
        }

        "3" -> {
            println("Available movies:")
            theaterDAO.showAvailableMovies(showMovie)
            homePage(theater)
        }

        "4" -> theaterDAO.bookTicket()

        "5" -> exitProcess(1)

        else -> {
            println("invalid choice")
            homePage(theater)
        }
    }
}

fun inputCustomerInfo(regex: String): String{
    val input = readlnOrNull()
    if (input != null) {
        if(input.matches(regex.toRegex())){
            return input
        }
    }
    print("Invalid info! Please try again: ")
    return inputCustomerInfo(regex)
}

fun Theater.getRawPrice(type: String): Float{
    return this.price.filter { it.type == type }[0].price
}

fun Theater.inputSlot(slots: MutableMap<String, Int>): String {
    val input = readlnOrNull()
    if (input?.let { slots.isSlotInputValid(it) } == true) {
        if (input == "0") {
            homePage(this)
        } else {
            return input
        }
    }
    print("Invalid slot name! Please try again: ")
    return this.inputSlot(slots)
}

fun MutableMap<String, Int>.isSlotInputValid(input: String): Boolean {
    this.forEach {
        if (it.key.lowercase() == input.lowercase() || input == "0") {
            return true
        }
    }
    return false
}

fun MutableMap<String, Int>.getVIPRooms(): Map<String, Int> = this.filter { it.value == 1 }

fun Theater.mapSlotWithData(showtime: ShowTime): MutableMap<String, Int> {
    val result = mutableMapOf<String, Int>()
    val map = this.getMapByRoomName(showtime.room)
    for (i in map.indices) {
        for (j in map[i].indices) {
            result[slots[i][j]] = map[i][j]
        }
    }
    return result
}

fun Theater.getMapByRoomName(roomName: String): Array<Array<Int>> {
    val room = this.rooms.filter { it.name == roomName }[0]
    return room.map
}

fun inputTimeChoice(showTimes: List<ShowTime>): ShowTime? {
    val choice = readlnOrNull()
    if (choice?.let { validateTimeChoice(it, showTimes) } == true) {
        if (choice.toInt() != 0) {
            return showTimes[choice.toInt() - 1]
        } else {
            homePage(theaters[0])
            return null
        }
    }
    print("Invalid input! Please try again: ")
    return inputTimeChoice(showTimes)
}

fun validateTimeChoice(choice: String, showTimes: List<ShowTime>): Boolean =
    (choice.matches("^[0-9]*".toRegex()) && choice.toInt() in 0..showTimes.size)

fun List<ShowTime>.showShowTime() {
    var number = 1
    this.forEach {
        println("$number. Time: " + it.time + " - " + "Type: " + it.type)
        number++
    }
    println("0. Exit")
}

fun Movie.getShowTime(date: String?): List<ShowTime> {
    if (date == getCurrentDate()) {
        return this.showTime.sortedBy { it.time }.filter {
            it.time < Calendar.getInstance().getTimeStringByFormat(DateFormat.HOUR_MIN.formatter)
        }
    }
    return this.showTime.sortedBy { it.time }
}

fun inputDateChoice(theater: Theater, scheduleList: MutableMap<Int, String>): String? {
    val choice = readlnOrNull()
    if (choice?.let { validateDateChoice(it, scheduleList) } == true) {
        if (choice.toInt() != 0) {
            return scheduleList[choice.toInt()]
        } else {
            homePage(theater)
            return null
        }
    }
    print("Invalid input! Please try again: ")
    return inputDateChoice(theater, scheduleList)
}

fun validateDateChoice(choice: String, scheduleList: MutableMap<Int, String>): Boolean =
    (choice.matches("^[0-9]*".toRegex()) && choice.toInt() in 0..scheduleList.size)

fun Movie.getMovieShowingSchedule(): MutableMap<Int, String> {
    val result = mutableMapOf<Int, String>()
//    print(this.isNowShowing())
    if (this.isNowShowing()) {
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

fun checkMovieCode(movies: ArrayList<Movie>, codeInput: String?): Boolean {
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
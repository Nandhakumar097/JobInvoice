package zuper.dev.android.dashboard.data.remote

import android.annotation.SuppressLint
import zuper.dev.android.dashboard.data.model.InvoiceApiModel
import zuper.dev.android.dashboard.data.model.InvoiceStatus
import zuper.dev.android.dashboard.data.model.JobApiModel
import zuper.dev.android.dashboard.data.model.JobStatus
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale
import kotlin.random.Random

object SampleData {

    private val isoFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")

    fun generateRandomJobList(size: Int): List<JobApiModel> {
        val random = Random
        return (1..size).map {
            JobApiModel(
                jobNumber = it,
                title = generateRandomJobTitle(),
                startTime = LocalDateTime.now().plusDays(random.nextLong(1, 30))
                    .format(isoFormatter),
                endTime = LocalDateTime.now().plusDays(random.nextLong(31, 60))
                    .format(isoFormatter),
                status = when (random.nextInt(5)) {
                    0 -> JobStatus.YetToStart
                    1 -> JobStatus.InProgress
                    2 -> JobStatus.Canceled
                    3 -> JobStatus.Completed
                    else -> JobStatus.Incomplete
                }
            )
        }
    }

    fun generateRandomInvoiceList(size: Int): List<InvoiceApiModel> {
        val random = Random
        return (1..size).map {
            InvoiceApiModel(
                invoiceNumber = random.nextInt(1, Int.MAX_VALUE),
                customerName = generateRandomCustomerName(),
                total = random.nextInt(1, 10) * 1000,
                status = when (random.nextInt(4)) {
                    0 -> InvoiceStatus.Draft
                    1 -> InvoiceStatus.Pending
                    2 -> InvoiceStatus.Paid
                    else -> InvoiceStatus.BadDebt
                }
            )
        }
    }

    fun getRequiredDateFormat(jobApiModel: JobApiModel) : String {
        return dateFormatter(jobApiModel)
    }

    fun getCurrentDate() : String {
        val currentDate = LocalDate.now()
        val formatter = DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy", Locale.ENGLISH)
        val dayFormatter = DateTimeFormatter.ofPattern("d", Locale.ENGLISH)
        val currentDay = currentDate.format(dayFormatter)
        return currentDate.format(formatter).replace("$currentDay,","$currentDay${getDayOfMonthSuffix(currentDay.toInt())},")
    }
}

private fun generateRandomJobTitle(): String {
    val adjectives = listOf("Amazing", "Fantastic", "Awesome", "Incredible", "Superb")
    val nouns = listOf("Job", "Task", "Project", "Assignment", "Work")

    val randomAdjective = adjectives.random()
    val randomNoun = nouns.random()

    return "$randomAdjective $randomNoun"
}

fun generateRandomCustomerName(): String {
    val firstNames = listOf("John", "Jane", "Alice", "Bob", "Eva")
    val lastNames = listOf("Doe", "Smith", "Johnson", "Brown", "Lee")

    val randomFirstName = firstNames.random()
    val randomLastName = lastNames.random()

    return "$randomFirstName $randomLastName"
}

@SuppressLint("SimpleDateFormat")
private fun dateFormatter(jobApiModel : JobApiModel) : String{
    val finalDate: String
    val currentDateFormat = SimpleDateFormat("dd/MM/yyyy")
    val currentDate = currentDateFormat.format(Date())
    val dateFormatEnglish = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.ENGLISH)
    val startEndTimeFormat = DateTimeFormatter.ofPattern("hh:mm a", Locale.ENGLISH)
    val startDate = dateFormatEnglish.format(OffsetDateTime.parse(jobApiModel.startTime))
    val endDate = dateFormatEnglish.format(OffsetDateTime.parse(jobApiModel.endTime))
    val isStartToday = currentDate.equals(startDate)
    val isEndToday = currentDate.equals(endDate)
    val startTime = startEndTimeFormat.format(OffsetDateTime.parse(jobApiModel.startTime))
    val endTime = startEndTimeFormat.format(OffsetDateTime.parse(jobApiModel.endTime))
    finalDate = if (isStartToday && isEndToday) {
        "Today, $startTime - $endTime"
    }else if(isStartToday) {
        "Today, $startTime - $startDate $endTime"
    }else if(isEndToday) {
        "$startDate, $startTime - Today $endTime"
    } else{
        "$startDate, $startTime - $endDate $endTime"
    }

    return finalDate
}

fun getDayOfMonthSuffix(n: Int): String {
    if (n in 11..13) {
        return "th"
    }
    when (n % 10) {
        1 -> return "st"
        2 -> return "nd"
        3 -> return "rd"
        else -> return "th"
    }
}


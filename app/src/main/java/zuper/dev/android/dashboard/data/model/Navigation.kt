package zuper.dev.android.dashboard.data.model

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Face
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.google.accompanist.flowlayout.FlowRow
import com.google.accompanist.flowlayout.MainAxisAlignment
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import zuper.dev.android.dashboard.R
import zuper.dev.android.dashboard.data.remote.SampleData
import zuper.dev.android.dashboard.data.viewmodel.MyViewModel

@Composable
fun Navigation(myViewModel: MyViewModel) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Screen.MainScreen.route) {
        composable(route = Screen.MainScreen.route) {
            Dashboard(navController = navController, myViewModel = myViewModel)
        }
        composable(route = Screen.JobScreen.route + "/{name}",
            arguments = listOf(
                navArgument("name") {
                    type = NavType.StringType
                    defaultValue = ""
                    nullable = true
                }
            )
        ) { entry ->

            val userJson = entry.arguments?.getString("name")
            val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
            val dataListType = Types.newParameterizedType(List::class.java, JobApiModel::class.java)
            val jsonAdapter: JsonAdapter<List<JobApiModel>> = moshi.adapter(dataListType)
            val userObject = userJson?.let { jsonAdapter.fromJson(it) }

            if (userObject != null) {
                DetailsScreen(jobList = userObject, navController = navController)
            }
        }
    }
}

@SuppressLint("SuspiciousIndentation")
@Composable
fun ChartView(
    modifier: Modifier = Modifier,
    jobList: List<JobApiModel> = arrayListOf(),
    invoiceList: List<InvoiceApiModel> = arrayListOf(),
    isJob: Boolean,
    isDetailNeeded: Boolean = true
) {

    val jobStatus = jobList.map { it.status.name }.toSet()
    val invoiceStatus = invoiceList.map { it.status.name }.toSet()
    val statusMap = mutableMapOf<String, Int>()
    var totalJob = 0
    var completedCount = 0
    var paidAmount = 0

    if (isJob)
        jobStatus.forEach { status ->
            val count = jobList.filter { it.status.name == status }.size
            statusMap[status] = count
            totalJob += count
            if (status == JobStatus.Completed.name)
                completedCount = count
        }
    else {
        invoiceStatus.forEach { status ->
            val filteredList = invoiceList.filter { it.status.name == status }
            val count = filteredList.map { it.total }.reduce { acc, i -> acc + i }
            statusMap[status] = count
            totalJob += count
            if (status == InvoiceStatus.Paid.name)
                paidAmount = count
        }
    }

    var sortedMap = statusMap.toSortedMap(compareByDescending<String> { statusMap[it] }.thenByDescending { it })

    Column {
        Row(
            modifier = modifier
                .padding(start = 10.dp, end = 10.dp),
        ) {
            Text(
                text = if (isJob) "$totalJob Jobs" else "Total Value (#$totalJob)",
                modifier
                    .weight(1f),
                textAlign = TextAlign.Start
            )
            Text(
                text = if (isJob) "$completedCount of $totalJob Completed" else "#$paidAmount Collected",
                modifier
                    .weight(1f),
                textAlign = TextAlign.End
            )
        }
        var colorIndex = 0

        Row(
            modifier = modifier
                .padding(10.dp)
                .clip(shape = RoundedCornerShape(15.dp, 15.dp, 15.dp, 15.dp))
                .height(15.dp),
        ) {
            sortedMap.forEach { (s, _) ->
                val weightValue = sortedMap[s]?.toFloat() as Float
                Text(
                    text = "",
                    modifier
                        .weight(weight = weightValue / totalJob)
                        .background(color = getColor()[colorIndex])
                )
                colorIndex++
            }
        }

        var colorCount = 0

        if (isDetailNeeded)
            FlowRow(
                mainAxisSpacing = 10.dp,
                crossAxisSpacing = 10.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                mainAxisAlignment = MainAxisAlignment.Center,
            ) {
                sortedMap.forEach { (s, i) ->
                    GridData(modifier = modifier, data = s, i, colorCount, isJob)
                    colorCount++
                }
            }
    }

}

fun getColor(): List<Color> {
    return listOf(
        Color.Green,
        Color.Blue,
        Color.Magenta,
        Color.Yellow,
        Color.Red,
        Color.Blue,
        Color.Gray
    )
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun DetailsScreen(jobList: List<JobApiModel>, navController: NavController) {
    var tabIndex by remember { mutableIntStateOf(0) }

    val jobStatus = jobList.map { it.status.name }.toSet()
    val statusMap = mutableMapOf<List<JobApiModel>, Int>()
    val statusIndex = mutableMapOf<List<JobApiModel>, Int>()

    var totalJob = 0

    jobStatus.forEachIndexed { index, status ->
        val count = jobList.filter { it.status.name == status }
        statusMap[count] = count.size
        statusIndex[count] = index
        totalJob += count.size
    }

    Scaffold(
        topBar = { CustomTopAppBar("Job (${jobList.size})", navController) },
        content = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(it)
            ) {
                Divider()
                Spacer(modifier = Modifier.height(10.dp))
                ChartView(isJob = true, jobList = jobList, isDetailNeeded = false)

                ScrollableTabRow(
                    selectedTabIndex = tabIndex,
                    modifier = Modifier
                ) {
                    statusIndex.forEach { (list, index) ->
                        Tab(text = { Text(list[0].status.name + "(${statusMap[list]})") },
                            selected = tabIndex == index,
                            onClick = { tabIndex = index }
                        )
                    }
                }
                val jobListTab = statusIndex.keys.toList()
                TabScreen(jobListTab[tabIndex])
            }
        }
    )

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomTopAppBar(screenName: String, navController: NavController) {
    TopAppBar(
        title = { Text(screenName) },
        navigationIcon = {
            IconButton(onClick = {
                navController.popBackStack()
            }) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = "Back",
                )
            }
        }
    )
}

@Composable
fun TabScreen(jobModelData: List<JobApiModel>) {

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.LightGray)
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        items(jobModelData.size) { index ->
            Column(
                modifier = Modifier
                    .clip(shape = RoundedCornerShape(5.dp, 5.dp, 5.dp, 5.dp))
                    .background(Color.White)
                    .padding(10.dp)
                    .fillMaxWidth()
                    .background(color = Color.White),
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Text(text = "#${jobModelData[index].jobNumber}")
                Text(
                    text = jobModelData[index].title,
                    fontWeight = FontWeight.Bold
                )
                Text(text = SampleData.getRequiredDateFormat(jobModelData[index]))
            }
        }
    }
}

@Composable
fun JobInvoice(
    isJob: Boolean,
    modifier: Modifier,
    jobData: List<JobApiModel> = listOf(),
    invoiceList: List<InvoiceApiModel> = arrayListOf(), navController: NavController
) {
    val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
    val dataListType = Types.newParameterizedType(List::class.java, JobApiModel::class.java)
    val adapter: JsonAdapter<List<JobApiModel>> = moshi.adapter(dataListType)
    val json = adapter.toJson(jobData)
    Column(
        modifier = modifier
            .clip(shape = RoundedCornerShape(5.dp, 5.dp, 5.dp, 5.dp))
            .background(color = Color.White)
            .fillMaxWidth()
            .clickable {
                if (isJob)
                    navController.navigate(Screen.JobScreen.withArg(json))
            },
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {

        Row(
            modifier.padding(10.dp)
        ) {
            Text(text = if (isJob) "Job Stats" else "Invoice Stats")
        }

        Spacer(
            modifier = modifier
                .height(1.dp)
                .background(Color.LightGray)
                .fillMaxWidth(),
        )

        if (isJob)
            ChartView(jobList = jobData, isJob = true)
        else
            ChartView(invoiceList = invoiceList, isJob = false)

    }
}

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun Dashboard(modifier: Modifier = Modifier, navController: NavController, myViewModel: MyViewModel = hiltViewModel()) {
    var jobdata by remember {
        mutableStateOf(listOf<JobApiModel>())
    }
    var invoicedata by remember {
        mutableStateOf(listOf<InvoiceApiModel>())
    }
    val job = myViewModel.observeJobList()
    val invoice = myViewModel.observeInvoiceList()

    LaunchedEffect(Unit) {
        job.collect {
            jobdata = it
        }
    }
    LaunchedEffect(Unit) {
        invoice.collect {
            invoicedata = it
        }
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dashboard") })
        },
        content = {
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(it)
            ) {

                Column(
                    modifier = modifier
                        .background(color = Color.LightGray)
                        .padding(10.dp)
                        .fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Profile(modifier)
                    JobInvoice(true, modifier, jobData = jobdata, navController = navController)
                    JobInvoice(
                        false,
                        modifier,
                        invoiceList = invoicedata,
                        navController = navController
                    )
                }
            }
        }
    )

}

@Composable
fun Profile(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .clip(shape = RoundedCornerShape(5.dp, 5.dp, 5.dp, 5.dp))
            .background(color = Color.White)
            .padding(10.dp)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.End
    ) {
        Column(modifier = modifier.weight(1f)) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Hello, Henry Jones!",
                    modifier = modifier,
                    fontWeight = FontWeight.Bold,
                    style = TextStyle(fontSize = 20.sp)
                )
                Icon(
                    Icons.Default.Face, "", tint = Color.Red,
                    modifier = modifier.padding(5.dp)
                )
            }
            Spacer(modifier = modifier.height(5.dp))
            Text(
                text = SampleData.getCurrentDate(),
                modifier = modifier
            )
        }
        Image(
            painter = painterResource(id = R.drawable.profile), contentDescription = null,
            modifier = modifier
                .clip(shape = RoundedCornerShape(5.dp, 5.dp, 5.dp, 5.dp))
                .height(60.dp)
                .width(60.dp)
        )
    }

}


@Composable
fun GridData(modifier: Modifier, data: String, index: Int, colorCount: Int, isJob: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = "",
            modifier
                .height(10.dp)
                .width(10.dp)
                .padding(1.dp)
                .clip(shape = RoundedCornerShape(1.dp, 1.dp, 1.dp, 1.dp))
                .background(color = getColor()[colorCount])
        )
        Spacer(modifier.width(5.dp))
        Text(
            text = if (isJob) "$data ($index)" else "$data (#$index)"
        )
    }
}


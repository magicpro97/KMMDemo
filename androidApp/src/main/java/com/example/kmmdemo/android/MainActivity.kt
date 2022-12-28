package com.example.kmmdemo.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.kmmdemo.shared.entity.Links
import com.example.kmmdemo.shared.entity.RocketLaunch

class MainActivity : ComponentActivity() {
  private val mainActivityViewModel by viewModels<MainActivityViewModel> {
    MainActivityViewModel.provideFactory(this.application)
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    mainActivityViewModel.getLaunchRocketList()
    setContent {
      MyApplicationTheme {
        Surface(
          modifier = Modifier.fillMaxSize(),
          color = MaterialTheme.colors.background
        ) {
          MainScreen(mainActivityViewModel = mainActivityViewModel)
        }
      }
    }
  }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun MainScreen(
  mainActivityViewModel: MainActivityViewModel,
) {
  val uiState by mainActivityViewModel.uiState.collectAsState()
  var rocketLaunchItems by remember {
    mutableStateOf(emptyList<RocketLaunch>())
  }

  fun refresh() = mainActivityViewModel.getLaunchRocketList()

  val pullRefreshState = rememberPullRefreshState(uiState is MainActivityState.Loaded, ::refresh)

  LaunchedEffect(uiState) {
    when (uiState) {
      is MainActivityState.Loaded -> {
        rocketLaunchItems = (uiState as MainActivityState.Loaded).rocketLaunchList
      }
      else -> {}
    }
  }
  Scaffold(
    topBar = {
      TopAppBar(title = { Text("SpaceX") })
    },
  ) { contentPadding ->
    Box(
      Modifier
        .fillMaxSize()
        .pullRefresh(pullRefreshState)
    ) {
      LazyColumn(
        modifier = Modifier.padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        contentPadding = contentPadding,
        state = rememberLazyListState(),
      ) {
        items(items = rocketLaunchItems, key = { item -> item.flightNumber }) { rocketLaunch ->
          LaunchItem(rocketLaunch)
        }
      }
      PullRefreshIndicator(
        uiState is MainActivityState.Loaded,
        pullRefreshState,
        Modifier.align(Alignment.TopCenter)
      )
    }
  }
}

@Composable
fun LaunchItem(
  rocketLaunch: RocketLaunch,
) {
  val launchSuccess = rocketLaunch.launchSuccess
  val launchSuccessString = if (launchSuccess == null) null else
    if (launchSuccess) stringResource(R.string.successful) else stringResource(R.string.unsuccessful)
  Card(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(16.dp)
  ) {
    Column(
      modifier = Modifier.padding(8.dp),
    ) {
      ItemLabel(
        title = stringResource(R.string.mission_name_field),
        content = rocketLaunch.missionName,
      )
      ItemLabel(
        content = launchSuccessString,
        contentStyle = SpanStyle(
          color = Color.Red
        )
      )
      ItemLabel(
        title = stringResource(R.string.launch_year_field),
        content = rocketLaunch.launchYear.toString()
      )
      ItemLabel(
        title = stringResource(R.string.details_field),
        content = rocketLaunch.details
      )
    }
  }
}

@Composable
fun ItemLabel(
  title: String? = null,
  content: String? = null,
  contentStyle: SpanStyle = SpanStyle(),
) {
  Row {
    Text(buildAnnotatedString {
      if (title != null) withStyle(
        style = SpanStyle(
          fontWeight = FontWeight.Bold
        )
      ) {
        append(title)
      }
      append(" ")
      withStyle(style = contentStyle) {
        append(content ?: stringResource(R.string.no_data))
      }
    })
  }
}

@Preview
@Composable
fun DefaultPreview() {
  MyApplicationTheme {
    LaunchItem(
      RocketLaunch(
        missionName = "mission name",
        launchSuccess = true,
        details = "ABC",
        launchDateUTC = "",
        links = Links(
          patch = null,
          article = null,
        ),
        flightNumber = 123,
      )
    )
  }
}

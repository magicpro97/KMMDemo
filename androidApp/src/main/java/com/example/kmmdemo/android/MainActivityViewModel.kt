package com.example.kmmdemo.android

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.kmmdemo.shared.SpaceXSDK
import com.example.kmmdemo.shared.cache.DatabaseDriverFactory
import com.example.kmmdemo.shared.entity.RocketLaunch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class MainActivityState {
  object Initial : MainActivityState()
  object Loading : MainActivityState()
  object Error : MainActivityState()
  data class Loaded(val rocketLaunchList: List<RocketLaunch>) : MainActivityState()
}

class MainActivityViewModel(private val spaceXSDK: SpaceXSDK) : ViewModel() {
  private val _uiState = MutableStateFlow<MainActivityState>(MainActivityState.Initial)

  val uiState = _uiState.asStateFlow()

  fun getLaunchRocketList() {
    viewModelScope.launch {
      _uiState.emit(MainActivityState.Loading)
      try {
        val launchRocketList = spaceXSDK.getLaunches(forceReload = false)

        _uiState.emit(MainActivityState.Loaded(launchRocketList))
      } catch (e: Exception) {
        _uiState.emit(MainActivityState.Error)
      }
    }
  }

  companion object {
    fun provideFactory(application: Application) =
      object : ViewModelProvider.AndroidViewModelFactory(application) {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
          return MainActivityViewModel(SpaceXSDK(DatabaseDriverFactory(application.baseContext))) as T
        }
      }
  }
}
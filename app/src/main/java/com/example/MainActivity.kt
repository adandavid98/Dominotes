package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.DominoViewModel
import com.example.ui.screens.DominoGameScreen
import com.example.ui.screens.MatchHistoryScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
  private val viewModel: DominoViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
          DominoApp(viewModel = viewModel)
        }
      }
    }
  }
}

@Composable
fun DominoApp(viewModel: DominoViewModel) {
  val state by viewModel.gameState.collectAsStateWithLifecycle()
  val history by viewModel.matchHistory.collectAsStateWithLifecycle()

  AnimatedContent(
    targetState = state.showHistoryScreen,
    transitionSpec = { fadeIn() togetherWith fadeOut() },
    label = "screen_transition"
  ) { showHistory ->
    if (showHistory) {
      MatchHistoryScreen(
        matches = history,
        activeMatchId = state.matchId,
        onBack = { viewModel.setShowHistoryScreen(false) },
        onSelectMatch = { matchId -> viewModel.loadMatch(matchId) },
        onDeleteMatch = { matchId -> viewModel.deleteMatch(matchId) }
      )
    } else {
      DominoGameScreen(
        state = state,
        viewModel = viewModel
      )
    }
  }
}


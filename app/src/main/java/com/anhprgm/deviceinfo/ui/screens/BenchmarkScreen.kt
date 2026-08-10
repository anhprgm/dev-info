package com.anhprgm.deviceinfo.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.anhprgm.deviceinfo.R
import com.anhprgm.deviceinfo.ui.components.DetailColumn
import com.anhprgm.deviceinfo.ui.components.DetailRow
import com.anhprgm.deviceinfo.ui.components.DevInfoScaffold
import com.anhprgm.deviceinfo.ui.components.InfoCard
import com.anhprgm.deviceinfo.ui.format.Formatters
import com.anhprgm.deviceinfo.ui.theme.Dimens
import com.anhprgm.deviceinfo.ui.viewmodel.DeviceInfoViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BenchmarkScreen(
    viewModel: DeviceInfoViewModel,
    onNavigateBack: () -> Unit
) {
    val result by viewModel.benchmarkResult.collectAsStateWithLifecycle()
    val running by viewModel.isRunningBenchmark.collectAsStateWithLifecycle()

    DevInfoScaffold(
        title = stringResource(R.string.screen_benchmark),
        onNavigateBack = onNavigateBack
    ) { padding ->
        DetailColumn(padding) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (running) {
                    CircularProgressIndicator(modifier = Modifier.size(48.dp))
                    Spacer(Modifier.height(Dimens.spaceMd))
                    Text(
                        text = stringResource(R.string.benchmark_running),
                        style = MaterialTheme.typography.bodyLarge
                    )
                } else {
                    Button(onClick = { viewModel.runBenchmark() }) {
                        Text(stringResource(R.string.benchmark_run))
                    }
                }
            }

            val current = result ?: return@DetailColumn

            Spacer(Modifier.height(Dimens.spaceXl))

            InfoCard(title = stringResource(R.string.benchmark_overall)) {
                Text(
                    text = current.overallScore.toString(),
                    style = MaterialTheme.typography.displayMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.height(Dimens.spaceSm))
                // Scores derive from fixed baselines and clamp at 1000, so fast
                // devices all saturate. Say so instead of implying comparability.
                Text(
                    text = stringResource(R.string.benchmark_score_note),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(Modifier.height(Dimens.cardSpacing))

            InfoCard(title = stringResource(R.string.benchmark_section_cpu)) {
                DetailRow(
                    stringResource(R.string.benchmark_cpu_score),
                    current.cpuScore.toString()
                )
                HorizontalDivider(Modifier.padding(vertical = Dimens.spaceSm))
                DetailRow(
                    stringResource(R.string.benchmark_single_core),
                    current.singleCoreScore.toString()
                )
                HorizontalDivider(Modifier.padding(vertical = Dimens.spaceSm))
                DetailRow(
                    stringResource(R.string.benchmark_multi_core),
                    current.multiCoreScore.toString()
                )
            }

            Spacer(Modifier.height(Dimens.cardSpacing))

            InfoCard(title = stringResource(R.string.benchmark_section_details)) {
                Column(verticalArrangement = Arrangement.spacedBy(Dimens.spaceXs)) {
                    DetailRow(
                        stringResource(R.string.benchmark_total_time),
                        Formatters.elapsed(current.totalDurationMillis)
                    )
                    HorizontalDivider(Modifier.padding(vertical = Dimens.spaceSm))
                    DetailRow(
                        stringResource(R.string.benchmark_single_time),
                        Formatters.elapsed(current.singleCoreMillis)
                    )
                    HorizontalDivider(Modifier.padding(vertical = Dimens.spaceSm))
                    DetailRow(
                        stringResource(R.string.benchmark_multi_time),
                        Formatters.elapsed(current.multiCoreMillis)
                    )
                    HorizontalDivider(Modifier.padding(vertical = Dimens.spaceSm))
                    DetailRow(
                        stringResource(R.string.benchmark_memory_time),
                        Formatters.elapsed(current.memoryMillis)
                    )
                    HorizontalDivider(Modifier.padding(vertical = Dimens.spaceSm))
                    DetailRow(
                        stringResource(R.string.benchmark_cores_used),
                        current.coresUsed.toString()
                    )
                }
            }
        }
    }
}

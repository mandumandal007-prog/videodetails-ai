@file:OptIn(ExperimentalMaterial3Api::class)

package com.videodetails.ai.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ContentPaste
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Key
import androidx.compose.material.icons.rounded.Link
import androidx.compose.material.icons.rounded.PlayCircle
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import coil.compose.AsyncImage
import com.videodetails.ai.R
import com.videodetails.ai.data.HistoryItem
import com.videodetails.ai.data.HistoryStore
import com.videodetails.ai.data.SettingsStore
import com.videodetails.ai.data.extractUrl
import java.text.DateFormat
import java.util.Date

@Composable
fun HomeScreen(onAnalyze: (String) -> Unit) {
    val context = LocalContext.current
    val focus = LocalFocusManager.current
    val settings = remember { SettingsStore(context) }

    var input by remember { mutableStateOf("") }
    var inputError by remember { mutableStateOf(false) }
    var history by remember { mutableStateOf(HistoryStore.load(context)) }
    var showSettings by remember { mutableStateOf(false) }
    var hasKey by remember { mutableStateOf(settings.geminiKey.isNotBlank()) }

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        history = HistoryStore.load(context)
        hasKey = settings.geminiKey.isNotBlank()
    }

    fun analyze() {
        val url = extractUrl(input)
        if (url == null) {
            inputError = true
        } else {
            inputError = false
            focus.clearFocus()
            onAnalyze(url)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(stringResource(R.string.app_name), fontWeight = FontWeight.Bold)
                },
                actions = {
                    IconButton(onClick = { showSettings = true }) {
                        Icon(Icons.Rounded.Settings, contentDescription = stringResource(R.string.settings))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = stringResource(R.string.home_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            item {
                OutlinedTextField(
                    value = input,
                    onValueChange = { input = it; inputError = false },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text(stringResource(R.string.paste_hint)) },
                    leadingIcon = { Icon(Icons.Rounded.Link, contentDescription = null) },
                    trailingIcon = {
                        IconButton(onClick = {
                            readClipboard(context)?.let { input = it; inputError = false }
                        }) {
                            Icon(Icons.Rounded.ContentPaste, contentDescription = stringResource(R.string.paste))
                        }
                    },
                    isError = inputError,
                    supportingText = if (inputError) {
                        { Text(stringResource(R.string.invalid_link)) }
                    } else null,
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Uri,
                        imeAction = ImeAction.Search
                    ),
                    keyboardActions = KeyboardActions(onSearch = { analyze() })
                )
            }
            item {
                Button(
                    onClick = { analyze() },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Rounded.Search, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.get_details))
                }
            }
            item { TipCard(hasKey = hasKey, onOpenSettings = { showSettings = true }) }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Rounded.History,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.history),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.weight(1f)
                    )
                    if (history.isNotEmpty()) {
                        TextButton(onClick = {
                            HistoryStore.clear(context)
                            history = emptyList()
                        }) { Text(stringResource(R.string.clear)) }
                    }
                }
            }
            if (history.isEmpty()) {
                item {
                    Text(
                        text = stringResource(R.string.history_empty),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                }
            } else {
                items(history, key = { it.url }) { item ->
                    HistoryRow(item = item, onClick = { onAnalyze(item.url) })
                }
            }
        }
    }

    if (showSettings) {
        SettingsSheet(
            settings = settings,
            onDismiss = {
                showSettings = false
                hasKey = settings.geminiKey.isNotBlank()
            }
        )
    }
}

@Composable
private fun TipCard(hasKey: Boolean, onOpenSettings: () -> Unit) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = stringResource(R.string.tip_title),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                text = stringResource(R.string.tip_body),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            if (!hasKey) {
                Spacer(Modifier.height(4.dp))
                Button(onClick = onOpenSettings, shape = RoundedCornerShape(12.dp)) {
                    Icon(Icons.Rounded.Key, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.add_key_cta))
                }
            }
        }
    }
}

@Composable
private fun HistoryRow(item: HistoryItem, onClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(width = 96.dp, height = 56.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                if (item.thumbnail != null) {
                    AsyncImage(
                        model = item.thumbnail,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        Icons.Rounded.PlayCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = item.platform + " · " +
                        DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(Date(item.time)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

package com.example.compose2.musicUi

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.compose2.model.ThemeSelection

@Composable
fun ThemeSelectionDialog(dismissAlert:()->Unit={},
                         currentTheme:String=ThemeSelection.SYSTEM_THEME.name,
                         onThemeClick:(String)->Unit={}){
    val themeListMap= linkedMapOf(
        ThemeSelection.SYSTEM_THEME.name to "Use System Theme",
        ThemeSelection.LIGHT_THEME.name to "Light Theme",
        ThemeSelection.DARK_THEME.name to "Dark Theme")

    Dialog(onDismissRequest = { dismissAlert() }) {
        Card(modifier = Modifier
            .fillMaxWidth()
            .height(290.dp)
            .padding(30.dp),
            shape = RoundedCornerShape(6.dp)
        ) {
            Text(
                text = "Select App Theme",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                modifier = Modifier.padding(10.dp)
            )
            themeListMap.forEach {
                Row(modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        onThemeClick(it.key)
                        dismissAlert()
                    }
                    .padding(horizontal = 5.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = it.key==currentTheme, onClick = {
                        onThemeClick(it.key)
                        dismissAlert()
                    })
                    Text(
                        text = it.value,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(start = 5.dp)
                    )
                }
            }
        }
    }
}
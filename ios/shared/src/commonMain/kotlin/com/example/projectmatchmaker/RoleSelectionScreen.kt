package com.example.projectmatchmaker

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun RoleSelectionScreen(
    selectedRole: String,
    onRoleSelected: (String) -> Unit,
    onDone: () -> Unit,
    onCatalog: () -> Unit,
    onMatches: () -> Unit,
    onChats: () -> Unit,
    onProfile: () -> Unit,
) {
    ScreenFrame(MainDestination.PROFILE, onCatalog, onProfile, onMatches, onChats) {
        PageCard {
            // LazyColumn has a bounded viewport here, so the full role list scrolls
            // independently while the app's bottom navigation stays in place.
            ResponsivePageColumn {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    item(key = "heading") {
                        Column {
                            PageTitle("Выбор роли")
                            Text(
                                "Выбери основную роль. Под каждым названием — пояснение.",
                                color = MutedTextColor,
                                fontSize = 12.sp,
                            )
                            Text(
                                "Выбери вариант, затем нажми «Готово». Из списка можно прокручивать роли.",
                                color = MutedTextColor,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(top = 6.dp, bottom = 12.dp),
                            )
                            PrimaryButton(
                                text = "Готово",
                                onClick = onDone,
                                enabled = selectedRole.isNotBlank(),
                            )
                            Spacer(Modifier.height(8.dp))
                        }
                    }

                    teamRoleGroups.forEachIndexed { groupIndex, (group, roles) ->
                        item(key = "group-$groupIndex") {
                            Text(
                                text = group,
                                color = TextDarkColor,
                                fontSize = 19.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(top = 14.dp, bottom = 2.dp),
                            )
                        }
                        items(roles, key = { role -> role.title }) { role ->
                            val selected = role.title == selectedRole
                            Surface(
                                color = if (selected) ChipBgColor else PanelBgColor,
                                shape = RoundedCornerShape(12.dp),
                                border = if (selected) BorderStroke(1.dp, PrimaryButtonColor) else null,
                                modifier = Modifier.fillMaxWidth().clickable { onRoleSelected(role.title) },
                            ) {
                                Column(Modifier.padding(14.dp)) {
                                    Text(
                                        role.title,
                                        color = TextDarkColor,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                    )
                                    Text(
                                        role.description,
                                        color = MutedTextColor,
                                        fontSize = 12.sp,
                                        lineHeight = 17.sp,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(name = "Выбор роли · телефон", widthDp = 390, heightDp = 844, showBackground = true)
@Composable
fun RoleSelectionScreenPreview() {
    MaterialTheme { RoleSelectionScreen("UI/UX-дизайнер", {}, {}, {}, {}, {}, {}) }
}

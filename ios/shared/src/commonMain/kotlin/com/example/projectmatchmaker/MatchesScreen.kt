package com.example.projectmatchmaker

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.projectmatchmaker.network.MatchDto

@Composable
fun MatchesScreen(
    onCatalog: () -> Unit,
    onChats: () -> Unit,
    onProfile: () -> Unit,
    backendMatches: List<MatchDto>? = null,
    backendEnabled: Boolean = false,
    incomingLikeAccepted: Boolean = false,
    incomingLikeDismissed: Boolean = false,
    onAcceptIncomingLike: () -> Unit = {},
    onDismissIncomingLike: () -> Unit = {},
    onOpenIncomingChat: () -> Unit = onChats,
    onOpenDemoChat: () -> Unit = onChats,
) {
    ScreenFrame(MainDestination.MATCHES, onCatalog, onProfile, {}, onChats) {
        PageCard {
            Column(Modifier.fillMaxSize().padding(18.dp)) {
                PageTitle("Мэтчи")
                Text(
                    "Взаимные совпадения с людьми и проектами",
                    color = MutedTextColor,
                    fontSize = 13.sp,
                )
                Spacer(Modifier.height(12.dp))
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    if (backendMatches.isNullOrEmpty() && !incomingLikeAccepted && !incomingLikeDismissed) {
                        item(key = "incoming-codegarden") {
                            IncomingLikeCard(onAcceptIncomingLike, onDismissIncomingLike)
                        }
                    }
                    if (backendMatches.isNullOrEmpty() && incomingLikeAccepted) {
                        item(key = "mutual-codegarden") {
                            MatchCard(demoIncomingMatch, onOpenIncomingChat)
                        }
                    }
                    if (backendMatches.isNullOrEmpty()) {
                        items(demoMatches, key = { it.id }) { match -> MatchCard(match, onOpenDemoChat) }
                    } else {
                        items(backendMatches.orEmpty(), key = { it.id }) { match -> BackendMatchCard(match, onChats) }
                    }
                }
            }
        }
    }
}

@Composable
private fun IncomingLikeCard(onAccept: () -> Unit, onDismiss: () -> Unit) {
    Surface(Modifier.fillMaxWidth(), color = PanelBgColor, shape = RoundedCornerShape(16.dp)) {
        Column(Modifier.padding(14.dp)) {
            Text("Пример · вам понравилась команда", color = PrimaryButtonColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Row(Modifier.padding(top = 9.dp), verticalAlignment = Alignment.CenterVertically) {
                Surface(color = ProfileAvatarColor, shape = CircleShape, modifier = Modifier.size(50.dp)) {
                    Text("CG", color = CardBgColor, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.padding(top = 14.dp))
                }
                Column(Modifier.weight(1f).padding(start = 11.dp)) {
                    Text("CodeGarden", color = TextDarkColor, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                    Text("Ищут UI/UX-дизайнера · совпадение 93%", color = MutedTextColor, fontSize = 12.sp)
                }
            }
            Text("Это пример входящего интереса. Ответь взаимностью, чтобы создать мэтч и начать чат.", color = TextDarkColor, fontSize = 13.sp, modifier = Modifier.padding(top = 10.dp))
            PrimaryButton("Ответить взаимностью", onAccept, modifier = Modifier.padding(top = 8.dp))
            SecondaryButton("Не сейчас", onDismiss, Modifier.padding(top = 4.dp))
        }
    }
}

@Composable
private fun BackendMatchCard(match: MatchDto, onOpenChat: () -> Unit) {
    Surface(Modifier.fillMaxWidth(), color = PanelBgColor, shape = RoundedCornerShape(16.dp)) {
        Column(Modifier.padding(14.dp)) {
            Text("Совпадение #${match.id}", color = TextDarkColor, fontSize = 17.sp, fontWeight = FontWeight.Bold)
            Text("Проект #${match.projectId} · участник #${match.candidateId}", color = MutedTextColor, fontSize = 13.sp, modifier = Modifier.padding(top = 4.dp))
            PrimaryButton("Открыть чат", onOpenChat)
        }
    }
}

@Composable
private fun MatchCard(match: DemoMatch, onOpenChat: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = PanelBgColor,
        shape = RoundedCornerShape(16.dp),
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(color = ProfileAvatarColor, shape = CircleShape, modifier = Modifier.size(52.dp)) {
                    Text(
                        match.initials,
                        color = CardBgColor,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 14.dp),
                    )
                }
                Column(Modifier.weight(1f).padding(start = 12.dp)) {
                    Text(match.name, color = TextDarkColor, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                    Text(match.role, color = MutedTextColor, fontSize = 13.sp)
                }
                Surface(color = PrimaryButtonColor, shape = RoundedCornerShape(12.dp)) {
                    Text(
                        "${match.matchPercent}%",
                        color = CardBgColor,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp),
                    )
                }
            }
            Text(match.description, color = TextDarkColor, fontSize = 13.sp, modifier = Modifier.padding(top = 12.dp))
            SkillChips(match.skills)
            PrimaryButton("Открыть чат", onOpenChat)
        }
    }
}

@Preview(name = "Мэтчи · телефон", widthDp = 390, heightDp = 844, showBackground = true)
@Composable
fun MatchesScreenPreview() {
    MaterialTheme { MatchesScreen({}, {}, {}) }
}

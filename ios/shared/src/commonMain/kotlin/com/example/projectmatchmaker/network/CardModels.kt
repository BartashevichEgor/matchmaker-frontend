package com.example.projectmatchmaker.network

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SkillLevelDto(
    @SerialName("skill_id") val skillId: Int,
    val level: Int,
)

@Serializable
data class PublicProfileDto(
    @SerialName("user_id") val userId: Int,
    val username: String,
    @SerialName("first_name") val firstName: String,
    @SerialName("last_name") val lastName: String,
    val bio: String,
    @SerialName("github_url") val githubUrl: String? = null,
    val experience: String,
    val interests: String,
    @SerialName("role_id") val roleId: Int? = null,
    val skills: List<SkillLevelDto> = emptyList(),
)

@Serializable
data class ProfileReadDto(
    val id: Int,
    @SerialName("user_id") val userId: Int,
    val username: String,
    @SerialName("first_name") val firstName: String,
    @SerialName("last_name") val lastName: String,
    val bio: String,
    @SerialName("birth_date") val birthDate: String? = null,
    @SerialName("github_url") val githubUrl: String? = null,
    val experience: String,
    val interests: String,
    @SerialName("role_id") val roleId: Int? = null,
    val skills: List<SkillLevelDto> = emptyList(),
)

@Serializable
data class ProjectSkillDto(
    @SerialName("skill_id") val skillId: Int,
    val weight: Int,
)

@Serializable
data class ProjectDto(
    val id: Int,
    @SerialName("owner_id") val ownerId: Int,
    val title: String,
    val description: String,
    val status: String,
    @SerialName("created_at") val createdAt: String,
    val skills: List<ProjectSkillDto> = emptyList(),
    @SerialName("role_ids") val roleIds: List<Int> = emptyList(),
)

@Serializable
data class ProjectScoreDto(
    val project: ProjectDto,
    val score: Double,
    @SerialName("matched_skill_ids") val matchedSkillIds: List<Int> = emptyList(),
)

@Serializable
data class CandidateScoreDto(
    val profile: PublicProfileDto,
    val score: Double,
    @SerialName("matched_skill_ids") val matchedSkillIds: List<Int> = emptyList(),
)

@Serializable
data class SwipeRequestDto(val decision: String)

@Serializable
data class MatchDto(
    val id: Int,
    @SerialName("project_id") val projectId: Int,
    @SerialName("candidate_id") val candidateId: Int,
    @SerialName("created_at") val createdAt: String,
    @SerialName("room_key") val roomKey: String,
)

@Serializable
data class SwipeResponseDto(
    val decision: String,
    val match: MatchDto? = null,
)

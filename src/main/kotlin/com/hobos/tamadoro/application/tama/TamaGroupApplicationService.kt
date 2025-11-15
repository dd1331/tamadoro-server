package com.hobos.tamadoro.application.tama

import com.hobos.tamadoro.domain.common.Country
import com.hobos.tamadoro.domain.tamas.repository.UserTamaRepository
import com.hobos.tamadoro.domain.tamas.entity.Group
import com.hobos.tamadoro.domain.tamas.repository.GroupRepository
import com.hobos.tamadoro.domain.tamas.entity.TamaGroup
import com.hobos.tamadoro.domain.tamas.repository.TamaGroupRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class TamaGroupApplicationService(
    private val groupRepository: GroupRepository,
    private val userTamaRepository: UserTamaRepository,
    private val tamaGroupRepository: TamaGroupRepository
) {

    @Transactional(readOnly = true)
    fun getGroups(): List<GroupDto> =
        groupRepository.findAll().map { group ->
            val id = requireNotNull(group.id) { "Group id must not be null" }
            GroupDto(
                id = id,
                name = group.name,
                avatar = group.avatar,
                background = group.background,
                countryCode = group.country.name
            )
        }

    @Transactional
    fun createGroup(request: CreateGroupRequest): GroupDto {
        val name = request.name.trim()
        require(name.isNotEmpty()) { "Group name must not be blank" }

        val country = Country.fromCode(request.countryCode)

        val group = Group().apply {
            this.name = name
            this.avatar = request.avatar
            this.background = request.background
            this.country = country
        }

        val saved = groupRepository.save(group)
        val generatedId = saved.id ?: throw IllegalStateException("Group ID was not generated")

        val tama = userTamaRepository.findById(request.tamaId).orElseThrow()
        tamaGroupRepository.save(TamaGroup(tama = tama, group = group ))
        userTamaRepository.save(tama)

        return GroupDto(
            id = generatedId,
            name = saved.name,
            avatar = saved.avatar,
            background = saved.background,
            countryCode = saved.country.name
        )
    }
}

data class CreateGroupRequest(
    val name: String,
    val avatar: String,
    val background: String,
    val tamaId: Long,
    val countryCode: String
)

data class GroupDto(
    val id: Long,
    val name: String,
    val avatar: String?,
    val background: String?,
    val countryCode: String
)

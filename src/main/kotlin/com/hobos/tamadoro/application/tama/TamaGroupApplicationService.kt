package com.hobos.tamadoro.application.tama

import com.hobos.tamadoro.domain.common.Country
import com.hobos.tamadoro.domain.tamas.repository.UserTamaRepository
import com.hobos.tamadoro.domain.tamas.entity.Group
import com.hobos.tamadoro.domain.tamas.repository.GroupRepository
import com.hobos.tamadoro.domain.tamas.entity.TamaGroup
import com.hobos.tamadoro.domain.tamas.repository.TamaGroupRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

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

        val existing = groupRepository.findByName(name)

        if(existing.isPresent) {
            throw IllegalArgumentException("Group with name $name already exists")
        }

        val group = Group().apply {
            this.name = name
            this.avatar = request.avatar
            this.background = request.background
            this.country = country
        }

        val saved = groupRepository.save(group)

        val tama = userTamaRepository.findById(request.tamaId).orElseThrow()
        tamaGroupRepository.save(TamaGroup(tama = tama, group = group ))
        userTamaRepository.save(tama)

        return GroupDto(
            id = saved.id,
            name = saved.name,
            avatar = saved.avatar,
            background = saved.background,
            countryCode = saved.country.name
        )
    }
    fun assignGroup(userId: UUID, tamaId: Long, groupId: Long) {

        val existing = tamaGroupRepository.findOneByTamaIdAndGroupId(tamaId, groupId)
        if(existing.isPresent) {
            tamaGroupRepository.delete(existing.orElseThrow())
        }

        val tama = userTamaRepository.findById(tamaId).orElseThrow()
        val group = groupRepository.findById(groupId).orElseThrow()
        tamaGroupRepository.save(TamaGroup(tama = tama, group = group ))
        userTamaRepository.save(tama)


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

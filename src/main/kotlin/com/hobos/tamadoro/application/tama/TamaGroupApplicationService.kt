package com.hobos.tamadoro.application.tama

import com.hobos.tamadoro.domain.tama.UserTamaRepository
import com.hobos.tamadoro.domain.tamas.Group
import com.hobos.tamadoro.domain.tamas.GroupRepository
import com.hobos.tamadoro.domain.tamas.TamaGroup
import com.hobos.tamadoro.domain.tamas.TamaGroupRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class TamaGroupApplicationService(
    private val groupRepository: GroupRepository,
    private val userTamaRepository: UserTamaRepository,
    private val tamaGroupRepository: TamaGroupRepository
) {

    @Transactional
    fun createGroup(request: CreateGroupRequest): GroupDto {
        val name = request.name.trim()
        require(name.isNotEmpty()) { "Group name must not be blank" }

        val group = Group().apply {
            this.name = name
            this.avatar = request.avatar
            this.background = request.background
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
            background = saved.background
        )
    }
}

data class CreateGroupRequest(
    val name: String,
    val avatar: String,
    val background: String,
    val tamaId: Long
)

data class GroupDto(
    val id: Long,
    val name: String,
    val avatar: String?,
    val background: String?
)

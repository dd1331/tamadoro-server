package com.hobos.tamadoro.api.randombox

import com.hobos.tamadoro.api.common.ApiResponse
import com.hobos.tamadoro.application.randombox.RandomBoxApplicationService
import com.hobos.tamadoro.config.CurrentUserId
import com.hobos.tamadoro.domain.randombox.RandomBox
import com.hobos.tamadoro.domain.randombox.Reward
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/random-boxes")
class RandomBoxController(
    private val randomBoxApplicationService: RandomBoxApplicationService
) {
    @GetMapping
    fun list(@CurrentUserId userId: UUID): ResponseEntity<ApiResponse<List<RandomBox>>> =
        ResponseEntity.ok(ApiResponse.success(randomBoxApplicationService.list(userId)))

    @PostMapping("/{id}/purchase")
    fun purchase(
        @CurrentUserId userId: UUID,
        @PathVariable id: String
    ): ResponseEntity<ApiResponse<List<Reward>>> =
        ResponseEntity.ok(ApiResponse.success(randomBoxApplicationService.purchase(userId, id)))
}

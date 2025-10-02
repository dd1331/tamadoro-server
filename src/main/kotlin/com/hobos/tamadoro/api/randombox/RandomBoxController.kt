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

}

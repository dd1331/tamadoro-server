package com.hobos.tamadoro.application.randombox

import com.hobos.tamadoro.domain.randombox.RandomBoxService
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class RandomBoxApplicationService(
    private val randomBoxService: RandomBoxService
) {
}



package com.hobos.tamadoro.api.health

import com.hobos.tamadoro.api.common.ApiResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * Simple root endpoint so that requests to "/" do not blow up the app.
 */
@RestController
@RequestMapping("/")
class HomeController {

    @GetMapping
    fun root(): ResponseEntity<ApiResponse<String>> {
        return ResponseEntity.ok(ApiResponse.success("ok"))
    }
}

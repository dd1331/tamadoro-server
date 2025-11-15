package com.hobos.tamadoro.api.tamas

import com.hobos.tamadoro.api.common.ApiResponse
import com.hobos.tamadoro.application.tama.CustomTamaRequest
import com.hobos.tamadoro.application.tama.TamaApplicationService
import com.hobos.tamadoro.application.tama.TamaDto
import com.hobos.tamadoro.application.tama.TamaRankApplicationService
import com.hobos.tamadoro.application.tama.PagedResponse
import com.hobos.tamadoro.application.tama.PagingRequest
import com.hobos.tamadoro.application.tama.TamaRankDto
import com.hobos.tamadoro.application.tama.TamaGroupRankDto
import com.hobos.tamadoro.application.tama.HeatmapNodeDto
import com.hobos.tamadoro.application.tama.UpdateTamaRequest
import com.hobos.tamadoro.application.tama.TamaGroupApplicationService
import com.hobos.tamadoro.application.tama.CreateGroupRequest
import com.hobos.tamadoro.application.tama.GroupDto
import com.hobos.tamadoro.config.CurrentUserId
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.net.URI
import java.util.UUID

@RestController
@RequestMapping("/tamas")
class TamasController(
    private val tamaApplicationService: TamaApplicationService,
    private val tamaRankApplicationService: TamaRankApplicationService,
    private val tamaGroupApplicationService: TamaGroupApplicationService,

) {
    @GetMapping
    fun getTamas(@CurrentUserId userId: UUID): ResponseEntity<ApiResponse<List<TamaDto>>> {
        val tamas = tamaApplicationService.getTamas(userId)
        println("tamas+" + tamas)
        return ResponseEntity.ok(ApiResponse.success(tamas))
    }


    @PostMapping
    fun createCustomTama(
        @CurrentUserId userId: UUID,
        @RequestBody request: CustomTamaRequest
    ): ResponseEntity<ApiResponse<TamaDto>> {

        println("userId" +userId)
        val tama = tamaApplicationService.createCustomTama(userId, request)
        return ResponseEntity.created(URI.create("/tamas/${tama.id}")).body(ApiResponse.success(tama))
    }
    @GetMapping("/ranking")
    fun getRank(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<ApiResponse<PagedResponse<TamaRankDto>>> {
        val pagingRequest = PagingRequest(page, size)
        val pagedResult = tamaRankApplicationService.getRankWithPaging(pagingRequest)
        return ResponseEntity.ok(ApiResponse.success(pagedResult))
    }

    @GetMapping("/ranking/groups")
    fun getGroupRanking(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<ApiResponse<PagedResponse<TamaGroupRankDto>>> {
        val pagingRequest = PagingRequest(page, size)
        val pagedResult = tamaRankApplicationService.getGroupRankingWithPaging(pagingRequest)
        return ResponseEntity.ok(ApiResponse.success(pagedResult))
    }

    @GetMapping("/ranking/groups/heatmap")
    fun getGroupRankingHeatmap(
        @RequestParam(required = false) limit: Int?,
        @RequestParam(required = false, name = "entryLimit") entryLimit: Int?
    ): ResponseEntity<ApiResponse<List<HeatmapNodeDto>>> =
        getGroupHeatmapByRegion(limit, entryLimit)

    @GetMapping("/ranking/groups/heatmap/regions")
    fun getGroupHeatmapByRegion(
        @RequestParam(required = false) limit: Int?,
        @RequestParam(required = false, name = "entryLimit") entryLimit: Int?
    ): ResponseEntity<ApiResponse<List<HeatmapNodeDto>>> {
        val heatmap = tamaRankApplicationService.getRegionalHeatmap(limit, entryLimit)
        return ResponseEntity.ok(ApiResponse.success(heatmap))
    }

    @GetMapping("/ranking/groups/heatmap/groups")
    fun getGroupHeatmapByGroup(
        @RequestParam(required = false) limit: Int?,
        @RequestParam(required = false, name = "entryLimit") entryLimit: Int?
    ): ResponseEntity<ApiResponse<List<HeatmapNodeDto>>> {
        val heatmap = tamaRankApplicationService.getGroupHeatmap(limit, entryLimit)
        return ResponseEntity.ok(ApiResponse.success(heatmap))
    }

    @PostMapping("/groups")
    fun createGroup(
        @RequestBody request: CreateGroupRequest
    ): ResponseEntity<ApiResponse<GroupDto>> {
        val group = tamaGroupApplicationService.createGroup(request)


        return ResponseEntity.created(URI.create("/tamas/groups/${group.id}")).body(ApiResponse.success(group))
    }

    @PutMapping("/{tamaId}")
    fun updateTama(
        @CurrentUserId userId: UUID,
        @PathVariable tamaId: Long,
        @RequestBody request: UpdateTamaRequest
    ): ResponseEntity<ApiResponse<TamaDto>> {
        val tama = tamaApplicationService.updateTama(userId, tamaId, request)
        return ResponseEntity.ok(ApiResponse.success(tama))
    }

    @DeleteMapping("/{tamaId}")
    fun deleteTama(
        @CurrentUserId userId: UUID,
        @PathVariable tamaId: Long
    ): ResponseEntity<ApiResponse<Unit>> {
        tamaApplicationService.deleteTama(userId, tamaId)
        return ResponseEntity.ok(ApiResponse.success())
    }




    @GetMapping("/groups")
    fun getGroups(): ResponseEntity<ApiResponse<List<GroupDto>>> {
        val groups = tamaGroupApplicationService.getGroups()
        return ResponseEntity.ok(ApiResponse.success(groups))
    }





}

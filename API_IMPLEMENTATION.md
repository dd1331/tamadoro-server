# Tamadoro API Implementation Guide — 최신

현재 프론트 코드 기준(`app/services/realApi.ts`, `app/services/mockApi.ts`, `app/types/api.ts`, `app/constants/config.ts`)으로 백엔드와의 연동 방법을 정리했습니다.

## 1) 구조

```
app/services/
├── mockApi.ts   # 모킹 클라이언트
└── realApi.ts   # 실제 백엔드 클라이언트

app/constants/config.ts  # BACKEND_URL, 플래그
```

## 2) 환경 설정

- BACKEND_URL: `API_CONFIG.BACKEND_URL` (예: http://localhost:3000)
- 개발 중 로깅: `ENV_CONFIG.LOG_API_CALLS` 활성화됨
- 기본적으로 개발 모드에서 모킹 사용(`ENV_CONFIG.USE_MOCK`)

예시(.env)

```dotenv
EXPO_PUBLIC_BACKEND_URL=http://localhost:3000
```

## 3) 인증/토큰 처리 흐름

- Apple Sign-In: `POST /auth/apple` → 토큰 저장
- 토큰 갱신: `POST /auth/refresh` → 새 토큰 저장
- 로그아웃: `POST /auth/logout` → 토큰 제거

프론트는 Authorization 헤더에 Bearer 토큰을 자동 첨부합니다.

## 4) 엔드포인트 목록(최신)

- 인증: `POST /auth/apple`, `POST /auth/refresh`, `POST /auth/logout`
- 사용자: `GET/PUT /user/profile`
- 타이머: `GET/PUT /timer/settings`, `POST /timer/sessions`, `PUT /timer/sessions/{id}`
- 통계: `GET /stats/daily`, `GET /stats/weekly`, `GET /stats/monthly`, `GET/PUT /stats/goals/weekly`, `POST /stats/pomodoros`, `POST /stats/tasks`
- 태스크: `GET/POST /tasks`, `PUT/DELETE /tasks/{id}`, `POST /tasks/{id}/complete`
- 타마고치: `GET/POST /tamas`, `PUT/DELETE /tamas/{id}`, `POST /tamas/{id}/feed|play|experience`
- 인벤토리: `GET /inventory`, `PUT /inventory/coins|gems|active-tama`
- 컬렉션: `GET /backgrounds | /music/tracks | /characters`, `PUT /backgrounds/active | /music/active | /characters/active`, `POST /backgrounds/{id}/purchase | /music/tracks/{id}/purchase | /characters/{id}/purchase`
- 랜덤 박스: `GET /random-boxes`, `POST /random-boxes/{id}/purchase`
- 결제/구독: `GET /purchase/coins|gems`, `POST /purchase/coins|gems`, `GET /subscription/plans|status`, `POST /subscription/subscribe|cancel`
- 출석: `GET /attendance`, `POST /attendance/check`, `GET /attendance/streak`
- 광고: `POST /ads/interstitial`, `POST /ads/banner`

각 요청/응답 스키마는 `API.md`와 `app/types/api.ts`를 1:1로 따릅니다.

## 5) 코틀린 스프링 구현 가이드(샘플 시그니처)

```kotlin
// 공통 응답 래퍼
data class ApiResponse<T>(val success: Boolean = true, val data: T, val message: String? = null)
data class ApiErrorDetail(val code: Int, val message: String, val details: Any? = null)
data class ApiError(val success: Boolean = false, val error: ApiErrorDetail)

// 인증
@RestController
@RequestMapping("/auth")
class AuthController(private val service: AuthService) {
  @PostMapping("/apple")
  fun apple(@RequestBody req: AppleAuthRequest): ApiResponse<AuthTokens> = service.apple(req)

  @PostMapping("/refresh")
  fun refresh(@RequestBody req: RefreshTokenRequest): ApiResponse<AuthTokens> = service.refresh(req)

  @PostMapping("/logout")
  fun logout(): ApiResponse<Void?> = service.logout()
}

// 사용자
@RestController
@RequestMapping("/user")
class UserController(private val service: UserService) {
  @GetMapping("/profile") fun get(): ApiResponse<UserDto> = service.get()
  @PutMapping("/profile") fun update(@RequestBody body: UserUpdateDto): ApiResponse<UserDto> = service.update(body)
}

// 타이머
@RestController
@RequestMapping("/timer")
class TimerController(private val service: TimerService) {
  @GetMapping("/settings") fun getSettings(): ApiResponse<TimerSettingsDto> = service.getSettings()
  @PutMapping("/settings") fun updateSettings(@RequestBody body: TimerSettingsUpdateDto): ApiResponse<TimerSettingsDto> = service.updateSettings(body)
  @PostMapping("/sessions") fun create(@RequestBody body: TimerSessionCreateDto): ApiResponse<TimerSessionDto> = service.create(body)
  @PutMapping("/sessions/{id}") fun update(@PathVariable id: String, @RequestBody body: TimerSessionUpdateDto): ApiResponse<TimerSessionDto> = service.update(id, body)
}

// 통계
@RestController
@RequestMapping("/stats")
class StatsController(private val service: StatsService) {
  @GetMapping("/daily") fun daily(@RequestParam startDate: String?, @RequestParam endDate: String?): ApiResponse<List<DailyStatsDto>> = service.daily(startDate, endDate)
  @GetMapping("/weekly") fun weekly(@RequestParam week: String): ApiResponse<WeeklyStatsDto> = service.weekly(week)
  @GetMapping("/monthly") fun monthly(@RequestParam month: String): ApiResponse<MonthlyStatsDto> = service.monthly(month)
  @GetMapping("/goals/weekly") fun goal(): ApiResponse<WeeklyGoalDto> = service.goal()
  @PutMapping("/goals/weekly") fun updateGoal(@RequestBody body: WeeklyGoalUpdateDto): ApiResponse<WeeklyGoalDto> = service.updateGoal(body)
  @PostMapping("/pomodoros") fun addPomodoro(): ApiResponse<Void?> = service.addPomodoro()
  @PostMapping("/tasks") fun addTask(): ApiResponse<Void?> = service.addTask()
}

// 태스크/타마고치/인벤토리/컬렉션/랜덤박스/결제/구독/출석/광고도 동일한 방식으로 매핑
```

보안은 Spring Security의 JWT 필터로 보호하고, `@RestControllerAdvice`로 `ApiError`를 공통 처리하세요.

## 6) 권한/보안

- 인증 필요: 위 목록 중 `/auth/*` 제외 모든 경로
- 401/403 표준 처리, Rate limiting 권장

## 7) 배포 체크리스트(업데이트)

- [ ] 엔드포인트 전부 구현 및 Swagger 노출
- [ ] JWT 인증/리프레시 플로우 검증
- [ ] DB 마이그레이션(필요 스키마만 우선 적용)
- [ ] 결제/구독은 플래그로 비활성화 가능하도록 설계
- [ ] 로깅/모니터링, 에러 트래킹

세부 스키마는 `API.md` 참조. 본 가이드는 최신 프론트 기준입니다.

## 8) 프론트-백엔드 연동 현황 및 누락(중요)

- 인증(AuthContext)

  - 현재: `expo-apple-authentication`으로 로컬 로그인만 수행, 토큰 저장/검증 없음. 백엔드 `POST /auth/apple` 미연동.
  - 조치: 로그인 성공 후 `realApi.appleSignIn(...)` 호출해 토큰 수령/저장, `useAuth`에서 `authCompleted` 전 JWT 확보.

- 타이머 설정(TimerContext)

  - 현재: AsyncStorage 로컬 저장. `useTimerSettings` 훅은 미사용. UI에 “API 기반 설정 제거” 주석 존재.
  - 조치: Auth 완료 시 `GET /timer/settings` 로드 → `syncWithApiSettings` 반영. 수정 시 `PUT /timer/settings` 호출 + 로컬 동기화.

- 통계(StatsContext)

  - 현재: 전부 로컬(AsyncStorage) 계산/저장. `useDailyStats`/`useWeeklyStats`/`useMonthlyStats` 훅 미사용.
  - 조치: 읽기 경로를 점진적 서버 소스로 전환. 오프라인 병합 전략(로컬 delta → 서버 반영) 설계. `POST /stats/pomodoros`/`/stats/tasks` 이벤트 송신.

- 태스크(useApi)

  - 현재: `useTasks` 사용 중. 서버/모킹 클라이언트에 의존. 배포 플래그에선 TASK_SYSTEM=false라 UI 노출 범위 점검 필요.
  - 조치: 실제 백엔드 연결 시 `ENV_CONFIG.USE_MOCK=false` 및 서버 가용성 확인.

- 타마고치/인벤토리(TamaContext)

  - 현재: 완전 로컬(AsyncStorage). 서버 `GET /inventory`, `GET /tamas` 미연동.
  - 조치: 초기 로드 시 서버 인벤토리 동기화, 상호작용(feed/play/exp) 시 서버 엔드포인트 호출 후 로컬 반영.

- 결제/구독(PaymentContext)

  - 현재: `mockPaymentService`만 사용. `expo-iap` 유틸(`iapService.ts`)은 분리돼 있으나 컨텍스트 미연결. 서버 `/purchase/*`, `/subscription/*` 미사용.
  - 조치: 스토어 결제 성공 콜백 → 백엔드 영수증 검증 → 코인/젬/권한 지급. 구독 상태는 서버 `GET /subscription/status` 기준으로 동기화.

- 광고

  - 현재: AdMob SDK 직접 호출(`showInterstitialAd`, `showRewardedAd`), 서버 `/ads/*` 엔드포인트 미사용.
  - 조치: 서버 로깅/빈도제어가 필요하면 `/ads/*`로 이벤트 송신(선택).

- API 스위처(ApiSwitcher)

  - 현재: 런타임 `ENV_CONFIG.USE_MOCK` 토글하지만 `apiClient`는 모듈 로드 시 고정 선택. 실질 전환엔 앱 재시작 필요.
  - 조치: 문서화(재시작 필요). 또는 `apiClient`를 팩토리/컨텍스트로 만들어 런타임 핫스왑 지원.

- 기능 플래그(app/constants/config.ts)
  - 현재: 다수 기능 비활성(STATS_SYSTEM, TASK_SYSTEM 등). 화면/훅 노출과 불일치 없는지 확인 필요.

권장 전환 순서: 인증 → 타이머 설정/통계 이벤트 송신 → 태스크 → 인벤토리/타마고치 → 구독/결제 → 통계 조회 서버 이관.

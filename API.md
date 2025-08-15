# Tamadoro (Pomodoro + Tama) API 명세서 — 최신

본 문서는 현재 프론트엔드 구현(`app/services/realApi.ts`, `app/types/api.ts`, `app/constants/config.ts`)을 기준으로 백엔드가 구현해야 할 REST API를 정의합니다.

## 1. 공통

- Base URL: `API_CONFIG.BACKEND_URL` (예: http://localhost:3000)
- 인증: JWT Bearer 토큰 (헤더 `Authorization: Bearer <token>`)
- 응답 래퍼:

```typescript
interface ApiResponse<T> {
  success: boolean;
  data: T;
  message?: string;
}
interface ApiError {
  success: false;
  error: { code: number; message: string; details?: any };
}
```

## 2. 인증

- POST /auth/apple
  - body: { identityToken: string; authorizationCode: string; user: any }
  - res: { user: User; token: string; refreshToken: string }
- POST /auth/refresh
  - body: { refreshToken: string }
  - res: { token: string; refreshToken: string }
- POST /auth/logout

## 3. 사용자

- GET /user/profile → User
- PUT /user/profile
  - body: Partial<User>
  - res: User

```typescript
export interface User {
  id: string;
  email: string;
  name: string;
  isPremium: boolean;
  createdAt: string;
  updatedAt: string;
  lastLoginAt: string;
  subscription?: {
    type: "monthly" | "yearly";
    startDate: string;
    endDate: string;
    status: "active" | "cancelled" | "expired";
  };
}
```

## 4. 타이머

- GET /timer/settings → TimerSettings
- PUT /timer/settings
  - body: Partial<TimerSettings>
  - res: TimerSettings
- POST /timer/sessions
  - body: Omit<TimerSession, "id" | "userId">
  - res: TimerSession
- PUT /timer/sessions/{id}
  - body: Partial<TimerSession>
  - res: TimerSession

```typescript
export interface TimerSettings {
  workTime: number;
  shortBreakTime: number;
  longBreakTime: number;
  longBreakInterval: number;
  autoStartBreaks: boolean;
  autoStartPomodoros: boolean;
  soundEnabled: boolean;
  vibrationEnabled: boolean;
}

export interface TimerSession {
  id: string;
  userId: string;
  type: "focus" | "shortBreak" | "longBreak";
  duration: number;
  completed: boolean;
  startedAt: string;
  completedAt?: string;
  taskId?: string;
}
```

## 5. 태스크

- GET /tasks → Task[]
- POST /tasks
  - body: CreateTaskRequest
  - res: Task
- PUT /tasks/{id}
  - body: UpdateTaskRequest
  - res: Task
- DELETE /tasks/{id}
- POST /tasks/{id}/complete

```typescript
export interface Task {
  id: string;
  userId: string;
  title: string;
  description?: string;
  completed: boolean;
  priority: "low" | "medium" | "high";
  estimatedPomodoros: number;
  completedPomodoros: number;
  createdAt: string;
  updatedAt: string;
  completedAt?: string;
}

export interface CreateTaskRequest {
  title: string;
  description?: string;
  priority: "low" | "medium" | "high";
  estimatedPomodoros: number;
}

export interface UpdateTaskRequest {
  title?: string;
  description?: string;
  priority?: "low" | "medium" | "high";
  estimatedPomodoros?: number;
  completed?: boolean;
  completedPomodoros?: number;
}
```

## 6. 통계

- GET /stats/daily?startDate=YYYY-MM-DD&endDate=YYYY-MM-DD → DailyStats[]
- GET /stats/weekly?week=YYYY-Www → WeeklyStats
- GET /stats/monthly?month=YYYY-MM → MonthlyStats
- GET /stats/goals/weekly → WeeklyGoal
- PUT /stats/goals/weekly
  - body: Partial<WeeklyGoal>
  - res: WeeklyGoal
- POST /stats/pomodoros
- POST /stats/tasks

```typescript
export interface DailyStats {
  date: string;
  completedPomodoros: number;
  totalFocusTime: number;
  completedTasks: number;
  attendance: boolean;
  coinsEarned: number;
  gemsEarned: number;
}

export interface WeeklyGoal {
  pomodoros: number;
  focusTime: number;
  tasks: number;
}
```

## 7. 타마고치

- GET /tamas → Tama[]
- POST /tamas (생성)
- PUT /tamas/{id}
- DELETE /tamas/{id}
- POST /tamas/{id}/feed
- POST /tamas/{id}/play
- POST /tamas/{id}/experience

```typescript
export interface Tama {
  id: string;
  userId: string;
  name: string;
  type:
    | "tomato"
    | "coffee"
    | "book"
    | "tree"
    | "cat"
    | "kitten"
    | "dragon"
    | "unicorn"
    | "phoenix"
    | "owl"
    | "fox"
    | "panda"
    | "rabbit"
    | "dog";
  rarity: "common" | "rare" | "epic" | "legendary" | "mythic";
  level: number;
  experience: number;
  maxExperience: number;
  isActive: boolean;
  acquiredAt: string;
  growthStage: "egg" | "baby" | "child" | "teen" | "adult";
  happiness: number;
  hunger: number;
  energy: number;
  lastInteraction: string;
}
```

## 8. 인벤토리

- GET /inventory → { coins, gems, tamas, activeTamaId? }
- PUT /inventory/coins (body: { amount: number })
- PUT /inventory/gems (body: { amount: number })
- PUT /inventory/active-tama (body: { id: string })

## 9. 컬렉션

- GET /backgrounds → BackgroundItem[]
- PUT /backgrounds/active (body: { id })
- POST /backgrounds/{id}/purchase
- GET /music/tracks → MusicItem[]
- PUT /music/active (body: { id })
- POST /music/tracks/{id}/purchase
- GET /characters → CharacterItem[]
- PUT /characters/active (body: { id })
- POST /characters/{id}/purchase

```typescript
export interface BackgroundItem {
  id: string;
  name: string;
  value: string;
  type: "gradient" | "image";
  focusColor?: string;
  breakColor?: string;
  theme: "light" | "dark" | "color" | "nature";
  imagePath?: string;
  isPremium: boolean;
}

export interface MusicItem {
  id: string;
  name: string;
  value: string;
  type: "ambient" | "nature" | "focus" | "none";
  volume: number;
  isPremium: boolean;
}

export interface CharacterItem {
  id: string;
  name: string;
  type: Tama["type"];
  size: number;
  isPremium: boolean;
}
```

## 10. 랜덤 박스

- GET /random-boxes → RandomBox[]
- POST /random-boxes/{id}/purchase → Reward[]

```typescript
export interface RandomBox {
  id: string;
  name: string;
  price: number;
  currency: "coins" | "gems";
  description: string;
  rarity: "common" | "rare" | "epic" | "legendary";
  rewards: Reward[];
}

export interface Reward {
  type: "tama" | "coin" | "gem";
  rarity: string;
  name: string;
  icon: string;
  amount?: number;
}
```

## 11. 결제/구독

- GET /purchase/coins → CoinPackage[]
- GET /purchase/gems → GemPackage[]
- POST /purchase/coins (body: { packageId: string })
- POST /purchase/gems (body: { packageId: string })
- GET /subscription/plans → PremiumSubscription[]
- GET /subscription/status → { ... }
- POST /subscription/subscribe (body: { type: "monthly" | "yearly" })
- POST /subscription/cancel

```typescript
export interface CoinPackage {
  amount: number;
  price: number;
  bonus: number;
}
export interface GemPackage {
  amount: number;
  price: number;
  bonus: number;
}
export interface PremiumSubscription {
  type: "monthly" | "yearly";
  price: number;
  features: string[];
}
```

## 12. 출석

- GET /attendance → Attendance[]
- POST /attendance/check → Attendance
- GET /attendance/streak → number

```typescript
export interface Attendance {
  id: string;
  userId: string;
  date: string;
  checkedAt: string;
  streakDays: number;
  reward: { coins: number; gems: number };
}
```

## 13. 광고

- POST /ads/interstitial (body: { context: "timer_complete" | "skip_timer" | "task_complete" })
- POST /ads/banner (body: { placement: "timer" | "tasks" | "collection" })

## 14. 엔드포인트 요약

- 인증: POST /auth/apple, POST /auth/refresh, POST /auth/logout
- 사용자: GET/PUT /user/profile
- 타이머: GET/PUT /timer/settings, POST /timer/sessions, PUT /timer/sessions/{id}
- 태스크: GET/POST /tasks, PUT/DELETE /tasks/{id}, POST /tasks/{id}/complete
- 통계: GET /stats/daily, /stats/weekly, /stats/monthly, GET/PUT /stats/goals/weekly, POST /stats/pomodoros, POST /stats/tasks
- 타마고치: GET/POST /tamas, PUT/DELETE /tamas/{id}, POST /tamas/{id}/feed|play|experience
- 인벤토리: GET /inventory, PUT /inventory/coins|gems|active-tama
- 컬렉션: GET /backgrounds | /music/tracks | /characters, PUT /backgrounds/active | /music/active | /characters/active, POST /backgrounds/{id}/purchase | /music/tracks/{id}/purchase | /characters/{id}/purchase
- 랜덤 박스: GET /random-boxes, POST /random-boxes/{id}/purchase
- 결제/구독: GET /purchase/coins|gems | /subscription/plans|status, POST /purchase/coins|gems | /subscription/subscribe|cancel
- 출석: GET /attendance, POST /attendance/check, GET /attendance/streak
- 광고: POST /ads/interstitial, POST /ads/banner

## 15. 보안/에러

- 모든 보호 엔드포인트는 JWT 필요
- 표준 에러 응답은 `ApiError` 사용, HTTP 상태코드와 동기화(401/403/404/409/422/429/500 등)

본 명세는 프론트 최신 코드 기준이며, 코틀린 스프링 백엔드가 구현해야 할 최소 집합입니다.

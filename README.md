# Tamadoro (Pomodoro + Tama) API 명세서

## 1. 프로젝트 개요

**앱 이름**: Tamadoro  
**앱 설명**: 뽀모도로 타이머와 타마고치 시스템을 결합한 생산성 앱  
**주요 기능**:

- 뽀모도로 타이머 (작업/휴식)
- 태스크 관리 및 통계
- 타마고치 수집 및 육성
- 배경음악 및 테마 커스터마이징
- 프리미엄 구독 시스템

## 2. 인증 시스템

### 2.1 Apple Sign-In

```typescript
// POST /auth/apple
interface AppleAuthRequest {
  identityToken: string;
  authorizationCode: string;
  user: {
    id: string;
    email?: string;
    name?: {
      firstName?: string;
      lastName?: string;
    };
  };
}

interface AppleAuthResponse {
  success: boolean;
  data: {
    user: User;
    token: string;
    refreshToken: string;
  };
}
```

### 2.2 토큰 관리

```typescript
// POST /auth/refresh
interface RefreshTokenRequest {
  refreshToken: string;
}

// POST /auth/logout
interface LogoutRequest {
  token: string;
}
```

## 3. 사용자 관리

### 3.1 사용자 정보

```typescript
interface User {
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

// GET /user/profile
// PUT /user/profile
interface UpdateUserRequest {
  name?: string;
  email?: string;
}
```

## 4. 타이머 시스템

### 4.1 타이머 설정

```typescript
interface TimerSettings {
  workTime: number; // 25분 고정
  shortBreakTime: number; // 5분
  longBreakTime: number; // 15분
  longBreakInterval: number; // 4회
  autoStartBreaks: boolean;
  autoStartPomodoros: boolean;
  soundEnabled: boolean;
  vibrationEnabled: boolean;
}

// GET /timer/settings
// PUT /timer/settings
```

### 4.2 타이머 세션

```typescript
interface TimerSession {
  id: string;
  userId: string;
  type: "work" | "shortBreak" | "longBreak";
  duration: number; // 실제 작업 시간 (분)
  completed: boolean;
  startedAt: string;
  completedAt?: string;
  taskId?: string;
}

// POST /timer/sessions
// PUT /timer/sessions/:id
```

## 5. 태스크 관리

### 5.1 태스크 CRUD

```typescript
interface Task {
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

interface CreateTaskRequest {
  title: string;
  description?: string;
  priority: "low" | "medium" | "high";
  estimatedPomodoros: number;
}

interface UpdateTaskRequest {
  title?: string;
  description?: string;
  priority?: "low" | "medium" | "high";
  estimatedPomodoros?: number;
  completed?: boolean;
  completedPomodoros?: number;
}

// GET /tasks
// POST /tasks
// PUT /tasks/:id
// DELETE /tasks/:id
// POST /tasks/:id/complete
```

## 6. 통계 시스템

### 6.1 일일 통계

```typescript
interface DailyStats {
  date: string; // YYYY-MM-DD
  completedPomodoros: number;
  totalFocusTime: number; // 분 단위
  completedTasks: number;
  attendance: boolean;
  coinsEarned: number;
  gemsEarned: number;
}

// GET /stats/daily?date=2024-01-15
// GET /stats/daily/range?start=2024-01-01&end=2024-01-31
```

### 6.2 주간/월간 통계

```typescript
interface WeeklyStats {
  weekStart: string; // YYYY-MM-DD
  totalPomodoros: number;
  totalFocusTime: number;
  totalTasks: number;
  averageDailyFocus: number;
  streakDays: number;
  coinsEarned: number;
  gemsEarned: number;
}

interface MonthlyStats {
  month: string; // YYYY-MM
  totalPomodoros: number;
  totalFocusTime: number;
  totalTasks: number;
  averageDailyFocus: number;
  bestDay: {
    date: string;
    focusTime: number;
  };
}

// GET /stats/weekly?week=2024-W03
// GET /stats/monthly?month=2024-01
```

### 6.3 목표 설정

```typescript
interface WeeklyGoal {
  pomodoros: number;
  focusTime: number; // 분 단위
  tasks: number;
}

interface WeeklyProgress {
  pomodorosProgress: number; // 0-100
  focusTimeProgress: number; // 0-100
  tasksProgress: number; // 0-100
}

// GET /stats/goals/weekly
// PUT /stats/goals/weekly
```

## 7. 타마고치 시스템

### 7.1 타마고치 정보

```typescript
interface Tama {
  id: string;
  userId: string;
  name: string;
  type: "tomato" | "coffee" | "book" | "tree" | "cat" | "dog" | "bird" | "fish";
  rarity: "common" | "rare" | "epic" | "legendary" | "mythic";
  level: number;
  experience: number;
  maxExperience: number;
  isActive: boolean;
  acquiredAt: string;
  growthStage: "egg" | "baby" | "child" | "teen" | "adult";
  happiness: number; // 0-100
  hunger: number; // 0-100
  energy: number; // 0-100
  lastInteraction: string;
}

// GET /tamas
// POST /tamas
// PUT /tamas/:id
// DELETE /tamas/:id
```

### 7.2 사용자 인벤토리

```typescript
interface UserInventory {
  coins: number;
  gems: number;
  tamas: Tama[];
  activeTamaId?: string;
}

// GET /inventory
// PUT /inventory/coins
// PUT /inventory/gems
// PUT /inventory/active-tama
```

### 7.3 타마고치 상호작용

```typescript
// POST /tamas/:id/feed
// POST /tamas/:id/play
// POST /tamas/:id/experience
```

## 8. 컬렉션 시스템

### 8.1 배경 테마

```typescript
interface BackgroundSettings {
  type: "gradient" | "image";
  value: string;
  focusColor?: string;
  breakColor?: string;
  theme: "light" | "dark" | "color" | "nature";
  imagePath?: string;
  isPremium: boolean;
}

// GET /backgrounds
// POST /backgrounds/purchase
// PUT /backgrounds/active
```

### 8.2 음악 시스템

```typescript
interface MusicSettings {
  type: "ambient" | "nature" | "focus" | "none";
  value: string;
  volume: number;
  isPremium: boolean;
}

interface MusicTrack {
  id: string;
  name: string;
  artist: string;
  duration: number;
  category: string;
  isPremium: boolean;
  isOwned: boolean;
}

// GET /music/tracks
// POST /music/purchase
// PUT /music/active
```

### 8.3 캐릭터 시스템

```typescript
interface CharacterSettings {
  type: "tomato" | "coffee" | "book" | "tree";
  size: number;
  isPremium: boolean;
}

// GET /characters
// POST /characters/purchase
// PUT /characters/active
```

## 9. 랜덤 박스 시스템

### 9.1 랜덤 박스 정보

```typescript
interface RandomBox {
  id: string;
  name: string;
  price: number;
  currency: "coins" | "gems";
  description: string;
  rarity: "common" | "rare" | "epic" | "legendary";
  rewards: Reward[];
}

interface Reward {
  type: "tama" | "coin" | "gem";
  rarity: string;
  name: string;
  icon: string;
  amount?: number;
}

// GET /random-boxes
// POST /random-boxes/:id/purchase
```

## 10. 결제 시스템

### 10.1 코인/젬 구매

```typescript
interface CoinPackage {
  amount: number;
  price: number; // USD
  bonus: number;
}

interface GemPackage {
  amount: number;
  price: number; // USD
  bonus: number;
}

// GET /purchase/coins
// GET /purchase/gems
// POST /purchase/coins
// POST /purchase/gems
```

### 10.2 프리미엄 구독

```typescript
interface PremiumSubscription {
  type: "monthly" | "yearly";
  price: number;
  features: string[];
}

// GET /subscription/plans
// POST /subscription/subscribe
// POST /subscription/cancel
// GET /subscription/status
```

## 11. 출석 시스템

### 11.1 출석 체크

```typescript
interface Attendance {
  id: string;
  userId: string;
  date: string; // YYYY-MM-DD
  checkedAt: string;
  streakDays: number;
  reward: {
    coins: number;
    gems: number;
  };
}

// GET /attendance
// POST /attendance/check
// GET /attendance/streak
```

## 12. 광고 시스템

### 12.1 광고 표시

```typescript
// POST /ads/interstitial
interface InterstitialAdRequest {
  userId: string;
  context: "timer_complete" | "skip_timer" | "task_complete";
}

// POST /ads/banner
interface BannerAdRequest {
  userId: string;
  placement: "timer" | "tasks" | "collection";
}
```

## 13. 데이터베이스 스키마

### 13.1 주요 테이블

```sql
-- 사용자 테이블
CREATE TABLE users (
  id UUID PRIMARY KEY,
  email VARCHAR(255) UNIQUE NOT NULL,
  name VARCHAR(255),
  is_premium BOOLEAN DEFAULT FALSE,
  created_at TIMESTAMP DEFAULT NOW(),
  updated_at TIMESTAMP DEFAULT NOW(),
  last_login_at TIMESTAMP
);

-- 타이머 세션 테이블
CREATE TABLE timer_sessions (
  id UUID PRIMARY KEY,
  user_id UUID REFERENCES users(id),
  type VARCHAR(20) NOT NULL,
  duration INTEGER NOT NULL,
  completed BOOLEAN DEFAULT FALSE,
  started_at TIMESTAMP NOT NULL,
  completed_at TIMESTAMP,
  task_id UUID REFERENCES tasks(id)
);

-- 태스크 테이블
CREATE TABLE tasks (
  id UUID PRIMARY KEY,
  user_id UUID REFERENCES users(id),
  title VARCHAR(255) NOT NULL,
  description TEXT,
  completed BOOLEAN DEFAULT FALSE,
  priority VARCHAR(10) DEFAULT 'medium',
  estimated_pomodoros INTEGER DEFAULT 1,
  completed_pomodoros INTEGER DEFAULT 0,
  created_at TIMESTAMP DEFAULT NOW(),
  updated_at TIMESTAMP DEFAULT NOW(),
  completed_at TIMESTAMP
);

-- 타마고치 테이블
CREATE TABLE tamas (
  id UUID PRIMARY KEY,
  user_id UUID REFERENCES users(id),
  name VARCHAR(255) NOT NULL,
  type VARCHAR(50) NOT NULL,
  rarity VARCHAR(20) NOT NULL,
  level INTEGER DEFAULT 1,
  experience INTEGER DEFAULT 0,
  max_experience INTEGER DEFAULT 100,
  is_active BOOLEAN DEFAULT FALSE,
  growth_stage VARCHAR(20) DEFAULT 'baby',
  happiness INTEGER DEFAULT 80,
  hunger INTEGER DEFAULT 60,
  energy INTEGER DEFAULT 90,
  acquired_at TIMESTAMP DEFAULT NOW(),
  last_interaction TIMESTAMP DEFAULT NOW()
);

-- 사용자 인벤토리 테이블
CREATE TABLE user_inventories (
  user_id UUID PRIMARY KEY REFERENCES users(id),
  coins INTEGER DEFAULT 100,
  gems INTEGER DEFAULT 10,
  active_tama_id UUID REFERENCES tamas(id),
  updated_at TIMESTAMP DEFAULT NOW()
);

-- 통계 테이블
CREATE TABLE daily_stats (
  id UUID PRIMARY KEY,
  user_id UUID REFERENCES users(id),
  date DATE NOT NULL,
  completed_pomodoros INTEGER DEFAULT 0,
  total_focus_time INTEGER DEFAULT 0,
  completed_tasks INTEGER DEFAULT 0,
  attendance BOOLEAN DEFAULT FALSE,
  coins_earned INTEGER DEFAULT 0,
  gems_earned INTEGER DEFAULT 0,
  UNIQUE(user_id, date)
);
```

## 14. API 엔드포인트 요약

### 14.1 인증

- `POST /auth/apple` - Apple Sign-In
- `POST /auth/refresh` - 토큰 갱신
- `POST /auth/logout` - 로그아웃

### 14.2 사용자

- `GET /user/profile` - 사용자 정보 조회
- `PUT /user/profile` - 사용자 정보 수정

### 14.3 타이머

- `GET /timer/settings` - 타이머 설정 조회
- `PUT /timer/settings` - 타이머 설정 수정
- `POST /timer/sessions` - 타이머 세션 생성
- `PUT /timer/sessions/:id` - 타이머 세션 수정

### 14.4 태스크

- `GET /tasks` - 태스크 목록 조회
- `POST /tasks` - 태스크 생성
- `PUT /tasks/:id` - 태스크 수정
- `DELETE /tasks/:id` - 태스크 삭제
- `POST /tasks/:id/complete` - 태스크 완료

### 14.5 통계

- `GET /stats/daily` - 일일 통계
- `GET /stats/weekly` - 주간 통계
- `GET /stats/monthly` - 월간 통계
- `GET /stats/goals/weekly` - 주간 목표 조회
- `PUT /stats/goals/weekly` - 주간 목표 수정

### 14.6 타마고치

- `GET /tamas` - 타마고치 목록
- `POST /tamas` - 타마고치 생성
- `PUT /tamas/:id` - 타마고치 수정
- `POST /tamas/:id/feed` - 타마고치 먹이기
- `POST /tamas/:id/play` - 타마고치 놀아주기

### 14.7 인벤토리

- `GET /inventory` - 인벤토리 조회
- `PUT /inventory/coins` - 코인 수정
- `PUT /inventory/gems` - 젬 수정

### 14.8 컬렉션

- `GET /backgrounds` - 배경 목록
- `GET /music/tracks` - 음악 트랙 목록
- `GET /characters` - 캐릭터 목록
- `POST /backgrounds/purchase` - 배경 구매
- `POST /music/purchase` - 음악 구매
- `POST /characters/purchase` - 캐릭터 구매

### 14.9 랜덤 박스

- `GET /random-boxes` - 랜덤 박스 목록
- `POST /random-boxes/:id/purchase` - 랜덤 박스 구매

### 14.10 결제

- `GET /purchase/coins` - 코인 패키지 목록
- `GET /purchase/gems` - 젬 패키지 목록
- `POST /purchase/coins` - 코인 구매
- `POST /purchase/gems` - 젬 구매
- `GET /subscription/plans` - 구독 플랜 목록
- `POST /subscription/subscribe` - 구독 시작
- `POST /subscription/cancel` - 구독 취소

### 14.11 출석

- `GET /attendance` - 출석 기록 조회
- `POST /attendance/check` - 출석 체크
- `GET /attendance/streak` - 연속 출석 조회

### 14.12 광고

- `POST /ads/interstitial` - 전면 광고 요청
- `POST /ads/banner` - 배너 광고 요청

## 15. 보안 고려사항

1. **JWT 토큰**: 모든 API 요청에 JWT 토큰 필요
2. **Apple Sign-In**: Apple의 인증 시스템 활용
3. **결제 보안**: Stripe 또는 App Store/Google Play 결제 시스템 활용
4. **데이터 암호화**: 민감한 사용자 데이터 암호화 저장
5. **Rate Limiting**: API 요청 제한으로 DDoS 방지

## 16. 성능 최적화

1. **캐싱**: Redis를 활용한 API 응답 캐싱
2. **인덱싱**: 자주 조회되는 컬럼에 인덱스 설정
3. **페이지네이션**: 대용량 데이터 조회 시 페이지네이션 적용
4. **CDN**: 정적 리소스(이미지, 음악) CDN 활용

## 17. 에러 코드

```typescript
enum ErrorCode {
  // 인증 관련
  UNAUTHORIZED = 401,
  FORBIDDEN = 403,
  TOKEN_EXPIRED = 4011,
  INVALID_TOKEN = 4012,

  // 사용자 관련
  USER_NOT_FOUND = 4041,
  USER_ALREADY_EXISTS = 4091,

  // 결제 관련
  INSUFFICIENT_COINS = 4001,
  INSUFFICIENT_GEMS = 4002,
  PAYMENT_FAILED = 4003,

  // 타마고치 관련
  TAMA_NOT_FOUND = 4042,
  TAMA_ALREADY_OWNED = 4092,

  // 일반
  VALIDATION_ERROR = 400,
  INTERNAL_SERVER_ERROR = 500,
  SERVICE_UNAVAILABLE = 503,
}
```

## 18. 응답 형식

### 18.1 성공 응답

```typescript
interface ApiResponse<T> {
  success: true;
  data: T;
  message?: string;
}
```

### 18.2 에러 응답

```typescript
interface ApiError {
  success: false;
  error: {
    code: ErrorCode;
    message: string;
    details?: any;
  };
}
```

이 명세서를 바탕으로 백엔드 개발자가 완전한 API 서버를 구축할 수 있습니다.

# Tamadoro Backend API Spec (based on current app)

This document lists required endpoints, payloads, and behaviors derived from the current frontend. Use it to scaffold the server and align with the mobile app.

Sections

- Auth
- User
- Timer
- Stats
- Tasks
- Tamas (characters) + Care
- Inventory (coins/gems, active tama)
- Collection (backgrounds, music, characters)
- Purchases (coins/gems/items)
- Subscription (premium)

Notes

- All responses use a common envelope: { success: boolean, data: T, message?: string }
- Use HTTP status codes; errors may include { success: false, error: { code, message, details? } } when applicable.
- Authenticated endpoints require Bearer token.

Auth

- POST /auth/apple

  - body: { identityToken: string, authorizationCode?: string, user: { id: string, email?: string, name?: { firstName?: string, lastName?: string } } }
  - 200: { success, data: { user: User, token: string, refreshToken: string } }

- POST /auth/refresh

  - body: { refreshToken: string }
  - 200: { success, data: { token: string, refreshToken: string } }

- POST /auth/logout
  - 200: { success, data: null }

User

- GET /user/profile

  - 200: { success, data: User }

- PUT /user/profile
  - body: Partial<User>
  - 200: { success, data: User }

Types

- User: { id: string, email: string, name: string, isPremium: boolean, createdAt: string, updatedAt: string, lastLoginAt: string, subscription?: { type: "MONTHLY"|"YEARLY", startDate: string, endDate: string, status: "active"|"cancelled"|"expired" } }

Timer

- GET /timer/settings

  - 200: { success, data: TimerSettings }

- PUT /timer/settings

  - body: Partial<TimerSettings>
  - 200: { success, data: TimerSettings }

- POST /timer/sessions

  - body: Omit<TimerSession, "id"|"userId">
  - 201: { success, data: TimerSession }

- PUT /timer/sessions/:id
  - body: Partial<TimerSession>
  - 200: { success, data: TimerSession }

Types

- TimerSettings: { workTime: number, shortBreakTime: number, longBreakTime: number, longBreakInterval: number, autoStartBreaks: boolean, autoStartPomodoros: boolean, soundEnabled: boolean, vibrationEnabled: boolean, notificationsEnabled: boolean }
- TimerSession: { id: string, userId: string, type: "focus"|"shortBreak"|"longBreak", duration: number, completed: boolean, startedAt: string, completedAt?: string, taskId?: string }

Stats

- GET /stats/daily?startDate=YYYY-MM-DD&endDate=YYYY-MM-DD

  - 200: { success, data: DailyStats[] }

- GET /stats/weekly?week=YYYY-[W]ww

  - 200: { success, data: { ... } } (shape flexible for now)

- GET /stats/monthly?month=YYYY-MM
  - 200: { success, data: { ... } }

Types

- DailyStats: { date: string, completedPomodoros: number, totalFocusTime: number, completedTasks: number, attendance: boolean, coinsEarned: number, gemsEarned: number }

Tasks

- GET /tasks

  - 200: { success, data: Task[] }

- POST /tasks

  - body: CreateTaskRequest
  - 201: { success, data: Task }

- PUT /tasks/:id

  - body: UpdateTaskRequest
  - 200: { success, data: Task }

- DELETE /tasks/:id

  - 200: { success, data: null }

- POST /tasks/:id/complete
  - 200: { success, data: null }

Types

- Task: { id: string, userId: string, title: string, description?: string, completed: boolean, priority: "low"|"medium"|"high", estimatedPomodoros: number, completedPomodoros: number, createdAt: string, updatedAt: string, completedAt?: string }
- CreateTaskRequest: { title: string, description?: string, priority: "low"|"medium"|"high", estimatedPomodoros: number }
- UpdateTaskRequest: Partial<CreateTaskRequest> & { completed?: boolean, completedPomodoros?: number }

Tamas (characters) + Care

- GET /tamas

  - 200: { success, data: UserInventory["tamas"] }

- POST /tamas

  - body: { id: string, name?: string }
  - 201: { success, data: any }

- PUT /tamas/:id

  - body: { name?: string, stage?: "egg"|"baby"|"child"|"teen"|"adult" }
  - 200: { success, data: any }

- DELETE /tamas/:id

  - 200: { success, data: null }

- POST /tamas/:id/feed

  - 200: { success, data: null }

- POST /tamas/:id/play

  - 200: { success, data: null }

- POST /tamas/:id/experience
  - 200: { success, data: null }

Inventory

- GET /inventory

  - 200: { success, data: UserInventory }

- PUT /inventory/coins

  - body: { amount: number } (absolute or delta; choose one policy)
  - 200: { success, data: null }

- PUT /inventory/gems

  - body: { amount: number }
  - 200: { success, data: null }

- PUT /inventory/active-tama
  - body: { id: string }
  - 200: { success, data: null }

Types

- UserInventory: { coins: number, gems: number, tamas: Tama[], activeTamaId?: string }
- Tama: { id: string, userId: string, name: string, type: "tomato"|"coffee"|"book"|"tree"|..., rarity: "common"|"rare"|"epic"|"legendary"|"mythic", level: number, experience: number, maxExperience: number, isActive: boolean, acquiredAt: string, growthStage: "egg"|"baby"|"child"|"teen"|"adult", happiness: number, hunger: number, energy: number, lastInteraction: string }

Collection

- GET /backgrounds

  - 200: { success, data: BackgroundItem[] }

- GET /sound/tracks

  - 200: { success, data: MusicItem[] }

- GET /characters
  - 200: { success, data: CharacterItem[] }

Types

- BackgroundItem: { id: string, title: string, theme: string, isPremium: boolean, url: string }
- MusicItem: { id: string, title: string, theme: string, resource: string, url: string, isPremium: boolean }
- CharacterItem: { id: string, title: string, theme: string, isPremium: boolean, stages: { name: "egg"|"baby"|"child"|"teen"|"adult", experience: number, maxExperience: number, level: number, url: string }[], happiness: number, hunger: number, energy: number }

Purchases (store)

- GET /purchase/coins

  - 200: { success, data: CoinPackage[] }

- GET /purchase/gems

  - 200: { success, data: GemPackage[] }

- POST /purchase/coins

  - body: { packageId: string }
  - 200: { success, data: null }

- POST /purchase/gems

  - body: { packageId: string }
  - 200: { success, data: null }

- POST /backgrounds/:id/purchase

  - 200: { success, data: null }

- POST /sound/tracks/:id/purchase

  - 200: { success, data: null }

- POST /tamas/:id/purchase

  - 200: { success, data: null }

- POST /random-boxes/:id/purchase
  - 200: { success, data: Reward[] }

Types

- CoinPackage: { amount: number, price: number, bonus: number }
- GemPackage: { amount: number, price: number, bonus: number }
- Reward: { type: "tama"|"coin"|"gem", rarity: string, name: string, icon: string, amount?: number }

Subscription (premium)

- GET /subscription/plans

  - 200: { success, data: { type: "MONTHLY"|"YEARLY", price: number, features: string[] }[] }

- POST /subscription

  - body: { type: "MONTHLY"|"YEARLY" }
  - 200: { success, data: null }

- POST /subscription/cancel

  - 200: { success, data: null }

- GET /subscription/status
  - 200: { success, data: { isPremium: boolean } | { isActive: boolean } | { status: "active"|"inactive" } }

Server behaviors and hooks

- When purchasing items or currency, also update /inventory accordingly.
- Feeding/playing with a tama should update hunger/happiness/energy and optionally grant small XP; also record timestamps to support decay.
- For Apple Sign-In, validate identityToken with Apple, create user on first sign-in, and return app-issued tokens.
- For subscription, verify App Store receipts server-side (future); for now, honor plan selection and set user.isPremium.

Open questions to finalize

- Are coin/gem updates absolute set or delta? App currently calls additive flows; prefer delta: { delta: number }.
- Should /characters include CDN absolute URLs for stage images? Frontend can accept absolute URLs.
- Random box reward tables and probabilities to be confirmed.

Appendix: minimal shapes (from frontend)

- Payment mock products: premium*monthly, premium_yearly, coins*_, gems\__.
- Feature flags exist in-app; backend should tolerate absent features gracefully.

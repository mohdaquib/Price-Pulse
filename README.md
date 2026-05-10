<div align="center">

<img src="design/ic_launcher_full.svg" width="108" alt="PricePulse logo"/>

# PricePulse

**Real-time stock price tracking for Android**

[![API](https://img.shields.io/badge/API-24%2B-brightgreen)](https://android-arsenal.com/api?level=24)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.3-7F52FF)](https://kotlinlang.org)
[![Compose](https://img.shields.io/badge/Jetpack%20Compose-BOM%202025-4285F4)](https://developer.android.com/jetpack/compose)
[![Architecture](https://img.shields.io/badge/Clean%20Architecture-MVVM-F0B90B)](https://developer.android.com/topic/architecture)
[![License](https://img.shields.io/badge/License-MIT-02C076)](LICENSE)
[![CI](https://github.com/mohdaquib/Price-Pulse/actions/workflows/android-ci.yml/badge.svg)](https://github.com/mohdaquib/Price-Pulse/actions/workflows/android-ci.yml)

*Live prices · Animated charts · Price alerts · Order book · Watchlist · Offline support*

</div>

---

## Demo

<div align="center">

![PricePulse demo](docs/demo.gif)

</div>

---

## Why PricePulse?

Most Android portfolio projects stop at a basic list that fetches data once.
PricePulse goes further by solving the problems that come up in real trading apps:

| Real-world problem | How PricePulse handles it |
|--------------------|--------------------------|
| Live data at sub-second latency | Finnhub WebSocket with 30 s ping keepalive |
| Network drops killing the feed | Exponential back-off reconnect (1 s → 64 s + jitter) |
| Cold-start with no data yet | REST snapshot on launch — list is never blank |
| User goes offline mid-session | Cached prices shown with "X min ago" banner |
| Alerts when app is closed | WorkManager `AlertCheckWorker` runs every 15 min |
| Charts that feel alive | Cubic Bézier path + 600 ms animated clip per update |

The goal was not just to make a demo — it was to build something that handles
the same edge cases a production trading app would need to handle.

---

## Features

### Live data
- **WebSocket feed** via [Finnhub](https://finnhub.io/) for 25 NASDAQ stocks simultaneously
- **REST snapshot** on every launch so the list is populated before the socket connects
- **Price flash animation** — each row briefly glows green (up) or red (down) on every tick

### Charts & visualisation
- **Animated line chart** — cubic Bézier curves, vertical gradient fill, 600 ms left-to-right entry
- **Latest-price dot** with glow ring pinned to the chart's current endpoint
- **Order book panel** — bids (green) vs. asks (red) with proportional depth bars and spread display

### Alerts & watchlist
- **Price alerts** — set ABOVE or BELOW a target price; system notification fires when triggered
- **Background polling** — WorkManager job checks alerts every 15 minutes, even when the app is closed
- **Watchlist** — star any symbol; persisted across sessions with DataStore

### App quality
- **Offline cache** — SharedPreferences-backed snapshot; shows "cached · N min ago" banner
- **Dark / Light mode toggle** — Binance-inspired dark palette by default
- **Android 12 SplashScreen API** — adaptive icon on dark canvas with 400 ms fade-out
- **Edge-to-edge layout**, Material 3, and dynamic typography

---

## Architecture

Clean Architecture with three strictly separated layers and MVVM in the presentation tier.
Hilt wires the dependency graph at compile time.

```
┌─────────────────────────────────────────────────────────────────────┐
│                        PRESENTATION LAYER                           │
│                                                                     │
│  ┌──────────────────────────┐      ┌─────────────────────────────┐  │
│  │    Jetpack Compose UI    │◀─────│    PriceTrackerViewModel    │  │
│  │                          │      │                             │  │
│  │  PriceTrackerScreen      │      │  StateFlow<PriceTrackerUI   │  │
│  │  ├─ TradingTopBar        │      │  State>                     │  │
│  │  ├─ TradingTabRow        │      │  ├─ stocks: List<Stock>     │  │
│  │  │    MARKETS / WATCHLIST│      │  ├─ watchlist: Set<String>  │  │
│  │  ├─ PriceList            │      │  ├─ alerts: List<Alert>     │  │
│  │  ├─ StockChartPanel      │      │  ├─ isConnected: Boolean    │  │
│  │  │    └─ PriceLineChart  │      │  ├─ isOffline: Boolean      │  │
│  │  ├─ OrderBookPanel       │      │  └─ orderBook: OrderBook?   │  │
│  │  └─ SetAlertDialog       │      └─────────────────────────────┘  │
│  └──────────────────────────┘                                       │
└──────────────────────────────────────────┬──────────────────────────┘
                                           │ 13 use cases
┌──────────────────────────────────────────▼──────────────────────────┐
│                          DOMAIN LAYER                               │
│                       (zero Android imports)                        │
│                                                                     │
│  Use Cases                            Entities                      │
│  ├─ GetInitialStocksUseCase           ├─ Stock                      │
│  ├─ GetCachedStocksUseCase            ├─ PriceAlert (ABOVE/BELOW)   │
│  ├─ SubscribeToPriceUpdatesUseCase    ├─ OrderBook                  │
│  ├─ WatchSymbolsUseCase               └─ OrderBookEntry             │
│  ├─ ManageConnectionUseCase                                         │
│  ├─ ObserveWatchlistUseCase           Repository interfaces         │
│  ├─ AddToWatchlistUseCase             ├─ PriceRepository            │
│  ├─ RemoveFromWatchlistUseCase        ├─ WatchlistRepository        │
│  ├─ ObserveAlertsUseCase              ├─ AlertRepository            │
│  ├─ AddAlertUseCase                   └─ ConnectionRepository       │
│  ├─ RemoveAlertUseCase                                              │
│  ├─ CheckAlertsUseCase                                              │
│  └─ ObserveOrderBookUseCase                                         │
└──────────────────────────────────────────┬──────────────────────────┘
                                           │ concrete implementations
┌──────────────────────────────────────────▼──────────────────────────┐
│                           DATA LAYER                                │
│                                                                     │
│  ┌─────────────────────┐ ┌───────────────────┐ ┌─────────────────┐  │
│  │ WebSocket           │ │ REST (Finnhub)     │ │ Local storage   │  │
│  │                     │ │                   │ │                 │  │
│  │ wss://ws.finnhub.io │ │ /api/v1/quote     │ │ DataStore       │  │
│  │                     │ │ 25 symbols/launch │ │ watchlist +     │  │
│  │ Exponential back-off│ │ OkHttp + Gson     │ │ alerts          │  │
│  │ 1 s → 64 s + jitter │ │                   │ │                 │  │
│  │ 30 s ping keepalive │ │                   │ │ SharedPrefs     │  │
│  └─────────────────────┘ └───────────────────┘ │ price cache     │  │
│                                                 └─────────────────┘  │
└─────────────────────────────────────────────────────────────────────┘
                      ▲ Hilt DI wires all dependencies
```

### Data flow — price update

```
Finnhub WebSocket
    │  tick JSON
    ▼
WebSocketDataSource (SharedFlow, capacity 64)
    │
    ▼
SubscribeToPriceUpdatesUseCase
    │
    ▼
PriceTrackerViewModel
    ├─ updates priceHistory (ArrayDeque, max 50 points)
    ├─ computes flash colour (BullGreen / BearRed)
    └─ emits new StateFlow snapshot
         │
         ▼
    Compose recomposes affected rows only
```

---

## 🧩 Modularization

Multi-module Gradle architecture for scalability, maintainability, and clean separation of concerns.

| Module | Responsibility |
|--------|----------------|
| `:app` | Entry point and navigation |
| `:core:network` | WebSocket + networking setup |
| `:core:common` | Shared utilities |
| `:domain` | Business logic, use cases, and repository interfaces |
| `:data` | Repository implementations and data sources |
| `:feature:price` | UI screens and ViewModel |

- **Independent feature development** — each module compiles and tests in isolation
- **Faster builds** — Gradle only recompiles changed modules
- **Better testability** — minimal surface area per module

---

## Tech stack

| Layer | Library / Tool | Version |
|-------|---------------|---------|
| Language | Kotlin | 2.3 |
| UI | Jetpack Compose + Material 3 | BOM 2025 |
| DI | Hilt | 2.59 |
| Async | Coroutines · StateFlow · SharedFlow | 1.10 |
| Network | OkHttp (WebSocket + REST) | 5.3 |
| Serialisation | Gson | 2.13 |
| Local storage | DataStore Preferences | 1.2 |
| Background work | WorkManager | 2.10 |
| Splash screen | AndroidX Core SplashScreen | 1.0 |
| Build | Gradle KTS + Version Catalog | AGP 9.1 |

---

## 🚀 CI/CD

Runs on every push and pull request to `main` via [GitHub Actions](https://github.com/mohd-aquib/Realtime-Price-Tracker/actions/workflows/ci.yml).

- **Build** — `./gradlew assembleDebug`
- **Unit tests** — `./gradlew test` across all modules
- **Lint** — `./gradlew lint`
- **APK artifact** — debug APK uploaded as a workflow artifact

---

## Getting started

### Prerequisites

- Android Studio Meerkat 2024.3 or later
- JDK 17
- A free [Finnhub API key](https://finnhub.io/register) (no credit card required)

### 1. Clone

```bash
git clone https://github.com/mohd-aquib/Realtime-Price-Tracker.git
cd Realtime-Price-Tracker
```

### 2. Add your API key

Edit `app/src/main/java/com/aquib/pricepulse/data/config/Constants.kt`:

```kotlin
const val API_KEY = "your_finnhub_api_key_here"
```

### 3. Run

```bash
./gradlew installDebug
```

Or press **Run ▶** in Android Studio. The free Finnhub plan covers all 25 default symbols.

---

## Package structure

```
com.aquib.pricepulse/
│
├── MainActivity.kt
├── PricePulseApp.kt
│
├── di/
│   └── DataModule.kt                 # Hilt @Provides and @Binds
│
├── domain/                           # No Android dependencies
│   ├── config/Constants.kt           # 25 default stock symbols
│   ├── entities/
│   │   ├── Stock.kt
│   │   ├── PriceAlert.kt             # AlertCondition: ABOVE | BELOW
│   │   ├── OrderBook.kt
│   │   └── OrderBookEntry.kt
│   ├── repositories/                 # Interfaces only
│   └── usecases/                     # 13 single-responsibility classes
│
├── data/
│   ├── config/Constants.kt           # API_KEY, WS_URL, endpoints
│   ├── datasource/
│   │   ├── WebSocketDataSource.kt    # OkHttp WS + back-off + reconnect
│   │   └── FinnhubRestDataSource.kt  # Quote endpoint, coroutine IO
│   ├── local/
│   │   ├── StockCacheDataSource.kt   # SharedPrefs snapshot + timestamp
│   │   ├── WatchlistDataSource.kt    # DataStore persistence
│   │   └── AlertDataSource.kt        # DataStore persistence
│   ├── dto/
│   │   ├── FinnhubDtos.kt
│   │   └── PriceUpdateDto.kt
│   ├── repositories/                 # 4 concrete implementations
│   ├── worker/AlertCheckWorker.kt    # WorkManager 15-min alert poll
│   └── notification/NotificationHelper.kt
│
└── presentation/
    ├── state/PriceTrackerUiState.kt
    ├── viewmodel/PriceTrackerViewModel.kt
    └── ui/
        ├── PriceTrackerScreen.kt     # Markets + Watchlist tabs
        ├── PriceLineChart.kt         # Cubic Bézier + animated clip
        ├── OrderBookPanel.kt         # Depth bars, spread display
        ├── SetAlertDialog.kt
        └── theme/
            ├── Color.kt              # Binance-inspired dark palette
            ├── Theme.kt
            └── Type.kt
```

---

## Tracked symbols

25 NASDAQ tech stocks configured out of the box:

```
AAPL  GOOG  TSLA  AMZN  MSFT  NVDA  META  NFLX  ADBE  CRM
INTC  AMD   ORCL  CSCO  IBM   QCOM  AVGO  TXN   MU    AMAT
LRCX  KLAC  NOW   SNPS  CDNS
```

To add or swap symbols, edit `domain/config/Constants.kt`:

```kotlin
val STOCK_SYMBOLS = listOf("AAPL", "GOOG", /* your symbols */)
```

---

## License

Distributed under the MIT License. See [`LICENSE`](LICENSE) for details.

---

## Contributing

1. Fork the repository
2. Create a branch: `git checkout -b feature/my-feature`
3. Commit: `git commit -m 'Add my feature'`
4. Push: `git push origin feature/my-feature`
5. Open a Pull Request

---

<div align="center">

Built by [Mohd Aquib](https://github.com/mohd-aquib) · Data by [Finnhub](https://finnhub.io/)

</div>


# Revy Launcher - Implementation Plan

## 1. Project Setup and Configuration

### 1.1 Update AndroidManifest.xml
- Add launcher intent filter to MainActivity
  ```xml
  <intent-filter>
      <action android:name="android.intent.action.MAIN" />
      <category android:name="android.intent.category.LAUNCHER" />
      <category android:name="android.intent.category.HOME" />
      <category android:name="android.intent.category.DEFAULT" />
  </intent-filter>
  ```
- Add required permissions
  ```xml
  <uses-permission android:name="android.permission.PACKAGE_USAGE_STATS" />
  <uses-permission android:name="android.permission.REQUEST_DELETE_PACKAGES" />
  ```

### 1.2 Update Dependencies
- Add Hilt for dependency injection
  ```kotlin
  implementation(libs.hilt.android)
  kapt(libs.hilt.compiler)
  ```
- Add DataStore for preferences
  ```kotlin
  implementation(libs.androidx.datastore.preferences)
  implementation(libs.androidx.datastore.proto)
  ```
- Add Navigation component
  ```kotlin
  implementation(libs.androidx.navigation.compose)
  ```
- Add Pager for horizontal swiping
  ```kotlin
  implementation(libs.androidx.compose.foundation.pager)
  ```
- Add AppWidget hosting libraries
  ```kotlin
  implementation(libs.androidx.glance.appwidget)
  ```

### 1.3 Setup Project Structure
```
app/src/main/java/me/semoro/revy/
├── MainActivity.kt
├── RevyApplication.kt
├── data/
│   ├── repository/
│   │   ├── AppUsageRepository.kt
│   │   ├── PinnedAppsRepository.kt
│   │   └── WidgetRepository.kt
│   ├── local/
│   │   ├── AppUsageDataSource.kt
│   │   ├── PinnedAppsDataStore.kt
│   │   └── WidgetLayoutDataStore.kt
│   └── model/
│       ├── AppInfo.kt
│       ├── RecencyBucket.kt
│       └── WidgetLayout.kt
├── di/
│   ├── AppModule.kt
│   ├── RepositoryModule.kt
│   └── ViewModelModule.kt
├── ui/
│   ├── navigation/
│   │   ├── NavGraph.kt
│   │   └── Screen.kt
│   ├── theme/
│   │   └── Theme.kt
│   ├── components/
│   │   ├── AppGrid.kt
│   │   ├── AppIcon.kt
│   │   ├── BucketHeader.kt
│   │   ├── PinnedAppsStrip.kt
│   │   ├── SearchBar.kt
│   │   └── WidgetHost.kt
│   ├── home/
│   │   ├── HomeScreen.kt
│   │   └── HomeViewModel.kt
│   ├── widgets/
│   │   ├── WidgetScreen.kt
│   │   └── WidgetViewModel.kt
│   ├── settings/
│   │   ├── SettingsScreen.kt
│   │   └── SettingsViewModel.kt
│   └── permissions/
│       └── PermissionScreen.kt
└── util/
    ├── AppLauncherUtils.kt
    ├── PermissionUtils.kt
    └── WallpaperUtils.kt
```

## 2. Implementation Phases

### Phase 1: Core Infrastructure (2 weeks)

#### 2.1 Setup Application Class and DI
- Create `RevyApplication` class with Hilt
- Setup dependency injection modules

#### 2.2 Data Layer Implementation
- Implement `AppUsageDataSource` using `UsageStatsManager`
- Create `AppUsageRepository` for accessing app usage data
- Implement `PinnedAppsDataStore` using DataStore
- Create `PinnedAppsRepository` for managing pinned apps

#### 2.3 Permission Handling
- Implement `PermissionUtils` for checking and requesting permissions
- Create `PermissionScreen` for onboarding and permission requests
- Implement block-until-granted flow for Usage Access permission

### Phase 2: UI Foundation (2 weeks)

#### 2.1 Navigation Setup
- Implement `NavGraph` with main screens
- Setup navigation between screens

#### 2.2 Theme and Components
- Implement Material You dynamic colors with One UI fallback
- Create reusable UI components (AppIcon, BucketHeader, etc.)

#### 2.3 Home Screen Implementation
- Create `HomeViewModel` for managing app list state
- Implement `HomeScreen` with recency-sorted grid
- Add recency bucket headers (Today, Yesterday, This week, Older)

### Phase 3: Core Features (3 weeks)

#### 3.1 App Grid and Recency Buckets
- Implement 4×5 grid layout for portrait mode
- Add stable positioning within buckets
- Implement bucket headers that remain visible when scrolling

#### 3.2 Pinning Functionality
- Implement long-press to pin/unpin
- Create pinned apps strip above the recency grid
- Add persistence for pinned apps order

#### 3.3 Search Implementation
- Add pull-down gesture to reveal search field
- Implement live filtering with debouncing
- Add back press handling to dismiss search

### Phase 4: Advanced Features (3 weeks)

#### 4.1 Pager and Multiple Pages
- Implement horizontal swipe with Compose Pager
- Setup page 1 for app grid and subsequent pages for widgets
- Ensure pinned strip is shared across all pages

#### 4.2 Widget Support
- Implement widget host functionality
- Add widget configuration and resizing
- Create persistence for widget layouts

#### 4.3 App Context Menu
- Implement long-press context menu
- Add app shortcuts, App info, and Uninstall options
- Handle permission for package deletion

### Phase 5: Wallpaper and Polish (2 weeks)

#### 5.1 Wallpaper Picker
- Implement settings screen with wallpaper option
- Add Photo Picker integration
- Implement optional crop/scale preview
- Add WallpaperManager integration

#### 5.2 Animations and Haptics
- Add content size animations for resorting
- Implement blur/fade transitions for wallpaper changes
- Add haptic feedback for pin/unpin actions

#### 5.3 Final Polish
- Implement error handling and edge cases
- Add loading states and placeholders
- Optimize performance for smooth scrolling

## 3. Testing Strategy

### 3.1 Unit Tests
- Test repositories and data sources
- Test ViewModels and state management
- Test utility functions

### 3.2 UI Tests
- Test navigation flows
- Test app grid and recency buckets
- Test search functionality
- Test widget integration

### 3.3 Integration Tests
- Test permission handling
- Test app launching
- Test widget hosting

## 4. Deployment

### 4.1 Build Configuration
- Setup release signing configuration
- Configure ProGuard rules
- Implement version management

### 4.2 Distribution
- Setup GitHub Actions for CI/CD
- Configure direct APK builds from the repository
- Create release documentation

## 5. Timeline and Milestones

### Milestone 1: Core Infrastructure (Week 2)
- Application setup complete
- Data layer implemented
- Permission handling working

### Milestone 2: Basic UI (Week 4)
- Navigation working
- Theme implemented
- Home screen with basic app grid

### Milestone 3: Core Features (Week 7)
- Recency buckets implemented
- Pinning functionality working
- Search implemented

### Milestone 4: Advanced Features (Week 10)
- Pager and multiple pages working
- Widget support implemented
- App context menu working

### Milestone 5: Final Release (Week 12)
- Wallpaper picker implemented
- Animations and haptics added
- Testing complete
- APK builds available

## 6. Risk Management

| Risk | Mitigation |
|------|------------|
| Usage Access permission denial | Implement clear onboarding explaining the need for permission with visual guides |
| Default launcher switching issues | Add detailed instructions for setting Revy as default launcher |
| Widget compatibility issues | Test with popular widgets and implement fallbacks for incompatible widgets |
| Performance with many apps | Implement pagination and efficient rendering for large app lists |
| API changes in future Android versions | Regular updates to target latest API and test on beta releases |

This implementation plan provides a comprehensive roadmap for developing the Revy Launcher according to the specified requirements, with a focus on the core functionality of displaying recently used apps in a user-friendly manner.
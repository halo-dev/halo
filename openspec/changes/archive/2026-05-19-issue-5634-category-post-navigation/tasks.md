## 1. Finder Interface & Implementation

- [x] 1.1 Add `cursorByCategory(String currentName)` method to `PostFinder` interface
- [x] 1.2 Implement `cursorByCategory` in `PostFinderImpl` with exact-match primary category filtering
- [x] 1.3 Add fallback logic: when post has no categories, delegate to existing `cursor()`
- [x] 1.4 Run `./gradlew spotlessApply` to format new code

## 2. REST API Endpoint

- [x] 2.1 Update `PostQueryEndpoint.getPostNavigationByName` to accept optional `scope` query parameter
- [x] 2.2 Route `scope=category` to `postFinder.cursorByCategory(name)`, default to `postFinder.cursor(name)`
- [x] 2.3 Verify endpoint is documented by SpringDoc (check OpenAPI spec generation)

## 3. Tests

- [x] 3.1 Add unit tests for `PostFinderImpl.cursorByCategory` in `PostFinderImplTest`
  - Post with categories → scoped navigation
  - Post without categories → global fallback
  - Hidden adjacent post → skipped
- [x] 3.2 Add/update tests for `PostQueryEndpoint` navigation endpoint with `scope=category`
- [x] 3.3 Run `./gradlew test` to verify all tests pass

## 4. Verification

- [x] 4.1 Build project with `./gradlew build`
- [x] 4.2 Confirm no breaking changes to existing `cursor()` behavior
- [x] 4.3 Review OpenAPI docs to ensure new query parameter is documented


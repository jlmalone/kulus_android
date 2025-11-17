# Kulus Android - Critical Issues & Action Plan

## Overview

Two critical issues identified:

1. **Weak Onboarding Experience** - Users can see app content during setup
2. **Server-Side Security Flaw** - Anyone can access anyone's glucose data

## Issue 1: Weak Onboarding (UI/UX)

### Current Problems
- ❌ Dialog-based setup shows main app in background
- ❌ Can see "Latest Reading" before user is set up
- ❌ No explanation of features or privacy
- ❌ Single-step setup is not engaging
- ⚠️ Purple color appearing (needs investigation)

### Solution
**Full-screen onboarding flow with 5 screens:**

1. **Welcome** - Hamumu logo, app name, get started
2. **Features** - Photo OCR, trends, cloud sync, privacy
3. **Privacy** - Data segregation explanation, multi-device info
4. **Account Setup** - Name/phone entry with confirmation
5. **Completion** - Success message, auto-proceed to app

### Implementation
See: `ONBOARDING_REDESIGN.md` for complete code

**Files to create:**
- `ui/onboarding/OnboardingFlow.kt` - Main coordinator
- `ui/onboarding/WelcomeScreen.kt`
- `ui/onboarding/FeaturesScreen.kt`
- `ui/onboarding/PrivacyScreen.kt`
- `ui/onboarding/AccountSetupScreen.kt`
- `ui/onboarding/CompletionScreen.kt`

**Files to modify:**
- `MainActivity.kt` - Add onboarding check before showing Dashboard

**Timeline:** 2-3 days

## Issue 2: Server-Side Security Flaw (CRITICAL)

### Current Vulnerability

🚨 **HIPAA/Privacy Violation** - Any user can access any other user's medical data

**How it works now (BROKEN):**
```typescript
// Alice authenticates and gets a token
POST /validatePassword { password: "kulus2025" }
→ Returns token (not linked to any user)

// Alice can now access BOB's data!
GET /reportingApiV2?action=readings&name=Bob
Authorization: Bearer <alice_token>
→ Returns all of Bob's glucose readings! 🚨
```

**The problem:**
- Token proves password was entered correctly
- Token does NOT identify which user
- API trusts the `name` parameter from client
- Server doesn't verify user is requesting their own data

### Solution Architecture

**Token-Based User Identity:**

```typescript
// NEW: Token includes user identity
interface KulusTokenPayload {
    authenticated: boolean;
    userId: string;       // Hash of userName
    userName: string;     // "Alice" or "(555) 123-4567"
    timestamp: number;
    expiresAt: number;
}

// Client authenticates WITH their userName
POST /validatePassword {
    password: "kulus2025",
    userName: "Alice"     // NEW!
}

// Server binds token to this user
→ Returns token containing userId + userName

// Client requests their readings
GET /reportingApiV2?action=readings
Authorization: Bearer <token>

// Server extracts userName from token (ignores any name parameter)
→ Returns ONLY Alice's readings ✅

// Even if Alice tries to hack:
GET /reportingApiV2?action=readings&name=Bob
→ Server ignores "name" parameter, uses token identity
→ Still returns ONLY Alice's readings ✅
```

### Implementation Priority

**🔴 CRITICAL - Week 1:**

Backend (`~/WebstormProjects/Kulus-App/functions/src/`):
1. Update `validatePassword` to accept `userName` parameter
2. Modify token structure to include `userId` + `userName`
3. Update `getUserFromToken` to extract user identity
4. Modify ALL endpoints to use token identity instead of query params:
   - `reportingApiV2` - Remove `name` parameter, use token
   - `addReadingFromUrl` - Remove `name` parameter, use token
   - `getAllReadings` - DEPRECATED (security risk)
5. Add `userId` field to all new readings
6. Deploy backend changes

Android (`~/StudioProjects/kulus_android/`):
1. Update `AuthRequest` to include `userName: String`
2. Modify `authenticate()` to pass user's name from preferences
3. Update `TokenStore` to save `userId` and `userName`
4. Remove `name` parameter from `getReadingsByName()`
5. Remove `name` parameter from `addReading()`
6. Update `KulusRepository` to authenticate with userName on first launch
7. Test with multiple users

**🟡 SHORT-TERM - Week 2:**
1. Database migration: Add `userId` to existing readings
2. Add access logging/auditing
3. Add Firestore security rules
4. Add monitoring/alerts for suspicious access patterns

**🟢 LONG-TERM - Month 1:**
1. Migrate to Firebase Authentication (proper user accounts)
2. Add email/phone verification
3. HIPAA compliance review
4. Penetration testing

### Testing Plan

**Security Test Cases:**

```bash
# Test 1: User can only access own data
alice_token=$(authenticate "Alice")
curl /reportingApiV2?action=readings -H "Authorization: Bearer $alice_token"
→ Should return ONLY Alice's readings ✅

# Test 2: Cannot access other user's data via name parameter
curl /reportingApiV2?action=readings&name=Bob -H "Authorization: Bearer $alice_token"
→ Should return ONLY Alice's readings (ignores name=Bob) ✅

# Test 3: Cannot add readings for other users
curl /addReadingFromUrl?reading=5.5&name=Bob -H "Authorization: Bearer $alice_token"
→ Should add reading for Alice (ignores name=Bob) ✅

# Test 4: Token validation
curl /reportingApiV2?action=readings -H "Authorization: Bearer invalid_token"
→ Should return 401 Unauthorized ✅
```

## Purple Color Investigation

**Files to check:**

```kotlin
// 1. Check theme colors
app/src/main/java/org/kulus/android/ui/theme/Color.kt

// Look for:
val GlucosePurple = Color(0xFFB967FF)  // When is this used?

// 2. Check glucose level color mapping
app/src/main/java/org/kulus/android/ui/components/GlucoseReadingCard.kt

// Verify:
when (reading.color) {
    "Purple" -> GlucosePurple  // What triggers "Purple" level?
    // Should be: Critical readings (>13.9 mmol/L or <2.2 mmol/L)
}

// 3. Check navigation bar colors
app/src/main/java/org/kulus/android/ui/screens/DashboardScreen.kt

// Ensure:
NavigationBarItemDefaults.colors(
    selectedIconColor = MatrixNeon,     // Should be green
    indicatorColor = MatrixPrimary,     // Not purple
)
```

**Likely causes:**
- GlucosePurple used for critical/dangerous readings (>13.9 or <2.2 mmol/L)
- Material3 default colors showing through
- Navigation indicator using wrong color scheme

## Complete Action Plan

### Phase 1: Security Fix (CRITICAL - This Week)

**Day 1-2: Backend**
- [ ] Update token structure in `functions/src/auth.ts`
- [ ] Modify validatePassword to accept userName
- [ ] Update all endpoints to use token identity
- [ ] Add userId generation logic
- [ ] Deploy to Firebase
- [ ] Test with curl commands

**Day 3: Android Client**
- [ ] Update AuthRequest/AuthResponse models
- [ ] Modify authentication flow to pass userName
- [ ] Remove name parameters from API calls
- [ ] Update TokenStore to save user identity
- [ ] Test with multiple user accounts

**Day 4: Testing**
- [ ] End-to-end testing with 3+ users
- [ ] Verify data segregation works
- [ ] Verify cannot access other users' data
- [ ] Load testing
- [ ] Security audit

### Phase 2: Onboarding Redesign (Next Week)

**Day 1: Setup**
- [ ] Create onboarding package structure
- [ ] Set up navigation coordinator
- [ ] Create shared components (FeatureItem, PrivacyPoint)

**Day 2: Screens**
- [ ] Implement WelcomeScreen
- [ ] Implement FeaturesScreen
- [ ] Implement PrivacyScreen
- [ ] Implement AccountSetupScreen
- [ ] Implement CompletionScreen

**Day 3: Integration**
- [ ] Update MainActivity to show onboarding
- [ ] Add slide animations
- [ ] Add progress indicator
- [ ] Test onboarding flow

**Day 4: Polish**
- [ ] Fix purple color issues
- [ ] Ensure Matrix theme throughout
- [ ] Add haptic feedback
- [ ] Add animations
- [ ] Final testing

### Phase 3: Long-term Improvements (Month 1)

- [ ] Firebase Authentication migration
- [ ] Firestore security rules
- [ ] Database migration script
- [ ] Access logging/auditing
- [ ] HIPAA compliance review
- [ ] Penetration testing
- [ ] Bug bounty program

## Critical Deliverables

### This Week
1. **Backend security patch deployed**
2. **Android client updated for secure auth**
3. **Security testing completed**
4. **Documentation updated**

### Next Week
1. **Rich onboarding experience deployed**
2. **Purple color issue resolved**
3. **User testing completed**

## Documentation

**Created files:**
- `ONBOARDING_REDESIGN.md` - Complete onboarding implementation
- `~/WebstormProjects/Kulus-App/SECURITY_FIX_REQUIRED.md` - Backend security fixes
- `ACTION_PLAN.md` - This file

**Next to create:**
- `TESTING_PLAN.md` - Comprehensive testing checklist
- `DEPLOYMENT_GUIDE.md` - Step-by-step deployment
- `USER_GUIDE.md` - End-user documentation

## Success Criteria

### Security
- ✅ No user can access another user's glucose data
- ✅ Token binds to specific user identity
- ✅ Server enforces authorization on all endpoints
- ✅ Security testing passes all test cases
- ✅ Logging captures all data access

### Onboarding
- ✅ No app content visible during onboarding
- ✅ Users understand privacy and data segregation
- ✅ Name setup has validation and confirmation
- ✅ Smooth animations and professional UX
- ✅ Matrix theme consistent throughout

### User Experience
- ✅ Clear understanding of how data is protected
- ✅ Easy setup process (< 2 minutes)
- ✅ Works seamlessly across multiple devices
- ✅ No purple color confusion
- ✅ Professional, polished appearance

## Contact

For questions or clarification:
- Security issues: IMMEDIATE attention required
- Onboarding design: Can iterate based on user feedback
- Testing: Coordinate with QA team

---

**PRIORITY:** Security fix must be completed before any other work.
**TIMELINE:** Security patch target: End of week
**STATUS:** 🔴 CRITICAL - Work in progress

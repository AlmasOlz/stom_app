# Firebase Setup

## 1) Android config

1. Open Firebase Console.
2. Select your project.
3. Add Android app with package `com.example.stomatology.app` (if not added).
4. Download `google-services.json`.
5. Put it into:
   - `app/google-services.json`

If you do not have the file yet, the first Gradle sync will **copy** `app/google-services.example.json` → `app/google-services.json` so the project can build. Replace that copy with the real download from Firebase when you have API keys and production settings.

Reference template:
- `app/google-services.example.json`

## 2) Firestore/Storage rules and indexes

Deploy from project root:

```bash
firebase deploy --only firestore:rules
firebase deploy --only firestore:indexes
firebase deploy --only storage
```

Files used:
- `firestore.rules`
- `firestore.indexes.json`
- `storage.rules`

## 3) Admin bootstrap

Role changes are allowed only for admin.
Create the first admin manually in Firestore:

1. Register a normal user in the app.
2. Open `users/{uid}` for this account in Firestore Console.
3. Set `role = "admin"`.

After that, admin can manage doctors/roles/clinics from the app.

## 4) Doctor registration flow

1. User registers in app and selects role `doctor`.
2. App creates `users/{uid}` with:
   - `role = "patient"`
   - `requestedRole = "doctor"`
   - `requestStatus = "pending"`
   - `specialty`
   - `clinicId`
3. App signs this user out and shows message "wait for admin approval".
4. Admin opens Admin Panel, selects this user, sets role `doctor`, and saves.
5. After save:
   - `role = "doctor"`
   - `requestStatus = "approved"`

## 5) Clinic map + AI API local setup

In `~/.gradle/gradle.properties`:

```properties
MAPS_API_KEY=your_google_maps_key
apiBaseUrl=http://10.0.2.2:8000/
```

- `10.0.2.2` is for Android Emulator.
- For real device, use `apiBaseUrl=http://<your-lan-ip>:8000/`.
- If AI analysis shows timeout, verify backend is running on port `8000`.

## 6) Required Firestore collections and fields

Top-level collections:

- `users`
- `clinics`
- `appointments`

### `users/{uid}` required fields

- `uid` (string)
- `email` (string)
- `role` (string: `patient` | `doctor` | `admin`)
- `requestedRole` (string)
- `requestStatus` (string: `none` | `pending` | `approved` | `rejected`)
- `firstName` (string)
- `lastName` (string)
- `displayName` (string)
- `phone` (string)
- `specialty` (string, for doctor request)
- `clinicId` (string, for doctor request)
- `createdAt` (number)
- `updatedAt` (number)

### `clinics/{clinicId}` required fields

- `name` (string)
- `address` (string)
- `services` (array of strings)
- `priceFrom` (number)
- `description` (string)
- `imageUrl` (string)
- `rating` (number)
- `reviews` (number)
- `latitude` (number)
- `longitude` (number)
- `createdAt` (number, optional on update)
- `updatedAt` (number)

Important:
- Doctor registration requires clinic choice in app UI.
- So yes, clinics should exist in DB (or be created by admin in Admin Panel).

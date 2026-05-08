# Stomatology Android App

## Firebase troubleshooting

If registration says the email already exists but the user is missing in Firestore, open Firebase Console -> Authentication, find that email, and delete the orphan Auth user. Then try registration again.

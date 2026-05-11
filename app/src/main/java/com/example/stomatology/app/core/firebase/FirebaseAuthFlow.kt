package com.example.stomatology.app.core.firebase

import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged

/** Firebase сессиясы қалпына келгенде де uid дұрыс жіберілуі үшін. */
fun FirebaseAuth.currentUserIdFlow(): Flow<String?> = callbackFlow {
    val listener = FirebaseAuth.AuthStateListener { auth ->
        trySend(auth.currentUser?.uid)
    }
    addAuthStateListener(listener)
    trySend(currentUser?.uid)
    awaitClose { removeAuthStateListener(listener) }
}.distinctUntilChanged()

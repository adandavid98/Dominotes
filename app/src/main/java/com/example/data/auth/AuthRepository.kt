package com.example.data.auth

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AuthRepository(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("domino_auth_prefs", Context.MODE_PRIVATE)

    private val _currentUser = MutableStateFlow<AuthUser?>(loadUser())
    val currentUser: StateFlow<AuthUser?> = _currentUser.asStateFlow()

    private fun loadUser(): AuthUser? {
        val uid = prefs.getString("user_uid", null) ?: return null
        val email = prefs.getString("user_email", "") ?: ""
        val name = prefs.getString("user_name", "Jugador") ?: "Jugador"
        val photo = prefs.getString("user_photo", null)
        return AuthUser(uid = uid, email = email, displayName = name, photoUrl = photo)
    }

    fun signIn(user: AuthUser) {
        prefs.edit()
            .putString("user_uid", user.uid)
            .putString("user_email", user.email)
            .putString("user_name", user.displayName)
            .putString("user_photo", user.photoUrl)
            .apply()
        _currentUser.value = user
    }

    fun signOut() {
        prefs.edit().clear().apply()
        _currentUser.value = null
    }

    fun updateDisplayName(newName: String) {
        val current = _currentUser.value ?: return
        val updated = current.copy(displayName = newName)
        signIn(updated)
    }
}

package dev.myrlennia237.amlethmp.core.session

import android.content.Context
import com.google.crypto.tink.Aead
import com.google.crypto.tink.KeyTemplates
import com.google.crypto.tink.RegistryConfiguration
import com.google.crypto.tink.aead.AeadConfig
import com.google.crypto.tink.integration.android.AndroidKeysetManager

/** Builds the AES-256-GCM primitive whose keyset is wrapped by a key in the Android Keystore. */
internal object SessionAead {
    const val KEYSET_PREFS_NAME = "session_keyset_prefs"

    private const val KEYSET_NAME = "session_keyset"
    private const val MASTER_KEY_URI = "android-keystore://session_master_key"

    fun create(context: Context): Aead {
        AeadConfig.register()
        return AndroidKeysetManager.Builder()
            .withSharedPref(context, KEYSET_NAME, KEYSET_PREFS_NAME)
            .withKeyTemplate(KeyTemplates.get("AES256_GCM"))
            .withMasterKeyUri(MASTER_KEY_URI)
            .build()
            .keysetHandle
            .getPrimitive(RegistryConfiguration.get(), Aead::class.java)
    }
}

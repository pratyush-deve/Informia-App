package com.pratyush.infoapp.di

import android.content.Context
import com.pratyush.infoapp.data.VaultRepository
import com.pratyush.infoapp.data.local.VaultDatabase

object VaultAppContainer {
    fun repository(context: Context): VaultRepository {
        return VaultRepository(
            dao = VaultDatabase.getInstance(context).vaultDao(),
            context = context.applicationContext
        )
    }
}

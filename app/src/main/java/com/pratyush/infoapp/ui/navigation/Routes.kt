package com.pratyush.infoapp.ui.navigation

object Routes {
    const val VAULT = "vault"
    const val CARD_EDIT = "card_edit"
    
    fun cardEdit(cardId: Long? = null): String {
        return if (cardId != null && cardId != 0L) {
            "$CARD_EDIT?cardId=$cardId"
        } else {
            CARD_EDIT
        }
    }
}

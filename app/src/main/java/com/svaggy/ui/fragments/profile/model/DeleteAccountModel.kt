package com.svaggy.ui.fragments.profile.model

data class DeleteAccountModel(
    val `data`: List<Data?>?,
    val is_success: Boolean?,
    val message: String?,
    val status_code: Int?
)
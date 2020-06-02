package com.example.stayconnected.sample

data class Contact(
    val id: Long = counter++,
    val name: String,
    val city: String,
    val url: String
) {
    companion object {
        private var counter = 0L
    }
}

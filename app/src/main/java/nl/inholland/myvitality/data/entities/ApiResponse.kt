package nl.inholland.myvitality.data.entities

data class ApiResponse(
    val status: ResponseStatus,
    val message: String? = null,
)
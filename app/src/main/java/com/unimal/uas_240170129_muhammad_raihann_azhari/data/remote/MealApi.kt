package com.unimal.uas_240170129_muhammad_raihann_azhari.data.remote

import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

/** Ide resep acak dari API publik TheMealDB (tanpa API key). */
data class Inspiration(
    val name: String,
    val category: String,
    val area: String,
    val ingredients: List<String>,
    val instructions: String,
    val thumbUrl: String?
)

class ApiException(message: String) : Exception(message)

object MealApi {

    private const val RANDOM_URL = "https://www.themealdb.com/api/json/v1/1/random.php"
    private const val TIMEOUT_MS = 10_000

    /**
     * Blocking: panggil dari thread background.
     * @throws IOException bila jaringan bermasalah (tidak ada koneksi / timeout)
     * @throws ApiException bila respons server tidak valid
     */
    @Throws(IOException::class, ApiException::class)
    fun fetchRandom(): Inspiration {
        val connection = URL(RANDOM_URL).openConnection() as HttpURLConnection
        try {
            connection.connectTimeout = TIMEOUT_MS
            connection.readTimeout = TIMEOUT_MS
            connection.requestMethod = "GET"
            if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                throw ApiException("HTTP ${connection.responseCode}")
            }
            val body = connection.inputStream.bufferedReader().use { it.readText() }
            return parse(body)
        } finally {
            connection.disconnect()
        }
    }

    private fun parse(body: String): Inspiration {
        try {
            val meal = JSONObject(body).getJSONArray("meals").getJSONObject(0)
            val ingredients = mutableListOf<String>()
            for (i in 1..20) {
                val ingredient = meal.optString("strIngredient$i", "").trim()
                if (ingredient.isEmpty() || ingredient == "null") continue
                val measure = meal.optString("strMeasure$i", "").trim().let { if (it == "null") "" else it }
                ingredients.add(if (measure.isEmpty()) ingredient else "$measure $ingredient")
            }
            return Inspiration(
                name = meal.getString("strMeal"),
                category = meal.optString("strCategory", ""),
                area = meal.optString("strArea", ""),
                ingredients = ingredients,
                instructions = meal.optString("strInstructions", "").trim(),
                thumbUrl = meal.optString("strMealThumb", "").takeIf { it.isNotBlank() && it != "null" }
            )
        } catch (e: JSONException) {
            throw ApiException(e.message ?: "parse error")
        }
    }
}

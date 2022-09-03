package com.nonnonstop.apimate

import android.content.Context
import android.net.Uri
import androidx.preference.PreferenceManager
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import timber.log.Timber

class UpdateChecker(appContext: Context, workerParams: WorkerParameters) :
    Worker(appContext, workerParams) {
    companion object {
        private const val API_URL =
            "https://api.github.com/repos/nonnonstop/apimater/releases/latest"
        private val client = OkHttpClient()
        private val versionRegex = """\d+(?:\.\d+)*""".toRegex()

        fun enqueue(context: Context): Boolean {
            val pref = PreferenceManager.getDefaultSharedPreferences(context)
            if (!pref.getBoolean("check_upgrade_startup", true))
                return false
            forceEnqueue(context)
            return true
        }

        fun forceEnqueue(context: Context) {
            WorkManager
                .getInstance(context)
                .enqueue(OneTimeWorkRequestBuilder<UpdateChecker>().build())
        }

        private fun parseVersionString(version: String): List<Int> {
            val matchResult = versionRegex.find(version)
                ?: throw IllegalArgumentException("Version: $version")
            return matchResult.value.split(".").map { it.toInt() }.toList()
        }

        private fun compareVersion(left: String, right: String): Int {
            val leftList = parseVersionString(left)
            val rightList = parseVersionString(right)
            leftList.zip(rightList).forEach { (x, y) ->
                if (x != y) {
                    return if (x > y) 1 else -1
                }
            }
            val leftSize = leftList.size
            val rightSize = rightList.size
            if (leftSize != rightSize) {
                return if (leftSize > rightSize) 1 else -1
            }
            return 0
        }
    }

    override fun doWork(): Result {
        return try {
            notifyUpgrade()
            Result.success()
        } catch (ex: Exception) {
            Timber.e(ex, "Failed to retrieve latest release")
            Result.failure()
        }
    }

    private fun notifyUpgrade() {
        val request = Request.Builder()
            .url(API_URL)
            .build()
        client.newCall(request).execute().use { response ->
            if (response.code != 200) {
                throw Exception("Failed to connect (${response.code})")
            }
            val body = response.body?.source()?.readUtf8()
            body ?: throw Exception("Body not found")
            val remoteVersion = JSONObject(body).getString("tag_name")
            if (compareVersion(remoteVersion, BuildConfig.VERSION_NAME) <= 0) {
                Timber.d("No update found")
                return
            }
            Timber.d("Update found")
            val htmlUrl = JSONObject(body).getString("html_url")
            Notifications.notifyUpgrade(applicationContext, remoteVersion, Uri.parse(htmlUrl))
            return
        }
    }
}
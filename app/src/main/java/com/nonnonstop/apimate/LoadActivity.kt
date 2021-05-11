package com.nonnonstop.apimate

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity

class LoadActivity : AppCompatActivity() {
    private val viewModel: LoadViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.load_activity)
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        dispatchIntent(intent)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        dispatchIntent(intent)
    }

    private fun dispatchIntent(intent: Intent?) {
        val htmlUrl = intent?.data?.normalizeScheme()?.run {
            if (scheme == "apimater")
                encodedSchemeSpecificPart
            else
                toString()
        } ?: return
        viewModel.htmlUrl.value = htmlUrl
    }
}
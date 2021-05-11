package com.nonnonstop.apimate

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity

class LoadActivity : AppCompatActivity() {
    private val loadViewModel: LoadViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.load_activity)
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        dispatchIndent(intent)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        dispatchIndent(intent)
    }

    private fun dispatchIndent(intent: Intent?) {
        val htmlUrl = intent?.data?.normalizeScheme()?.run {
            if (scheme == "apimater")
                encodedSchemeSpecificPart
            else
                toString()
        } ?: return
        loadViewModel.htmlUrl.value = htmlUrl
    }
}
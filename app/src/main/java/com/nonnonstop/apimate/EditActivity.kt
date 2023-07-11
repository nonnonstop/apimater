package com.nonnonstop.apimate

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity

class EditActivity : AppCompatActivity() {
    private val viewModel: EditViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.edit_activity)
        viewModel.fileName.value = intent.getStringExtra(Intent.EXTRA_TEXT)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.edit_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.save_script -> {
                viewModel.save()
                true
            }

            R.id.revert_script -> {
                viewModel.revert()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }
}
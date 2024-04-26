package com.example.mytravelers

import android.app.Dialog
import android.content.Context
import android.widget.ProgressBar
import android.widget.TextView

object LoaderGlobal {

    fun showProgressDialog(context: Context, message: String): Dialog {
        val dialog = Dialog(context)
        dialog.setContentView(R.layout.custom_loader_item)
        val progressBar = dialog.findViewById<ProgressBar>(R.id.progressBar)
        val progressText = dialog.findViewById<TextView>(R.id.textView)
        progressText.text = message
        dialog.setCancelable(false)
        dialog.show()
        return dialog
    }
}
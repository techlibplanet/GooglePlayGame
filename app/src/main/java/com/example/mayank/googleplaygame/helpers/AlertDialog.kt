package com.example.mayank.googleplaygame.helpers

import android.content.Context
import android.content.DialogInterface
import android.support.v7.app.AlertDialog

object AlertDialog  {

    fun alertDialog (context: Context, title : String, message: String) : AlertDialog? {
        return AlertDialog.Builder(context).setTitle(title).setMessage("\n$message").setPositiveButton("Ok", DialogInterface.OnClickListener {dialogInterface, i ->
            dialogInterface.dismiss()
        }).show()
    }
}
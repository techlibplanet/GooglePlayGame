package com.example.mayank.googleplaygame.multiplay.resultadapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.example.mayank.googleplaygame.R
import com.google.android.gms.common.images.ImageManager



class ResultViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    fun bindView(context : Context,resultViewModel: ResultViewModel, position: Int){
        val textViewPlayerName = itemView.findViewById<TextView>(R.id.text_view_player_name)
        val textViewRightAnswers = itemView.findViewById<TextView>(R.id.text_view_right_answers)
//        val textViewWrongAnswers = itemView.findViewById<TextView>(R.id.text_view_wrong_answers)
//        val textViewDropQuestions = itemView.findViewById<TextView>(R.id.text_view_drop_questions)
        val playerDisplayImage = itemView.findViewById<ImageView>(R.id.player_display_image)
        textViewPlayerName.text = resultViewModel.playerName
        textViewRightAnswers.text = resultViewModel.rightAnswers
        val mgr = ImageManager.create(context)

        if (position!=0){
//            playerDisplayImage.setImageURI(resultViewModel.imageUri)
            mgr.loadImage(playerDisplayImage, resultViewModel.imageUri)
        }


//        textViewWrongAnswers.text = resultViewModel.wrongAnswers
//        textViewDropQuestions.text = resultViewModel.dropQuestions
    }
}
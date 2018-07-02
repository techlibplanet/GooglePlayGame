package com.example.mayank.googleplaygame.multiplay.resultadapter

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.TextView
import com.example.mayank.googleplaygame.R

class ResultViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    fun bindView(resultViewModel: ResultViewModel, position: Int){
        val textViewPlayerName = itemView.findViewById<TextView>(R.id.text_view_player_name)
        val textViewRightAnswers = itemView.findViewById<TextView>(R.id.text_view_right_answers)
//        val textViewWrongAnswers = itemView.findViewById<TextView>(R.id.text_view_wrong_answers)
//        val textViewDropQuestions = itemView.findViewById<TextView>(R.id.text_view_drop_questions)

        textViewPlayerName.text = resultViewModel.playerName
        textViewRightAnswers.text = resultViewModel.rightAnswers

//        textViewWrongAnswers.text = resultViewModel.wrongAnswers
//        textViewDropQuestions.text = resultViewModel.dropQuestions
    }
}
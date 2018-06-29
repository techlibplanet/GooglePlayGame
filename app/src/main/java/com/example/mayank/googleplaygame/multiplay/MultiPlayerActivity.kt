package com.example.mayank.googleplaygame.multiplay

import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.Fragment
import com.example.mayank.googleplaygame.R

class MultiPlayerActivity : AppCompatActivity(), GameDetailFragment.OnFragmentInteractionListener, GameMenuFragment.OnFragmentInteractionListener, QuizFragment.OnFragmentInteractionListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_multi_player)

        val gameMenu = GameMenuFragment()
        switchToFragment(gameMenu)
//        val gameDetailFragment = GameDetailFragment()
//        switchToFragment(gameDetailFragment)
    }

    // Switch UI to the given fragment
    private fun switchToFragment(newFrag: Fragment) {
        supportFragmentManager.beginTransaction().replace(R.id.fragment_container, newFrag)
                .commit()
    }

    override fun onFragmentInteraction(uri: Uri) {

    }

}

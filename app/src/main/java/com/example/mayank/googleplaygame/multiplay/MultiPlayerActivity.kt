package com.example.mayank.googleplaygame.multiplay

import android.content.Intent
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.Fragment
import com.example.mayank.googleplaygame.Constants.logD
import com.example.mayank.googleplaygame.PlayGameLib
import com.example.mayank.googleplaygame.R

class MultiPlayerActivity : AppCompatActivity(),
        GameDetailFragment.OnFragmentInteractionListener,
        GameMenuFragment.OnFragmentInteractionListener,
        QuizFragment.OnFragmentInteractionListener,
        MultiplayerResultFragment.OnFragmentInteractionListener{

    private val TAG = MultiPlayerActivity::class.java.simpleName
    private var playGameLib : PlayGameLib? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_multi_player)
        playGameLib = PlayGameLib(this)

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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        logD(TAG, "Request code : $requestCode")
        logD(TAG, "Result Code : $resultCode")
        playGameLib?.onActivityResult(requestCode, resultCode, data)
        super.onActivityResult(requestCode, resultCode, data)
    }
    override fun onFragmentInteraction(uri: Uri) {

    }

}

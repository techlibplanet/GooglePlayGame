package com.example.mayank.googleplaygame.multiplay

import android.content.Intent
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.Fragment
import android.widget.ProgressBar
import com.example.mayank.googleplaygame.Constants.logD
import com.example.mayank.googleplaygame.PlayGameLib
import com.example.mayank.googleplaygame.R
import com.google.android.gms.games.Games
import com.google.android.gms.games.InvitationsClient
import com.google.android.gms.games.multiplayer.InvitationCallback

class MultiPlayerActivity : AppCompatActivity(),
        GameDetailFragment.OnFragmentInteractionListener,
        GameMenuFragment.OnFragmentInteractionListener,
        QuizFragment.OnFragmentInteractionListener,
        MultiplayerResultFragment.OnFragmentInteractionListener{

    private val TAG = MultiPlayerActivity::class.java.simpleName
    private var playGameLib : PlayGameLib? = null
    private var invitationClient : InvitationsClient? = null
    lateinit var progressBar : ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_multi_player)

        playGameLib = PlayGameLib(this)

        progressBar = findViewById(R.id.progressBar)

        PlayGameLib.GameConstants.mInvitationClient?.registerInvitationCallback(playGameLib?.mInvitationCallbackHandler!!)
        invitationClient = Games.getInvitationsClient(this@MultiPlayerActivity, playGameLib?.getSignInAccount()!!)
        val gameMenu = GameMenuFragment()
        switchToFragment(gameMenu)
//        val gameDetailFragment = GameDetailFragment()
//        switchToFragment(gameDetailFragment)
    }

    override fun onPause() {
        super.onPause()
        invitationClient?.unregisterInvitationCallback(playGameLib?.mInvitationCallbackHandler!!)
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

package com.example.mayank.googleplaygame.multiplay

import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.Fragment
import android.widget.ProgressBar
import com.example.mayank.googleplaygame.Constants.logD
import com.example.mayank.googleplaygame.PlayGameLib
import com.example.mayank.googleplaygame.R
import com.example.mayank.googleplaygame.wallet.AddPointsFragment
import com.google.android.gms.games.Games
import com.google.android.gms.games.InvitationsClient
import com.google.android.gms.games.multiplayer.InvitationCallback
import android.support.annotation.NonNull
import android.support.v7.app.AlertDialog
import android.view.WindowManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.games.multiplayer.realtime.RoomConfig
import com.google.android.gms.games.multiplayer.Invitation



class MultiPlayerActivity : AppCompatActivity(),
        GameDetailFragment.OnFragmentInteractionListener,
        GameMenuFragment.OnFragmentInteractionListener,
        QuizFragment.OnFragmentInteractionListener,
        MultiplayerResultFragment.OnFragmentInteractionListener,
        AddPointsFragment.OnFragmentInteractionListener{

    private val TAG = MultiPlayerActivity::class.java.simpleName
    private var playGameLib : PlayGameLib? = null
    private var invitationClient : InvitationsClient? = null
    lateinit var progressBar : ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_multi_player)

        playGameLib = PlayGameLib(this)

        progressBar = findViewById(R.id.progressBar)

//        PlayGameLib.GameConstants.mInvitationClient?.registerInvitationCallback(playGameLib?.mInvitationCallbackHandler!!)
        invitationClient = Games.getInvitationsClient(this@MultiPlayerActivity, playGameLib?.getSignInAccount()!!)
//        invitationClient = playGameLib?.getInvitationClient()
//        invitationClient?.registerInvitationCallback(mInvitationCallbackHandler)
        PlayGameLib.GameConstants.mInvitationClient?.registerInvitationCallback(playGameLib?.mInvitationCallbackHandler!!)
        val gameMenu = GameMenuFragment()
        switchToFragment(gameMenu)
//        val gameDetailFragment = GameDetailFragment()
//        switchToFragment(gameDetailFragment)
    }

    override fun onPause() {
        super.onPause()
//        invitationClient?.unregisterInvitationCallback(mInvitationCallbackHandler)
        PlayGameLib.GameConstants.mInvitationClient?.unregisterInvitationCallback(playGameLib?.mInvitationCallbackHandler!!)
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

//    private val mInvitationCallbackHandler = object : InvitationCallback() {
//        override fun onInvitationRemoved(invitationId: String) {
//            logD(TAG, "Invitation removed - $invitationId")
//        }
//
//        override fun onInvitationReceived(invitation: Invitation) {
//            logD(TAG, "On invitation received called...")
//            val builder = RoomConfig.builder(playGameLib?.mRoomUpdateCallback).setInvitationIdToAccept(invitation.invitationId)
//            PlayGameLib.GameConstants.mRoomConfig = builder.build()
//            Games.getRealTimeMultiplayerClient(this@MultiPlayerActivity, GoogleSignIn.getLastSignedInAccount(applicationContext)!!)
//                    .join(PlayGameLib.GameConstants.mRoomConfig!!)
//            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
//        }
//
//    }

    private val mInvitationCallbackHandler = object : InvitationCallback() {
        override fun onInvitationRemoved(invitationId: String) {
            logD(TAG, "Invitation removed - $invitationId")
        }

        override fun onInvitationReceived(invitation: Invitation) {
            logD(TAG, "On invitation received called...")
            val builder = RoomConfig.builder(playGameLib?.mRoomUpdateCallback).setInvitationIdToAccept(invitation.invitationId)
            PlayGameLib.GameConstants.mRoomConfig = builder.build()
            Games.getRealTimeMultiplayerClient(this@MultiPlayerActivity, GoogleSignIn.getLastSignedInAccount(applicationContext)!!)
                    .join(PlayGameLib.GameConstants.mRoomConfig!!)
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)


        }

    }



}

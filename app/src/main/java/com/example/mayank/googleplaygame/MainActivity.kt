package com.example.mayank.googleplaygame

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.util.Log
import android.view.View
import android.widget.Button
import com.example.mayank.googleplaygame.Constants.DISPLAY_NAME
import com.example.mayank.googleplaygame.Constants.PLAYER_ID
import com.example.mayank.googleplaygame.Constants.logD
import com.example.mayank.googleplaygame.Constants.logE
import com.example.mayank.googleplaygame.dashboard.DashboardActivity
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.games.Games
import com.google.android.gms.games.InvitationsClient
import com.google.android.gms.games.RealTimeMultiplayerClient
import android.view.Gravity
import com.example.mayank.googleplaygame.Constants.FIRST_NAME
import com.example.mayank.googleplaygame.Constants.LAST_NAME
import com.example.mayank.googleplaygame.quickplay.QuickPlayActivity


class MainActivity : AppCompatActivity(), View.OnClickListener {

    private val TAG = MainActivity::class.java.simpleName
    private val RC_SIGN_IN = 100

    internal var mSignedInAccount: GoogleSignInAccount? = null
    // Client used to interact with the real time multiplayer system.
    private var mRealTimeMultiplayerClient: RealTimeMultiplayerClient? = null
    //Client used to interact with the Invitation system.
    private var mInvitationsClient: InvitationsClient? = null
    private var mPlayerId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        findViewById<SignInButton>(R.id.sign_in_button).setOnClickListener(this)
        findViewById<Button>(R.id.sign_out_button).setOnClickListener(this)

    }


    private fun isSignedIn(): Boolean {
        return GoogleSignIn.getLastSignedInAccount(this) != null
    }

    private fun signInSilently() {
        val signInClient = GoogleSignIn.getClient(this,
                GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN)
        signInClient.silentSignIn().addOnCompleteListener(this
        ) { task ->
            if (task.isSuccessful) {
                // The signed in account is stored in the task's result.
                val signedInAccount = task.result
                logD(TAG, "Task Successful")
                onConnected(signedInAccount)
            } else {
                // Player will need to sign-in explicitly using via UI
                logD(TAG, "Unable to sign in silently")
            }
        }
    }

    override fun onResume() {
        super.onResume()
        signInSilently()
    }

    private fun startSignInIntent() {
        val signInClient = GoogleSignIn.getClient(this,
                GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN)

        val intent = signInClient.signInIntent
        startActivityForResult(intent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val result = Auth.GoogleSignInApi.getSignInResultFromIntent(data)
            if (result.isSuccess) {
                // The signed in account is stored in the result.
                val signedInAccount = result.signInAccount
                logD(TAG, "Result Successful")
                onConnected(signedInAccount!!)

            } else {
                var message = result.status.statusMessage
                if (message == null || message.isEmpty()) {
                    message = getString(R.string.signin_other_error)
                }
                logE(TAG, "Error : ${result.status}")
                AlertDialog.Builder(this).setMessage(message)
                        .setNeutralButton(android.R.string.ok, null).show()
            }
        }
    }


    private fun onConnected(signedInAccount: GoogleSignInAccount) {
        if (mSignedInAccount != signedInAccount) {

            mSignedInAccount = signedInAccount
            val gameClient = Games.getGamesClient(this, GoogleSignIn.getLastSignedInAccount(this)!!)
            gameClient.setViewForPopups(findViewById(android.R.id.content))
//            Games.getGamesClient(this, signedInAccount).setViewForPopups()
            // update the clients
            mRealTimeMultiplayerClient = Games.getRealTimeMultiplayerClient(this, signedInAccount)
            mInvitationsClient = Games.getInvitationsClient(this@MainActivity, signedInAccount)

            // get the playerId from the PlayersClient
            val playersClient = Games.getPlayersClient(this, signedInAccount)
            playersClient.currentPlayer.addOnSuccessListener { player ->
                mPlayerId = player.playerId
                Log.d("Player ID", mPlayerId)
                Log.d("Display Name", player.displayName)
                PlayGameApplication.sharedPrefs?.setStringPreference(this, PLAYER_ID, mPlayerId!!)
                PlayGameApplication.sharedPrefs?.setStringPreference(this, DISPLAY_NAME, player.displayName)
                val intent = Intent(this@MainActivity, DashboardActivity::class.java)
                val nameArray = player.name.split(" ")
                PlayGameApplication.sharedPrefs?.setStringPreference(this, FIRST_NAME, nameArray[0])
                PlayGameApplication.sharedPrefs?.setStringPreference(this, LAST_NAME, nameArray[1])
                //logD(TAG, "First Name : ${nameArray[0]} Last Name = ${nameArray[1]}")
                intent.putExtra("DisplayName", player.displayName)
                intent.putExtra("PlayerName", player.name)
                startActivity(intent)
                finish()

            }

        }

    }


    override fun onClick(v: View?) {
        if (v?.id == R.id.sign_in_button) {
            startSignInIntent();
        }

        if (v?.id == R.id.sign_out_button) {
            signOut()
        }
    }

    private fun signOut() {
        if (isSignedIn()) {
            val signInClient = GoogleSignIn.getClient(this,
                    GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN)
            signInClient.signOut().addOnCompleteListener(this
            ) {
                // at this point, the user is signed out.
                logD(TAG, "Sign Out Successfully !")
                val delete = PlayGameApplication.sharedPrefs?.deletePreferences(this)
                if (delete!!) {
                    logD(TAG, "Shared Preferences deleted successfully")
                } else {
                    logD(TAG, "Unable to delete preferences.")
                }
            }
        } else {
            logD(TAG, "Already sign out !")
        }

    }

}

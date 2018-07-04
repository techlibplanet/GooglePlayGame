package com.example.mayank.googleplaygame.dashboard

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import com.example.mayank.googleplaygame.Constants
import com.example.mayank.googleplaygame.MainActivity
import com.example.mayank.googleplaygame.PlayGameApplication
import com.example.mayank.googleplaygame.R
import com.example.mayank.googleplaygame.multiplay.MultiPlayerActivity
import com.example.mayank.googleplaygame.play.PlayActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.games.Games

class DashboardActivity : AppCompatActivity(), View.OnClickListener {

    private val TAG = DashboardActivity::class.java.simpleName
    private val RC_ACHIEVEMENT_UI = 9003
    private val RC_LEADERBOARD_UI = 9004

    private val CLICKABLES = intArrayOf(R.id.sign_out_button, R.id.achievements_button, R.id.leaderboards_button, R.id.play_button, R.id.wallet_button)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        for (id in CLICKABLES){
            findViewById<Button>(id).setOnClickListener(this)
        }

    }

    override fun onClick(v: View) {
        when(v.id){
            R.id.sign_out_button ->{
                signOut()
            }

            R.id.achievements_button ->{
                showAchievements()
            }

            R.id.leaderboards_button ->{
                showLeaderboards()
            }


//            R.id.wallet_button ->{
////                val intent = Intent(this, WalletActivity::class.java)
////                startActivity(intent)
//            }

            R.id.play_button ->{
                val intent = Intent(this, MultiPlayerActivity::class.java)
                startActivity(intent)
//                finish()
            }

            R.id.wallet_button ->{

            }

        }

    }

    private fun showLeaderboards() {
        Games.getLeaderboardsClient(this, GoogleSignIn.getLastSignedInAccount(this)!!)
                .getLeaderboardIntent(getString(R.string.leaderboard_global_rank))
                .addOnSuccessListener { intent -> startActivityForResult(intent, RC_LEADERBOARD_UI) }
    }

    private fun showAchievements() {
        Games.getAchievementsClient(this, GoogleSignIn.getLastSignedInAccount(this)!!)
                .achievementsIntent
                .addOnSuccessListener { intent -> startActivityForResult(intent, RC_ACHIEVEMENT_UI) }
    }

    private fun isSignedIn(): Boolean {
        return GoogleSignIn.getLastSignedInAccount(this) != null
    }

    private fun signOut() {
        if (isSignedIn()) {
            val signInClient = GoogleSignIn.getClient(this,
                    GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN)
            signInClient.signOut().addOnCompleteListener(this
            ) {
                // at this point, the user is signed out.
                Constants.logD(TAG, "Sign Out Successfully !")
                val delete = PlayGameApplication.sharedPrefs?.deletePreferences(this)
                if (delete!!) {
                    Constants.logD(TAG, "Shared Preferences deleted successfully")
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    Constants.logD(TAG, "Unable to delete preferences.")
                }
            }
        } else {
            Constants.logD(TAG, "Already sign out !")
        }
    }
}

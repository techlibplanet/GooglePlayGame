package com.example.mayank.googleplaygame.play

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import com.example.mayank.googleplaygame.Constants.logD
import com.example.mayank.googleplaygame.R
import com.example.mayank.googleplaygame.quickplay.QuickPlayActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.games.*
import com.google.android.gms.games.multiplayer.Invitation
import com.google.android.gms.games.multiplayer.InvitationCallback
import com.google.android.gms.games.multiplayer.Multiplayer
import com.google.android.gms.games.multiplayer.Participant
import com.google.android.gms.games.multiplayer.realtime.*
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import java.util.ArrayList
import java.util.HashMap
import java.util.HashSet

class PlayActivity : AppCompatActivity(), View.OnClickListener {

    private val TAG = PlayActivity::class.java.simpleName
    // Client used to sign in with Google APIs
    private var mGoogleSignInClient: GoogleSignInClient? = null
    // Request codes for the UIs that we show with startActivityForResult:
    private val RC_SELECT_PLAYERS = 10000
    private val RC_INVITATION_INBOX = 10001
    private val RC_WAITING_ROOM = 10002

    // Request code used to invoke sign in user interactions.
    private val RC_SIGN_IN = 9001
    // Client used to interact with the real time multiplayer system.
    private var mRealTimeMultiplayerClient: RealTimeMultiplayerClient? = null

    // Client used to interact with the Invitation system.
    private var mInvitationsClient: InvitationsClient? = null

    // Room ID where the currently active game is taking place; null if we're
    // not playing.
    internal var mRoomId: String? = null

    // Holds the configuration of the current room.
    internal var mRoomConfig: RoomConfig? = null

    // Are we playing in multiplayer mode?
    internal var mMultiplayer = false

    // The participants in the currently active game
    internal var mParticipants: ArrayList<Participant>? = null

    // My participant ID in the currently active game
    internal var mMyId: String? = null

    // If non-null, this is the id of the invitation we received via the
    // invitation listener
    internal var mIncomingInvitationId: String? = null

    // Message buffer for sending messages
    internal var mMsgBuf = ByteArray(2)

    private var googleSignInAccount : GoogleSignInAccount ? = null

    private val CLICKABLES = intArrayOf(R.id.single_player_button, R.id.quick_play__button, R.id.multi_player_button, R.id.broadcast_score_button)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_play)


        // Create the client used to sign in.
        googleSignInAccount = GoogleSignIn.getLastSignedInAccount(this)
        mGoogleSignInClient = GoogleSignIn.getClient(this, GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN)
        mRealTimeMultiplayerClient = Games.getRealTimeMultiplayerClient(this, googleSignInAccount!!)
        mInvitationsClient?.registerInvitationCallback(mInvitationCallback)
        // get the playerId from the PlayersClient
        val playersClient = Games.getPlayersClient(this, googleSignInAccount!!)
        playersClient.currentPlayer.addOnSuccessListener { player -> mPlayerId = player.playerId }

        for (id in CLICKABLES) {
            findViewById<Button>(id).setOnClickListener(this)
        }
    }

    override fun onResume() {
        super.onResume()

    }

    override fun onPause() {
        super.onPause()
        if (mInvitationsClient != null) {
            mInvitationsClient?.unregisterInvitationCallback(mInvitationCallback)
        }
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.single_player_button -> {
//                val intent = Intent(this, SinglePlayActivity::class.java)
//                startActivity(intent)
//                finish()
//                acceptInviteToRoom(mIncomingInvitationId!!)
//                mIncomingInvitationId = null
                showInvitationInbox()
            }
            R.id.quick_play__button -> {
//                val intent = Intent(this, QuickPlayActivity::class.java)
//                startActivity(intent)
//                finish()
                startQuickGame()
            }

            R.id.multi_player_button -> {
//                val intent = Intent(this, MultiPlayerActivity::class.java)
//                startActivity(intent)
//                finish()
                invitePlayers()
            }

            R.id.broadcast_score_button ->{
                broadcastScore(true)
            }
        }

    }

    private fun showInvitationInbox() {
        Games.getInvitationsClient(this, GoogleSignIn.getLastSignedInAccount(this)!!)
                .invitationInboxIntent
                .addOnSuccessListener { intent -> startActivityForResult(intent, RC_INVITATION_INBOX) }
    }

    // Clears the flag that keeps the screen on.
    private fun stopKeepingScreenOn() {
        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    private fun invitePlayers(){
        mRealTimeMultiplayerClient?.getSelectOpponentsIntent(1,7)?.addOnSuccessListener { intent ->
            startActivityForResult(intent, RC_SELECT_PLAYERS)

        }?.addOnFailureListener(createFailureListener("There was a problem selecting opponents."))
    }


    private fun startQuickGame() {
        // quick-start a game with 1 randomly selected opponent
        val MIN_OPPONENTS = 1
        val MAX_OPPONENTS = 1
        val autoMatchCriteria = RoomConfig.createAutoMatchCriteria(MIN_OPPONENTS,
                MAX_OPPONENTS, 0)
//        switchToScreen(R.id.screen_wait)
        keepScreenOn()
//        resetGameVars()

        mRoomConfig = RoomConfig.builder(mRoomUpdateCallback)
                .setOnMessageReceivedListener(mOnRealTimeMessageReceivedListener)
                .setRoomStatusUpdateCallback(mRoomStatusUpdateCallback)
                .setAutoMatchCriteria(autoMatchCriteria)
                .build()
        mRealTimeMultiplayerClient?.create(mRoomConfig!!)
    }

    private val mInvitationCallback = object : InvitationCallback() {
        // Called when we get an invitation to play a game. We react by showing that to the user.
        override fun onInvitationReceived(invitation: Invitation) {
            // We got an invitation to play a game! So, store it in
            // mIncomingInvitationId
            // and show the popup on the screen.
            mIncomingInvitationId = invitation.invitationId
            logD(TAG, "Inviter Name = ${invitation.inviter.displayName} ${getString(R.string.is_inviting_you)}")
//            (findViewById(R.id.incoming_invitation_text) as TextView).text = invitation.inviter.displayName + " " +
//                    getString(R.string.is_inviting_you)
//            switchToScreen(mCurScreen) // This will show the invitation popup
        }

        override fun onInvitationRemoved(invitationId: String) {

            if (mIncomingInvitationId == invitationId && mIncomingInvitationId != null) {
                mIncomingInvitationId = null
//                switchToScreen(mCurScreen) // This will hide the invitation popup
                logD(TAG, "Invitation removed")
            }
        }
    }

    private fun keepScreenOn() {
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }


    private val mRoomUpdateCallback = object : RoomUpdateCallback() {

        // Called when room has been created
        override fun onRoomCreated(statusCode: Int, room: Room?) {
            Log.d(TAG, "onRoomCreated($statusCode, $room)")
            if (statusCode != GamesCallbackStatusCodes.OK) {
                Log.e(TAG, "*** Error: onRoomCreated, status $statusCode")
//                showGameError()
                return
            }

            // save room ID so we can leave cleanly before the game starts.
            mRoomId = room!!.roomId

            // show the waiting room UI
            showWaitingRoom(room)
        }

        // Called when room is fully connected.
        override fun onRoomConnected(statusCode: Int, room: Room?) {
            Log.d(TAG, "onRoomConnected($statusCode, $room)")
            if (statusCode != GamesCallbackStatusCodes.OK) {
                Log.e(TAG, "*** Error: onRoomConnected, status $statusCode")
                showGameError()
                return
            }
            updateRoom(room)
        }

        override fun onJoinedRoom(statusCode: Int, room: Room?) {
            Log.d(TAG, "onJoinedRoom($statusCode, $room)")
            if (statusCode != GamesCallbackStatusCodes.OK) {
                Log.e(TAG, "*** Error: onRoomConnected, status $statusCode")
                showGameError()
                return
            }

            // show the waiting room UI
            showWaitingRoom(room!!)
        }

        // Called when we've successfully left the room (this happens a result of voluntarily leaving
        // via a call to leaveRoom(). If we get disconnected, we get onDisconnectedFromRoom()).
        override fun onLeftRoom(statusCode: Int, roomId: String) {
            // we have left the room; return to main screen.
            Log.d(TAG, "onLeftRoom, code $statusCode")
//            switchToMainScreen()
        }
    }

    private fun updateRoom(room: Room?) {
        if (room != null) {
            mParticipants = room.participants
        }
        if (mParticipants != null) {
//            updatePeerScoresDisplay()
        }
    }

    private fun showGameError() {
        AlertDialog.Builder(this)
                .setMessage(getString(R.string.game_problem))
                .setNeutralButton(android.R.string.ok, null).create()

//        switchToMainScreen()
    }

    private fun showWaitingRoom(room: Room) {
        // minimum number of players required for our game
        // For simplicity, we require everyone to join the game before we start it
        // (this is signaled by Integer.MAX_VALUE).
        val MIN_PLAYERS = Integer.MAX_VALUE
        mRealTimeMultiplayerClient?.getWaitingRoomIntent(room, MIN_PLAYERS)
                ?.addOnSuccessListener { intent ->
                    // show waiting room UI
                    startActivityForResult(intent, RC_WAITING_ROOM)
                }
                ?.addOnFailureListener(createFailureListener("There was a problem getting the waiting room!"))
    }

    private fun createFailureListener(string: String): OnFailureListener {
        return OnFailureListener { e -> handleException(e, string) }
    }

    private fun handleException(exception: Exception?, details: String) {
        var status = 0

        if (exception is ApiException) {
            val apiException = exception as ApiException?
            status = apiException!!.statusCode
        }

        var errorString: String? = null
        when (status) {
            GamesCallbackStatusCodes.OK -> {
            }
            GamesClientStatusCodes.MULTIPLAYER_ERROR_NOT_TRUSTED_TESTER -> errorString = getString(R.string.status_multiplayer_error_not_trusted_tester)
            GamesClientStatusCodes.MATCH_ERROR_ALREADY_REMATCHED -> errorString = getString(R.string.match_error_already_rematched)
            GamesClientStatusCodes.NETWORK_ERROR_OPERATION_FAILED -> errorString = getString(R.string.network_error_operation_failed)
            GamesClientStatusCodes.INTERNAL_ERROR -> errorString = getString(R.string.internal_error)
            GamesClientStatusCodes.MATCH_ERROR_INACTIVE_MATCH -> errorString = getString(R.string.match_error_inactive_match)
            GamesClientStatusCodes.MATCH_ERROR_LOCALLY_MODIFIED -> errorString = getString(R.string.match_error_locally_modified)
            else -> errorString = getString(R.string.unexpected_status, GamesClientStatusCodes.getStatusCodeString(status))
        }

        if (errorString == null) {
            return
        }

        val message = getString(R.string.status_exception_error, details, status, exception)

        AlertDialog.Builder(this@PlayActivity)
                .setTitle("Error")
                .setMessage(message + "\n" + errorString)
                .setNeutralButton(android.R.string.ok, null)
                .show()
    }


    // Score of other participants. We update this as we receive their scores
    // from the network.
    internal var mParticipantScore: MutableMap<String, Int> = HashMap()

    // Participants who sent us their final score.
    internal var mFinishedParticipants: MutableSet<String> = HashSet()

    // Called when we receive a real-time message from the network.
    // Messages in our game are made up of 2 bytes: the first one is 'F' or 'U'
    // indicating
    // whether it's a final or interim score. The second byte is the score.
    // There is also the
    // 'S' message, which indicates that the game should start.
    internal var mOnRealTimeMessageReceivedListener: OnRealTimeMessageReceivedListener = OnRealTimeMessageReceivedListener { realTimeMessage ->
        val buf = realTimeMessage.messageData
        val sender = realTimeMessage.senderParticipantId
        logD(TAG, "Sender : $sender")
        Log.d(TAG, "Message received: " + buf[0].toChar() + "/" + buf[1].toInt())

//        if (buf[0] == 'F'.toByte() || buf[0] == 'U'.toByte()) {
//
////            // score update.
////            val existingScore = if (mParticipantScore.containsKey(sender))
////                mParticipantScore[sender]
////            else
////                0
////            val thisScore = buf[1].toInt()
////            if (thisScore > existingScore) {
////                // this check is necessary because packets may arrive out of
////                // order, so we
////                // should only ever consider the highest score we received, as
////                // we know in our
////                // game there is no way to lose points. If there was a way to
////                // lose points,
////                // we'd have to add a "serial number" to the packet.
////                mParticipantScore[sender] = thisScore
////            }
//
//            // update the scores on the screen
//            //updatePeerScoresDisplay()
//
//            // if it's a final score, mark this participant as having finished
//            // the game
//            if (buf[0].toChar() == 'F') {
//                mFinishedParticipants.add(realTimeMessage.senderParticipantId)
//            }
//        }
    }

    private var mPlayerId: String? = null

    private val mRoomStatusUpdateCallback = object : RoomStatusUpdateCallback() {
        // Called when we are connected to the room. We're not ready to play yet! (maybe not everybody
        // is connected yet).
        override fun onConnectedToRoom(room: Room?) {
            Log.d(TAG, "onConnectedToRoom.")

            //get participants and my ID:
            mParticipants = room?.participants
            mMyId = room?.getParticipantId(mPlayerId)

            // save room ID if its not initialized in onRoomCreated() so we can leave cleanly before the game starts.
            if (mRoomId == null) {
                mRoomId = room?.roomId
            }

            // print out the list of participants (for debug purposes)
            Log.d(TAG, "Room ID: $mRoomId")
            Log.d(TAG, "My ID $mMyId")
            Log.d(TAG, "<< CONNECTED TO ROOM>>")
            for (p in mParticipants!!){
                logD(TAG, "Participants Display Name "+p.displayName)
            }
            setContentView(R.layout.game_menu_screen)
        }

        // Called when we get disconnected from the room. We return to the main screen.
        override fun onDisconnectedFromRoom(room: Room?) {
            mRoomId = null
            mRoomConfig = null
            showGameError()
        }


        // We treat most of the room update callbacks in the same way: we update our list of
        // participants and update the display. In a real game we would also have to check if that
        // change requires some action like removing the corresponding player avatar from the screen,
        // etc.
        override fun onPeerDeclined(room: Room?, arg1: List<String>) {
            updateRoom(room)
        }

        override fun onPeerInvitedToRoom(room: Room?, arg1: List<String>) {
            updateRoom(room)
        }

        override fun onP2PDisconnected(participant: String) {}

        override fun onP2PConnected(participant: String) {}

        override fun onPeerJoined(room: Room?, arg1: List<String>) {
            updateRoom(room)
        }

        override fun onPeerLeft(room: Room?, peersWhoLeft: List<String>) {
            updateRoom(room)
        }

        override fun onRoomAutoMatching(room: Room?) {
            updateRoom(room)
        }

        override fun onRoomConnecting(room: Room?) {
            updateRoom(room)
        }

        override fun onPeersConnected(room: Room?, peers: List<String>) {
            updateRoom(room)
        }

        override fun onPeersDisconnected(room: Room?, peers: List<String>) {
            updateRoom(room)
        }
    }

    // Accept the given invitation.
    internal fun acceptInviteToRoom(invitationId: String) {
        // accept the invitation
        Log.d(TAG, "Accepting invitation: $invitationId")

        mRoomConfig = RoomConfig.builder(mRoomUpdateCallback)
                .setInvitationIdToAccept(invitationId)
                .setOnMessageReceivedListener(mOnRealTimeMessageReceivedListener)
                .setRoomStatusUpdateCallback(mRoomStatusUpdateCallback)
                .build()

        logD(TAG, "show waiting room")
        keepScreenOn()
//        resetGameVars()

        mRealTimeMultiplayerClient?.join(mRoomConfig!!)
                ?.addOnSuccessListener { Log.d(TAG, "Room Joined Successfully!") }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {

        if (requestCode == RC_SELECT_PLAYERS) {
            // we got the result from the "select players" UI -- ready to create the room
            if (intent!=null){
                handleSelectPlayersResult(resultCode, intent)
            }

        } else if (requestCode == RC_INVITATION_INBOX) {
            // we got the result from the "select invitation" UI (invitation inbox). We're
            // ready to accept the selected invitation:
            if (intent!=null){

                handleInvitationInboxResult(resultCode, intent)
            }

        } else if (requestCode == RC_WAITING_ROOM) {
            // we got the result from the "waiting room" UI.
            if (resultCode == Activity.RESULT_OK) {
                // ready to start playing
                Log.d(TAG, "Starting game (waiting room returned OK).")
                //startGame(true)
            } else if (resultCode == GamesActivityResultCodes.RESULT_LEFT_ROOM) {
                // player indicated that they want to leave the room
                leaveRoom()
            } else if (resultCode == Activity.RESULT_CANCELED) {
                // Dialog was cancelled (user pressed back key, for instance). In our game,
                // this means leaving the room too. In more elaborate games, this could mean
                // something else (like minimizing the waiting room UI).
                leaveRoom()
            }
        }
        super.onActivityResult(requestCode, resultCode, intent)
    }

    // Leave the room.
    private fun leaveRoom() {
        Log.d(TAG, "Leaving room.")
        //mSecondsLeft = 0
        stopKeepingScreenOn()
        if (mRoomId != null) {
            mRealTimeMultiplayerClient?.leave(mRoomConfig!!, mRoomId!!)
                    ?.addOnCompleteListener {
                        mRoomId = null
                        mRoomConfig = null
                    }
            //switchToScreen(R.id.screen_wait)
            logD(TAG, "Room left successfully")
        } else {
//            switchToMainScreen()
            logD(TAG, "Room is is null")
        }
    }

    // Handle the result of the invitation inbox UI, where the player can pick an invitation
    // to accept. We react by accepting the selected invitation, if any.
    private fun handleInvitationInboxResult(response: Int, data: Intent) {
        if (response != Activity.RESULT_OK) {
            Log.w(TAG, "*** invitation inbox UI cancelled, $response")
//            switchToMainScreen()
            return
        }

        Log.d(TAG, "Invitation inbox UI succeeded.")
        val invitation = data.extras!!.getParcelable<Invitation>(Multiplayer.EXTRA_INVITATION)

        // accept invitation
        if (invitation != null) {
            acceptInviteToRoom(invitation.invitationId)
        }
    }

    private fun handleSelectPlayersResult(response: Int, data: Intent) {
        if (response != Activity.RESULT_OK) {
            Log.w(TAG, "*** select players UI cancelled, $response")
//            switchToMainScreen()
            return
        }

        Log.d(TAG, "Select players UI succeeded.")

        // get the invitee list
        val invitees = data.getStringArrayListExtra(Games.EXTRA_PLAYER_IDS)
        Log.d(TAG, "Invitee count: " + invitees.size)

        // get the automatch criteria
        var autoMatchCriteria: Bundle? = null
        val minAutoMatchPlayers = data.getIntExtra(Multiplayer.EXTRA_MIN_AUTOMATCH_PLAYERS, 0)
        val maxAutoMatchPlayers = data.getIntExtra(Multiplayer.EXTRA_MAX_AUTOMATCH_PLAYERS, 0)
        if (minAutoMatchPlayers > 0 || maxAutoMatchPlayers > 0) {
            autoMatchCriteria = RoomConfig.createAutoMatchCriteria(
                    minAutoMatchPlayers, maxAutoMatchPlayers, 0)
            Log.d(TAG, "Automatch criteria: " + autoMatchCriteria!!)
        }

        // create the room
        Log.d(TAG, "Creating room...")
//        switchToScreen(R.id.screen_wait)
        keepScreenOn()
//        resetGameVars()

        mRoomConfig = RoomConfig.builder(mRoomUpdateCallback)
                .addPlayersToInvite(invitees)
                .setOnMessageReceivedListener(mOnRealTimeMessageReceivedListener)
                .setRoomStatusUpdateCallback(mRoomStatusUpdateCallback)
                .setAutoMatchCriteria(autoMatchCriteria).build()
        mRealTimeMultiplayerClient?.create(mRoomConfig!!)
        Log.d(TAG, "Room created, waiting for it to be ready...")
    }


    private var mScore = 10

    // Broadcast my score to everybody else.
    private fun broadcastScore(finalScore: Boolean) {

        // First byte in message indicates whether it's a final score or not
        mMsgBuf[0] = (if (finalScore) 'F' else 'U').toByte()

        // Second byte is the score.
        mMsgBuf[1] = mScore.toByte()

        // Send to every other participant.
        for (p in mParticipants!!) {
            if (p.participantId == mMyId) {
                continue
            }
            if (p.getStatus() != Participant.STATUS_JOINED) {
                continue
            }
            if (finalScore) {
                // final score notification must be sent via reliable message
                mRealTimeMultiplayerClient?.sendReliableMessage(mMsgBuf,
                        mRoomId!!, p.getParticipantId(), RealTimeMultiplayerClient.ReliableMessageSentCallback { statusCode, tokenId, recipientParticipantId ->
                    Log.d(TAG, "RealTime message sent")
                    Log.d(TAG, "  statusCode: $statusCode")
                    Log.d(TAG, "  tokenId: $tokenId")
                    Log.d(TAG, "  recipientParticipantId: $recipientParticipantId")
                })
                        ?.addOnSuccessListener { tokenId -> Log.d(TAG, "Created a reliable message with tokenId: " + tokenId!!) }
            } else {
                // it's an interim score notification, so we can use unreliable
                mRealTimeMultiplayerClient?.sendUnreliableMessage(mMsgBuf, mRoomId!!,
                        p.participantId)
            }
        }
    }



}

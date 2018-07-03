package com.example.mayank.googleplaygame

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.view.WindowManager
import com.example.mayank.googleplaygame.Constants.logD
import com.example.mayank.googleplaygame.PlayGameLib.GameConstants.mFinishedParticipants
import com.example.mayank.googleplaygame.PlayGameLib.GameConstants.mInvitationClient
import com.example.mayank.googleplaygame.PlayGameLib.GameConstants.mParticipantScore
import com.example.mayank.googleplaygame.PlayGameLib.GameConstants.mResultBuf
import com.example.mayank.googleplaygame.multiplay.GameDetailFragment
import com.example.mayank.googleplaygame.multiplay.GameMenuFragment
import com.example.mayank.googleplaygame.multiplay.MultiplayerResultFragment
import com.example.mayank.googleplaygame.multiplay.resultadapter.ResultViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.games.*
import com.google.android.gms.games.multiplayer.Invitation
import com.google.android.gms.games.multiplayer.InvitationCallback
import com.google.android.gms.games.multiplayer.Multiplayer
import com.google.android.gms.games.multiplayer.Participant
import com.google.android.gms.games.multiplayer.realtime.*
import com.google.android.gms.tasks.OnFailureListener
import kotlinx.android.synthetic.main.activity_multi_player.*
import java.util.ArrayList
import java.util.HashMap
import java.util.HashSet

class PlayGameLib(private val activity : Activity) {

    object GameConstants{
        // Holds the configuration of the current room.
        internal var mRoomConfig: RoomConfig? = null
        // Client used to interact with the real time multiplayer system.
        var mRealTimeMultiplayerClient: RealTimeMultiplayerClient? = null
        var mPlayerClient : PlayersClient? = null
        // Room ID where the currently active game is taking place; null if we're
        // not playing.
        var mRoomId: String? = null
        // The participants in the currently active game
        lateinit var mParticipants: ArrayList<Participant>
        // My participant ID in the currently active game
        var mMyId: String? = null
        // Request codes for the UIs that we show with startActivityForResult:
        val RC_SELECT_PLAYERS = 10000
        val RC_INVITATION_INBOX = 10001
        val RC_WAITING_ROOM = 10002
        // Message buffer for sending messages
        var mMsgBuf = ByteArray(2)
        var mResultBuf = ByteArray(5)

        var mRoom :Room? = null

        var displayName : String? = null
        // Score of other participants. We update this as we receive their scores
        // from the network.
        var mParticipantScore: MutableMap<String, Int> = HashMap()
        // Participants who sent us their final score.
        var mFinishedParticipants: MutableSet<String> = HashSet()
        var mPlayerId: String? = null
        var mInvitationClient : InvitationsClient ? = null
        lateinit var modelList: MutableList<ResultViewModel>
        var imageUri : Uri?= null
}

    private val TAG = PlayGameLib::class.java.simpleName


    init {
        GameConstants.mRealTimeMultiplayerClient = getRealTimeMultiPlayerClient()
        GameConstants.mPlayerClient = getPlayerClient()
        GameConstants.displayName = getDisplayName()
        GameConstants.mPlayerId = getPlayerId()
        GameConstants.mMyId = getMyId()
        GameConstants.modelList = mutableListOf<ResultViewModel>()
        GameConstants.mInvitationClient = Games.getInvitationsClient(activity, getSignInAccount()!!)

//        GameConstants.mInvitationClient = getInvitationClient()


    }

    fun getSignInAccount(): GoogleSignInAccount? {
        return GoogleSignIn.getLastSignedInAccount(activity)
    }

    fun getRealTimeMultiPlayerClient(): RealTimeMultiplayerClient? {
        return Games.getRealTimeMultiplayerClient(activity, getSignInAccount()!!)
    }

    fun getPlayerClient(): PlayersClient? {
        return Games.getPlayersClient(activity, getSignInAccount()!!)
    }

    fun getPlayerId(): String? {
        GameConstants.mPlayerClient?.currentPlayer?.addOnSuccessListener { player -> GameConstants.mPlayerId = player.playerId  }
        return GameConstants.mPlayerId
    }

    fun getDisplayName(): String?{
        GameConstants.mPlayerClient?.currentPlayer?.addOnSuccessListener { player -> GameConstants.displayName = player.displayName }
        return GameConstants.displayName
    }

    fun getImageUri(): Uri? {
        GameConstants.mPlayerClient?.currentPlayer?.addOnSuccessListener { player -> GameConstants.imageUri = player.iconImageUri }
        return GameConstants.imageUri
    }

    fun getRoomId(): String? {
        if (GameConstants.mRoomId!=null){
            return GameConstants.mRoomId
        }
        return null
    }

    fun getInvitationClient(): InvitationsClient? {
        return Games.getInvitationsClient(activity, GoogleSignIn.getLastSignedInAccount(activity)!!)
    }

    fun showInvitationInbox(){
        Games.getInvitationsClient(activity, GoogleSignIn.getLastSignedInAccount(activity)!!)
                .invitationInboxIntent
                .addOnSuccessListener { intent -> activity.startActivityForResult(intent, GameConstants.RC_INVITATION_INBOX) }
    }

    fun getMyId(): String? {
        if (GameConstants.mMyId!= null){
            return GameConstants.mMyId
        }
        return null
    }


    fun invitePlayers(){
        activity.progressBar.visibility = View.VISIBLE
        logD(TAG, "Multiplayer clicked")
        GameConstants.mRealTimeMultiplayerClient?.getSelectOpponentsIntent(1,7)?.addOnSuccessListener { intent ->
            activity.startActivityForResult(intent, GameConstants.RC_SELECT_PLAYERS)

        }?.addOnFailureListener(createFailureListener("There was a problem selecting opponents."))
    }

    fun startQuickGame() {
        activity.progressBar.visibility = View.VISIBLE
        logD(TAG, "Start Quick Game")
        // quick-start a game with 1 randomly selected opponent
        val MIN_OPPONENTS = 1
        val MAX_OPPONENTS = 1
        val autoMatchCriteria = RoomConfig.createAutoMatchCriteria(MIN_OPPONENTS,
                MAX_OPPONENTS, 0)
//        switchToScreen(R.id.screen_wait)
        keepScreenOn()
//        resetGameVars()

        GameConstants.mRoomConfig = RoomConfig.builder(mRoomUpdateCallback)
                .setOnMessageReceivedListener(mOnRealTimeMessageReceivedListener)
                .setRoomStatusUpdateCallback(mRoomStatusUpdateCallback)
                .setAutoMatchCriteria(autoMatchCriteria)
                .build()
        GameConstants.mRealTimeMultiplayerClient?.create(GameConstants.mRoomConfig!!)
    }

    private fun keepScreenOn() {
        activity.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    // Clears the flag that keeps the screen on.
    private fun stopKeepingScreenOn() {
        activity.window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    private val mRoomUpdateCallback = object : RoomUpdateCallback() {

        // Called when room has been created
        override fun onRoomCreated(statusCode: Int, room: Room?) {
            Log.d(TAG, "onRoomCreated($statusCode, $room)")
            if (statusCode != GamesCallbackStatusCodes.OK) {
                Log.e(TAG, "*** Error: onRoomCreated, status $statusCode")
//                showGameError()
                // Get the player Id here
                GameConstants.mRoom = room
                return
            }

            // save room ID so we can leave cleanly before the game starts.
            GameConstants.mRoomId = room!!.roomId

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

    private fun showWaitingRoom(room: Room) {
        // minimum number of players required for our game
        // For simplicity, we require everyone to join the game before we start it
        // (this is signaled by Integer.MAX_VALUE).
        val MIN_PLAYERS = Integer.MAX_VALUE
        GameConstants.mRealTimeMultiplayerClient?.getWaitingRoomIntent(room, MIN_PLAYERS)
                ?.addOnSuccessListener { intent ->
                    // show waiting room UI
                    activity.startActivityForResult(intent, GameConstants.RC_WAITING_ROOM)
                }
                ?.addOnFailureListener(createFailureListener("There was a problem getting the waiting room!"))
    }

    private fun showGameError() {
        AlertDialog.Builder(activity)
                .setMessage(activity.getString(R.string.game_problem))
                .setNeutralButton(android.R.string.ok, null).create()
    }

    private fun updateRoom(room: Room?) {
        if (room != null) {
            GameConstants.mParticipants = room.participants
        }
        if (GameConstants.mParticipants != null) {
//            updatePeerScoresDisplay()
            logD(TAG, "Update peer score display..")
        }
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
            GamesClientStatusCodes.MULTIPLAYER_ERROR_NOT_TRUSTED_TESTER -> errorString = activity.getString(R.string.status_multiplayer_error_not_trusted_tester)
            GamesClientStatusCodes.MATCH_ERROR_ALREADY_REMATCHED -> errorString = activity.getString(R.string.match_error_already_rematched)
            GamesClientStatusCodes.NETWORK_ERROR_OPERATION_FAILED -> errorString = activity.getString(R.string.network_error_operation_failed)
            GamesClientStatusCodes.INTERNAL_ERROR -> errorString = activity.getString(R.string.internal_error)
            GamesClientStatusCodes.MATCH_ERROR_INACTIVE_MATCH -> errorString = activity.getString(R.string.match_error_inactive_match)
            GamesClientStatusCodes.MATCH_ERROR_LOCALLY_MODIFIED -> errorString = activity.getString(R.string.match_error_locally_modified)
            else -> errorString = activity.getString(R.string.unexpected_status, GamesClientStatusCodes.getStatusCodeString(status))
        }

        if (errorString == null) {
            return
        }

        val message = activity.getString(R.string.status_exception_error, details, status, exception)

        AlertDialog.Builder(activity)
                .setTitle("Error")
                .setMessage(message + "\n" + errorString)
                .setNeutralButton(android.R.string.ok, null)
                .show()
    }


    private var mOnRealTimeMessageReceivedListener: OnRealTimeMessageReceivedListener = OnRealTimeMessageReceivedListener { realTimeMessage ->
        val buf = realTimeMessage.messageData
        val sender = realTimeMessage.senderParticipantId
        logD(TAG, "Sender : $sender")


        val state = buf[0].toChar()
        val value1 = buf[1].toInt()
        if (state == 'R'){
            val value2 = buf[2].toInt()
            val value3 = buf[1].toInt()
            Log.d(TAG, "Message received: " + buf[0].toChar() + "/" + buf[1].toInt()+ "/" + buf[2].toInt()+ "/" + buf[3].toInt())
            GameConstants.mParticipantScore.put(sender, value1)
            if (GameConstants.mFinishedParticipants.contains(sender)){
                logD(TAG, "Participants already added")
            }else {
                GameConstants.mFinishedParticipants.add(sender)
                logD(TAG, "Adding sender in realtime")
                sendBroadcastResult(state, value1, value2, value3)
            }



        }

        if (state == 'A' || state == 'S'){
            Log.d(TAG, "Message received: " + buf[0].toChar() + "/" + buf[1].toInt())
            sendBroadcastMessage(state, value1)
        }




    }



    // formats a score as a three-digit number
    fun formatScore(i: Int): String {
        var i = i
        if (i < 0) {
            i = 0
        }
        val s = i.toString()
        return if (s.length == 1) "00$s" else if (s.length == 2) "0$s" else s
    }

    private fun sendBroadcastResult(state: Char, value1: Int, value2: Int, value3: Int) {
        logD(TAG, "Sending broadcast")
        activity.sendBroadcast(Intent(MultiplayerResultFragment.ACTION_RESULT_RECEIVED)
                .putExtra("state", state)
                .putExtra("RightAnswers", value1)
                .putExtra("WrongAnswers", value2)
                .putExtra("DropQuestions", value3))
    }

    private fun sendBroadcastFinalResult(state: Char, displayName : String,value1: Int){
        activity.sendBroadcast(Intent(MultiplayerResultFragment.ACTION_RESULT_RECEIVED)
                .putExtra("state", state)
                .putExtra("DisplayName", displayName)
                .putExtra("RightAnswers", value1))
    }


    private fun sendBroadcastMessage(state: Char, value: Int){
        activity.sendBroadcast(Intent(GameDetailFragment.ACTION_MESSAGE_RECEIVED)
                .putExtra("state", state)
                .putExtra("value", value));
    }



    private val mRoomStatusUpdateCallback = object : RoomStatusUpdateCallback() {
        // Called when we are connected to the room. We're not ready to play yet! (maybe not everybody
        // is connected yet).
        override fun onConnectedToRoom(room: Room?) {
            Log.d(TAG, "onConnectedToRoom.")

            //get participants and my ID:
            GameConstants.mParticipants = room?.participants!!
            GameConstants.mMyId = room.getParticipantId(GameConstants.mPlayerId)

            // save room ID if its not initialized in onRoomCreated() so we can leave cleanly before the game starts.
            if (GameConstants.mRoomId == null) {
                GameConstants.mRoomId = room.roomId
            }

            // print out the list of participants (for debug purposes)
            Log.d(TAG, "Room ID: ${GameConstants.mRoomId}")
            Log.d(TAG, "My ID ${GameConstants.mMyId}")
            Log.d(TAG, "<< CONNECTED TO ROOM>>")
            val gameDetailFragment = GameDetailFragment()
            switchToFragment(gameDetailFragment)
            if (activity.progressBar!=null){
                activity.progressBar.visibility = View.GONE
            }
            for (p in GameConstants.mParticipants){
                logD(TAG, "Participants Display Name "+p.displayName)
            }

        }

        // Called when we get disconnected from the room. We return to the main screen.
        override fun onDisconnectedFromRoom(room: Room?) {
            logD(TAG, "On disconnected from room")
            GameConstants.mRoomId = null
            GameConstants.mRoomConfig = null
            showGameError()

            val gameMenuFragment = GameMenuFragment()
            switchToFragment(gameMenuFragment)
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


    fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?){
        logD(TAG, "Request code - $requestCode")
        logD(TAG, "Result Code - $resultCode")
        if (requestCode == GameConstants.RC_SELECT_PLAYERS) {
            // we got the result from the "select players" UI -- ready to create the room
            logD(TAG, "Inside rc select player activity result")
            if (intent != null) {
                handleSelectPlayersResult(resultCode, intent)
            }
        } else if (requestCode == GameConstants.RC_WAITING_ROOM) {
            // we got the result from the "waiting room" UI.
            when (resultCode) {
                Activity.RESULT_OK -> // ready to start playing
                    Log.d(TAG, "Starting game (waiting room returned OK).")
                    //startGame(true)
                GamesActivityResultCodes.RESULT_LEFT_ROOM -> // player indicated that they want to leave the room
                    leaveRoom()
                Activity.RESULT_CANCELED -> // Dialog was cancelled (user pressed back key, for instance). In our game,
                    // this means leaving the room too. In more elaborate games, this could mean
                    // something else (like minimizing the waiting room UI).
                    leaveRoom()
            }
        }else if (requestCode == GameConstants.RC_INVITATION_INBOX) {
            if (resultCode != Activity.RESULT_OK) {
                // Canceled or some error.
                return;
            }

            val invitation = intent?.extras?.getParcelable<Invitation>(Multiplayer.EXTRA_INVITATION)
            if (invitation != null) {
//                val builder = RoomConfig.builder(mRoomUpdateCallback).setInvitationIdToAccept(invitation.invitationId)
//                mJoinedRoomConfig = builder.build()
//                Games.getRealTimeMultiplayerClient(thisActivity, GoogleSignIn.getLastSignedInAccount(thisActivity)!!)
//                        .join(mJoinedRoomConfig!!)
//                window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                acceptInviteToRoom(invitation.invitationId)
            }

        }

    }

    private fun acceptInviteToRoom(invitationId: String?) {
        Log.d(TAG, "Accepting invitation: $invitationId")

        GameConstants.mRoomConfig = RoomConfig.builder(mRoomUpdateCallback)
                .setInvitationIdToAccept(invitationId)
                .setOnMessageReceivedListener(mOnRealTimeMessageReceivedListener)
                .setRoomStatusUpdateCallback(mRoomStatusUpdateCallback).build()
        activity.window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        Games.getRealTimeMultiplayerClient(activity, GoogleSignIn.getLastSignedInAccount(activity)!!).join(GameConstants.mRoomConfig!!)


    }

    val mInvitationCallbackHandler = object : InvitationCallback() {
        override fun onInvitationRemoved(invitationId: String) {
            logD(TAG, "Invitation removed - $invitationId")
        }

        override fun onInvitationReceived(invitation: Invitation) {
            logD(TAG, "On invitation received called...")
            val builder = RoomConfig.builder(mRoomUpdateCallback).setInvitationIdToAccept(invitation.invitationId)
            GameConstants.mRoomConfig = builder.build()
            Games.getRealTimeMultiplayerClient(activity, GoogleSignIn.getLastSignedInAccount(activity)!!)
                    .join(GameConstants.mRoomConfig!!)
            activity.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }

    }


    private fun handleSelectPlayersResult(response: Int, data: Intent) {
        logD(TAG, "Inside select player result")
        if (response != Activity.RESULT_OK) {
            Log.w(TAG, "*** select players UI cancelled, $response")
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
        keepScreenOn()

        GameConstants.mRoomConfig = RoomConfig.builder(mRoomUpdateCallback)
                .addPlayersToInvite(invitees)
                .setOnMessageReceivedListener(mOnRealTimeMessageReceivedListener)
                .setRoomStatusUpdateCallback(mRoomStatusUpdateCallback)
                .setAutoMatchCriteria(autoMatchCriteria).build()
        GameConstants.mRealTimeMultiplayerClient?.create(GameConstants.mRoomConfig!!)
        Log.d(TAG, "Room created, waiting for it to be ready...")




    }

    // Leave the room.
    fun leaveRoom() {
        Log.d(TAG, "Leaving room.")
        //mSecondsLeft = 0
        stopKeepingScreenOn()
        if (GameConstants.mRoomId != null) {
            GameConstants.mRealTimeMultiplayerClient?.leave(GameConstants.mRoomConfig!!, GameConstants.mRoomId!!)
                    ?.addOnCompleteListener {
                        GameConstants.mRoomId = null
                        GameConstants.mRoomConfig = null
                    }
            //switchToScreen(R.id.screen_wait)
            logD(TAG, "Room left successfully")
            val gameMenuFragment = GameMenuFragment()
            switchToFragment(gameMenuFragment)

        } else {
//            switchToMainScreen()
            logD(TAG, "Room is null")
        }
    }

    // Switch UI to the given fragment
    fun switchToFragment(newFrag: Fragment) {
        val manager = (activity as AppCompatActivity).supportFragmentManager
        if (activity.isFinishing){
            manager.beginTransaction().replace(R.id.fragment_container, newFrag)
                    .commit()
        }else{
            manager.beginTransaction().replace(R.id.fragment_container, newFrag)
                    .commit()
        }
    }

    private var mScore = 10

    private var mMsgBuffer = ByteArray(2)

    // For Result :
    // state =
        // R = True Answers
        // W = Wrong Answers
        // D = Drop Answers
        // state = A, int = 0 => Amount Next
        // state = A, int = 1 => Amount Previous
        // state = Q, int = 0 => Question Next
        // state = Q, int = 1 => Question Previous

    // Result :
    // state = F : Final result
    // state = R : Result
    // right = Right Answers
    // wrong = Wrong Answers
    // drop = Drop Questions
    fun broadcastResult(state :Char, right : Int, wrong: Int, drop : Int){
        mResultBuf[0] = state.toByte()
        mResultBuf[1] = right.toByte()
        mResultBuf[2] = wrong.toByte()
        mResultBuf[3] = drop.toByte()

        for (p in GameConstants.mParticipants){
            if (p.participantId == GameConstants.mMyId) {
                continue
            }
            if (p.status != Participant.STATUS_JOINED) {
                continue
            }

            // Sending result to all the participants
            sendReliableMessage(p, mResultBuf)
        }
    }

    fun broadcastMessage(state: Char, score: Int) {
        // First byte in message indicates whether its Right, Wrong or Drop Question.
        mMsgBuffer[0] = state.toByte()
        // Second byte is the score
        mMsgBuffer[1] = score.toByte()


        logD(TAG, "List size - ${GameConstants.mParticipants.size}")
        for (p in GameConstants.mParticipants){
            if (p.participantId == GameConstants.mMyId) {
                continue
            }
            if (p.status != Participant.STATUS_JOINED) {
                continue
            }

            // Sending button inputs to all the participants and changing the UI
            sendReliableMessage(p, mMsgBuffer)
        }

    }

    private fun sendReliableMessage(p: Participant, mMsgBuffer: ByteArray) {
        GameConstants.mRealTimeMultiplayerClient?.sendReliableMessage(mMsgBuffer,
                GameConstants.mRoomId!!, p.participantId, RealTimeMultiplayerClient.ReliableMessageSentCallback { statusCode, tokenId, recipientParticipantId ->
            Log.d(TAG, "RealTime message sent")
//            Log.d(TAG, "  statusCode: $statusCode")
//            Log.d(TAG, "  tokenId: $tokenId")
//            Log.d(TAG, "  recipientParticipantId: $recipientParticipantId")
        })
                ?.addOnSuccessListener { tokenId -> Log.d(TAG, "Created a reliable message with tokenId: " + tokenId!!) }
    }


    // Broadcast my score to everybody else.
    fun broadcastScore(finalScore: Boolean) {

        // First byte in message indicates whether it's a final score or not
        GameConstants.mMsgBuf[0] = (if (finalScore) 'F' else 'U').toByte()

        // Second byte is the score.
        GameConstants.mMsgBuf[1] = mScore.toByte()

        // Send to every other participant.
        for (p in GameConstants.mParticipants!!) {
            if (p.participantId == GameConstants.mMyId) {
                continue
            }
            if (p.status != Participant.STATUS_JOINED) {
                continue
            }
            if (finalScore) {
                // final score notification must be sent via reliable message
                GameConstants.mRealTimeMultiplayerClient?.sendReliableMessage(GameConstants.mMsgBuf,
                        GameConstants.mRoomId!!, p.getParticipantId(), RealTimeMultiplayerClient.ReliableMessageSentCallback { statusCode, tokenId, recipientParticipantId ->
                    Log.d(TAG, "RealTime message sent")
                    Log.d(TAG, "  statusCode: $statusCode")
                    Log.d(TAG, "  tokenId: $tokenId")
                    Log.d(TAG, "  recipientParticipantId: $recipientParticipantId")
                })
                        ?.addOnSuccessListener { tokenId -> Log.d(TAG, "Created a reliable message with tokenId: " + tokenId!!) }
            } else {
                // it's an interim score notification, so we can use unreliable
                GameConstants.mRealTimeMultiplayerClient?.sendUnreliableMessage(GameConstants.mMsgBuf, GameConstants.mRoomId!!,
                        p.participantId)
            }
        }
    }



}
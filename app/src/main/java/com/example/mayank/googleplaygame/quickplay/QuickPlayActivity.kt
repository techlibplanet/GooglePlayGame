package com.example.mayank.googleplaygame.quickplay

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.example.mayank.googleplaygame.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import android.view.WindowManager
import com.google.android.gms.games.multiplayer.realtime.RoomConfig
import com.google.android.gms.games.multiplayer.realtime.Room
import android.support.annotation.NonNull
import com.google.android.gms.games.multiplayer.realtime.RoomUpdateCallback
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.games.multiplayer.realtime.RoomStatusUpdateCallback
import android.app.Activity
import com.google.android.gms.games.multiplayer.Participant
import android.content.Intent
import android.os.Parcelable
import android.support.annotation.Nullable
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import com.example.mayank.googleplaygame.Constants.logD
import com.example.mayank.googleplaygame.PlayGameLib
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.games.*
import com.google.android.gms.games.multiplayer.Invitation
import com.google.android.gms.games.multiplayer.InvitationCallback
import com.google.android.gms.games.multiplayer.realtime.RealTimeMessage
import com.google.android.gms.games.multiplayer.realtime.OnRealTimeMessageReceivedListener
import com.google.android.gms.games.multiplayer.Multiplayer
import kotlin.math.log


class QuickPlayActivity : AppCompatActivity(), View.OnClickListener {


    private val TAG = QuickPlayActivity::class.java.simpleName
    private val RC_SELECT_PLAYERS = 10000
    private val ROLE_ANY: Long = 0x0 // can play in any match.
    private val ROLE_FARMER: Long = 0x1 // 001 in binary
    private val ROLE_ARCHER: Long = 0x2 // 010 in binary
    private val ROLE_WIZARD: Long = 0x4 // 100 in binary
    private val RC_WAITING_ROOM = 9007
    var mWaitingRoomFinishedFromCode = false
    private val RC_INVITATION_INBOX = 9008
    private var playGameLib : PlayGameLib? = null

    private var mJoinedRoomConfig: RoomConfig? = null
    private var mMyParticipantId: String? = null
    private var mInvitationClient : InvitationsClient? = null

    val CLICKABLES = intArrayOf(R.id.my_quick_play_button, R.id.multi_play_button, R.id.invitation_inbox_button)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quick_play)
        for (id in CLICKABLES) {
            findViewById<Button>(id).setOnClickListener(this)
        }

        playGameLib = PlayGameLib(this)

//        checkForInvitation()
        //mInvitationClient?.registerInvitationCallback(mInvitationCallbackHandler)


    }


    override fun onClick(view: View) {
        when (view.id) {
            R.id.my_quick_play_button -> {
                startQuickGame(ROLE_ANY)
            }

            R.id.multi_play_button -> {
                invitePlayers()
            }

            R.id.invitation_inbox_button -> {
                showInvitationInbox()
//                checkForInvitation()
            }
        }
    }


    private fun showInvitationInbox() {
        Games.getInvitationsClient(this, GoogleSignIn.getLastSignedInAccount(this)!!)
                .invitationInboxIntent
                .addOnSuccessListener { intent -> startActivityForResult(intent, RC_INVITATION_INBOX) }
    }

    private fun invitePlayers() {
        // launch the player selection screen
        // minimum: 1 other player; maximum: 3 other players
        Games.getRealTimeMultiplayerClient(this, GoogleSignIn.getLastSignedInAccount(this)!!)
                .getSelectOpponentsIntent(1, 7, true)
                .addOnSuccessListener { intent -> startActivityForResult(intent, RC_SELECT_PLAYERS) }
    }


    private fun startQuickGame(role: Long) {
        logD(TAG, "Starting quick game")
        // auto-match criteria to invite one random automatch opponent.
        // You can also specify more opponents (up to 3).
        val autoMatchCriteria = RoomConfig.createAutoMatchCriteria(1, 1, role)

        // build the room config:
        val roomConfig = RoomConfig.builder(mRoomUpdateCallback)
                .setOnMessageReceivedListener(mMessageReceivedHandler)
                .setRoomStatusUpdateCallback(mRoomStatusCallbackHandler)
                .setAutoMatchCriteria(autoMatchCriteria)
                .build()

        // prevent screen from sleeping during handshake
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        // Save the roomConfig so we can use it if we call leave().
        mJoinedRoomConfig = roomConfig

        // create room:
        Games.getRealTimeMultiplayerClient(this, GoogleSignIn.getLastSignedInAccount(this)!!)
                .create(roomConfig)


    }



    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        logD(TAG, "Inside on Activity result")
        playGameLib?.onActivityResult(requestCode, resultCode, data)
//        if (requestCode == RC_SELECT_PLAYERS) {
//            if (resultCode != Activity.RESULT_OK) {
//                // Canceled or some other error.
//                logD(TAG, "Canceled or some other error.")
//                return
//            }
//
//            // Get the invitee list.
//            val invitees = data!!.getStringArrayListExtra(Games.EXTRA_PLAYER_IDS)
//
//            // Get Automatch criteria.
//            val minAutoPlayers = data.getIntExtra(Multiplayer.EXTRA_MIN_AUTOMATCH_PLAYERS, 0)
//            val maxAutoPlayers = data.getIntExtra(Multiplayer.EXTRA_MAX_AUTOMATCH_PLAYERS, 0)
//
//            logD(TAG, "Max Auto - $maxAutoPlayers")
//
//            // Create the room configuration.
//            val roomBuilder = RoomConfig.builder(mRoomUpdateCallback)
//                    .setOnMessageReceivedListener(mMessageReceivedHandler)
//                    .setRoomStatusUpdateCallback(mRoomStatusCallbackHandler)
//                    .addPlayersToInvite(invitees)
//            if (minAutoPlayers > 0) {
//                logD(TAG, "Inside automatch")
//                roomBuilder.setAutoMatchCriteria(
//                        RoomConfig.createAutoMatchCriteria(minAutoPlayers, maxAutoPlayers, 0))
//            }
//
//            // Save the roomConfig so we can use it if we call leave().
//            mJoinedRoomConfig = roomBuilder.build()
//            Games.getRealTimeMultiplayerClient(this, GoogleSignIn.getLastSignedInAccount(this)!!)
//                    .create(mJoinedRoomConfig!!)
//        }
//
//        if (requestCode == RC_WAITING_ROOM) {
//
//            // Look for finishing the waiting room from code, for example if a
//            // "start game" message is received.  In this case, ignore the result.
//            if (mWaitingRoomFinishedFromCode) {
//                return;
//            }
//
//            if (resultCode == Activity.RESULT_OK) {
//                // Start the game!
//            } else if (resultCode == Activity.RESULT_CANCELED) {
//                // Waiting room was dismissed with the back button. The meaning of this
//                // action is up to the game. You may choose to leave the room and cancel the
//                // match, or do something else like minimize the waiting room and
//                // continue to connect in the background.
//
//                // in this example, we take the simple approach and just leave the room:
//                if (mRoom?.roomId!=null){
//                    Games.getRealTimeMultiplayerClient(thisActivity,
//                            GoogleSignIn.getLastSignedInAccount(this)!!)
//                            .leave(mJoinedRoomConfig!!, mRoom?.roomId!!);
//                    window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
//                }
//
//            } else if (resultCode == GamesActivityResultCodes.RESULT_LEFT_ROOM) {
//                // player wants to leave the room.
//                Games.getRealTimeMultiplayerClient(thisActivity,
//                        GoogleSignIn.getLastSignedInAccount(this)!!)
//                        .leave(mJoinedRoomConfig!!, mRoom?.roomId!!);
//                window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
//            }
//        }
//
//        if (requestCode == RC_INVITATION_INBOX) {
//            if (resultCode != Activity.RESULT_OK) {
//                // Canceled or some error.
//                return;
//            }
//
//            val invitation = data?.extras?.getParcelable<Invitation>(Multiplayer.EXTRA_INVITATION)
//            if (invitation != null) {
////                val builder = RoomConfig.builder(mRoomUpdateCallback).setInvitationIdToAccept(invitation.invitationId)
////                mJoinedRoomConfig = builder.build()
////                Games.getRealTimeMultiplayerClient(thisActivity, GoogleSignIn.getLastSignedInAccount(thisActivity)!!)
////                        .join(mJoinedRoomConfig!!)
////                window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
//                acceptInviteToRoom(invitation.invitationId)
//            }
//
//        }


    }

    private fun acceptInviteToRoom(invitationId: String?) {
        Log.d(TAG, "Accepting invitation: $invitationId")

        mJoinedRoomConfig = RoomConfig.builder(mRoomUpdateCallback)
                .setInvitationIdToAccept(invitationId)
                .setOnMessageReceivedListener(mMessageReceivedHandler)
                .setRoomStatusUpdateCallback(mRoomStatusCallbackHandler).build()
        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        Games.getRealTimeMultiplayerClient(thisActivity, GoogleSignIn.getLastSignedInAccount(this)!!).join(mJoinedRoomConfig!!)


    }


//    private fun checkForInvitation() {
//        Games.getGamesClient(this, GoogleSignIn.getLastSignedInAccount(this)!!).activationHint.addOnSuccessListener { bundle ->
//            val invitation = bundle.getParcelable<Invitation>(Multiplayer.EXTRA_INVITATION)
//            if (invitation != null) {
//                val builder = RoomConfig.builder(mRoomUpdateCallback)
//                        .setInvitationIdToAccept(invitation.invitationId)
//                mJoinedRoomConfig = builder.build()
//                Games.getRealTimeMultiplayerClient(thisActivity, GoogleSignIn.getLastSignedInAccount(thisActivity)!!)
//                        .join(mJoinedRoomConfig!!)
//
//                window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
//            }
//        }
//    }

    private val mInvitationCallbackHandler = object : InvitationCallback() {
        override fun onInvitationRemoved(invitationId: String) {
            logD(TAG, "Invitation removed - $invitationId")
        }

        override fun onInvitationReceived(invitation: Invitation) {
            val builder = RoomConfig.builder(mRoomUpdateCallback).setInvitationIdToAccept(invitation.invitationId)
            mJoinedRoomConfig = builder.build()
            Games.getRealTimeMultiplayerClient(thisActivity, GoogleSignIn.getLastSignedInAccount(thisActivity)!!)
                    .join(mJoinedRoomConfig!!)
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }

    }


    private fun onStartGameMessageReceived() {
        mWaitingRoomFinishedFromCode = true
        finishActivity(RC_WAITING_ROOM)
    }


    private val mRoomUpdateCallback = object : RoomUpdateCallback() {
        override fun onRoomCreated(code: Int, @Nullable room: Room?) {
            // Update UI and internal state based on room updates.
            if (code == GamesCallbackStatusCodes.OK && room != null) {
                showWaitingRoom(room, 2)
                Log.d(TAG, "Room " + room.roomId + " created.")
            } else {
                Log.w(TAG, "Error creating room: $code")
                // let screen go to sleep
                window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

            }
        }

        override fun onJoinedRoom(code: Int, @Nullable room: Room?) {
            // Update UI and internal state based on room updates.
            if (code == GamesCallbackStatusCodes.OK && room != null) {
                Log.d(TAG, "Room " + room.roomId + " joined.")
            } else {
                Log.w(TAG, "Error joining room: $code")
                // let screen go to sleep
                window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

            }
        }

        override fun onLeftRoom(code: Int, roomId: String) {
            Log.d(TAG, "Left room$roomId")
        }

        override fun onRoomConnected(code: Int, @Nullable room: Room?) {
            if (code == GamesCallbackStatusCodes.OK && room != null) {
                Log.d(TAG, "Room " + room.roomId + " connected.")
            } else {
                Log.w(TAG, "Error connecting to room: $code")
                // let screen go to sleep
                window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

            }
        }
    }

    // are we already playing?
    var mPlaying = false

    // at least 2 players required for our game
    val MIN_PLAYERS = 2

    // returns whether there are enough players to start the game
    fun shouldStartGame(room: Room?): Boolean {
        var connectedPlayers = 0
        for (p in room!!.participants) {
            if (p.isConnectedToRoom) {
                ++connectedPlayers
            }
        }
        return connectedPlayers >= MIN_PLAYERS
    }

    // Returns whether the room is in a state where the game should be canceled.
    fun shouldCancelGame(room: Room?): Boolean {
        // TODO: Your game-specific cancellation logic here. For example, you might decide to
        // cancel the game if enough people have declined the invitation or left the room.
        // You can check a participant's status with Participant.getStatus().
        // (Also, your UI should have a Cancel button that cancels the game too)
        return false
    }

    private val thisActivity = this
    private var mRoom: Room? = null
    private val mRoomStatusCallbackHandler = object : RoomStatusUpdateCallback() {
        override fun onDisconnectedFromRoom(room: Room?) {
            logD(TAG, "On disconnected from room")
            // This usually happens due to a network error, leave the game.
            Games.getRealTimeMultiplayerClient(thisActivity, GoogleSignIn.getLastSignedInAccount(thisActivity)!!)
                    .leave(mJoinedRoomConfig!!, room?.roomId!!)
            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            // show error message and return to main screen
            mRoom = null
            mJoinedRoomConfig = null
        }

        override fun onRoomConnecting(@Nullable room: Room?) {
            logD(TAG, "On room connecting..")
            // Update the UI status since we are in the process of connecting to a specific room.
        }

        override fun onRoomAutoMatching(@Nullable room: Room?) {
            logD(TAG, "On room auto matching...")
            // Update the UI status since we are in the process of matching other players.
        }

        override fun onPeerInvitedToRoom(@Nullable room: Room?, list: List<String>) {
            // Update the UI status since we are in the process of matching other players.
            logD(TAG, "On peer invited to room")
        }

        override fun onPeerDeclined(@Nullable room: Room?, list: List<String>) {
            // Peer declined invitation, see if game should be canceled
            logD(TAG, "On peer declined...")
            if (!mPlaying && shouldCancelGame(room)) {
                Games.getRealTimeMultiplayerClient(thisActivity,
                        GoogleSignIn.getLastSignedInAccount(thisActivity)!!)
                        .leave(mJoinedRoomConfig!!, room!!.roomId)
                window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            }
        }

        override fun onPeerJoined(@Nullable room: Room?, list: List<String>) {
            // Update UI status indicating new players have joined!
            logD(TAG, "On peer joined room")
        }

        override fun onPeerLeft(@Nullable room: Room?, list: List<String>) {
            // Peer left, see if game should be canceled.
            logD(TAG, "On peer left room")
            if (!mPlaying && shouldCancelGame(room)) {
                Games.getRealTimeMultiplayerClient(thisActivity,
                        GoogleSignIn.getLastSignedInAccount(thisActivity)!!)
                        .leave(mJoinedRoomConfig!!, room!!.roomId)
                window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            }
        }

        override fun onConnectedToRoom(@Nullable room: Room?) {
            // Connected to room, record the room Id.
            logD(TAG, "On connected to room")
            mRoom = room
            Games.getPlayersClient(thisActivity, GoogleSignIn.getLastSignedInAccount(thisActivity)!!)
                    .currentPlayerId.addOnSuccessListener { playerId -> mMyParticipantId = mRoom!!.getParticipantId(playerId) }
        }


        override fun onPeersConnected(@Nullable room: Room?, list: List<String>) {
            logD(TAG, "On peers connected to ongoing game")
            if (mPlaying) {
                // add new player to an ongoing game
            } else if (shouldStartGame(room)) {
                // start game!
            }
        }

        override fun onPeersDisconnected(@Nullable room: Room?, list: List<String>) {
            logD(TAG, "On peer disconnected from room")
            if (mPlaying) {
                // do game-specific handling of this -- remove player's avatar
                // from the screen, etc. If not enough players are left for
                // the game to go on, end the game and leave the room.
            } else if (shouldCancelGame(room)) {
                // cancel the game
                Games.getRealTimeMultiplayerClient(thisActivity,
                        GoogleSignIn.getLastSignedInAccount(thisActivity)!!)
                        .leave(mJoinedRoomConfig!!, room!!.roomId)
                window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            }
        }

        override fun onP2PConnected(participantId: String) {
            logD(TAG, "On P2PConnected")
            // Update status due to new peer to peer connection.
        }

        override fun onP2PDisconnected(participantId: String) {
            logD(TAG, "On P@PDisconnected..")
            // Update status due to  peer to peer connection being disconnected.
        }
    }


    private fun showWaitingRoom(room: Room, maxPlayersToStartGame: Int) {
        Games.getRealTimeMultiplayerClient(this, GoogleSignIn.getLastSignedInAccount(this)!!)
                .getWaitingRoomIntent(room, maxPlayersToStartGame)
                .addOnSuccessListener { intent -> startActivityForResult(intent, RC_WAITING_ROOM) }
    }

    var pendingMessageSet: HashSet<Int> = HashSet()

    @Synchronized
    fun recordMessageToken(tokenId: Int) {
        pendingMessageSet.add(tokenId)
    }

    private val handleMessageSentCallback = object : RealTimeMultiplayerClient.ReliableMessageSentCallback {
        override fun onRealTimeMessageSent(statusCode: Int, tokenId: Int, recipientId: String) {
            // handle the message being sent.
            synchronized(this) {
                pendingMessageSet.remove(tokenId)
            }
        }
    }

    private val mMessageReceivedHandler = OnRealTimeMessageReceivedListener { realTimeMessage ->
        // Handle messages received here.
        val message = realTimeMessage.messageData
        // process message contents...
    }
}

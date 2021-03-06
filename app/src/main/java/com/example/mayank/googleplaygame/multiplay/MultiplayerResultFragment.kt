package com.example.mayank.googleplaygame.multiplay

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.example.mayank.googleplaygame.Constants.logD
import com.example.mayank.googleplaygame.PlayGameLib

import com.example.mayank.googleplaygame.R
import com.example.mayank.googleplaygame.helpers.AlertDialog
import com.example.mayank.googleplaygame.multiplay.resultadapter.ResultViewAdapter
import com.example.mayank.googleplaygame.multiplay.resultadapter.ResultViewModel
import com.example.mayank.googleplaygame.network.wallet.Itransaction
import com.example.mayank.googleplaygame.network.wallet.Transactions
import com.example.mayank.myplaygame.network.ApiClient
import com.google.android.gms.games.multiplayer.Participant
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val RIGHT_ANSWERS = "RightAnswers"
private const val WRONG_ANSWERS = "WrongAnswers"
private const val DROP_QUESTIONS = "DropQuestions"
private const val AMOUNT = "Amount"


/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [MultiplayerResultFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [MultiplayerResultFragment.newInstance] factory method to
 * create an instance of this fragment.
 *
 */
class MultiplayerResultFragment : Fragment() {

    private val TAG = MultiplayerResultFragment::class.java.simpleName
    // TODO: Rename and change types of parameters
    private var rightAnswers: Int? = 0
    private var wrongAnswers: Int? = 0
    private var dropQuestions: Int? = 0
    private var amount : Float? = 0F
    private var listener: OnFragmentInteractionListener? = null

    private lateinit var resultRecyclerView: RecyclerView
    val adapter: ResultViewAdapter by lazy { ResultViewAdapter() }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            rightAnswers = it.getInt(RIGHT_ANSWERS)
            wrongAnswers = it.getInt(WRONG_ANSWERS)
            dropQuestions = it.getInt(DROP_QUESTIONS)
            amount = it.getFloat(AMOUNT)
        }

        logD(TAG, "Right Answers - $rightAnswers")
        logD(TAG, "Wrong Answers - $wrongAnswers")
        logD(TAG, "Drop Questions - $dropQuestions")

        context?.registerReceiver(resultBroadcastReceiver, syncIntentFilter);
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_multiplayer_result, container, false)
        resultRecyclerView = view.findViewById(R.id.result_recycler_view)
        resultRecyclerView.layoutManager = LinearLayoutManager(activity)
        resultRecyclerView.setHasFixedSize(true)
        resultRecyclerView.addItemDecoration(DividerItemDecoration(activity, LinearLayoutManager.VERTICAL))

        resultRecyclerView.adapter = adapter
        setSettingsItem()
        view.findViewById<Button>(R.id.back_result_screen_button).setOnClickListener {
            val playGameLib = PlayGameLib(activity!!)
            playGameLib.leaveRoom()
            rightAnswers=0
            wrongAnswers=0
            dropQuestions=0
            amount=0F
            playGameLib.clearData()
            val mainMenuFragment = GameMenuFragment()
            playGameLib.switchToFragment(mainMenuFragment)
        }
        return view
    }



    private fun setSettingsItem() {
        PlayGameLib.GameConstants.modelList.clear()
        logD(TAG, "MyImageUri : ${PlayGameLib.GameConstants.imageUri}")
        PlayGameLib.GameConstants.modelList.add(ResultViewModel(PlayGameLib.GameConstants.displayName!!, formatScore(rightAnswers!!),PlayGameLib.GameConstants.imageUri!!))

        for (p in PlayGameLib.GameConstants.mParticipants) {
            if (PlayGameLib.GameConstants.mRoomId != null) {
                val pid = p.participantId
                if (pid == PlayGameLib.GameConstants.mMyId) {
                    logD(TAG, "Adding sender in fragment")
                    PlayGameLib.GameConstants.mFinishedParticipants.add(pid)

                }
            }
        }
//        updateMyScore(PlayGameLib.GameConstants.mMyId!!, rightAnswers!!)

        updateScore()
        //setRecyclerViewAdapter(PlayGameLib.GameConstants.modelList)
    }


    private fun setRecyclerViewAdapter(list: List<ResultViewModel>) {
        adapter.items = list
        adapter.notifyDataSetChanged()
    }

    fun updateScore() {
        logD(TAG, "Inside update score")
        if (PlayGameLib.GameConstants.mRoomId != null) {
            for (p in PlayGameLib.GameConstants.mParticipants) {
                val pid = p.participantId
                if (pid == PlayGameLib.GameConstants.mMyId) {
                    continue
                }
                if (p.status != Participant.STATUS_JOINED) {
                    continue
                }
                val score = if (PlayGameLib.GameConstants.mParticipantScore.containsKey(pid)) PlayGameLib.GameConstants.mParticipantScore.get(pid) else 0

                logD(TAG, "score -$score")
//                (findViewById<View>(arr[i]) as TextView).text = formatScore(score) + " - " +
//                        p.getDisplayName()
//                ++i

//                if (pid== PlayGameLib.GameConstants.mMyId){
//                    PlayGameLib.GameConstants.modelList.add(ResultViewModel(p.displayName, formatScore(rightAnswers!!)))
//                    setRecyclerViewAdapter(PlayGameLib.GameConstants.modelList)
//                }
                logD(TAG, "Participants list size - ${PlayGameLib.GameConstants.mParticipants.size}")
                logD(TAG, "Finished participants list size ${PlayGameLib.GameConstants.mFinishedParticipants.size}")



                if (PlayGameLib.GameConstants.mParticipants.size == PlayGameLib.GameConstants.mFinishedParticipants.size) {

                    PlayGameLib.GameConstants.modelList.add(ResultViewModel(p.displayName, formatScore(score!!), p.iconImageUri))

                    logD(TAG, "Player Image Uri : ${p.iconImageUri}")
                    // dismiss progress bar
                    Log.d(TAG, "All players finish the game")

                    PlayGameLib.GameConstants.modelList.sortByDescending {
                        it.rightAnswers
                    }




                    val result = Collections.min(PlayGameLib.GameConstants.modelList, compResult())
                    logD(TAG, "Result max value is = Player Name ${result.playerName}, Player score - ${result.rightAnswers}")
                    if (result.playerName == PlayGameLib.GameConstants.displayName){
//                        Toast.makeText(activity, "Congrats! You Win!", Toast.LENGTH_SHORT).show()
                        if (PlayGameLib.GameConstants.modelList[1].rightAnswers == result.rightAnswers){
                            com.example.mayank.googleplaygame.helpers.AlertDialog.alertDialog(activity!!, "Result", "Tie between ${result.playerName} and ${PlayGameLib.GameConstants.modelList[1].playerName}!")
                        }else{

                            var totalAmount = (amount?.times(PlayGameLib.GameConstants.mFinishedParticipants.size))?.times(80)?.div(100)
                            totalAmount = totalAmount?.minus(amount!!)
                            logD(TAG, "Total amount = $totalAmount")
                            val message = "Congrats! You Win!\nAmount Bid - $amount\nAmount Win - ${totalAmount}"
                            if (!PlayGameLib.GameConstants.balanceAdded){
                                PlayGameLib.GameConstants.balanceAdded = true
                                updateBalance(PlayGameLib.GameConstants.displayName!!, totalAmount, Calendar.getInstance().time.toString(), message)
                            }
                        }

                    }else{
                        logD(TAG, "Subtract method will call")
                        val message = "Sorry! You Loose!\nAmount Bid - $amount\nAmount Loose - $amount"
                        if (!PlayGameLib.GameConstants.balanceAdded){
                            PlayGameLib.GameConstants.balanceAdded = true
                            subtractBalance(PlayGameLib.GameConstants.displayName!!, amount, Calendar.getInstance().time.toString(), message)
                        }
                    }


                } else {
                    // show progress bar
                    Log.d(TAG, "Please wait for other participants to finish the game")
                }


            }
        }
    }

    private fun subtractBalance(playerName: String, amount: Float?, timeStamp: String, message : String) {
        val apiClient = ApiClient()
        var retrofit = apiClient.getService<Itransaction>()
        retrofit.subtractResultBalance(playerName, amount!!, timeStamp).enqueue(object : Callback<Transactions>{
            override fun onFailure(call: Call<Transactions>?, t: Throwable?) {
                AlertDialog.alertDialog(activity!!, "Error", "Error : $t")
            }

            override fun onResponse(call: Call<Transactions>?, response: Response<Transactions>?) {
                if (response?.isSuccessful!!){
                    val balance = response.body()?.balance
                    if (PlayGameLib.GameConstants.modelList[0].playerName != "Player Name"){
                        PlayGameLib.GameConstants.modelList.add(0,ResultViewModel("Player Name", "Scores", null))
                    }
                    setRecyclerViewAdapter(PlayGameLib.GameConstants.modelList)
                    AlertDialog.alertDialog(activity!!, "Summary", "$message\nBalance : $balance")
                }else{
                    AlertDialog.alertDialog(activity!!, "Summary", "$message\nError : ${response.body()?.error}")
                }
            }

        })
    }

    private fun updateBalance(displayName: String, totalAmount: Float?, timeStamp: String, message : String) {
        val apiClient = ApiClient()
        var retrofit = apiClient.getService<Itransaction>()
        retrofit.addResultBalance(displayName, totalAmount!!, timeStamp).enqueue(object : Callback<Transactions>{
            override fun onFailure(call: Call<Transactions>?, t: Throwable?) {
                AlertDialog.alertDialog(activity!!, "Error", "Error : $t")
            }

            override fun onResponse(call: Call<Transactions>?, response: Response<Transactions>?) {
                if (response?.isSuccessful!!){
                    val balance = response.body()?.balance
                    logD(TAG, "Message - $message Balance - $balance")
                    if (PlayGameLib.GameConstants.modelList[0].playerName != "Player Name"){
                        PlayGameLib.GameConstants.modelList.add(0,ResultViewModel("Player Name", "Scores", null))
                    }
                    AlertDialog.alertDialog(activity!!, "Summary", "$message\nBalance : $balance")
                    setRecyclerViewAdapter(PlayGameLib.GameConstants.modelList)
                }
            }

        })

    }

    private fun winAmount() {

    }

    inner class compResult : Comparator<ResultViewModel> {
        override fun compare(a: ResultViewModel, b: ResultViewModel): Int {
            if (a.rightAnswers > b.rightAnswers)
                return -1 // highest value first
            return if (a.rightAnswers === b.rightAnswers) 0 else 1
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

    // TODO: Rename method, update argument and hook method into UI event
    fun onButtonPressed(uri: Uri) {
        listener?.onFragmentInteraction(uri)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnFragmentInteractionListener) {
            listener = context
        } else {
            throw RuntimeException(context.toString() + " must implement OnFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     *
     *
     * See the Android Training lesson [Communicating with Other Fragments]
     * (http://developer.android.com/training/basics/fragments/communicating.html)
     * for more information.
     */
    interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        fun onFragmentInteraction(uri: Uri)
    }

    private val syncIntentFilter = IntentFilter(ACTION_RESULT_RECEIVED)

    private val resultBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            logD(TAG, "Receiving broadcast...")
            logD(TAG, "${intent.action}")
            if (MultiplayerResultFragment.ACTION_RESULT_RECEIVED == intent.action) {
                val state = intent.getCharExtra("state", 'Z')
                val rightAnswers = intent.getIntExtra("RightAnswers", -1)
                val wrongAnswers = intent.getIntExtra("WrongAnswers", -1)
                val dropQuestions = intent.getIntExtra("DropQuestions", -1)
                logD(TAG, "State - $state")
                logD(TAG, "RightAnswers - $rightAnswers")
                logD(TAG, "WrongAnswers - $wrongAnswers")
                logD(TAG, "DropQuestions - $dropQuestions")
                updateScore()


            }
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment MultiplayerResultFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String, param3: String, param4: String) =
                MultiplayerResultFragment().apply {
                    arguments = Bundle().apply {
                        putString(RIGHT_ANSWERS, param1)
                        putString(WRONG_ANSWERS, param2)
                        putString(DROP_QUESTIONS, param3)
                        putString(AMOUNT, param4)
                    }
                }

        const val ACTION_RESULT_RECEIVED = "com.example.mayank.googleplaygame.ACTION_RESULT_RECEIVED"
    }
}

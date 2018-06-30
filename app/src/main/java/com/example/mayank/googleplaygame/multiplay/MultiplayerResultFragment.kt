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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.mayank.googleplaygame.Constants.logD
import com.example.mayank.googleplaygame.PlayGameLib

import com.example.mayank.googleplaygame.R
import com.example.mayank.googleplaygame.multiplay.resultadapter.ResultViewAdapter
import com.example.mayank.googleplaygame.multiplay.resultadapter.ResultViewModel
import com.google.android.gms.games.multiplayer.Participant

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val RIGHT_ANSWERS = "RightAnswers"
private const val WRONG_ANSWERS = "WrongAnswers"
private const val DROP_QUESTIONS = "DropQuestions"

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
    private var listener: OnFragmentInteractionListener? = null

    private lateinit var resultRecyclerView : RecyclerView
    val adapter: ResultViewAdapter by lazy { ResultViewAdapter() }
    private lateinit var modelList: MutableList<ResultViewModel>



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            rightAnswers = it.getInt(RIGHT_ANSWERS)
            wrongAnswers = it.getInt(WRONG_ANSWERS)
            dropQuestions = it.getInt(DROP_QUESTIONS)
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
        modelList = mutableListOf<ResultViewModel>()
        setSettingsItem()
        return view
    }

    private fun setSettingsItem() {
        modelList.clear()
        modelList.add(ResultViewModel("Player Name","Scores"))
        //modelList.add(ResultViewModel(PlayGameLib.GameConstants.displayName!!, rightAnswers.toString()))
        updateMyScore(PlayGameLib.GameConstants.mMyId!!, rightAnswers!!)
        updateScore()
        setRecyclerViewAdapter(modelList)
    }

    private fun setRecyclerViewAdapter(list: List<ResultViewModel>) {
        adapter.items = list
        adapter.notifyDataSetChanged()
    }

    fun updateScore(){
        if (PlayGameLib.GameConstants.mRoomId!= null){
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

                if (pid== PlayGameLib.GameConstants.mMyId){
                    modelList.add(ResultViewModel(p.displayName, formatScore(score!!)))
                    setRecyclerViewAdapter(modelList)
                }


            }
        }
    }

    fun updateMyScore(sender: String, value1: Int) {
        val existingScore = if (PlayGameLib.GameConstants.mParticipantScore.containsKey(sender)) PlayGameLib.GameConstants.mParticipantScore[sender] else 0
        val thisScore = value1
        if (thisScore> existingScore!!){
            PlayGameLib.GameConstants.mParticipantScore.put(sender, thisScore)
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
            if (GameDetailFragment.ACTION_MESSAGE_RECEIVED == intent.action) {
                val state = intent.getCharExtra("state", 'Z')
                val rightAnswers = intent.getIntExtra("RightAnswers", -1)
                val wrongAnswers = intent.getIntExtra("WrongAnswers", -1)
                val dropQuestions = intent.getIntExtra("DropQuestions", -1)
                logD(TAG, "State - $state")
                logD(TAG, "RightAnswers - $rightAnswers")
                logD(TAG, "WrongAnswers - $wrongAnswers")
                logD(TAG, "DropQuestions - $dropQuestions")


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
        fun newInstance(param1: String, param2: String, param3 : String) =
                MultiplayerResultFragment().apply {
                    arguments = Bundle().apply {
                        putString(RIGHT_ANSWERS, param1)
                        putString(WRONG_ANSWERS, param2)
                        putString(DROP_QUESTIONS, param3)
                    }
                }

        const val ACTION_RESULT_RECEIVED = "com.example.mayank.googleplaygame.ACTION_RESULT_RECEIVED"
    }
}

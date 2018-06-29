package com.example.mayank.googleplaygame.multiplay

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.CountDownTimer
import android.support.v4.app.Fragment
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.example.mayank.googleplaygame.Constants.logD
import com.example.mayank.googleplaygame.PlayGameLib

import com.example.mayank.googleplaygame.R
import kotlinx.android.synthetic.main.game_detail_screen.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [GameDetailScreen.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [GameDetailScreen.newInstance] factory method to
 * create an instance of this fragment.
 *
 */
class GameDetailFragment : Fragment(), View.OnClickListener {

    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private var listener: OnFragmentInteractionListener? = null

    private val TAG = GameDetailFragment::class.java.simpleName
    private var i = -1
    private var j:Int = 0
    private var k = -1
    private var l:Int = 0
    private var subject = ""
    private var subCode = ""
    private var amount = ""
    private lateinit var amountList:Array<String>
    private lateinit var subjectList:Array<String>
    private lateinit var subjectCode:Array<String>
    private var playGameLib : PlayGameLib? = null
    private lateinit var textSwitcherCountdown: TextSwitcher
    private var countDownTimer: CountDownTimer? = null
    private lateinit var textViewCount: TextView

    private val CLICKABLES = intArrayOf(R.id.imageButtonNextAmount, R.id.imageButtonNextSubject,
            R.id.imageButtonPreviousAmount, R.id.imageButtonPreviousSubject)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
        // Subjects and Amounts from String.xml
        subjectList = resources.getStringArray(R.array.subjectList)
        subjectCode = resources.getStringArray(R.array.subject_code)
        amountList = resources.getStringArray(R.array.amount)
        playGameLib = PlayGameLib(activity!!)
        context?.registerReceiver(messageBroadcastReceiver, syncIntentFilter);
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment.
        val view = inflater.inflate(R.layout.fragment_game_detail_screen, container, false)
        textSwitcherCountdown = view.findViewById<TextSwitcher>(R.id.textSwitcherCountdown)
        textSwitcherCountdown.setFactory {
            val switcherTextView = TextView(activity)
            switcherTextView.textSize = 16F
            switcherTextView.setTextColor(Color.RED)
            switcherTextView.gravity = Gravity.CENTER
            switcherTextView
        }
        for (id in CLICKABLES){
            view.findViewById<ImageButton>(id).setOnClickListener(this)
        }
        view.findViewById<Button>(R.id.leave_room_button).setOnClickListener(this)
        return view
    }

    override fun onClick(view: View?) {
        when(view?.id){
            R.id.imageButtonNextAmount ->{
                if (k<20){
                    k++
                    l=k
                    amount = amountList[k]
                    textViewAmount.text = amount
                }else {
                    k=0
                    amount = amountList[k]
                    textViewAmount.text = amount
                }
//                playGameLib?.broadcastScore(true)
                playGameLib?.broadcastMessage('A',0)
                resetCountdownTimer(10000,1000)
            }

            R.id.imageButtonPreviousAmount ->{
                if (l>0){
                    l--
                    k=l
                    amount = amountList[l]
                    textViewAmount.text = amount
                }else {
                    l = 20
                    amount = amountList[l]
                    textViewAmount.text = amount
                }
                playGameLib?.broadcastMessage('A',1)
                resetCountdownTimer(10000,1000)

            }
            R.id.imageButtonPreviousSubject ->{
                if (j > 0) {
                    j--
                    i = j
                    subCode = subjectCode[j]
                    subject = subjectList[j]
                    textViewSubject.text = subject
                } else {
                    j = 6
                    subCode = subjectCode[j]
                    subject = subjectList[j]
                    textViewSubject.text = subject
                }
                playGameLib?.broadcastMessage('S',1)
                resetCountdownTimer(10000,1000)
            }

            R.id.imageButtonNextSubject ->{
                if (i < 6) {
                    i++
                    j = i
                    subject = subjectList[i]
                    subCode = subjectCode[i]
                    textViewSubject.text = subject
                } else {
                    i = 0
                    subject = subjectList[i]
                    subCode = subjectCode[i]
                    textViewSubject.text = subject
                }
                resetCountdownTimer(10000,1000)
                playGameLib?.broadcastMessage('S',0)
            }

            R.id.leave_room_button ->{
                playGameLib?.leaveRoom()
            }
        }
    }

    private fun startCountdownTimer(max: Long, min: Long) {
        countDownTimer = object : CountDownTimer(max, min) {
            override fun onTick(millisUntilFinished: Long) {
                var seconds = (millisUntilFinished / 1000).toInt()
                val minutes = seconds / 60
                seconds = seconds % 60
                //                countdownTextView.setText("TIME : " + String.format("%02d", minutes)
                //                        + ":" + String.format("%02d", seconds));
                textViewCount = textSwitcherCountdown.getChildAt(0) as TextView
                //                if(millisUntilFinished < 10001)
                //                    textView.setTextColor(Color.RED);

                textViewCount.text = "Time left : 0" + millisUntilFinished / 1000
            }

            override fun onFinish() {
                //textViewCount.setText("Starting Quiz...");
                //                showAlertDialog(QuickPlayActivity.this,TAG,"Quiz Starting...");
                if (countDownTimer != null) {
                    logD("MyTag", "Cancelled Countdown")
                    countDownTimer!!.cancel()
                }
                textViewCount.text = "Countdown Finished !"
                logD("Alert", "Countdown Finished !")
                val bundle = Bundle()
                bundle.putString("Subject", subject)
                bundle.putString("SubjectCode", subCode)
                bundle.putString("Amount", amount)
//                val quizFragment = SinglePlayerQuizFragment()
//                quizFragment.arguments = bundle
//                playGameLib?.switchToFragment(quizFragment)

            }
        }
        (countDownTimer as CountDownTimer).start()
    }

    private fun resetCountdownTimer(max: Long, min: Long) {
        if (countDownTimer != null) {
            countDownTimer?.cancel()
        }
        startCountdownTimer(max, min)
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

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment GameDetailScreen.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
                GameDetailFragment().apply {
                    arguments = Bundle().apply {
                        putString(ARG_PARAM1, param1)
                        putString(ARG_PARAM2, param2)
                    }
                }

        const val ACTION_FINISHED_SYNC = "com.example.mayank.googleplaygame.ACTION_MESSAGE_RECEIVED"
    }

    private val syncIntentFilter = IntentFilter(ACTION_FINISHED_SYNC)


    private val messageBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (ACTION_FINISHED_SYNC == intent.action) {
                val state = intent.getCharExtra("state", 'Z')
                val value = intent.getIntExtra("value", -1)
                logD(TAG, "State - $state")
                logD(TAG, "Value - $value")
                if (state == 'A'){
                    if (value == 0){
                        k++
                        l=k
                        amount = amountList[k]
                        textViewAmount.text = amount
                    }else if(value == 1){
                        l--
                        k=l
                        amount = amountList[l]
                        textViewAmount.text = amount
                    }
                }

            }
        }
    }


}

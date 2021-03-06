package com.example.mayank.googleplaygame.multiplay

import android.content.*
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.CountDownTimer
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.example.mayank.googleplaygame.Constants
import com.example.mayank.googleplaygame.Constants.logD
import com.example.mayank.googleplaygame.PlayGameApplication
import com.example.mayank.googleplaygame.PlayGameLib

import com.example.mayank.googleplaygame.R
import com.example.mayank.googleplaygame.network.wallet.Itransaction
import com.example.mayank.googleplaygame.network.wallet.Transactions
import com.example.mayank.googleplaygame.wallet.AddPointsFragment
import com.example.mayank.myplaygame.network.ApiClient
import kotlinx.android.synthetic.main.game_detail_screen.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

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
    private var j: Int = 0
    private var k = -1
    private var l: Int = 0
    private var subject : String?=null
    private var subCode : String?= null
    private var amount : String? = null
    private lateinit var amountList: Array<String>
    private lateinit var subjectList: Array<String>
    private lateinit var subjectCode: Array<String>
    private var playGameLib: PlayGameLib? = null
    private lateinit var textSwitcherCountdown: TextSwitcher
    private var countDownTimer: CountDownTimer? = null
    private lateinit var textViewCount: TextView
    private var check: Int = -1

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
        for (id in CLICKABLES) {
            view.findViewById<ImageButton>(id).setOnClickListener(this)
        }
        view.findViewById<Button>(R.id.leave_room_button).setOnClickListener(this)
        return view
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.imageButtonNextAmount -> {
                nextAmount()
//                playGameLib?.broadcastScore(true)
                playGameLib?.broadcastMessage('A', 0)
                resetCountdownTimer(10000, 1000)
            }

            R.id.imageButtonPreviousAmount -> {
                previousAmount()
                playGameLib?.broadcastMessage('A', 1)
                resetCountdownTimer(10000, 1000)

            }
            R.id.imageButtonPreviousSubject -> {
                previousSubject()
                playGameLib?.broadcastMessage('S', 1)
                resetCountdownTimer(10000, 1000)
            }

            R.id.imageButtonNextSubject -> {
                nextSubject()
                resetCountdownTimer(10000, 1000)
                playGameLib?.broadcastMessage('S', 0)
            }

            R.id.leave_room_button -> {
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

                textViewCount.text = "Quiz starts in : 0" + millisUntilFinished / 1000
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
                if (amount==null){
                    Toast.makeText(activity, "Select a valid Amount!", Toast.LENGTH_SHORT).show()
                }else if(subject == null){
                    Toast.makeText(activity, "Select a valid subject!", Toast.LENGTH_SHORT).show()
                }else{
                    checkBalance()
                }
            }
        }
        (countDownTimer as CountDownTimer).start()
    }

    private fun checkBalance() {
        val mobileNumber = PlayGameApplication.sharedPrefs?.getStringPreference(activity!!, Constants.MOBILE_NUMBER)
        if (mobileNumber != null) {
            val apiClient = ApiClient()
            var retrofit = apiClient.getService<Itransaction>()
            retrofit.checkBalance(mobileNumber).enqueue(object : Callback<Transactions> {
                override fun onFailure(call: Call<Transactions>?, t: Throwable?) {
                    logD(TAG, "Error - $t")

                }

                override fun onResponse(call: Call<Transactions>?, response: Response<Transactions>?) {
                    if (response?.isSuccessful!!) {
                        val balance = response.body()?.balance

                        if (amount!! <= balance!!) {
                            val bundle = Bundle()
                            bundle.putString("Subject", subject)
                            bundle.putString("SubjectCode", subCode)
                            bundle.putFloat("Amount", amount?.toFloat()!!)
//                              val quizFragment = SinglePlayerQuizFragment()
//                              quizFragment.arguments = bundle
//                               playGameLib?.switchToFragment(quizFragment)
                            val quizFragment = QuizFragment()
                            quizFragment.arguments = bundle
                            playGameLib?.switchToFragment(quizFragment)
                            unRegisterBroadcastReceiver()
                            if (countDownTimer != null) {
                                countDownTimer?.cancel()
                            }
                        } else {
                            com.example.mayank.googleplaygame.helpers.AlertDialog.alertDialog(activity!!, "Warning", "one of the opponent may have insufficient balance amount !.\nSelect lower amount")
                        }
                    }
                }
            })
        } else {
            logD(TAG, "Mobile number is null")
        }
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

        const val ACTION_MESSAGE_RECEIVED = "com.example.mayank.googleplaygame.ACTION_MESSAGE_RECEIVED"
    }

    private val syncIntentFilter = IntentFilter(ACTION_MESSAGE_RECEIVED)


    private val messageBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (ACTION_MESSAGE_RECEIVED == intent.action) {
                val state = intent.getCharExtra("state", 'Z')
                val value = intent.getIntExtra("value", -1)
                logD(TAG, "State - $state")
                logD(TAG, "Value - $value")
                if (state == 'A') {
                    if (value == 0) {
                        nextAmount()
                    } else if (value == 1) {
                        previousAmount()
                    }
                    resetCountdownTimer(10000, 1000)
                } else if (state == 'S') {
                    if (value == 0) {
                        nextSubject()
                    } else if (value == 1) {
                        previousSubject()
                    }
                    resetCountdownTimer(10000, 1000)
                }
            }
        }
    }

    private fun nextAmount() {
        if (k < 20) {
            k++
            l = k
            amount = amountList[k]
            textViewAmount.text = amount
        } else {
            k = 0
            amount = amountList[k]
            textViewAmount.text = amount
        }
    }

    private fun previousAmount() {
        if (l > 0) {
            l--
            k = l
            amount = amountList[l]
            textViewAmount.text = amount
        } else {
            l = 20
            amount = amountList[l]
            textViewAmount.text = amount
        }
    }

    private fun nextSubject() {
        if (i < 6) {
            i++
            logD(TAG, "value of i : $i")
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
    }

    private fun previousSubject() {
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
    }

    private fun unRegisterBroadcastReceiver() {
        context?.unregisterReceiver(messageBroadcastReceiver)
    }


}

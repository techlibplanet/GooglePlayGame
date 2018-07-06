package com.example.mayank.googleplaygame.wallet

import android.content.Context
import android.content.DialogInterface
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import com.example.mayank.googleplaygame.Constants
import com.example.mayank.googleplaygame.Constants.logD
import com.example.mayank.googleplaygame.PlayGameApplication
import com.example.mayank.googleplaygame.PlayGameLib

import com.example.mayank.googleplaygame.R
import com.example.mayank.googleplaygame.network.wallet.Itransaction
import com.example.mayank.googleplaygame.network.wallet.Transactions
import com.example.mayank.myplaygame.network.ApiClient
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
 * [WithdrawalPointsFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [WithdrawalPointsFragment.newInstance] factory method to
 * create an instance of this fragment.
 *
 */
class WithdrawalPointsFragment : Fragment(), View.OnClickListener {

    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private var listener: OnFragmentInteractionListener? = null

    private val TAG = WithdrawalPointsFragment::class.java.simpleName
    private lateinit var playGameLib: PlayGameLib
    private lateinit var inputAmount : EditText
    private var accountNumber : String? = null
    private var ifscCode : String? = null
    private lateinit var inputAccountNumber: EditText
    private lateinit var inputIfscCode : EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
        playGameLib = PlayGameLib(activity!!)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_withdrawal_points, container, false)
        inputAmount = view.findViewById(R.id.amount_edit_text)
        inputAccountNumber = view.findViewById(R.id.account_number_edit_text)
        inputIfscCode = view.findViewById(R.id.ifsc_code_edit_text)

        accountNumber = PlayGameApplication.sharedPrefs?.getStringPreference(activity!!, Constants.ACCOUNT_NUMBER)
        ifscCode = PlayGameApplication.sharedPrefs?.getStringPreference(activity!!, Constants.IFSC_CODE)
        if (accountNumber == null && ifscCode == null){
            inputAccountNumber.visibility = View.VISIBLE
            inputIfscCode.visibility = View.VISIBLE
        }
        view.findViewById<Button>(R.id.withdrawal_points_button).setOnClickListener(this)
        return view
    }

    override fun onClick(view: View?) {
        when(view?.id){
            R.id.withdrawal_points_button ->{
                withdrawalPoints()
            }
        }
    }

    private fun withdrawalPoints() {
        if (accountNumber == null && ifscCode == null){
            accountNumber = inputAccountNumber.text.toString().trim()
            ifscCode = inputIfscCode.text.toString().trim()
            PlayGameApplication.sharedPrefs?.setStringPreference(activity!!, Constants.ACCOUNT_NUMBER, accountNumber!!)
            PlayGameApplication.sharedPrefs?.setStringPreference(activity!!, Constants.IFSC_CODE, ifscCode!!)
        }
        val amount = inputAmount.text.toString().trim()
        val firstName = PlayGameApplication.sharedPrefs?.getStringPreference(activity!!, Constants.FIRST_NAME)
        val lastName = PlayGameApplication.sharedPrefs?.getStringPreference(activity!!, Constants.LAST_NAME)
        val mobileNumber = PlayGameApplication.sharedPrefs?.getStringPreference(activity!!, Constants.MOBILE_NUMBER)
        val email = PlayGameApplication.sharedPrefs?.getStringPreference(activity!!, Constants.EMAIL)
        logD(TAG, "$firstName $lastName $mobileNumber $email $amount $accountNumber $ifscCode")
        val apiClient = ApiClient()
        var retrofit = apiClient.getService<Itransaction>()


        retrofit.withdrawalPayments("$firstName","$lastName","${playGameLib.getDisplayName()}","$mobileNumber","","","$email",
                "Withdrawal Payments",amount,"${playGameLib.getDisplayName()}${System.currentTimeMillis()}","","${System.currentTimeMillis()}","${System.currentTimeMillis()}",
                "","","Debited","$accountNumber","$ifscCode","processed").enqueue(object : Callback<Transactions>{
            override fun onFailure(call: Call<Transactions>?, t: Throwable?) {
                Log.d(TAG, "Error - $t")
            }

            override fun onResponse(call: Call<Transactions>?, response: Response<Transactions>?) {
                if (response?.isSuccessful!!){
                    Log.d(TAG, "Response - $response")
                    val responseBody = response.body()
                    Log.d(TAG, "Mobile Number - ${responseBody?.mobileNumber}")
                    Log.d(TAG, "Balance - ${responseBody?.balance}")
                    val walletFragment = WalletFragment()
                    playGameLib.switchToFragment(walletFragment)
                    AlertDialog.Builder(activity!!).setTitle("Withdrawal Points")
                            .setMessage("\n\nWithdrawal points successfully!\n Balance : ${responseBody?.balance}")
                            .setNeutralButton("Ok", DialogInterface.OnClickListener { dialog, which ->
                                dialog.dismiss()
                            }).show()
                }
            }

        })
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
         * @return A new instance of fragment WithdrawalPointsFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
                WithdrawalPointsFragment().apply {
                    arguments = Bundle().apply {
                        putString(ARG_PARAM1, param1)
                        putString(ARG_PARAM2, param2)
                    }
                }
    }
}

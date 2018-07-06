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
 * [TransferPointsFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [TransferPointsFragment.newInstance] factory method to
 * create an instance of this fragment.
 *
 */
class TransferPointsFragment : Fragment(), View.OnClickListener {

    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private var listener: OnFragmentInteractionListener? = null
    private val TAG = TransferPointsFragment::class.java.simpleName
    private lateinit var inputAmount : EditText
    private lateinit var amount : String
    private lateinit var inputContact : EditText
    private lateinit var contact : String
    private lateinit var playGameLib: PlayGameLib

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
        val view = inflater.inflate(R.layout.fragment_transfer_points, container, false)
        inputContact = view.findViewById(R.id.contact_edit_text)
        inputAmount = view.findViewById(R.id.amount_edit_text)

        view.findViewById<Button>(R.id.transfer_points_button).setOnClickListener(this)
        return view
    }

    override fun onClick(view: View?) {
        amount = inputAmount.text.toString().trim()
        contact = inputContact.text.toString().trim()
        val firstName = PlayGameApplication.sharedPrefs?.getStringPreference(activity!!, Constants.FIRST_NAME)
        val lastName = PlayGameApplication.sharedPrefs?.getStringPreference(activity!!, Constants.LAST_NAME)
        val mobileNumber = PlayGameApplication.sharedPrefs?.getStringPreference(activity!!, Constants.MOBILE_NUMBER)
        val email = PlayGameApplication.sharedPrefs?.getStringPreference(activity!!, Constants.EMAIL)

        val apiClient = ApiClient()
        var retrofit = apiClient.getService<Itransaction>()
        retrofit.transferPoints("$firstName", "$lastName", "${PlayGameLib.GameConstants.displayName}", "$mobileNumber", "$contact", "", "$email","Transfer Points",amount, "", "",
                "${System.currentTimeMillis()}", "${System.currentTimeMillis()}", "-", "-", "Debited","success").enqueue(object : Callback<Transactions> {
            override fun onFailure(call: Call<Transactions>?, t: Throwable?) {
                Log.d(TAG, "Error - $t")
            }

            override fun onResponse(call: Call<Transactions>?, response: Response<Transactions>?) {
                if (response?.isSuccessful!!){
                    Log.d(TAG, "Response - $response")
                    val responseBody = response.body()
                    Log.d(TAG, "MobileNumber - ${responseBody?.mobileNumber}")
                    Log.d(TAG, "Total Balance - ${responseBody?.balance}")
                    val walletFragment = WalletFragment()
                    playGameLib.switchToFragment(walletFragment)
                    AlertDialog.Builder(activity!!).setTitle("Transfer Points")
                            .setMessage("\n\nTransfer points successfully!\n Balance : ${responseBody?.balance}")
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
         * @return A new instance of fragment TransferPointsFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
                TransferPointsFragment().apply {
                    arguments = Bundle().apply {
                        putString(ARG_PARAM1, param1)
                        putString(ARG_PARAM2, param2)
                    }
                }
    }
}

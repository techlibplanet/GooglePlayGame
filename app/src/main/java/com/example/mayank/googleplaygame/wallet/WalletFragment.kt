package com.example.mayank.googleplaygame.wallet

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import com.example.mayank.googleplaygame.Constants
import com.example.mayank.googleplaygame.Constants.logD
import com.example.mayank.googleplaygame.PlayGameApplication
import com.example.mayank.googleplaygame.PlayGameLib

import com.example.mayank.googleplaygame.R
import com.example.mayank.googleplaygame.helpers.AlertDialog
import com.example.mayank.googleplaygame.network.wallet.Itransaction
import com.example.mayank.googleplaygame.network.wallet.Transactions
import com.example.mayank.myplaygame.network.ApiClient
import org.w3c.dom.Text
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
 * [WalletFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [WalletFragment.newInstance] factory method to
 * create an instance of this fragment.
 *
 */
class WalletFragment : Fragment(), View.OnClickListener {

    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private var listener: OnFragmentInteractionListener? = null

    private val TAG = WalletFragment::class.java.simpleName
    private val CLICKABLES = intArrayOf(R.id.add_points_button, R.id.withdrawal_points_button, R.id.transfer_points_button)
    private var playGameLib : PlayGameLib? =null
    private lateinit var balanceTextView : TextView

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
        val view = inflater.inflate(R.layout.fragment_wallet, container, false)
        balanceTextView = view.findViewById(R.id.balance_text_view)

        checkBalance()

        for (id in CLICKABLES){
            view.findViewById<Button>(id).setOnClickListener(this)
        }
        return view
    }

    private fun checkBalance() {
        val mobileNumber = PlayGameApplication.sharedPrefs?.getStringPreference(activity!!, Constants.MOBILE_NUMBER)
        if (mobileNumber!=null){
            val apiClient = ApiClient()
            var retrofit = apiClient.getService<Itransaction>()
            retrofit.checkBalance(mobileNumber).enqueue(object : Callback<Transactions>{
                override fun onFailure(call: Call<Transactions>?, t: Throwable?) {
                    logD(TAG, "Error - $t")
                }

                override fun onResponse(call: Call<Transactions>?, response: Response<Transactions>?) {
                    if (response?.isSuccessful!!){
                        val balance = response.body()?.balance
                        balanceTextView.text = "Points : $balance"
                    }else {
                        logD(TAG, "${response.body()?.error}")
                        AlertDialog.alertDialog(activity!!, "Error", "${response.body()?.error}")
                    }
                }

            })
        }else {
            balanceTextView.visibility = View.GONE
        }
    }

    override fun onClick(view: View?) {
        when(view?.id){
            R.id.add_points_button ->{
                val addPointsFragment = AddPointsFragment()
                playGameLib?.switchToFragment(addPointsFragment)
            }

            R.id.withdrawal_points_button ->{
                val withdrawalPointsFragment = WithdrawalPointsFragment()
                playGameLib?.switchToFragment(withdrawalPointsFragment)
            }

            R.id.transfer_points_button ->{
                val transferPointsFragment = TransferPointsFragment()
                playGameLib?.switchToFragment(transferPointsFragment)
            }
        }
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
         * @return A new instance of fragment WalletFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
                WalletFragment().apply {
                    arguments = Bundle().apply {
                        putString(ARG_PARAM1, param1)
                        putString(ARG_PARAM2, param2)
                    }
                }
    }
}

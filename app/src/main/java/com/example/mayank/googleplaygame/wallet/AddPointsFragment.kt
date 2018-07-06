package com.example.mayank.googleplaygame.wallet

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import com.example.mayank.googleplaygame.Constants
import com.example.mayank.googleplaygame.Constants.logD
import com.example.mayank.googleplaygame.PayUMoney
import com.example.mayank.googleplaygame.PlayGameApplication

import com.example.mayank.googleplaygame.R
import com.payumoney.core.entity.TransactionResponse
import com.payumoney.sdkui.ui.utils.PayUmoneyFlowManager
import com.payumoney.sdkui.ui.utils.ResultModel
import org.json.JSONObject

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [AddPointsFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [AddPointsFragment.newInstance] factory method to
 * create an instance of this fragment.
 *
 */
class AddPointsFragment : Fragment(), View.OnClickListener {


    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private var listener: OnFragmentInteractionListener? = null

    private val TAG = AddPointsFragment::class.java.simpleName
    private var email :String ? = null
    private var mobileNumber : String ?= null
    private var amount : String? = null

    private lateinit var inputAmount: EditText
    private lateinit var inputEmail : EditText
    private lateinit var inputMobileNumber :EditText

    private lateinit var payUMoney: PayUMoney

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
        payUMoney = PayUMoney(activity!!)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_add_points, container, false)

        view.findViewById<Button>(R.id.add_points_button).setOnClickListener(this)
        inputAmount = view.findViewById(R.id.amount_edit_text)
        inputEmail = view.findViewById(R.id.email_edit_text)
        inputMobileNumber = view.findViewById(R.id.mobile_number_edit_text)
        email = PlayGameApplication.sharedPrefs?.getStringPreference(activity!!,Constants.EMAIL)
        mobileNumber = PlayGameApplication.sharedPrefs?.getStringPreference(activity!!, Constants.MOBILE_NUMBER)
        if (email==null && mobileNumber == null){
            inputEmail.visibility = View.VISIBLE
            inputMobileNumber.visibility = View.VISIBLE
        }
        return view
    }

    override fun onClick(view: View?) {
        when(view?.id){
            R.id.add_points_button ->{
                if (email == null && mobileNumber == null){
                    email = inputEmail.text.toString().trim()
                    mobileNumber = inputMobileNumber.text.toString().trim()
                    PlayGameApplication.sharedPrefs?.setStringPreference(activity!!, Constants.EMAIL, email!!)
                    PlayGameApplication.sharedPrefs?.setStringPreference(activity!!, Constants.MOBILE_NUMBER, mobileNumber!!)
                }
                amount = inputAmount.text.toString().trim()
                val firstName = PlayGameApplication.sharedPrefs?.getStringPreference(activity!!, Constants.FIRST_NAME)

                logD(TAG, "Amount - $amount")
                logD(TAG, "First name - $firstName")
                logD(TAG, "Email - $email")
                logD(TAG, "Mobile Number -$mobileNumber")
                logD(TAG, "Product - ${Constants.PRODUCT_RECHARGE_POINTS}")
                payUMoney.launchPayUMoney(amount?.toDouble()!!,firstName!!,mobileNumber!!,email!!, Constants.PRODUCT_RECHARGE_POINTS)
            }
        }
    }




    private fun validate(){
        // Do this later
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
         * @return A new instance of fragment AddPointsFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
                AddPointsFragment().apply {
                    arguments = Bundle().apply {
                        putString(ARG_PARAM1, param1)
                        putString(ARG_PARAM2, param2)
                    }
                }
    }
}

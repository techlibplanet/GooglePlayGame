package com.example.mayank.googleplaygame

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.games.Games
import com.google.android.gms.games.InvitationsClient
import com.google.android.gms.games.RealTimeMultiplayerClient


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [LoginFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [LoginFragment.newInstance] factory method to
 * create an instance of this fragment.
 *
 */
class LoginFragment : Fragment(), View.OnClickListener {

    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private var listener: OnFragmentInteractionListener? = null

    private val TAG = LoginFragment::class.java.simpleName
    private val RC_SIGN_IN = 100

    internal var mSignedInAccount: GoogleSignInAccount? = null
    // Client used to interact with the real time multiplayer system.
    private var mRealTimeMultiplayerClient: RealTimeMultiplayerClient? = null
    //Client used to interact with the Invitation system.
    private var mInvitationsClient: InvitationsClient? = null
    private var mPlayerId: String? = null
    private lateinit var playGameLib: PlayGameLib


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_login, container, false)
        view.findViewById<SignInButton>(R.id.sign_in_button).setOnClickListener(this)
        view.findViewById<Button>(R.id.sign_out_button).setOnClickListener(this)
        return view
    }

    override fun onClick(view: View?) {
        if (view?.id == R.id.sign_in_button) {
            startSignInIntent();
        }

        if (view?.id == R.id.sign_out_button) {
            signOut()
        }
    }

    private fun signOut() {
        if (isSignedIn()) {
            val signInClient = GoogleSignIn.getClient(activity!!,
                    GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN)
            signInClient.signOut().addOnCompleteListener(activity!!
            ) {
                // at this point, the user is signed out.
                Constants.logD(TAG, "Sign Out Successfully !")
                val delete = PlayGameApplication.sharedPrefs?.deletePreferences(activity!!)
                if (delete!!) {
                    Constants.logD(TAG, "Shared Preferences deleted successfully")
                } else {
                    Constants.logD(TAG, "Unable to delete preferences.")
                }
            }
        } else {
            Constants.logD(TAG, "Already sign out !")
        }

    }

    private fun isSignedIn(): Boolean {
        return GoogleSignIn.getLastSignedInAccount(activity) != null
    }


    private fun startSignInIntent() {
        val signInClient = GoogleSignIn.getClient(activity!!,
                GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN)

        val intent = signInClient.signInIntent
        startActivityForResult(intent, RC_SIGN_IN)
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
         * @return A new instance of fragment LoginFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
                LoginFragment().apply {
                    arguments = Bundle().apply {
                        putString(ARG_PARAM1, param1)
                        putString(ARG_PARAM2, param2)
                    }
                }
    }
}

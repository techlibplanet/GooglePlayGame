package com.example.mayank.googleplaygame.multiplay

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.example.mayank.googleplaygame.Constants.logD
import com.example.mayank.googleplaygame.PlayGameLib

import com.example.mayank.googleplaygame.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.games.Games
import kotlinx.android.synthetic.main.activity_multi_player.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [GameMenuFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [GameMenuFragment.newInstance] factory method to
 * create an instance of this fragment.
 *
 */
class GameMenuFragment : Fragment(), View.OnClickListener {

    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private var listener: OnFragmentInteractionListener? = null
    private var playGameLib : PlayGameLib? = null

    private val CLICKABLES = intArrayOf(R.id.quick_game, R.id.multi_play_button, R.id.show_invitation_button)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
        playGameLib = PlayGameLib(activity!!)

//        PlayGameLib.GameConstants.mInvitationClient.registerInvitationCallback()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_game_menu, container, false)
        for (id in CLICKABLES){
            view.findViewById<Button>(id).setOnClickListener(this)
        }
        return view
    }

    override fun onClick(view: View?) {
        when(view?.id){
            R.id.quick_game ->{
                playGameLib?.startQuickGame()
            }
            R.id.multi_play_button ->{
                playGameLib?.invitePlayers()
            }
            R.id.show_invitation_button ->{
                showInvitationInbox()
            }
        }
    }

    private fun showInvitationInbox() {
        playGameLib?.showInvitationInbox()

    }

//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        logD("GameMenuFragment", "Inside on activity result")
//        playGameLib?.onActivityResult(requestCode, resultCode, data)
//        //super.onActivityResult(requestCode, resultCode, data)
//    }

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
         * @return A new instance of fragment GameMenuFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
                GameMenuFragment().apply {
                    arguments = Bundle().apply {
                        putString(ARG_PARAM1, param1)
                        putString(ARG_PARAM2, param2)
                    }
                }
    }
}

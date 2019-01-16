package com.chazo.personal.two.cleanarchitecturestudy.controller.auth


import android.Manifest
import android.accounts.AccountManager
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController

import com.chazo.personal.two.cleanarchitecturestudy.R
import com.chazo.personal.two.cleanarchitecturestudy.constant.RC_ACCOUNT_PICKER
import com.chazo.personal.two.cleanarchitecturestudy.constant.RP_GET_ACCOUNTS
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import dagger.android.support.DaggerFragment
import kotlinx.android.synthetic.main.fragment_auth.*
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.EasyPermissions
import javax.inject.Inject

class AuthFragment : DaggerFragment() {

    @Inject
    lateinit var googleAccountCredential: GoogleAccountCredential

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_auth, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        button_sign_in.setOnClickListener { selectAccount() }
    }

    @AfterPermissionGranted(RP_GET_ACCOUNTS)
    private fun selectAccount() {
        val perms = arrayOf(Manifest.permission.GET_ACCOUNTS)
        if (EasyPermissions.hasPermissions(requireContext(), *perms)) {
            startActivityForResult(googleAccountCredential.newChooseAccountIntent(), RC_ACCOUNT_PICKER)
        } else {
            requestPermission(perms)
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_ACCOUNT_PICKER && resultCode == Activity.RESULT_OK) {
            data?.let {
                val accountName = it.getStringExtra(AccountManager.KEY_ACCOUNT_NAME)
                accountName?.let {
                    googleAccountCredential.selectedAccountName = it
                    moveToCalendarSelectFragment()
                }?: run {
                    Toast.makeText(requireContext(), "선택된 계정이 없습니다.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun moveToCalendarSelectFragment() {
        AuthFragmentDirections.actionDestAuthToDestCalendarSelect().apply {
            findNavController().navigate(this)
        }
//        Navigation.createNavigateOnClickListener(R.id.action_dest_auth_to_dest_calendar_select)
    }

    private fun requestPermission(permissions: Array<String>) =
            EasyPermissions.requestPermissions(
                    this,
                    "구글 계정을 가져오기 위해 필요합니다.",
                    RP_GET_ACCOUNTS, *permissions
            )

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

}

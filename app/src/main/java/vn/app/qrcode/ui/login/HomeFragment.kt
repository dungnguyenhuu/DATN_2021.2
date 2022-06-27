package vn.app.qrcode.ui.login

import android.content.Intent
import android.os.Bundle
import android.text.Spannable
import android.text.TextUtils
import android.text.style.UnderlineSpan
import android.view.View
import androidx.core.text.toSpannable
import androidx.databinding.Observable
import androidx.navigation.fragment.findNavController
import com.base.common.base.fragment.BaseMVVMFragment
import com.base.common.data.result.ErrorApi
import com.base.common.utils.ext.setDebounceClickListener
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.common.api.ApiException
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber
import vn.app.qrcode.R
import vn.app.qrcode.databinding.FragmentHomeBinding

class HomeFragment : BaseMVVMFragment<LoginEvent, FragmentHomeBinding, LoginViewModel>() {

    companion object {
        const val TAG = "LoginFragment"
    }

    override val layoutId: Int
        get() = R.layout.fragment_home
    override val viewModel: LoginViewModel by viewModel()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViewEvent()
    }

    private fun setupViewEvent() {
        viewDataBinding.apply {
            btnLoginGoogle.setDebounceClickListener {
                activity?.let {
                    viewModel.requestGoogleLogin(this@HomeFragment)
                }
            }

            btnLogin.setDebounceClickListener {
                viewModel.revokeAccessIfNeeded()
            }

            btnSkip.setDebounceClickListener{
                findNavController().navigate(R.id.action_login_to_skipAuth)
            }

            tvForgot.apply {
                val span = text.toSpannable()
                val inputText = getString(R.string.forgotCredentialText)
                val underlineText = getString(R.string.forgotCredentialTextUnderline)
                span.setSpan(
                    UnderlineSpan(),
                    inputText.indexOf(underlineText),
                    inputText.indexOf(underlineText) + underlineText.length,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                text = span
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == LoginViewModel.REQUEST_LOGIN_CODE) {
            viewModel.loadingStatus.value = false
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)!!
                updateDataAccount(account)
                Timber.d("firebaseAuthWithGoogle:" + account.id)
            } catch (e: ApiException) {
                // Google Sign In failed, update UI appropriately
                viewModel.revokeAccessIfNeeded()
                viewModel.errorResponse.postValue(
                    ErrorApi(
                        getString(R.string.can_not_login_google),
                        ""
                    )
                )
                Timber.d(e, "Google sign in failed")
            }
        }
    }

    private fun updateDataAccount(account: GoogleSignInAccount) {
        if (!TextUtils.isEmpty(account.id)) {
            viewModel.saveUserCredential(account)
        } else viewModel.errorResponse.postValue(
            ErrorApi(
                getString(R.string.can_not_login_google),
                ""
            )
        )
    }

    override fun onReceiverMessage(sender: Observable?, event: LoginEvent) {
        super.onReceiverMessage(sender, event)
        if (event is LoginEvent.LoginSuccess)
            findNavController().navigate(R.id.action_login_to_dashBoard)
    }
}
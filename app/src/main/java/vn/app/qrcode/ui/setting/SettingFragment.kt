package vn.app.qrcode.ui.setting

import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.Voice
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.base.common.base.fragment.BaseMVVMFragment
import com.base.common.base.viewmodel.CommonEvent
import java.util.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import vn.app.qrcode.R
import vn.app.qrcode.databinding.FragmentSettingBinding
import vn.app.qrcode.utils.sharepref.SharedPreUtils

class SettingFragment : BaseMVVMFragment<CommonEvent, FragmentSettingBinding, SettingViewModel>(), TextToSpeech.OnInitListener {
    override val layoutId: Int
        get() = R.layout.fragment_setting
    override val viewModel: SettingViewModel by viewModel()
    companion object {
        fun newInstance() = SettingFragment()
    }
    private var tts: TextToSpeech? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tts = TextToSpeech(activity, this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewDataBinding.radioGroupVoid.setOnCheckedChangeListener { group, checkedId ->
            when(checkedId) {
                R.id.radioButton_voice_1 -> {
                    SharedPreUtils.putString("choose_voice", "vi-vn-x-gft-network")
                    speakVoice("vi-vn-x-gft-network")
                }
                R.id.radioButton_voice_2 -> {
                    SharedPreUtils.putString("choose_voice", "vi-vn-x-vic-local")
                    speakVoice("vi-vn-x-vic-local")
                }
                R.id.radioButton_voice_3 -> {
                    SharedPreUtils.putString("choose_voice", "vi-vn-x-vid-network")
                    speakVoice("vi-vn-x-vid-network")
                }
                R.id.radioButton_voice_4 -> {
                    SharedPreUtils.putString("choose_voice", "vi-vn-x-vif-local")
                    speakVoice("vi-vn-x-vif-local")
                }
            }
        }
    }

    private fun speakVoice(voiceName: String) {
        val a: MutableSet<String> = HashSet()
        a.add("male") //here you can give male if you want to select male voice.

        val v = Voice(voiceName, Locale("vi_VN"), 400, 200, true, a)

        val resultSetVoice: Int =  tts!!.setVoice(v)
        println("AAA resultSetVoice $resultSetVoice")
        tts!!.speak("Xin chào các bạn", TextToSpeech.QUEUE_FLUSH, null,"")
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts!!.setLanguage(Locale("vi_VN"))

            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS","The Language not supported!")
            }
        }
    }
}
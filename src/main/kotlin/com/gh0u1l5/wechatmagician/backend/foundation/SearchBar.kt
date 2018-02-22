package com.gh0u1l5.wechatmagician.backend.foundation

import android.content.Context.INPUT_METHOD_SERVICE
import android.text.Editable
import android.text.TextWatcher
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import com.gh0u1l5.wechatmagician.Global.STATUS_FLAG_COMMAND
import com.gh0u1l5.wechatmagician.backend.WechatPackage
import com.gh0u1l5.wechatmagician.backend.foundation.base.EventCenter
import com.gh0u1l5.wechatmagician.backend.interfaces.ISearchBarConsole
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge.hookAllConstructors

object SearchBar : EventCenter() {

    private val pkg = WechatPackage

    @JvmStatic fun hookEvents() {
        hookAllConstructors(pkg.ActionBarEditText, object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) {
                val search = param.thisObject as EditText
                search.addTextChangedListener(object : TextWatcher {
                    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit

                    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit

                    override fun afterTextChanged(editable: Editable?) {
                        val command = editable.toString()
                        if (!command.endsWith("#")) {
                            return
                        }
                        notifyParallel("onHandleCommand") { plugin ->
                            if (plugin is ISearchBarConsole) {
                                val consumed = plugin.onHandleCommand(search.context, command.drop(1).dropLast(1))
                                if (consumed) {
                                    val imm = search.context.getSystemService(INPUT_METHOD_SERVICE)
                                    (imm as InputMethodManager).hideSoftInputFromWindow(search.windowToken, 0)
                                    editable?.clear()
                                }
                            }
                        }
                    }
                })
            }
        })

        pkg.setStatus(STATUS_FLAG_COMMAND, true)
    }
}
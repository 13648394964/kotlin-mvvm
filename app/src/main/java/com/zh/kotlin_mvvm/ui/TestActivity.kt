package com.zh.kotlin_mvvm.ui

import android.os.Bundle
import android.widget.Toast
import androidx.databinding.ViewDataBinding
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.PhoneUtils
import com.blankj.utilcode.util.ToastUtils
import com.zh.common.base.BaseActivity
import com.zh.common.base.viewmodel.NormalViewModel
import com.zh.kotlin_mvvm.R
import kotlinx.coroutines.*
import kotlin.concurrent.thread

class TestActivity(
    override val layoutRes: Int = R.layout.activity_test,
    override val viewModel: NormalViewModel = NormalViewModel()
) : BaseActivity<ViewDataBinding, NormalViewModel>() {

    override fun initView(savedInstanceState: Bundle?) {

        GlobalScope.apply {
            val launch = launch {
                LogUtils.a("--" + Thread.currentThread().name)
                launch(Dispatchers.Main) {
                    Toast.makeText(this@TestActivity, "你好啊", Toast.LENGTH_SHORT).show()
                }
            }
        }
       /* GlobalScope.launch {
            LogUtils.a("111111111111111-" + Thread.currentThread().name)
            delay(2000)
            LogUtils.a("222222222222222-" + Thread.currentThread().name)
//            testJoin()
        }
        LogUtils.a("3333333333333333-" + Thread.currentThread().name)*/
//      test()
    }

    override fun initData() {
//        thread()
    }

    private fun thread() {
        thread {
            LogUtils.a("aaaaaaaaa-" + Thread.currentThread().name)
            Thread.sleep(2000)
            LogUtils.a("bbbbbbbbb-" + Thread.currentThread().name)
        }
        LogUtils.a("ccccccccc-" + Thread.currentThread().name)
    }

    private fun test() = runBlocking {
        GlobalScope.launch {
            LogUtils.a("进入阻塞方法里面-" + Thread.currentThread().name)
            delay(5000)
            LogUtils.a("进入阻塞方法里面5000-" + Thread.currentThread().name)
        }
        LogUtils.a("进入阻塞方法外面-" + Thread.currentThread().name)
        delay(2000)
    }

    private suspend fun testJoin() {
        val joo = GlobalScope.launch {
            delay(500)
            LogUtils.a("你好" + Thread.currentThread().name)
        }
        LogUtils.a("小华" + Thread.currentThread().name)
        joo.cancelAndJoin()
        joo.join()
        LogUtils.a("帅" + Thread.currentThread().name)
    }
}
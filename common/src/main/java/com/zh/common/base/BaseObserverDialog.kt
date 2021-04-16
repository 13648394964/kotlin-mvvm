package com.zh.common.base

import android.content.Context
import android.util.Log
import com.blankj.utilcode.util.ActivityUtils
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.NetworkUtils
import com.blankj.utilcode.util.ToastUtils
import com.zh.common.base.bean.BaseResponse
import com.zh.common.exception.ApiException
import com.zh.common.exception.ERROR
import com.zh.common.view.dialog.LoadingDialog
import io.reactivex.Observer
import io.reactivex.disposables.Disposable

/**
 * 自定义Dialog的Subscriber
 */
abstract class BaseObserverDialog<T> : Observer<T> {
    private var impl: BaseImpl
    private val context: Context = ActivityUtils.getTopActivity()
    private var loadingDialog: LoadingDialog? = null
    private var isShowLoading = false //是否显示加载进度对话框

    constructor(impl: BaseImpl) {
        this.impl = impl
    }

    constructor(impl: BaseImpl, isShowLoading: Boolean) {
        this.impl = impl
        this.isShowLoading = isShowLoading
        if (isShowLoading) getLoadingDialog()
    }

    override fun onSubscribe(d: Disposable) {
        impl.disposable.add(d)

        if (!NetworkUtils.isConnected()) {
            ToastUtils.showShort("网络异常")
            d.dispose()
            onComplete() //一定要手动调用
            return
        }

        // 显示进度条
        if (isShowLoading) showLoading()
    }

    override fun onNext(response: T) {
        val result: BaseResponse<T> = response as BaseResponse<T>
        onISuccess(result.msg, response)
    }

    override fun onError(e: Throwable) {
        Log.e("--okhttp--", "onError : " + e.message)
        if (e is ApiException) {
            onIError(e)
        } else {
            onIError(ApiException(e, ERROR.UNKNOWN))
        }

        //关闭等待进度条
        if (isShowLoading) dismissLoading()
    }

    override fun onComplete() {
        //关闭等待进度条
        if (isShowLoading) dismissLoading()
    }

    protected abstract fun onISuccess(message: String, response: T)
    protected abstract fun onIError(e: ApiException)

    private fun getLoadingDialog() {
        loadingDialog ?: also { loadingDialog = LoadingDialog(context) }
    }

    /**
     * 显示加载dialog
     */
    private fun showLoading() {
        try {
            getLoadingDialog()
            loadingDialog?.show()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 结束dialog
     */
    private fun dismissLoading() {
        try {
            loadingDialog?.let { if (it.isShowing) it.dismiss() }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
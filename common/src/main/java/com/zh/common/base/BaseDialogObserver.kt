package com.zh.common.base

import android.content.Context
import com.zh.common.exception.ApiException
import com.zh.common.exception.ERROR
import com.zh.common.utils.LogUtil
import com.zh.common.utils.NetworkUtil
import com.zh.common.utils.ToastUtils
import com.zh.common.view.dialog.LoadingDialog
import io.reactivex.Observer
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

/**
 * 自定义Dialog的Subscriber
 */
abstract class BaseDialogObserver<T> : Observer<T> {
    private var context: Context
    private var loadingDialog: LoadingDialog? = null
    private var isShowLoadingDialog = false //是否显示加载进度对话框
    private var disposable: CompositeDisposable

    constructor(context: Context, disposable: CompositeDisposable) {
        this.context = context
        this.disposable = disposable
    }

    constructor(context: Context, disposable: CompositeDisposable, isShowLoadingDialog: Boolean) {
        this.context = context
        this.disposable = disposable
        this.isShowLoadingDialog = isShowLoadingDialog
        if (isShowLoadingDialog) getLoadingDialog()
    }

    override fun onSubscribe(d: Disposable) {
        LogUtil.d("ThomasDebug", "BaseObserver : Http is start")
        disposable.add(d)

        if (!NetworkUtil.isNetworkConnected(context)) {
            ToastUtils.showMessage("网络异常")
            onComplete() //一定要手动调用
        }

        // 显示进度条
        if (isShowLoadingDialog) showLoading()
    }

    override fun onNext(response: T) {
        onISuccess(response)
    }

    override fun onError(e: Throwable) {
        LogUtil.d("BaseObserver", "onError : " + e.message)
        if (e is ApiException) {
            onIError(e)
        } else {
            onIError(ApiException(e, ERROR.UNKNOWN))
        }

        //关闭等待进度条
        if (isShowLoadingDialog) dismissLoading()
    }

    override fun onComplete() {
        LogUtil.d("BaseObserver", "onCompleted : Http is complete")

        //关闭等待进度条
        if (isShowLoadingDialog) dismissLoading()
    }

    protected abstract fun onISuccess(response: T)
    protected abstract fun onIError(e: ApiException)

    fun getLoadingDialog(): LoadingDialog? {
        if (loadingDialog == null) {
            loadingDialog = LoadingDialog(context)
        }
        return loadingDialog
    }

    /**
     * 显示加载dialog
     */
    fun showLoading() {
        if (loadingDialog == null) {
            loadingDialog = LoadingDialog(context)
        }
        loadingDialog!!.show()
    }

    /**
     * 结束dialog
     */
    fun dismissLoading() {
        if (loadingDialog != null && loadingDialog!!.isShowing) {
            loadingDialog!!.dismiss()
        }
    }
}
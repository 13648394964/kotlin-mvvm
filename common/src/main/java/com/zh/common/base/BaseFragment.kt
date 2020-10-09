package com.zh.common.base

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.alibaba.android.arouter.core.LogisticsCenter
import com.alibaba.android.arouter.launcher.ARouter
import com.gyf.immersionbar.ImmersionBar
import com.trello.rxlifecycle2.components.support.RxFragment
import com.zh.common.R
import com.zh.common.base.factory.ViewModelFactory
import com.zh.common.base.viewmodel.BaseViewModel
import com.zh.common.immersion.ImmersionOwner
import com.zh.common.immersion.ImmersionProxy
import com.zh.config.ZjConfig
import me.jessyan.autosize.internal.CustomAdapt

/**
 * @auth xiaohua
 * @time 2020/10/7 - 15:21
 * @desc Fragment基类，MVVM架构
 */
abstract class BaseFragment<BINDING : ViewDataBinding, VM : BaseViewModel<*>> : RxFragment(),
    CustomAdapt, ImmersionOwner {

    private lateinit var binding: BINDING
    private var mViewModel: VM? = null
    private var viewModelId = 0
    private val minDelayTime = 500 // 两次点击间隔不能少于500ms
    private var lastClickTime: Long = 0
    private var rootView: View? = null
    //ImmersionBar代理类
    private val mImmersionProxy = ImmersionProxy(this)

    override fun initImmersionBar() {
        ImmersionBar.with(this).let {
            it.statusBarColor(R.color.colorPrimary) //状态栏颜色
            //状态栏为淡色statusBarDarkFont要设置为true
            it.statusBarDarkFont(false) //状态栏字体是深色，不写默认为亮色
            it.keyboardEnable(true) //解决软键盘与底部输入框冲突问题，默认为false
            it.init()
        }
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        mImmersionProxy.isUserVisibleHint = isVisibleToUser
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mImmersionProxy.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        ARouter.getInstance().inject(this)
        if (null == rootView) { //如果缓存中有rootView则直接使用
            initViewDataBinding(inflater, container)
            this.rootView = binding.root;
            //在OnCreate方法中调用下面方法，然后再使用线程，就能在uncaughtException方法中捕获到异常
            if (isAdded) {
                initView(savedInstanceState)
                initData()
            }
        } else {
            rootView?.let {
                it.parent?.let { it2 -> (it2 as ViewGroup).removeView(it) }
            }
        }
        return rootView
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        mImmersionProxy.onActivityCreated(savedInstanceState)
    }

    private fun initViewDataBinding(inflater: LayoutInflater, container: ViewGroup?) {
        if (layoutRes != 0) binding = DataBindingUtil.inflate<BINDING>(inflater, layoutRes, container, false)
        mViewModel = ViewModelProvider(this, ViewModelFactory(viewModel()))[viewModel()::class.java]
        viewModelId = onBindVariableId
        //允许设置变量的值而不反映
        binding?.let { binding.setVariable(viewModelId, mViewModel) }
        //让ViewModel拥有View的生命周期感应
        mViewModel?.let { lifecycle.addObserver(it) }
        //支持LiveData绑定xml，数据改变，UI自动会更新
        binding.lifecycleOwner = this
    }

    @get:LayoutRes
    abstract val layoutRes: Int
    abstract fun viewModel(): VM
    abstract val onBindVariableId: Int
    abstract fun initView(savedInstanceState: Bundle?)
    abstract fun initData()

    //今日头条适配方案
    override fun isBaseOnWidth(): Boolean = true
    override fun getSizeInDp(): Float = ZjConfig.screenWidth
    open fun getRootView(): View? = rootView

    override fun onResume() {
        super.onResume()
        mImmersionProxy.onResume()
    }

    override fun onPause() {
        super.onPause()
        mImmersionProxy.onPause()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        //为rootView做缓存，在viewpager中使用fragment时可以提升切换流畅度
        rootView?.let {
            it.parent?.let { it2 -> (it2 as ViewGroup).removeView(it) }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mImmersionProxy.onDestroy()
        binding?.let {
            it.unbind()
        }
        mViewModel?.let { lifecycle.removeObserver(it) }
    }

    /**
     * 页面跳转
     *
     * @param url 对应组建的名称  (“/mine/setting”)
     */
    open fun startActivity(url: String): Fragment {
        return ARouter.getInstance().build(url).navigation() as Fragment
    }

    /**
     * 携带数据的页面跳转
     *
     * @param url 对应组建的名称  (“/mine/setting”)
     */
    open fun startActivity(url: String, bundle: Bundle): Fragment {
        return ARouter.getInstance().build(url).with(bundle).navigation() as Fragment
    }

    /**
     * 携带数据的页面跳转
     *
     * @param url 对应组建的名称  (“/mine/setting”)
     * 第二个参数则是RequestCode
     */
    open fun startActivityForResult(url: String, bundle: Bundle, type: Int) {
        val intent = Intent(context, getDestination(url))
        intent.putExtras(bundle)
        startActivityForResult(intent, type)
    }

    /**
     * 页面跳转 - 清楚该类之前的所有activity
     *
     * @param url 对应组建的名称  (“/mine/setting”)
     */
    open fun startActivityNewTask(url: String): Fragment {
        return ARouter.getInstance().build(url)
            .withFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            .navigation() as Fragment
    }

    /**
     * 携带数据的页面跳转 - 清楚该类之前的所有activity
     *
     * @param url 对应组建的名称  (“/mine/setting”)
     */
    open fun startActivityNewTask(url: String, bundle: Bundle): Fragment {
        return ARouter.getInstance().build(url).with(bundle)
            .withFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            .navigation() as Fragment
    }

    /**
     * 由于ARouter不支持Fragment startActivityForResult(),需要获取跳转的Class
     * 根据路径获取具体要跳转的class
     */
    private fun getDestination(url: String): Class<*> {
        val postcard = ARouter.getInstance().build(url)
        LogisticsCenter.completion(postcard)
        return postcard.destination
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        mImmersionProxy.onHiddenChanged(hidden)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        mImmersionProxy.onConfigurationChanged(newConfig)
    }

    /**
     * 懒加载，在view初始化完成之前执行
     * On lazy after view.
     */
    override fun onLazyBeforeView() {}

    /**
     * 懒加载，在view初始化完成之后执行
     * On lazy before view.
     */
    override fun onLazyAfterView() {}

    /**
     * Fragment用户可见时候调用
     * On visible.
     */
    override fun onVisible() {}

    /**
     * Fragment用户不可见时候调用
     * On invisible.
     */
    override fun onInvisible() {}

    /**
     * 是否可以实现沉浸式，当为true的时候才可以执行initImmersionBar方法
     * Immersion bar enabled boolean.
     *
     * @return the boolean
     */
    override fun immersionBarEnabled(): Boolean = true

    /**
     * 防连点
     */
    open fun isFastClick(): Boolean {
        var flag = true
        val currentClickTime = System.currentTimeMillis()
        if (currentClickTime - lastClickTime >= minDelayTime) {
            flag = false
        }
        lastClickTime = currentClickTime
        return flag
    }
}
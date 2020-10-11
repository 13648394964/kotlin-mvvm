package com.zh.kotlin_mvvm.mvvm.model

import com.zh.common.base.BaseMapToBody
import com.zh.common.base.BaseObserver
import com.zh.common.base.model.BaseModel
import com.zh.common.exception.ResponseTransformer
import com.zh.common.schedulers.SchedulerProvider
import com.zh.kotlin_mvvm.net.INetService
import com.zh.kotlin_mvvm.net.bean.LoginBean

class MainModel : BaseModel<INetService>(INetService::class.java) {

    fun onLogin(map: Map<String, Any>, observer: BaseObserver<LoginBean>) {
        doNetRequest(getINetService().login(BaseMapToBody.convertMapToBody(map)) ,observer)


        getINetService().login(BaseMapToBody.convertMapToBody(map))
            .compose(ResponseTransformer.handleResult())
            .compose(SchedulerProvider.instance.applySchedulers())
            .subscribe(observer)
    }
}
package com.isoftstone.bluetooth;

import com.isoftstone.bluetooth.adapter.AdapterManager;
import com.isoftstone.bluetooth.entity.TouchObject;

import android.app.Application;

//管理全局状态信息的的Application
/*
mTouchObject
mAdapetrManager
application
 */
public class BluetoothApplication extends Application {

	private static BluetoothApplication application;
	

	private AdapterManager mAdapterManager;
	

	private TouchObject mTouchObject;

	@Override
	public void onCreate() {
		super.onCreate();
		if(null == application){
			application = this;
		}
		mTouchObject = new TouchObject();
	}
	
	//返回application 的对象
	public static BluetoothApplication getInstance(){
		return application;
	}

	public AdapterManager getAdapterManager() {
		return mAdapterManager;
	}

	//传入一个adapteManager给mAdapterManager
	public void setAdapterManager(AdapterManager adapterManager) {
		this.mAdapterManager = adapterManager;
	}

	//返回一歌mTouchObject的对象
	public TouchObject getTouchObject() {
		return mTouchObject;
	}

	//传入一个mTouchObject的对象给mTouchObject
	public void setTouchObject(TouchObject touchObject) {
		this.mTouchObject = touchObject;
	}

}

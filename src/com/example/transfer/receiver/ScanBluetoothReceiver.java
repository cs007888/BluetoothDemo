package com.example.transfer.receiver;

import com.example.transfer.activity.BluetoothActivity;
import com.example.transfer.adapter.AdapterManager;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;


public class ScanBluetoothReceiver extends BroadcastReceiver {
	private BluetoothActivity mActivity;
	private AdapterManager mAdapterManager;
	private ProgressDialog mProgressDialog;
	private boolean isFirstSearch = true;
	
	public ScanBluetoothReceiver(Activity activity, AdapterManager adapterManager, ProgressDialog progressDialog){
		this.mActivity = (BluetoothActivity) activity;
		this.mAdapterManager = adapterManager;
		this.mProgressDialog = progressDialog;
	}

	@Override
	public void onReceive(Context context, final Intent intent) {
		if(intent.getAction().equals(BluetoothDevice.ACTION_FOUND)){
			//扫描到蓝牙
			//取得扫描到的蓝牙，添加到设备列表，更新列表
			BluetoothDevice bluetoothDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
			mAdapterManager.addDevice(bluetoothDevice);
			mAdapterManager.updateDeviceAdapter();
		}else if(intent.getAction().equals(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)){
			//扫描设备结束
			mProgressDialog.dismiss();
			if(mAdapterManager.getDeviceList().size() == 0){
				//扫描到的设备数为0
				Toast.makeText(mActivity, "没有找到其它蓝牙设备！", Toast.LENGTH_LONG).show();
			}
			if(isFirstSearch){
				//第一次查找后， 设置按钮显示文本为 "重新查找"
			//	mActivity.changeSearchBtnText();
				isFirstSearch = false;
			}
			//取消监听
			mActivity.unregisterReceiver(this);
		}
	}

}

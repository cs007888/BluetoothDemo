package com.isoftstone.bluetooth.entity;

import android.bluetooth.BluetoothDevice;

/*
存放当前蓝牙设备的实列
 */
public class TouchObject {
	public BluetoothDevice bluetoothDevice;   //当前操作蓝牙设备
	public int clickDeviceItemId;     //选中的列表的蓝牙设备的id
}

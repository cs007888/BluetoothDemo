package com.example.transfer.listener;

import com.example.transfer.BluetoothApplication;
import com.example.transfer.activity.BluetoothActivity;
import com.example.transfer.entity.MyMenuItem;
import com.example.transfer.entity.TouchObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.view.ContextMenu;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnCreateContextMenuListener;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.AdapterView.AdapterContextMenuInfo;

/**
 * ListView元素长按事件监听器
 *
 *
 */

public class DeviceListCCMenuListener implements OnCreateContextMenuListener{
	private TouchObject mTouchObject;
	private ListView mDeviceListView;


	public DeviceListCCMenuListener(	ListView deviceListView){
		this.mDeviceListView = deviceListView;
		mTouchObject = BluetoothApplication.getInstance().getTouchObject();
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;
		mTouchObject.clickDeviceItemId = info.position;
		mTouchObject.bluetoothDevice = (BluetoothDevice) mDeviceListView.getAdapter().getItem(info.position);
		menu.setHeaderTitle("请选择操作");
		menu.add(MyMenuItem.MENU_GROUP_DEVICE, 
				 MyMenuItem.MENU_ITEM_PAIR_ID, 
				 MyMenuItem.MENU_ITEM_PAIR_ORDER, 
				 MyMenuItem.MENU_ITEM_PAIR_TITLE);

		menu.add(MyMenuItem.MENU_GROUP_DEVICE, 
				 MyMenuItem.MENU_ITEM_SEND_ID, 
				 MyMenuItem.MENU_ITEM_SEND_ORDER, 
				 MyMenuItem.MENU_ITEM_SEND_TITLE);
	}



}








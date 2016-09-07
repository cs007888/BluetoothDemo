package com.isoftstone.bluetooth.listener;

import com.isoftstone.bluetooth.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.AdapterView.OnItemSelectedListener;

/**
 * 设置蓝牙可见按钮监听器
 *
 */
public class SetVisibleBtnClickListener implements OnClickListener {

	
	private Activity mActivity;

	public SetVisibleBtnClickListener(Activity activity){
		this.mActivity = activity;
	}

	@Override
	public void onClick(View v) {



		Intent getVisible = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
		mActivity.startActivity(getVisible);
	}

}

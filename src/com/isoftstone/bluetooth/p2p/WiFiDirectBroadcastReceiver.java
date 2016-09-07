package com.isoftstone.bluetooth.p2p;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.net.wifi.p2p.WifiP2pManager.PeerListListener;

import com.isoftstone.bluetooth.R;
/*
  主要涉及4个action的匹配
 */
/**
 * 接收Wifi事件的改变
 */
public class WiFiDirectBroadcastReceiver extends BroadcastReceiver {

    private WifiP2pManager manager;
    private Channel channel;
    private WifiDirectActivity activity;

    /**
      广播接收
     */
    public WiFiDirectBroadcastReceiver(WifiP2pManager manager, Channel channel,
                                       WifiDirectActivity activity) {
        super();
        this.manager = manager;
        this.channel = channel;
        this.activity = activity;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        //action 动作匹配的检验 看执行了那些动作
        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) { //设备的P2p功能是否可用

            // UI update to indicate wifi p2p status.
            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
            if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                // Wifi Direct mode is enabled
                activity.setIsWifiP2pEnabled(true);
            } else {
                activity.setIsWifiP2pEnabled(false);
                //重置数据
                activity.resetData();

            }
        } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
            //DiscoverPeers搜索设备 并且显示搜索到的设备RequestPeers()
            //搜索和显示设备信息是一个异步处理的机制搜索到一个显示一个
            if (manager != null) {
                //-------------------------------------------------------------------------------------------又是这个
                manager.requestPeers(channel, (PeerListListener) activity.getFragmentManager()
                        .findFragmentById(R.id.frag_list));//显示的列表 frag_list
            }
        } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {

            if (manager == null) {
                return;
            }
//-----------------------------------------------------------------------------------
//getParcelableExtra的使用调用的都是系统的信息
            NetworkInfo networkInfo = (NetworkInfo) intent
                    .getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);//"netWorkInfo"

            if (networkInfo.isConnected()) {

                // we are connected with the other device, request connection
                // info to find group owner IP
//------------------------------------------------------------------------------------------------------------------------------
//初始化碎片DeviceDetail的对象
                DeviceDetailFragment fragment = (DeviceDetailFragment) activity
                        .getFragmentManager().findFragmentById(R.id.frag_detail);
                manager.requestConnectionInfo(channel, fragment);
            } else {
                // It's a disconnect
                activity.resetData();
            }
        }
        // - -------------------------------------------------------------------------------------------------------------------------
        // 初始化碎片DeviceList            设备列表的信息
        else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
            DeviceListFragment fragment = (DeviceListFragment) activity.getFragmentManager()
                    .findFragmentById(R.id.frag_list);
            //upDateThisDevice的函数
            fragment.updateThisDevice((WifiP2pDevice) intent.getParcelableExtra(
                    WifiP2pManager.EXTRA_WIFI_P2P_DEVICE));//"P2pDevice"

        }
    }
}

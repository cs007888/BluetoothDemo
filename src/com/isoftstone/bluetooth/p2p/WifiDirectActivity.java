package com.isoftstone.bluetooth.p2p;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.ActionListener;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.net.wifi.p2p.WifiP2pManager.ChannelListener;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import com.isoftstone.bluetooth.R;
import com.isoftstone.bluetooth.p2p.DeviceListFragment.DeviceActionListener;




public class WifiDirectActivity extends Activity implements ChannelListener,DeviceActionListener,OnClickListener {


    private static final int SEARCH_METHOD=1;
    public static final String TAG = "wifidirectdemo";
    public WifiP2pManager manager;
    private WifiManager wifiManager;
    private boolean isWifiP2pEnabled = false;//P2p是否可用
    private boolean retryChannel = false;//重新建立通道

    private final IntentFilter intentFilter = new IntentFilter();
    public Channel channel;
    //Wifi广播接收
    private BroadcastReceiver receiver = null;
    /*
     搜索Wifi设备的事件
     */
    private Handler handler=new Handler(){
        public  void handleMessage(Message msg) {
            switch (msg.what) {
                case SEARCH_METHOD: {
                    if (!isWifiP2pEnabled) {
                        Toast.makeText(WifiDirectActivity.this, R.string.p2p_off_warning,
                                Toast.LENGTH_SHORT).show();


                    }

                    final DeviceListFragment fragment = (DeviceListFragment) getFragmentManager()
                            .findFragmentById(R.id.frag_list);
                    fragment.onInitiateDiscovery();//初始化搜索
                    manager.discoverPeers(channel, new WifiP2pManager.ActionListener() {

                        @Override
                        public void onSuccess() {
                            Toast.makeText(WifiDirectActivity.this, "正在搜索",
                                    Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onFailure(int reasonCode) {
                            Toast.makeText(WifiDirectActivity.this, "搜索失败" ,
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                break;
            }
        }
    };

    /**
     * WIfiP2p是否可用
     */
    public void setIsWifiP2pEnabled(boolean isWifiP2pEnabled) {
        this.isWifiP2pEnabled = isWifiP2pEnabled;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main2);

        // 添加必要的 intent值被匹配
       //隐式的intent的Action的匹配
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        manager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);//实例化manager
        wifiManager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
        channel = manager.initialize(this, getMainLooper(), null);//通道的初始化为广播的注册做铺垫

        Button ss=(Button)findViewById(R.id.btn_searchDevice);
        Button dk=(Button)findViewById(R.id.btn_openWifi);
        Button server=(Button)findViewById(R.id.btn_reciver) ;
        ss.setOnClickListener(this);
        dk.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (wifiManager.isWifiEnabled()) {
                    wifiManager.setWifiEnabled(false);
                    Toast.makeText(WifiDirectActivity.this, "wifi已关闭",
                            Toast.LENGTH_SHORT).show();
                } else {
                    wifiManager.setWifiEnabled(true);
                    Toast.makeText(WifiDirectActivity.this, "wifi已打开",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
        server.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                manager.createGroup(channel,new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(WifiDirectActivity.this, "已经作为文件接收方",Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(int reason) {

                    }
                });


            }
        });

    }

    /**
     * 注册广播和action中匹配的地方
     * */
    @Override
    public void onResume() {
        super.onResume();
        receiver = new WiFiDirectBroadcastReceiver(manager, channel, this);//WifiDirectBroadCastReciver继承BroadcastReciver
        registerReceiver(receiver, intentFilter);
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
    }

    /**
     * 清空设备信息
     */
    public void resetData() {
        //一个是List的控件
        //一个是显示连接设备具体信息的控件
        DeviceListFragment fragmentList = (DeviceListFragment) getFragmentManager()
                .findFragmentById(R.id.frag_list);
        DeviceDetailFragment fragmentDetails = (DeviceDetailFragment) getFragmentManager()
                .findFragmentById(R.id.frag_detail);
        if (fragmentList != null) {
            fragmentList.clearPeers();
        }
        if (fragmentDetails != null) {
            fragmentDetails.resetViews();
        }
    }




    //DeviceActionListener的接口的实现的4个重写的函数
    @Override
    public void showDetails(WifiP2pDevice device) {
        DeviceDetailFragment fragment = (DeviceDetailFragment) getFragmentManager()
                .findFragmentById(R.id.frag_detail);
        fragment.showDetails(device);

    }

    @Override
    public void connect(WifiP2pConfig config) {
        manager.connect(channel, config, new ActionListener() {

            @Override
            public void onSuccess() {
            }

            @Override
            public void onFailure(int reason) {
                Toast.makeText(WifiDirectActivity.this, "Connect failed. Retry.",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void disconnect() {
        final DeviceDetailFragment fragment = (DeviceDetailFragment) getFragmentManager()
                .findFragmentById(R.id.frag_detail);
        fragment.resetViews();
        manager.removeGroup(channel, new ActionListener() {

            @Override
            public void onFailure(int reasonCode) {
            }

            @Override
            public void onSuccess() {
                fragment.getView().setVisibility(View.GONE);
            }

        });
    }

    @Override
    public void onChannelDisconnected() {
        if (manager != null && !retryChannel) {
            Toast.makeText(this, "，通道丢失，请重新试一次", Toast.LENGTH_LONG).show();
            resetData();
            retryChannel = true;
            manager.initialize(this, getMainLooper(), this);
        } else {
            Toast.makeText(this,
                    "通道丢失！无法连接",
                    Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void cancelDisconnect() {

        /*
         *中断连接
         */

        if (manager != null) {
            final DeviceListFragment fragment = (DeviceListFragment) getFragmentManager()
                    .findFragmentById(R.id.frag_list);
            if (fragment.getDevice() == null
                    || fragment.getDevice().status == WifiP2pDevice.CONNECTED) {
                disconnect();
            } else if (fragment.getDevice().status == WifiP2pDevice.AVAILABLE
                    || fragment.getDevice().status == WifiP2pDevice.INVITED) {

                manager.cancelConnect(channel, new ActionListener() {

                    @Override
                    public void onSuccess() {
                        Toast.makeText(WifiDirectActivity.this, "正在中断连接",
                                Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(int reasonCode) {
                        Toast.makeText(WifiDirectActivity.this,
                                "连接中断，请求失败 " + reasonCode,
                                Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }

    }
    /*

      搜索设备 点击事件

    */
    @Override
    public void onClick(View v) {


        switch(v.getId()){
            case  R.id.btn_searchDevice:
                new Thread(new Runnable(){
                    public void run() {
                        Message message = new Message();
                        message.what=SEARCH_METHOD;
                        handler.sendMessage(message);
                    }
                }).start();


                break;
        }

    }


}

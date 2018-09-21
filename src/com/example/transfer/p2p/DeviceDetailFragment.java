package com.example.transfer.p2p;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.ConnectionInfoListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.transfer.BluetoothApplication;
import com.example.transfer.R;
import com.example.transfer.activity.SelectFileActivity;
import com.example.transfer.adapter.AdapterManager;

import com.example.transfer.p2p.DeviceListFragment.DeviceActionListener;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * 进行设备连接和文件传输
 */

public class DeviceDetailFragment extends Fragment implements ConnectionInfoListener {


    public static final String SEND_FILE = "sendFile";
    public static final int CHOOSE_FILE_RESULT_CODE = 20;
    private View mContentView = null;
    private WifiP2pDevice device;
    private WifiP2pInfo info;
    public static String uri;
    ProgressDialog progressDialog = null;


    private BluetoothApplication mApplication;
    private AdapterManager mAdapterManager;     //Adapter管理器





    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        //实例化Adapter管理器并设置到Application
        mApplication = BluetoothApplication.getInstance();


        mAdapterManager = new AdapterManager(getActivity());
        mApplication.setAdapterManager(mAdapterManager);


        //device_detail视图的加载
        mContentView = inflater.inflate(R.layout.device_detail, null);

        /*
        连接的监听事件
         */
        mContentView.findViewById(R.id.btn_connect).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                WifiP2pConfig config = new WifiP2pConfig();
                config.deviceAddress = device.deviceAddress;
                config.wps.setup = WpsInfo.PBC;;
                if (progressDialog != null && progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
                progressDialog = ProgressDialog.show(getActivity(), "点击取消搜索",
                        "正在连接到 :" + device.deviceAddress, true, true

                );
                ((DeviceActionListener) getActivity()).connect(config);

            }
        });
        /*
        取消连接的监听事件
         */
        mContentView.findViewById(R.id.btn_disconnect).setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        ((DeviceActionListener) getActivity()).disconnect();

                    }
                });



       /*
        选择文件和传输的监听事件
        */
        mContentView.findViewById(R.id.btn_start_client).setOnClickListener(
                new View.OnClickListener()

              {
                  @Override
                   public void onClick(View v) {



                        Intent intent=new Intent(getActivity(),SelectFileActivity.class);
                        intent.putExtra("flag","2");
                        startActivityForResult(intent,99);


                   }
                });





        return mContentView;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

 if(resultCode==CHOOSE_FILE_RESULT_CODE) {

     uri = data.getStringExtra(SEND_FILE);
     TextView statusText = (TextView) mContentView.findViewById(R.id.status_text);
     statusText.setText("正在发送: " + uri);

     Intent serviceIntent = new Intent(getActivity(), FileTransferService.class);
     serviceIntent.setAction(FileTransferService.ACTION_SEND_FILE);

     serviceIntent.putExtra(DeviceDetailFragment.SEND_FILE, uri.toString());
     serviceIntent.putExtra(FileTransferService.EXTRAS_GROUP_OWNER_ADDRESS,
             info.groupOwnerAddress.getHostAddress());
     serviceIntent.putExtra(FileTransferService.EXTRAS_GROUP_OWNER_PORT, 8988);
     getActivity().startService(serviceIntent);
     }
        super.onActivityResult( requestCode, resultCode,data);

    }

    @Override
    public void onConnectionInfoAvailable(final WifiP2pInfo info) {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        this.info = info;
        this.getView().setVisibility(View.VISIBLE);

        // ip地址已知
        TextView view = (TextView) mContentView.findViewById(R.id.group_owner);
        view.setText(getResources().getString(R.string.group_owner_text)
                + ((info.isGroupOwner == true) ? getResources().getString(R.string.yes)
                : getResources().getString(R.string.no)));

        view = (TextView) mContentView.findViewById(R.id.device_info);
        view.setText("主机 IP - " + info.groupOwnerAddress.getHostAddress());

        // 文件传输是单向的，服务端接收文件，客户端传输文件
       if (info.groupFormed && info.isGroupOwner) {
            new FileServerAsyncTask(getActivity(), mContentView.findViewById(R.id.status_text))
                    .execute();
        } else if (info.groupFormed) {
            //客户端出现选择文件按钮
            mContentView.findViewById(R.id.btn_start_client).setVisibility(View.VISIBLE);
            ((TextView) mContentView.findViewById(R.id.status_text)).setText(getResources()
                    .getString(R.string.client_text));
        }
        // 隐藏连接按钮
        mContentView.findViewById(R.id.btn_connect).setVisibility(View.GONE);
    }

    /**
     * 利用设备数据更新ui
     */
    public void showDetails(WifiP2pDevice device) {
        this.device = device;
        this.getView().setVisibility(View.VISIBLE);
        TextView view = (TextView) mContentView.findViewById(R.id.device_address);
        view.setText(device.deviceAddress);
        view = (TextView) mContentView.findViewById(R.id.device_info);
        view.setText(device.toString());

    }

    /**
     * 断开连接后的ui更新
     */
    public void resetViews() {
        mContentView.findViewById(R.id.btn_connect).setVisibility(View.VISIBLE);
        TextView view = (TextView) mContentView.findViewById(R.id.device_address);
        view.setText(R.string.empty);
        view = (TextView) mContentView.findViewById(R.id.device_info);
        view.setText(R.string.empty);
        view = (TextView) mContentView.findViewById(R.id.group_owner);
        view.setText(R.string.empty);
        view = (TextView) mContentView.findViewById(R.id.status_text);
        view.setText(R.string.empty);
        mContentView.findViewById(R.id.btn_start_client).setVisibility(View.GONE);
        this.getView().setVisibility(View.GONE);
    }

    /**
     *  server socket 写入数据流
     */
    public static class FileServerAsyncTask extends AsyncTask<Void, Void, String> {

        private Context context;
        private TextView statusText;
        public FileServerAsyncTask(Context context, View statusText) {
            this.context = context;
            this.statusText = (TextView) statusText;
        }

        @Override
        protected String doInBackground(Void... params) {
            try {
                File dirs = new File(Environment.getExternalStorageDirectory() + "/"+"速传文件");
                if (!dirs.exists())
                    dirs.mkdirs();
                ServerSocket serverSocket = new ServerSocket(8988);
                Socket client = serverSocket.accept();

                InputStream inputstream = client.getInputStream();
                //修改2
                byte[] info = new byte[256];
                inputstream.read(info);
                String file_name = new String(info).trim();
                Log.d("AAAA", file_name);
                final File f = new File(dirs+"/"+file_name);
//此处添加接收文件夹
                f.createNewFile();
                copyFile(inputstream, new FileOutputStream(f));
                serverSocket.close();
                return f.getAbsolutePath();
            } catch (IOException e) {
                return null;
            }
        }
        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
                statusText.setText("文件 拷贝 - " + result);
                Intent intent = new Intent();
                intent.setAction(android.content.Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.parse("file://" + result), "*/*");
                context.startActivity(intent);
            }

        }
        @Override
        protected void onPreExecute() {
            statusText.setText("正在打开一个服务线程");
        }

    }

    public static boolean copyFile(InputStream inputStream, OutputStream out) {
        byte buf[] = new byte[1024];
        int len;


        try {
            while ((len = inputStream.read(buf)) != -1) {
                out.write(buf, 0, len);
            }
            out.close();
            inputStream.close();


        } catch (IOException e) {
            return false;
        }
        return true;
    }

}

package com.isoftstone.bluetooth.p2p;

import android.app.IntentService;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Arrays;

/**
 *文件传输服务
 */
public class FileTransferService extends IntentService {

    private static final int SOCKET_TIMEOUT = 5000;
    public static final String ACTION_SEND_FILE = ".activity.SelectFileActivity";
    public static final String EXTRAS_GROUP_OWNER_ADDRESS = "go_host";
    public static final String EXTRAS_GROUP_OWNER_PORT = "go_port";

    public FileTransferService(String name) {
        super(name);
    }

    public FileTransferService() {
        super("FileTransferService");
    }
    @Override
    protected void onHandleIntent(Intent intent) {
        Context context = getApplicationContext();
       if (intent.getAction().equals(ACTION_SEND_FILE)) {

            String fileUri = intent.getExtras().getString(DeviceDetailFragment.SEND_FILE);
            String   fileName= fileUri.substring(fileUri.lastIndexOf("/") + 1, fileUri.length());

            String host = intent.getExtras().getString(EXTRAS_GROUP_OWNER_ADDRESS);
            Socket socket = new Socket();
            int port = intent.getExtras().getInt(EXTRAS_GROUP_OWNER_PORT);
            try {
                socket.bind(null);
                socket.connect((new InetSocketAddress(host, port)), SOCKET_TIMEOUT);
                OutputStream stream = socket.getOutputStream();
                ContentResolver cr = context.getContentResolver();
                InputStream is = null;
                try {
                    //出现异常  看来是uri的问题  没有和Uri 相关联的数据或者 该uri不能被打开的时候
                    //2uri本身的问题没有相关联的数据
                    //3uri不能被打开
                    Uri test=  Uri.parse(fileUri);
                    is = cr.openInputStream(test);
                    //--------------------
                    byte[] b = fileName.getBytes();
                    byte[] info = Arrays.copyOf(b,256);
                    stream.write(info);

                } catch (FileNotFoundException e) {
                }
                DeviceDetailFragment.copyFile(is, stream);
            } catch (IOException e) {
            } finally {
                if (socket != null) {
                    if (socket.isConnected()) {
                        try {
                            socket.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

        }
    }
}

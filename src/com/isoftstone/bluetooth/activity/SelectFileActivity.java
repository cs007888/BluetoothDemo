package com.isoftstone.bluetooth.activity;

import java.io.File;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.isoftstone.bluetooth.BluetoothApplication;
import com.isoftstone.bluetooth.R;
import com.isoftstone.bluetooth.adapter.AdapterManager;
import com.isoftstone.bluetooth.p2p.DeviceDetailFragment;
import com.isoftstone.bluetooth.entity.Copy_File;

public class SelectFileActivity extends Activity {


	ListView mFileListView;
	AdapterManager mAdapterManager;

	private Handler mOtherHandler;
	private Runnable updateFileListRunnable;

	private File file;//当前操作文件 或 文件夹
	private File lfile;//长按时的文件夹
	private String sdcardPath;  //sd卡路径
	private String path;    //当前文件父目录
	private View myView;
	private EditText myEditText;//重命名弹框的文字输出框
	Button mBackBtn;  //返回按钮
	Button mCancelBtn;   //取消按钮
	Button mStick;
    private Intent getIntent;
	private boolean isSelected = false;//是否选择了文件   (非文件夹)
	File[] currentFiles;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.select_file);

		mFileListView = (ListView) findViewById(R.id.fileListView);
		mBackBtn = (Button) findViewById(R.id.selectFileBackBtn);
		mCancelBtn = (Button) findViewById(R.id.selectFileCancelBtn);
		mStick=(Button) findViewById(R.id.bt3);

		//取得sd卡目录
		sdcardPath = Environment.getExternalStorageDirectory().getAbsolutePath();
		path = sdcardPath;

		mAdapterManager = BluetoothApplication.getInstance().getAdapterManager();
		mFileListView.setAdapter(mAdapterManager.getFileListAdapter());
		//首先显示sd卡下所有文件及文件夹
		mAdapterManager.updateFileListAdapter(path);

		mFileListView.setOnItemClickListener(mFileListOnItemClickListener);
        mFileListView.setOnItemLongClickListener(FileOnLongClickListener);
		mBackBtn.setOnClickListener(mBackBtnClickListener);
		mStick.setOnClickListener(mStickClickListener);
		this.mStick.setVisibility(View.GONE);
		getIntent=getIntent();
		mCancelBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				SelectFileActivity.this.finish();
			}
		});

	}

	private OnItemClickListener mFileListOnItemClickListener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> parent, View view,
				int position, long id) {
			//当前操作文件 或 文件夹
  			file = (File) mFileListView.getAdapter().getItem(position);
			if(file.isFile()){
				Intent intent = new Intent();
				if(getIntent.getStringExtra("flag").equals("1")) {
					intent.putExtra(BluetoothActivity.SEND_FILE_NAME, file.getAbsolutePath());
					SelectFileActivity.this.setResult(BluetoothActivity.RESULT_CODE, intent);

				}
				else if(getIntent.getStringExtra("flag").equals("2"))
				{


					intent.putExtra(DeviceDetailFragment.SEND_FILE, "file://" + file.getAbsolutePath());
					SelectFileActivity.this.setResult(DeviceDetailFragment.CHOOSE_FILE_RESULT_CODE, intent);
				}
				SelectFileActivity.this.finish();
			}else {
				//如果是文件夹， 则显示该文件夹下所有文件 及 文件夹
				path = file.getAbsolutePath();
				updateFileList();
			}
		}

	};

	private OnClickListener mBackBtnClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			if(path.equals(sdcardPath)){
				//当前文件父目录为 sd卡， 不做任何操作
				return ;
			}
			//返回上一级目录
			path = path.substring(0, path.lastIndexOf("/"));
			updateFileList();
		}
	};

	private OnClickListener mStickClickListener = new OnClickListener() {
		@Override
		public void onClick(View view) {
			if (lfile.isDirectory())
				{
					Copy_File.copy(lfile.getPath() + "/", path+"/" + lfile.getName() + "/");

				} else//如果当前项为文件则进行文件拷贝
				{
					Copy_File.CopySdcardFile(lfile.getPath()+"/", path +"/"+ lfile.getName());
				}
				updateFileList();
			mStick.setVisibility(View.GONE);
			}

	};
//添加长按listview事件
	private AdapterView.OnItemLongClickListener FileOnLongClickListener=new AdapterView.OnItemLongClickListener() {
		@Override
		public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
			lfile = (File) mFileListView.getAdapter().getItem(position);
			fileHandle(lfile);
			return true;

		}
	};

	/**
	 * 根据父目录path显示path下所有文件及文件夹
	 */
	private void updateFileList() {
		if(null == updateFileListRunnable){
			updateFileListRunnable = new Runnable() {
							
				@Override
				public void run() {
					
					mAdapterManager.updateFileListAdapter(path);
				}
			};
		}
		if(null == mOtherHandler){
			HandlerThread handlerThread = new HandlerThread("other_thread");
			handlerThread.start();
			mOtherHandler = new Handler(handlerThread.getLooper());
		}
		mOtherHandler.post(updateFileListRunnable);
	}
	/**
	 * Start 在此标记，增加更改文件名，覆盖文件，删除文件三个事件，开始！
	 **/

	/**
	 * 点击文件时，调用此文件处理方法
	 **/

	private void fileHandle(final File filenow)
	{
    /* 点击文件时的OnClickListener */
		DialogInterface.OnClickListener listener1 = new DialogInterface.OnClickListener()
		{
			public void onClick(DialogInterface dialog, int which) {
				/**
				 * 当选择的item为打开文件时
				 * 如果是文件夹就打开文件夹，如果是文件就调用系统的其他应用打开这个文件
				 **/
				if (which == 0) {
					if (filenow.isFile()) {
						openFile(filenow);
					} else {
						path = filenow.getAbsolutePath();
						updateFileList();
					}

					// 在对话框上选择第一项为打开文件
				}

				/**
				 * 当选择的item为更改档名时
				 **/
				else if (which == 1) {
					LayoutInflater factory = LayoutInflater
							.from(SelectFileActivity.this);
					myView = factory.inflate(R.layout.rename_dialog, null);// 初始化myChoiceView，使用rename_alert_dialog为layout
					myEditText = (EditText) myView.findViewById(R.id.mEdit);
					myEditText.setText(filenow.getName());// 将原始文件名先放入EditText中

          /* new一个更改文件名的Dialog的确定按钮的listener */
					DialogInterface.OnClickListener listener2 = new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							String modName = myEditText.getText().toString();// 取得修改后的文件路径
							final String pFile = filenow.getParentFile().getPath() + "/";// 取得当前文件的父文件
							final String newPath = pFile + modName;// 取得修改后文件的名称

              /* 判断档名是否已存在 */
							if (new File(newPath).exists()) {
								if (!modName.equals(filenow.getName()))// 排除修改文件名时没修改直接送出的状况
								{
                  /* 跳出Alert警告档名重复，并确认是否覆盖 */
									new AlertDialog.Builder(SelectFileActivity.this)
											.setTitle("注意!").setMessage("档名已经存在，是否要覆盖?")
											.setPositiveButton("确定",
													new DialogInterface.OnClickListener() {
														public void onClick(DialogInterface dialog,
																			int which) {
                              /* 档名重复仍然修改会覆改掉已存在的文件 */
															filenow.renameTo(new File(newPath));
                              /* 重新产生文件列表的ListView */
															updateFileList();
														}
													}).setNegativeButton("取消",
											new DialogInterface.OnClickListener() {
												public void onClick(DialogInterface dialog,
																	int which) {
												}
											}).show();
								}
							} else {
                /* 档名不存在，直接做修改动作 */
								filenow.renameTo(new File(newPath));
                /* 重新产生文件列表的ListView */
								updateFileList();
							}
						}
					};

          /* create更改档名时跳出的Dialog */
					AlertDialog renameDialog = new AlertDialog.Builder(
							SelectFileActivity.this).create();
					renameDialog.setView(myView);

          /* 设置更改档名点击确认后的Listener */
					renameDialog.setButton("确定", listener2);
					renameDialog.setButton2("取消",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int which) {
								}
							});
					renameDialog.show();
				}

				/**
				 * 当选择的item为删除文件时
				 **/
				else if (which == 2) {
					new AlertDialog.Builder(SelectFileActivity.this).setTitle(
							"注意!").setMessage("确定要删除" + filenow + "吗?").setPositiveButton("确定",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int which) {
                  /* 删除文件 */
									if (filenow.isDirectory()) {           //删除文件夹
										File[] childFile = filenow.listFiles();
										if (childFile == null || childFile.length == 0) {
											filenow.delete();
											updateFileList();
										}
										for (File f : childFile) {
											f.delete();
										}
										filenow.delete();
										updateFileList();
									} else {
										filenow.delete();//删除文件
										updateFileList();
									}
								}

							}).setNegativeButton("取消",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int which) {
								}
							}).show();
				} else {
					if (!filenow.exists()) {
						Toast.makeText(SelectFileActivity.this, "文件拷贝失败！！！", Toast.LENGTH_SHORT).show();
					};
					Toast.makeText(SelectFileActivity.this, "文件拷贝成功！！！", Toast.LENGTH_SHORT).show();
					mStick.setVisibility(View.VISIBLE);
				}
			}
		};

		/**
		 * 选择一个文件时，跳出要如何处理文件的ListDialog
		 **/
		String[] menu =
				{ "打开文件", "更改文件名", "删除文件" ,"复制"};
		new AlertDialog.Builder(SelectFileActivity.this).setTitle("你要做什么?")
				.setItems(menu, listener1).setPositiveButton("取消",
				new DialogInterface.OnClickListener()
				{
					public void onClick(DialogInterface dialog, int which)
					{
					}
				}).show();
	}

	/**
	 * End 在此标记，增加更改文件名，覆盖文件，删除文件三个事件，结束！
	 **/
	/**
	 * 打开文件的方法
	 */
	private void openFile(File file){

		Intent intent = new Intent();
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		//设置intent的Action属性
		intent.setAction(Intent.ACTION_VIEW);
		//获取文件file的MIME类型
		String type = getMIMEType(file);
		//设置intent的data和Type属性。
		intent.setDataAndType(/*uri*/Uri.fromFile(file), type);
		//跳转
		startActivity(intent);

	}

	/**
	 * 根据文件后缀名获得对应的MIME类型。
	 */
	private String getMIMEType(File file) {

		String type="*/*";
		String fName = file.getName();
		//获取后缀名前的分隔符"."在fName中的位置。
		int dotIndex = fName.lastIndexOf(".");
		if(dotIndex < 0){
			return type;
		}
    /* 获取文件的后缀名 */
		String end=fName.substring(dotIndex,fName.length()).toLowerCase();
		if(end=="")return type;
		//在MIME和文件类型的匹配表中找到对应的MIME类型。
		for(int i=0;i<MIME_MapTable.length;i++){
			if(end.equals(MIME_MapTable[i][0]))
				type = MIME_MapTable[i][1];
		}
		return type;
	}
	private final String[][] MIME_MapTable={
			//{后缀名， MIME类型}
			{".3gp",    "video/3gpp"},
			{".apk",    "application/vnd.android.package-archive"},
			{".asf",    "video/x-ms-asf"},
			{".avi",    "video/x-msvideo"},
			{".bin",    "application/octet-stream"},
			{".bmp",    "image/bmp"},
			{".c",  "text/plain"},
			{".class",  "application/octet-stream"},
			{".conf",   "text/plain"},
			{".cpp",    "text/plain"},
			{".doc",    "application/msword"},
			{".docx",   "application/vnd.openxmlformats-officedocument.wordprocessingml.document"},
			{".xls",    "application/vnd.ms-excel"},
			{".xlsx",   "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"},
			{".exe",    "application/octet-stream"},
			{".gif",    "image/gif"},
			{".gtar",   "application/x-gtar"},
			{".gz", "application/x-gzip"},
			{".h",  "text/plain"},
			{".htm",    "text/html"},
			{".html",   "text/html"},
			{".jar",    "application/java-archive"},
			{".java",   "text/plain"},
			{".jpeg",   "image/jpeg"},
			{".jpg",    "image/jpeg"},
			{".js", "application/x-javascript"},
			{".log",    "text/plain"},
			{".m3u",    "audio/x-mpegurl"},
			{".m4a",    "audio/mp4a-latm"},
			{".m4b",    "audio/mp4a-latm"},
			{".m4p",    "audio/mp4a-latm"},
			{".m4u",    "video/vnd.mpegurl"},
			{".m4v",    "video/x-m4v"},
			{".mov",    "video/quicktime"},
			{".mp2",    "audio/x-mpeg"},
			{".mp3",    "audio/x-mpeg"},
			{".mp4",    "video/mp4"},
			{".mpc",    "application/vnd.mpohun.certificate"},
			{".mpe",    "video/mpeg"},
			{".mpeg",   "video/mpeg"},
			{".mpg",    "video/mpeg"},
			{".mpg4",   "video/mp4"},
			{".mpga",   "audio/mpeg"},
			{".msg",    "application/vnd.ms-outlook"},
			{".ogg",    "audio/ogg"},
			{".pdf",    "application/pdf"},
			{".png",    "image/png"},
			{".pps",    "application/vnd.ms-powerpoint"},
			{".ppt",    "application/vnd.ms-powerpoint"},
			{".pptx",   "application/vnd.openxmlformats-officedocument.presentationml.presentation"},
			{".prop",   "text/plain"},
			{".rc", "text/plain"},
			{".rmvb",   "audio/x-pn-realaudio"},
			{".rtf",    "application/rtf"},
			{".sh", "text/plain"},
			{".tar",    "application/x-tar"},
			{".tgz",    "application/x-compressed"},
			{".txt",    "text/plain"},
			{".wav",    "audio/x-wav"},
			{".wma",    "audio/x-ms-wma"},
			{".wmv",    "audio/x-ms-wmv"},
			{".wps",    "application/vnd.ms-works"},
			{".xml",    "text/plain"},
			{".z",  "application/x-compress"},
			{".zip",    "application/x-zip-compressed"},
			{"",        "*/*"}
	};

}

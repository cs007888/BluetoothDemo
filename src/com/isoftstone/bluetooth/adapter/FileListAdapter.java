package com.isoftstone.bluetooth.adapter;

import java.io.File;
import java.util.List;

import com.isoftstone.bluetooth.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class FileListAdapter extends BaseAdapter{
	private LayoutInflater mLayoutInflater;
	private List<File> mFileList;
	private int mLayoutId;
	public FileListAdapter(Context context, List<File> fileList, int layoutId){
		this.mLayoutInflater = LayoutInflater.from(context);
		this.mFileList = fileList;
		this.mLayoutId = layoutId;
	}

	@Override
	public int getCount() {
		return mFileList.size();
	}

	@Override
	public Object getItem(int position) {
		return mFileList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return mFileList.get(position).hashCode();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
	    File file = mFileList.get(position);
		Holder holder;
		if(null == convertView){
			holder =new Holder();
			convertView = mLayoutInflater.inflate(mLayoutId, null);
			holder.mFileImageView = (ImageView) convertView.findViewById(R.id.fileImageView);
			holder.mFileNameTV = (TextView) convertView.findViewById(R.id.fileNameTV);
			
			convertView.setTag(holder);
		}else {
			holder = (Holder) convertView.getTag();
		}
		
		if(file.isFile()){
			holder.mFileImageView.setImageResource(R.drawable.icon_file);
		}else {
			holder.mFileImageView.setImageResource(R.drawable.icon_folder);
		}
		holder.mFileNameTV.setText(file.getName());
		
		return convertView;
	}
	class Holder {
		TextView mFileNameTV;
		ImageView mFileImageView;
	}

}




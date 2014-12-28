package com.the.restaurant;


import com.the.restaurant.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

// adapter class for custom news list
class NewsListAdapter extends BaseAdapter {
		private LayoutInflater inflater;
		
		public NewsListAdapter(Context context) {
			inflater = LayoutInflater.from(context);
		}
		
		public int getCount() {
			// TODO Auto-generated method stub
			return News.Title.size();
		}

		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			ViewHolder holder;
			
			if(convertView == null){
				convertView = inflater.inflate(R.layout.news_list_item, null);
				holder = new ViewHolder();
				holder.txtText = (TextView) convertView.findViewById(R.id.txtText);
				holder.txtSubText = (TextView) convertView.findViewById(R.id.txtSubText);
				
				convertView.setTag(holder);
			}else{
				holder = (ViewHolder) convertView.getTag();
			}
			
			
			holder.txtText.setText(News.Title.get(position));
			holder.txtSubText.setText(News.PubDate.get(position));
			
			
			return convertView;
		}
		
		static class ViewHolder {
			TextView txtText, txtSubText;
		}
		
		
		
	}
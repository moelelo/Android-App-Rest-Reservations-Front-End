package com.the.restaurant;

import com.the.restaurant.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

// adapter class for custom order list
class YourOrderListAdapter extends BaseAdapter {
		private LayoutInflater inflater;
		
		public YourOrderListAdapter(Context context) {
			inflater = LayoutInflater.from(context);
		}
		
		public int getCount() {
			// TODO Auto-generated method stub
			return YourOrder.Menu_ID.size();
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
				convertView = inflater.inflate(R.layout.order_list_item, null);
				holder = new ViewHolder();
				holder.txtMenuName = (TextView) convertView.findViewById(R.id.txtMenuName);
				holder.txtQuantity = (TextView) convertView.findViewById(R.id.txtQuantity);
				holder.txtPrice = (TextView) convertView.findViewById(R.id.txtPrice);
				
				convertView.setTag(holder);
			}else{
				holder = (ViewHolder) convertView.getTag();
			}
			
			
			holder.txtMenuName.setText(YourOrder.Menu_name.get(position));
			holder.txtQuantity.setText(String.valueOf(YourOrder.Quantity.get(position)));
			holder.txtPrice.setText(YourOrder.Sub_total_price.get(position)+" "+YourOrder.Currency);
			
			return convertView;
		}
		
		static class ViewHolder {
			TextView txtMenuName, txtQuantity, txtPrice;
		}
		
		
	}
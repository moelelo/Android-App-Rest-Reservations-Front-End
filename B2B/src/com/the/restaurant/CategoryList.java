package com.the.restaurant;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.util.ArrayList;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.the.restaurant.R;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class CategoryList extends Activity {
	
	//declare view objects
	ImageButton imgNavBack, imgRefresh;
	ListView listCategory;
	ProgressBar prgLoading;
	TextView txtAlert;

	//declare adapter object to create custom category list
	CategoryListAdapter cla;
	
	//create arraylist variables to store data from server
	static ArrayList<Long> Category_ID = new ArrayList<Long>();
	static ArrayList<String> Category_name = new ArrayList<String>();
	static ArrayList<String> Category_image = new ArrayList<String>();
	
	String CategoryAPI;
	int IOConnect = 0;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.category_list);
        
        imgNavBack = (ImageButton) findViewById(R.id.imgNavBack);
        prgLoading = (ProgressBar) findViewById(R.id.prgLoading);
        listCategory = (ListView) findViewById(R.id.listCategory);
        txtAlert = (TextView) findViewById(R.id.txtAlert);
        imgRefresh = (ImageButton) findViewById(R.id.imgRefresh);
        
        cla = new CategoryListAdapter(CategoryList.this);

        //category API url
    	CategoryAPI = Utils.CategoryAPI+"?accesskey="+Utils.AccessKey;
        
        //call asynctask class to request data from server
        new getDataTask().execute();
        
        //event listener to handle list when clicked
		listCategory.setOnItemClickListener(new OnItemClickListener() {

			public void onItemClick(AdapterView<?> arg0, View arg1, int position,
					long arg3) {
				// TODO Auto-generated method stub
				// go to menu page
				Intent iMenuList = new Intent(CategoryList.this, MenuList.class);
				iMenuList.putExtra("category_id", Category_ID.get(position));
				iMenuList.putExtra("category_name", Category_name.get(position));
				startActivity(iMenuList);
			}
		});
		
		//event listener to handle refresh button when clicked
		imgRefresh.setOnClickListener(new OnClickListener() {
			
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				// re-request data from server by calling asynctask class
				IOConnect = 0;
				listCategory.invalidateViews();
    			clearData();
				new getDataTask().execute();
			}
		});
        
		//event listener to handle back button when clicked
        imgNavBack.setOnClickListener(new OnClickListener() {
			
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				// go back to previous page
				finish();
			}
		});
        
    }
    
    // clear arraylist variables before used
    void clearData(){
    	Category_ID.clear();
    	Category_name.clear();
    	Category_image.clear();
    }
    
    // asynctask class to handle parsing json in background
    public class getDataTask extends AsyncTask<Void, Void, Void>{
    	
    	// show progressbar first
    	getDataTask(){
    		if(!prgLoading.isShown()){
    			prgLoading.setVisibility(0);
				txtAlert.setVisibility(8);
    		}
    	}
    	
		@Override
		protected Void doInBackground(Void... arg0) {
			// TODO Auto-generated method stub
			// parse json data from server in background
			parseJSONData();
			return null;
		}
    	
		@Override
		protected void onPostExecute(Void result) {
			// TODO Auto-generated method stub
			// when finish parsing, hide progressbar
			prgLoading.setVisibility(8);
			
			// if internet connection and data available show data on list
			// if not, show alert text
			if((Category_ID.size() > 0) && (IOConnect == 0)){
				listCategory.setVisibility(0);
				listCategory.setAdapter(cla);
			}else{
				txtAlert.setVisibility(0);
			}
		}
    }
    
    // to parse json data from server
    public void parseJSONData(){
    	
    	clearData();
    	
    	try {
    		// request data from Category API
	        HttpClient client = new DefaultHttpClient();
			HttpConnectionParams.setConnectionTimeout(client.getParams(), 15000);
			HttpConnectionParams.setSoTimeout(client.getParams(), 15000);
	        HttpUriRequest request = new HttpGet(CategoryAPI);
			HttpResponse response = client.execute(request);
			InputStream atomInputStream = response.getEntity().getContent();
			BufferedReader in = new BufferedReader(new InputStreamReader(atomInputStream));
		
	        String line;
	        String str = "";
	        while ((line = in.readLine()) != null){
	        	str += line;
	        }
        
	        // parse json data and store into arraylist variables
			JSONObject json = new JSONObject(str);
			JSONArray data = json.getJSONArray("data");

			for (int i = 0; i < data.length(); i++) {
			    JSONObject object = data.getJSONObject(i); 
			    
			    JSONObject category = object.getJSONObject("Category");
			    
			    Category_ID.add(Long.parseLong(category.getString("Category_ID")));
			    Category_name.add(category.getString("Category_name"));
			    Category_image.add(category.getString("Category_image"));
			    Log.d("Category name", Category_name.get(i));
				    
			}
				
				
		} catch (MalformedURLException e) {
		    // TODO Auto-generated catch block
		    e.printStackTrace();
		} catch (IOException e) {
		    // TODO Auto-generated catch block
			IOConnect = 1;
		    e.printStackTrace();
		} catch (JSONException e) {
		    // TODO Auto-generated catch block
		    e.printStackTrace();
		}	
    }
    
    @Override
    protected void onDestroy() {
    	// TODO Auto-generated method stub
    	cla.imageLoader.clearCache();
    	listCategory.setAdapter(null);
    	super.onDestroy();
    }

    
    @Override
	public void onConfigurationChanged(final Configuration newConfig)
	{
	    // Ignore orientation change to keep activity from restarting
	    super.onConfigurationChanged(newConfig);
	}
    
}

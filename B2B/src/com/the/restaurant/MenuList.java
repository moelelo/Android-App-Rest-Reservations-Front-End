package com.the.restaurant;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.text.DecimalFormat;
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
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class MenuList extends Activity {
	
	// declare view objects
	ImageButton imgNavBack, imgRefresh;
	ListView listMenu;
	ProgressBar prgLoading;
	TextView txtTitle;
	EditText edtKeyword;
	ImageButton btnSearch;
	TextView txtAlert;
	
	// declare static variable to store tax and currency symbol
	static double Tax;
	static String Currency;
	
	// declare adapter object to create custom menu list
	MenuListAdapter mla;
	
	// create arraylist variables to store data from server
	static ArrayList<Long> Menu_ID = new ArrayList<Long>();
	static ArrayList<String> Menu_name = new ArrayList<String>();
	static ArrayList<Double> Menu_price = new ArrayList<Double>();
	static ArrayList<String> Menu_image = new ArrayList<String>();
	
	String MenuAPI;
	String TaxCurrencyAPI;
	int IOConnect = 0;
	long Category_ID;
	String Category_name;
	String Keyword;
	
	// create price format
	DecimalFormat formatData = new DecimalFormat("#.##");
	
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.menu_list);
        
        // connect view objects with xml id
        imgNavBack = (ImageButton) findViewById(R.id.imgNavBack);
        prgLoading = (ProgressBar) findViewById(R.id.prgLoading);
        listMenu = (ListView) findViewById(R.id.listMenu);
        txtTitle = (TextView) findViewById(R.id.txtTitle);
        edtKeyword = (EditText) findViewById(R.id.edtKeyword);
        btnSearch = (ImageButton) findViewById(R.id.btnSearch);
        txtAlert = (TextView) findViewById(R.id.txtAlert);
        imgRefresh = (ImageButton) findViewById(R.id.imgRefresh);
        
        // menu API url
        MenuAPI = Utils.MenuAPI+"?accesskey="+Utils.AccessKey+"&category_id=";
        // tax and currency API url
        TaxCurrencyAPI = Utils.TaxCurrencyAPI+"?accesskey="+Utils.AccessKey;
        
        // get category id and category name that sent from previous page
        Intent iGet = getIntent();
        Category_ID = iGet.getLongExtra("category_id",0);
        Category_name = iGet.getStringExtra("category_name");
        MenuAPI += Category_ID;
        
        // set category name to textview
        txtTitle.setText(Category_name);
        
        mla = new MenuListAdapter(MenuList.this);
       
        // call asynctask class to request tax and currency data from server
        new getTaxCurrency().execute();
		
        // event listener to handle search button when clicked
		btnSearch.setOnClickListener(new OnClickListener() {
			
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				// get keyword and send it to server
				Keyword = edtKeyword.getText().toString();
				MenuAPI += "&keyword="+Keyword;
				IOConnect = 0;
    			listMenu.invalidateViews();
    			clearData();
				new getDataTask().execute();
			}
		});
		
		// event listener to handle refresh button when clicked
		imgRefresh.setOnClickListener(new OnClickListener() {
			
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				// re-request data from server by calling asynctask class
				IOConnect = 0;
				listMenu.invalidateViews();
    			clearData();
    	        new getTaxCurrency().execute();
			}
		});
		
		// event listener to handle list when clicked
		listMenu.setOnItemClickListener(new OnItemClickListener() {

			public void onItemClick(AdapterView<?> arg0, View arg1, int position,
					long arg3) {
				// TODO Auto-generated method stub
				// go to menu detail page
				Intent iDetail = new Intent(MenuList.this, MenuDetail.class);
				iDetail.putExtra("menu_id", Menu_ID.get(position));
				startActivity(iDetail);
			}
		});
        
		// event listener to handle back button when clicked
        imgNavBack.setOnClickListener(new OnClickListener() {
			
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				// back to previous page
				finish();
			}
		});
        
    }
    
    // asynctask class to handle parsing json in background
    public class getTaxCurrency extends AsyncTask<Void, Void, Void>{
    	
    	// show progressbar first
    	getTaxCurrency(){
    		if(!prgLoading.isShown()){
    			prgLoading.setVisibility(0);
				txtAlert.setVisibility(8);
    		}
    	}
    	
		@Override
		protected Void doInBackground(Void... arg0) {
			// TODO Auto-generated method stub
			// parse json data from server in background
			parseJSONDataTax();
			return null;
		}
    	
		@Override
		protected void onPostExecute(Void result) {
			// TODO Auto-generated method stub
			// when finish parsing, hide progressbar
			prgLoading.setVisibility(8);
			// if internet connection and data available request menu data from server
			// otherwise, show alert text
			if((Currency != null) && IOConnect == 0){
				new getDataTask().execute();
			}else{
				txtAlert.setVisibility(0);
			}
		}
    }

    // method to parse json data from server
	public void parseJSONDataTax(){
		try {
	        // request data from tax and currency API
	        HttpClient client = new DefaultHttpClient();
			HttpConnectionParams.setConnectionTimeout(client.getParams(), 15000);
			HttpConnectionParams.setSoTimeout(client.getParams(), 15000);
	        HttpUriRequest request = new HttpGet(TaxCurrencyAPI);
			HttpResponse response = client.execute(request);
			InputStream atomInputStream = response.getEntity().getContent();
	
			BufferedReader in = new BufferedReader(new InputStreamReader(atomInputStream));
		        
	        String line;
	        String str = "";
	        while ((line = in.readLine()) != null){
	        	str += line;
	        }
    
	        
	        // parse json data and store into tax and currency variables
			JSONObject json = new JSONObject(str);
			JSONArray data = json.getJSONArray("data"); // this is the "items: [ ] part
			
			JSONObject object_tax = data.getJSONObject(0); 
			JSONObject tax = object_tax.getJSONObject("tax_n_currency");
			    
			Tax = Double.parseDouble(tax.getString("Value"));
			
			JSONObject object_currency = data.getJSONObject(1); 
			JSONObject currency = object_currency.getJSONObject("tax_n_currency");
			    
			Currency = currency.getString("Value");
			
			
		} catch (MalformedURLException e) {
		    // TODO Auto-generated catch block
		    e.printStackTrace();
		} catch (IOException e) {
			IOConnect = 1;
		    // TODO Auto-generated catch block
		    e.printStackTrace();
		} catch (JSONException e) {
		    // TODO Auto-generated catch block
		    e.printStackTrace();
		}	
	}
    
	// clear arraylist variables before used
    void clearData(){
    	Menu_ID.clear();
    	Menu_name.clear();
    	Menu_price.clear();
    	Menu_image.clear();
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
			
			// if data available show data on list
			// otherwise, show alert text
			if(Menu_ID.size() > 0){
				listMenu.setVisibility(0);
				listMenu.setAdapter(mla);
			}else{
				txtAlert.setVisibility(0);
			}
			
		}
    }
    
    // method to parse json data from server
    public void parseJSONData(){
    	
    	clearData();
    	
    	try {
	        // request data from menu API
	        HttpClient client = new DefaultHttpClient();
	        HttpConnectionParams.setConnectionTimeout(client.getParams(), 15000);
			HttpConnectionParams.setSoTimeout(client.getParams(), 15000);
	        HttpUriRequest request = new HttpGet(MenuAPI);
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
			JSONArray data = json.getJSONArray("data"); // this is the "items: [ ] part
			
			for (int i = 0; i < data.length(); i++) {
			    JSONObject object = data.getJSONObject(i); 
			    
			    JSONObject menu = object.getJSONObject("Menu");
			    
			    Menu_ID.add(Long.parseLong(menu.getString("Menu_ID")));
			    Menu_name.add(menu.getString("Menu_name"));
			    Menu_price.add(Double.valueOf(formatData.format(menu.getDouble("Price"))));
			    Menu_image.add(menu.getString("Menu_image"));
				    
			}
				
				
		} catch (MalformedURLException e) {
		    // TODO Auto-generated catch block
		    e.printStackTrace();
		} catch (IOException e) {
		    // TODO Auto-generated catch block
		    e.printStackTrace();
		} catch (JSONException e) {
		    // TODO Auto-generated catch block
		    e.printStackTrace();
		}	
    }


    @Override
    protected void onDestroy() {
    	// TODO Auto-generated method stub
    	mla.imageLoader.clearCache();
    	listMenu.setAdapter(null);
    	super.onDestroy();
    }
	 
    
    @Override
	public void onConfigurationChanged(final Configuration newConfig)
	{
	    // Ignore orientation change to keep activity from restarting
	    super.onConfigurationChanged(newConfig);
	}

    
}


package com.the.restaurant;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

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
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class News extends Activity {
	
	// declare view objects
	ListView listNews;
	ProgressBar prgLoading;
	TextView txtAlert;
	ImageButton imgNavBack, imgRefresh;
	
	// declare adapter object to create custom news list
	NewsListAdapter la;
	
	// create arraylist variables to store data from server
	static ArrayList<String> Title = new ArrayList<String>();
	static ArrayList<String> PubDate = new ArrayList<String>();
	static ArrayList<String> Link = new ArrayList<String>();
	
	// declare objects for parsing rss feeds
	URL Feed;
	DocumentBuilder db;
	Document doc;
	NodeList nodeList;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.news_list);
        
        listNews = (ListView) findViewById(R.id.listNews);
        prgLoading = (ProgressBar) findViewById(R.id.prgLoading);
        txtAlert = (TextView) findViewById(R.id.txtAlert);
        imgNavBack = (ImageButton) findViewById(R.id.imgNavBack);
        imgRefresh = (ImageButton) findViewById(R.id.imgRefresh);

        la = new NewsListAdapter(this);
        
        // call asynctask class to request latest news from rss feed
		new getDataTask().execute();
        
		// event listener to handle back button when pressed
		imgNavBack.setOnClickListener(new OnClickListener() {
			
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				// back to previous page
				finish();
			}
		});
		
		// event listener to handle refresh button when pressed
		imgRefresh.setOnClickListener(new OnClickListener() {
			
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				// re-request data
				listNews.invalidateViews();
    			clearData();
				new getDataTask().execute();
			}
		});
		
		// event listener to handle list when pressed
		listNews.setOnItemClickListener(new OnItemClickListener() {

			public void onItemClick(AdapterView<?> arg0, View arg1, int position,
					long arg3) {
				// TODO Auto-generated method stub
				// go to news detail
				Intent iDetail = new Intent(News.this, NewsDetail.class);
		    	iDetail.putExtra("title", Title.get(position));
		    	iDetail.putExtra("link", Link.get(position));
		    	startActivity(iDetail);
				
			}
		});
        
        
      
    }
    
    // asynctask class to handle parsing rss feed in background
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
			// parse rss feed from server in background
			getDataFromFeed();
			return null;
		}
    	
		@Override
		protected void onPostExecute(Void result) {
			// TODO Auto-generated method stub
			// when finish parsing, hide progressbar
			prgLoading.setVisibility(8);
			// if data available request menu data from server
			// otherwise, show alert text
			if(Title.size() > 0){
				listNews.setVisibility(0);
				listNews.setAdapter(la);
			}else{
				txtAlert.setVisibility(0);
			}
		}
    }
    
    // clear arraylist variables before used
    void clearData(){
    	Title.clear();
    	PubDate.clear();
    	Link.clear();
    }
    
    // method to parse rss feed
    public void getDataFromFeed(){
    	
    	clearData();
    	
		try {
			// request rss feed
			Feed = new URL(getString(R.string.feedurl));
			DocumentBuilderFactory dbf= DocumentBuilderFactory.newInstance();
	    	db = dbf.newDocumentBuilder();
	    	doc = db.parse(new InputSource(Feed.openStream()));
			doc.getDocumentElement().normalize();
	    	nodeList = doc.getElementsByTagName("item");
	    	
	    	// parse rss feed and store into arraylist variables
	    	for(int i=0;i<nodeList.getLength();i++){
	    		Node node = nodeList.item(i);
	    		
	    		Element fstElmnt = (Element) node;
	    		
	    		NodeList titleList = fstElmnt.getElementsByTagName("title");
	    		Element titleElement = (Element) titleList.item(0);
	    		titleList = titleElement.getChildNodes();
	    		Title.add(((Node) titleList.item(0)).getNodeValue());

	    		
	    		NodeList pubDateList = fstElmnt.getElementsByTagName("pubDate");
	    		Element pubDateElement = (Element) pubDateList.item(0);
	    		pubDateList = pubDateElement.getChildNodes();
	    		PubDate.add(((Node) pubDateList.item(0)).getNodeValue());
	    		
	    		
	    		NodeList linkList = fstElmnt.getElementsByTagName("link");
	    		Element linkElement = (Element) linkList.item(0);
	    		linkList = linkElement.getChildNodes();
	    		Link.add(((Node) linkList.item(0)).getNodeValue());
	    		
	    	}
	    	
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    }
   
    
    @Override
	public void onConfigurationChanged(final Configuration newConfig)
	{
	    // Ignore orientation change to keep activity from restarting
	    super.onConfigurationChanged(newConfig);
	}
    
    
}
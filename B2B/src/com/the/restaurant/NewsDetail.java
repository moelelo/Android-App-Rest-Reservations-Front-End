package com.the.restaurant;


import com.the.restaurant.R;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

public class NewsDetail extends Activity {
	
	// declare view objects
    WebView web;
    ProgressBar prgPageLoading;
    ImageButton imgNavBack, imgShare;
    
    // declare string variables to store article title and link
    String Title, Link;
    
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);        
        setContentView(R.layout.news_detail);
        
        // connect view objects and xml id
        web = (WebView) findViewById(R.id.web);
        prgPageLoading = (ProgressBar) findViewById(R.id.prgPageLoading);
        imgNavBack = (ImageButton) findViewById(R.id.imgNavBack);
        imgShare = (ImageButton) findViewById(R.id.imgShare);

        // get article title and link that snet from previous page
        Intent iGet = getIntent();
        Title = iGet.getStringExtra("title");
        Link = iGet.getStringExtra("link");
        
        // set webview
        web.setHorizontalScrollBarEnabled(true); 
        web.getSettings().setAllowFileAccess(true);
        setProgressBarVisibility(true);
        web.getSettings().setBuiltInZoomControls(true);
        web.getSettings().setUseWideViewPort(true);
        web.setInitialScale(1);
        
        // load article url to webview
        web.loadUrl(Link);
        
        // event listener to handle share button when pressed
        imgShare.setOnClickListener(new OnClickListener() {
			
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				// share article to other apps
				Intent iShare = new Intent(Intent.ACTION_SEND);
				iShare.setType("text/plain");
				iShare.putExtra(Intent.EXTRA_SUBJECT,Title);
				iShare.putExtra(Intent.EXTRA_TEXT, Link);
				startActivity(Intent.createChooser(iShare, "Share via"));
			}
		});
        
        // event listener to handle back button when pressed
        imgNavBack.setOnClickListener(new OnClickListener() {
    		
    		public void onClick(View v) {
    			// TODO Auto-generated method stub
    			// back to previous page
    			finish();
    		}
    	});
        
        final Activity act = this;
        
        // event listener to handle webview, show progressbar when loading page
        web.setWebChromeClient(new WebChromeClient(){
        	public void onProgressChanged(WebView webview, int progress){
        		
        		act.setProgress(progress*100);
        		prgPageLoading.setProgress(progress);
        		
        	}
        	
        	
        });
        
        // event listener to handle webview when start and finish loading page
        web.setWebViewClient(new WebViewClient() {
        	@Override
            public void onPageStarted( WebView view, String url, Bitmap favicon ) {

                super.onPageStarted( web, url, favicon );
                prgPageLoading.setVisibility(View.VISIBLE);
            }

            @Override
            public void onPageFinished( WebView view, String url ) {

                super.onPageFinished( web, url );
                prgPageLoading.setProgress(0);
                prgPageLoading.setVisibility(View.GONE);
                
            }   
        	public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
        	     Toast.makeText(act, description, Toast.LENGTH_SHORT).show();
        	   }
        	
        	
        	});
        
        	
        		
    }
    
    // read hard key event, when hard back key pressed
    // go back to previous web page
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(event.getAction() == KeyEvent.ACTION_DOWN){
            switch(keyCode)
            {
            case KeyEvent.KEYCODE_BACK:
                if(web.canGoBack() == true){
                    web.goBack();
                }else{
                    finish();
                }
                return true;
            }

        }
        
        return super.onKeyDown(keyCode, event);
    }

    
    @Override
	public void onConfigurationChanged(final Configuration newConfig)
	{
	    // Ignore orientation change to keep activity from restarting
	    super.onConfigurationChanged(newConfig);
	}
    
}
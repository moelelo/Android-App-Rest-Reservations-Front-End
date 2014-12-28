package com.the.restaurant;


import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;


import org.json.JSONObject;
import org.json.JSONTokener;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.facebook.android.AsyncFacebookRunner;
import com.facebook.android.BaseRequestListener;
import com.facebook.android.DialogError;
import com.facebook.android.Facebook;
import com.facebook.android.Facebook.DialogListener;
import com.facebook.android.FacebookError;
import com.facebook.android.SessionStore;
import com.the.restaurant.R;
import com.twitter.android.TwitterApp;
import com.twitter.android.TwitterApp.TwDialogListener;

public class ShareMenu extends Activity{
	
	// declare objects for Facebook and TwitterApp class
	Facebook mFacebook;
	TwitterApp mTwitter;
	
	// declare view objects
	EditText edtMessage;
	CheckBox chkFacebook, chkTwitter;
	Button btnPost;
	ImageButton imgNavBack;
	
	
	// declare strings
	String App_name, Menu_name, Menu_description, Menu_image;
	long Menu_ID;
	
	String MenuSharingPage;
	
	private boolean postToTwitter = false;
	private ProgressDialog mProgress;
	private Handler mRunOnUi = new Handler();
	private static final String[] PERMISSIONS = new String[] {"publish_stream", "read_stream", "offline_access"};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.share_menu);
		
		// connect view objects and xml id
		imgNavBack = (ImageButton) findViewById(R.id.imgNavBack);
        edtMessage = (EditText) findViewById(R.id.edtMessage);
		chkFacebook = (CheckBox) findViewById(R.id.chkFacebook);
		chkTwitter = (CheckBox) findViewById(R.id.chkTwitter);
		btnPost = (Button) findViewById(R.id.btnPost);
		
		// get values that sent from previous page and store them to string variables
		Intent i_get = getIntent();
		Menu_ID = i_get.getLongExtra("menu_id", 0);
		Menu_name = i_get.getStringExtra("menu_name");
		Menu_description = i_get.getStringExtra("menu_description");
		Menu_image = i_get.getStringExtra("menu_image");
		App_name = getString(R.string.app_name);
		
		MenuSharingPage = Utils.MenuSharingAPI+"?id="+Menu_ID;
		
		// set objects of Facebook and TwitterAp class
		mProgress = new ProgressDialog(this);
        mFacebook = new Facebook(getString(R.string.facebook_app_id));
        mTwitter = new TwitterApp(this, 
        		getString(R.string.twitter_consumer_key),
        		getString(R.string.twitter_secret_key));
        
        // restore facebook session
        SessionStore.restore(mFacebook, this);
		
        // checking facebook session
        if (mFacebook.isSessionValid()) {
			chkFacebook.setChecked(true);
			String name = SessionStore.getName(this);
		}
        
        // checking twitter token
        mTwitter.setListener(mTwLoginDialogListener);
		
		if (mTwitter.hasAccessToken()) {
			chkTwitter.setChecked(true);
			
		}
		
		// event listener to handle back button when pressed
		imgNavBack.setOnClickListener(new OnClickListener() {
			
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				finish();
			}
		});

		// event listener to handle facebook checkbox when checked
        chkFacebook.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				// TODO Auto-generated method stub
				onFacebookClick();
			}
		});

		// event listener to handle twitter checkbox when checked
        chkTwitter.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				// TODO Auto-generated method stub
				onTwitterClick();
			}
		});
        
        // event listener to handle post button when pressed
        btnPost.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
				// get text from edittext and store to string variable
				String review = edtMessage.getText().toString();
				
				// check to value of review
				if(review.equals("")){
					review = Menu_name+" at "+getString(R.string.app_name) +" ";
				}else{
					review +=" - "+Menu_name+" at "+getString(R.string.app_name) +" ";
				}
				
				// check the checkbox and post to both facebook and twitter or one of them
				if(chkFacebook.isChecked() && chkTwitter.isChecked()) {
					postReview(review);
					if (postToTwitter) {
						postToFacebook(review);
						postToTwitter(review);
					}
				}else if(chkFacebook.isChecked() && !chkTwitter.isChecked()){
					postToFacebook(review);
				}else if(!chkFacebook.isChecked() && chkTwitter.isChecked()){
					postReview(review);
					if (postToTwitter) postToTwitter(review);
				}else{
					Toast.makeText(ShareMenu.this, getString(R.string.select_share), Toast.LENGTH_SHORT).show();
				}
			}
		});
	}
	
	// check the value of review, if more than 110 show toast message
	private void postReview(String review) {
		Toast.makeText(this, getString(R.string.post_review), Toast.LENGTH_SHORT).show();
		
		if(review.length()>110){
			Toast.makeText(this, getString(R.string.post_limit), Toast.LENGTH_SHORT).show();
		}else{
			postToTwitter = true;
			
		}	
	}
	
	// method to check if twitter token not available, authorize user
	private void onTwitterClick() {
		if (!mTwitter.hasAccessToken()) {
			chkTwitter.setChecked(false);
			mTwitter.authorize();
		}
		
	}
	
	// method to post status to twitter
	private void postToTwitter(final String review) {
		new Thread() {
			@Override
			public void run() {
				int what = 0;
				
				try {
					mTwitter.updateStatus(review+MenuSharingPage);
					
				} catch (Exception e) {
					what = 1;
				}
				
				mHandlerTwitter.sendMessage(mHandlerTwitter.obtainMessage(what));
			}
		}.start();
	}

	// event handler for reading twitter posting result
	private Handler mHandlerTwitter = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			String text = (msg.what == 0) ? getString(R.string.post_to_twitter) : getString(R.string.post_to_twitter_failed);
			
			Toast.makeText(ShareMenu.this, text, Toast.LENGTH_SHORT).show();
			
			if(!(chkFacebook.isChecked() && chkTwitter.isChecked())) {
				finish();
			}
		}
	};
	
	// event listener to read twitter username from twitter dialog ... ready made
	private final TwDialogListener mTwLoginDialogListener = new TwDialogListener() {
		
		public void onComplete(String value) {
			String username = mTwitter.getUsername();
			username		= (username.equals("")) ? getString(R.string.no_name) : username;
		
			chkTwitter.setChecked(true);
			
			Toast.makeText(ShareMenu.this, getString(R.string.connect_twitter)+" "+ username, Toast.LENGTH_LONG).show();
		}
		
		public void onError(String value) {
			chkTwitter.setChecked(false);
			
			Toast.makeText(ShareMenu.this, getString(R.string.connect_twitter_failed), Toast.LENGTH_LONG).show();
		}
	};
	
	// method to post status to facebook ... ready made
	private void postToFacebook(String review) {	
		mProgress.setMessage("Posting ...");
		mProgress.show();
		
		AsyncFacebookRunner mAsyncFbRunner = new AsyncFacebookRunner(mFacebook);
		
		Bundle params = new Bundle();
    		
		params.putString("message", review);
		params.putString("name", Menu_name);
		params.putString("caption", App_name);
		params.putString("link", MenuSharingPage);
		params.putString("description", Menu_description);
		params.putString("picture", Utils.AdminPageURL+Menu_image);
		
		
		mAsyncFbRunner.request("me/feed", params, "POST", new WallPostListener());
	}

	// event listener to read facebook posting result
	private final class WallPostListener extends BaseRequestListener {
        public void onComplete(final String response) {
        	mRunOnUi.post(new Runnable() {
        		
        		public void run() {
        			mProgress.cancel();
        			
        			Toast.makeText(ShareMenu.this, getString(R.string.post_to_facebook), Toast.LENGTH_SHORT).show();
        			finish();
        		}
        	});
        }

		public void onComplete(String response, Object state) {
			// TODO Auto-generated method stub
			
		}

		public void onIOException(IOException e, Object state) {
			// TODO Auto-generated method stub
			
		}

		public void onFileNotFoundException(FileNotFoundException e,
				Object state) {
			// TODO Auto-generated method stub
			
		}

		public void onMalformedURLException(MalformedURLException e,
				Object state) {
			// TODO Auto-generated method stub
			
		}

		public void onFacebookError(FacebookError e, Object state) {
			// TODO Auto-generated method stub
			
		}
    }
	
	// method to check facebook session, if not valid, authorize user
	private void onFacebookClick() {
		if (!mFacebook.isSessionValid()) {
			chkFacebook.setChecked(false);
			mFacebook.authorize(this, PERMISSIONS, -1, new FbLoginDialogListener());
		}
	}
    
	// event listener to get facebook name from facebook dialog
    private final class FbLoginDialogListener implements DialogListener {
        public void onComplete(Bundle values) {
            SessionStore.save(mFacebook, ShareMenu.this);
           
            chkFacebook.setChecked(true);
			 
            getFbName();
        }

        public void onFacebookError(FacebookError error) {
           Toast.makeText(ShareMenu.this, "Facebook connection failed", Toast.LENGTH_SHORT).show();
           
           chkFacebook.setChecked(false);
        }
        
        public void onError(DialogError error) {
        	Toast.makeText(ShareMenu.this, "Facebook connection failed", Toast.LENGTH_SHORT).show(); 
        	
        	chkFacebook.setChecked(false);
        }

        public void onCancel() {
        	chkFacebook.setChecked(false);
        }
    }
    
    // method to get facebook name
	private void getFbName() {
		mProgress.setMessage("Finalizing ...");
		mProgress.show();
		
		new Thread() {
			@Override
			public void run() {
		        String name = "";
		        int what = 1;
		        
		        try {
		        	String me = mFacebook.request("me");
		        	
		        	JSONObject jsonObj = (JSONObject) new JSONTokener(me).nextValue();
		        	name = jsonObj.getString("name");
		        	what = 0;
		        } catch (Exception ex) {
		        	ex.printStackTrace();
		        }
		        
		        mFbHandler.sendMessage(mFbHandler.obtainMessage(what, name));
			}
		}.start();
	}
	
	// event handler to read facebook authentication
	private Handler mFbHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			mProgress.dismiss();
			
			if (msg.what == 0) {
				String username = (String) msg.obj;
		        username = (username.equals("")) ? getString(R.string.no_name) : username;
		            
		        SessionStore.saveName(username, ShareMenu.this);
		        
		         
		        Toast.makeText(ShareMenu.this, getString(R.string.connect_facebook)+" "+ username, Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(ShareMenu.this, getString(R.string.connect_facebook_2), Toast.LENGTH_SHORT).show();
			}
		}
	};
	
}

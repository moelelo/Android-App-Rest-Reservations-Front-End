package com.the.restaurant;

import org.json.JSONObject;
import org.json.JSONTokener;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.pm.PackageInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.Toast;

import com.facebook.android.DialogError;
import com.facebook.android.Facebook;
import com.facebook.android.Facebook.DialogListener;
import com.facebook.android.FacebookError;
import com.facebook.android.SessionStore;
import com.the.restaurant.R;
import com.twitter.android.TwitterApp;
import com.twitter.android.TwitterApp.TwDialogListener;


public class SettingPreferences extends PreferenceActivity{
	
	// declare facebook and twitter objects
	Facebook mFacebook;
	TwitterApp mTwitter;
	PackageInfo pInfo;
	
	// declare view objects
	ProgressDialog mProgress;
	ImageButton imgNavBack;
	CheckBoxPreference chkFacebook, chkTwitter;
	
	// create array variable for permission info
	private static final String[] PERMISSIONS = new String[] {"publish_stream", "read_stream", "offline_access"};
	
	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.setting_preferences);
		setContentView(R.layout.setting);
		
		// connect view objects and xml id
		chkFacebook = (CheckBoxPreference) findPreference("facebook");
		chkTwitter = (CheckBoxPreference) findPreference("twitter");
		imgNavBack = (ImageButton) findViewById(R.id.imgNavBack);
		
	    // set progress dialog, facebook id, and twitter key
		mProgress = new ProgressDialog(this);
        mFacebook = new Facebook(getString(R.string.facebook_app_id));
        mTwitter = new TwitterApp(this, 
        		getString(R.string.twitter_consumer_key),
        		getString(R.string.twitter_secret_key));

        SessionStore.restore(mFacebook, this);
        
        // check facebook session
        if (mFacebook.isSessionValid()) {
			chkFacebook.setChecked(true);
		}
        
        // event listener for twitter
        mTwitter.setListener(mTwLoginDialogListener);
		
		if (mTwitter.hasAccessToken()) {
			chkTwitter.setChecked(true);	
		}
		
		// event listener to handle back button when pressed
		imgNavBack.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				// TODO Auto-generated method stub
				// bak to previous page
				finish();
			}
		});
		
		// event listener to handle checkbox button when pressed
		chkFacebook.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			
			public boolean onPreferenceClick(Preference preference) {
				// TODO Auto-generated method stub
				onFacebookClick();
				return false;
			}
		});
		
		// event listener to handle checkbox button when pressed
		chkTwitter.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			
			public boolean onPreferenceClick(Preference preference) {
				// TODO Auto-generated method stub
				onTwitterClick();
				return false;
			}
		});
	}
	
	// event listener to read twitter username from twitter dialog
	private final TwDialogListener mTwLoginDialogListener = new TwDialogListener() {
		
		public void onComplete(String value) {
			String username = mTwitter.getUsername();
			username		= (username.equals("")) ? getString(R.string.no_name) : username;
		
			chkTwitter.setChecked(true);
			
			Toast.makeText(SettingPreferences.this, getString(R.string.connect_twitter)+" "+ username, Toast.LENGTH_LONG).show();
		}
		
		public void onError(String value) {
			chkTwitter.setChecked(false);
			
			Toast.makeText(SettingPreferences.this, getString(R.string.connect_twitter_failed), Toast.LENGTH_LONG).show();
		}
	};
	
	// method to check whether twitter token is available or not
	private void onTwitterClick() {
		if (mTwitter.hasAccessToken()) {
			// if available, show alert dialog and confirm to delete twitter account
			final AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle(getString(R.string.confirm));
			builder.setMessage(getString(R.string.delete_twitter_connection))
			       .setCancelable(false)
			       .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
			           public void onClick(DialogInterface dialog, int id) {
			        	   mTwitter.resetAccessToken();
			        	   
			        	   chkTwitter.setChecked(false);
			           }
			       })
			       .setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
			           public void onClick(DialogInterface dialog, int id) {
			                dialog.cancel();
			                
			                chkTwitter.setChecked(true);
			           }
			       });
			final AlertDialog alert = builder.create();
			
			alert.show();
		} else {
			// otherwise, authorize user
			chkTwitter.setChecked(false);
			
			mTwitter.authorize();
		}
	}

	// method to check whether facebook session is valid or not
	private void onFacebookClick() {
		if (mFacebook.isSessionValid()) {
			// if yes show alert dialog and confirm to delete facebook account
			final AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle(getString(R.string.confirm));
			builder.setMessage(getString(R.string.delete_facebook_connection))
			       .setCancelable(false)
			       .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
			           public void onClick(DialogInterface dialog, int id) {
			        	   fbLogout();
			           }
			       })
			       .setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
			           public void onClick(DialogInterface dialog, int id) {
			                dialog.cancel();
			                
			                chkFacebook.setChecked(true);
			           }
			       });
			
			final AlertDialog alert = builder.create();
			
			alert.show();
		} else {
			// otherwise, authorize user
			chkFacebook.setChecked(false);
			
			mFacebook.authorize(this, PERMISSIONS, -1, new FbLoginDialogListener());
		}
	}
    
	// event listener to get facebook name from facebook dialog
    private final class FbLoginDialogListener implements DialogListener {
        public void onComplete(Bundle values) {
            SessionStore.save(mFacebook, SettingPreferences.this);
           
            chkFacebook.setChecked(true);
			 
            getFbName();
        }

        public void onFacebookError(FacebookError error) {
           Toast.makeText(SettingPreferences.this, getString(R.string.connect_facebook_failed), Toast.LENGTH_SHORT).show();
           
           chkFacebook.setChecked(false);
        }
        
        public void onError(DialogError error) {
        	Toast.makeText(SettingPreferences.this, getString(R.string.connect_facebook_failed), Toast.LENGTH_SHORT).show(); 
        	
        	chkFacebook.setChecked(false);
        }

        public void onCancel() {
        	chkFacebook.setChecked(false);
        }
    }
    
    // method to get facebook name ... ready made
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
	
	// method to logout facebook account
	private void fbLogout() {
		mProgress.setMessage(getString(R.string.disconnect_facebook));
		mProgress.show();
			
		new Thread() {
			@Override
			public void run() {
				SessionStore.clear(SettingPreferences.this);
		        	   
				int what = 1;
					
		        try {
		        	mFacebook.logout(SettingPreferences.this);
		        		 
		        	what = 0;
		        } catch (Exception ex) {
		        	ex.printStackTrace();
		        }
		        	
		        mHandler.sendMessage(mHandler.obtainMessage(what));
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
			            
			        SessionStore.saveName(username, SettingPreferences.this);
			        
			         
			        Toast.makeText(SettingPreferences.this, getString(R.string.connect_facebook)+" "+ username, Toast.LENGTH_SHORT).show();
				} else {
					Toast.makeText(SettingPreferences.this, getString(R.string.connect_facebook_2), Toast.LENGTH_SHORT).show();
				}
			}
		};
	
	// event handler to read facebook logout
	private Handler mHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				mProgress.dismiss();
				
				if (msg.what == 1) {
					Toast.makeText(SettingPreferences.this, getString(R.string.logout_facebook_failed), Toast.LENGTH_SHORT).show();
				} else {
					chkFacebook.setChecked(false);
					
		        	   
					Toast.makeText(SettingPreferences.this, getString(R.string.disconnect_facebook_2), Toast.LENGTH_SHORT).show();
				}
			}
		};
}
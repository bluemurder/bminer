/*
 *  bminer - the first bitcoin miner for Android OS 
 *  Copyright (C) 2013  Alessio Leoncini
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see http://www.gnu.org/licenses/
 */



package bluemurder.miner;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class BminerActivity extends Activity {
	
	TextView tv_info;
	TextView tv_speed;
	TextView tv_status;
	MinerTask minerTask = new MinerTask(this);
	public String rpchost;
	public String rpcport;
	public String rpcuser;
	public String rpcpass;
	
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        // Initialize strings on screen
        tv_info = (TextView) findViewById(R.id.tv_info);
        tv_speed = (TextView) findViewById(R.id.tv_speed);
        tv_status = (TextView) findViewById(R.id.tv_status);
        tv_status.setText("Not running");
        
        // Read preferences
        getPrefs();
 
        // Start button actions
        final Button b_start = (Button) findViewById(R.id.b_start);
        b_start.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(minerTask.running==false){
					// running flag=1, launch mining task
					InitTask();
					minerTask.running=true;
					minerTask.execute(0);//////////////////////////////////////////////////
					//tv_status.setText(rpchost+"\n"+rpcport+"\n"+rpcuser+"\n"+rpcpass);//////////////////////////////////////////////
				}
				else {
					minerTask.running=false;
					tv_status.setText("Stopped");
				}
			}
		});
        
        // Preferences button actions
        final Button b_prefs = (Button) findViewById(R.id.b_prefs);
        b_prefs.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent settingsActivity = new Intent(getBaseContext(),Preferences.class);
				startActivity(settingsActivity);
			}
		});
    }
    
    public void InitTask(){
    	minerTask = new MinerTask(this);
    }
    
    // Set speed value
    public void SetSpeed(final String s){
    	runOnUiThread(new Runnable() {
            @Override
            public void run() {
            	tv_speed.setText(s);
            }
        });
    }
    
    // Set info value
    public void SetInfo(final String s){
    	runOnUiThread(new Runnable() {
            @Override
            public void run() {
            	tv_info.setText(s);
            }
        });
    }
    
    // Set status value
    public void SetStatus(final String s){
    	runOnUiThread(new Runnable() {
            @Override
            public void run() {
            	tv_status.setText(s);
            }
        });
    }
    
	private void getPrefs() {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);		
		rpchost = prefs.getString("editTextPrefHost","Nothing has been entered");
		rpcport = prefs.getString("editTextPrefPort","Nothing has been entered");
		rpcuser = prefs.getString("editTextPrefUser","Nothing has been entered");
		rpcpass = prefs.getString("editTextPrefPass","Nothing has been entered");
	}
	
}
package com.example.kfacheckin;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import sql.DbHelper;
import android.app.ActionBar.LayoutParams;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FilterQueryProvider;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class Index extends ActionBarActivity implements
		NavigationDrawerFragment.NavigationDrawerCallbacks {
	
	private static SQLiteDatabase mDbRW;
	private static String DbName = "kfacheckin";
	private static String DbStaffsTableSrt = "staffs";
	private static String DbCheckinTableSrt = "checkin";
	private static Index mContext;
	private NfcAdapter mNfcAdapter = null;
	private PendingIntent mNfcPendingIntent = null;
	PlaceholderFragment1 pf;
	static AlertDialog alertDialog = null;
	
	/**
	 * Fragment managing the behaviors, interactions and presentation of the
	 * navigation drawer.
	 */
	private NavigationDrawerFragment mNavigationDrawerFragment;

	/**
	 * Used to store the last screen title. For use in
	 * {@link #restoreActionBar()}.
	 */
	private CharSequence mTitle;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_index);

		mNavigationDrawerFragment = (NavigationDrawerFragment) getSupportFragmentManager()
				.findFragmentById(R.id.navigation_drawer);
		mTitle = getTitle();

		// Set up the drawer.
		mNavigationDrawerFragment.setUp(R.id.navigation_drawer,
				(DrawerLayout) findViewById(R.id.drawer_layout));
		
		mContext = this;
		
		DbHelper Dbhp = new DbHelper(getApplicationContext(), DbName, null, 1);
		
		mDbRW = Dbhp.getReadableDatabase();
		Cursor cursor = mDbRW.rawQuery("Select * from " + DbCheckinTableSrt, null);
		if(cursor.getCount()>0){		

		}else{

		}
		checkNFCFunction();		
		initNFC();		
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		enableForegroundDispatch();
	}
	
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		disableForegroundDispatch();
	}
	//NFC
	private void initNFC(){
		mNfcPendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
	}
	private void checkNFCFunction() {
		mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
		if(mNfcAdapter == null){
			Toast.makeText(this, "no NFC Adapter", Toast.LENGTH_SHORT).show();
		}else {
			if(!mNfcAdapter.isEnabled()){
				Toast.makeText(this, "NFC Adapter Disabled", Toast.LENGTH_SHORT).show();
			}
		}
	}
	@Override
	protected void onNewIntent(Intent intent) {
		// TODO Auto-generated method stub
		//super.onNewIntent(intent);
		setIntent(intent);			
			if(NfcAdapter.ACTION_NDEF_DISCOVERED.equals(getIntent().getAction())){
				resolveIntent(getIntent());
			}else if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(getIntent().getAction())) {
				resolveIntent(getIntent());
			}else if(NfcAdapter.ACTION_TAG_DISCOVERED.equals(getIntent().getAction())){
				resolveIntent(getIntent());
			}
	}	
	void resolveIntent(Intent intent)
	{
		String actoin = intent.getAction();
		if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(actoin)) {
			NdefMessage[] messages = null;
			Parcelable[] rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
			if(rawMsgs !=null){
				messages = new NdefMessage[rawMsgs.length];
				for(int i = 0;i< rawMsgs.length; i++){
					messages[i] = (NdefMessage) rawMsgs[i];
				}
			}else {
				byte[] empty = new byte[0];
                byte[] id = intent.getByteArrayExtra(NfcAdapter.EXTRA_ID);
                Parcelable tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
                byte[] payload = dumpTagData(tag).getBytes();
                NdefRecord record = new NdefRecord(NdefRecord.TNF_UNKNOWN, empty, id, payload);
                NdefMessage msg = new NdefMessage(new NdefRecord[] { record });
                messages = new NdefMessage[] { msg };
			}
			for(int i = 0; i < messages.length; i++){
				int length = messages[i].getRecords().length;
				NdefRecord[] records = messages[i].getRecords();
				for(int j = 0; j < length; j++){
					for(NdefRecord record : records){
						byte[] payload =record.getPayload();
						pf.cardIdEditText.setText(new String(payload));
					}
				}
			}
		}else {
			((Activity) mContext).finish();
			return;
		}
	}
	private String dumpTagData(Parcelable p) {
        StringBuilder sb = new StringBuilder();
        Tag tag = (Tag) p;
        byte[] id = tag.getId();
        sb.append(getReversed(id));       
        return sb.toString();
    }
	private long getReversed(byte[] bytes) {
        long result = 0;
        long factor = 1;
        //for (int i = bytes.length - 1; i >= 0; --i) {
        for (int i = 0; i<bytes.length; i++) {
            long value = bytes[i] & 0xff;
            result += value * factor;
            factor *= 256;
        }
        return result;
    }	
	private void enableForegroundDispatch(){
		if(mNfcAdapter != null){
			mNfcAdapter.enableForegroundDispatch(this, mNfcPendingIntent, null, null);
		}
	}
	private void disableForegroundDispatch(){
		if(mNfcAdapter != null){
			mNfcAdapter.disableForegroundDispatch(this);
		}
	}
	
	@Override
	public void onNavigationDrawerItemSelected(int position) {
		// update the main content by replacing fragments
		FragmentManager fragmentManager = getSupportFragmentManager();
		if(position==0)
		fragmentManager
				.beginTransaction()
				.replace(R.id.container,
						PlaceholderFragment.newInstance(position + 1)).commit();
		else if(position==1){
			pf = PlaceholderFragment1.newInstance(position + 1);
			fragmentManager
			.beginTransaction()
			.replace(R.id.container,
					pf).commit();
		}
		else if(position==2)
			fragmentManager
			.beginTransaction()
			.replace(R.id.container,
					PlaceholderFragment2.newInstance(position + 1)).commit();
	}

	public void onSectionAttached(int number) {
		switch (number) {
		case 1:
			mTitle = getString(R.string.title_section1);
			break;
		case 2:
			mTitle = getString(R.string.title_section2);
			break;
		case 3:
			mTitle = getString(R.string.title_section3);
			break;
		}
	}

	public void restoreActionBar() {
		ActionBar actionBar = getSupportActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
		actionBar.setDisplayShowTitleEnabled(true);
		actionBar.setTitle(mTitle);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		if (!mNavigationDrawerFragment.isDrawerOpen()) {
			// Only show items in the action bar relevant to this screen
			// if the drawer is not showing. Otherwise, let the drawer
			// decide what to show in the action bar.
			getMenuInflater().inflate(R.menu.index, menu);
			restoreActionBar();
			return true;
		}
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			Intent intent = new Intent();			
			intent.setClass(this,SettingActivity.class);			
			startActivity(intent);		
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {
		/**
		 * The fragment argument representing the section number for this
		 * fragment.
		 */
		private static final String ARG_SECTION_NUMBER = "section_number";

		/**
		 * Returns a new instance of this fragment for the given section number.
		 */
		public static PlaceholderFragment newInstance(int sectionNumber) {
			PlaceholderFragment fragment = new PlaceholderFragment();
			Bundle args = new Bundle();
			args.putInt(ARG_SECTION_NUMBER, sectionNumber);
			fragment.setArguments(args);
			return fragment;
		}

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_index,
					container, false);
			return rootView;
		}

		@Override
		public void onAttach(Activity activity) {
			super.onAttach(activity);
			((Index) activity).onSectionAttached(getArguments().getInt(
					ARG_SECTION_NUMBER));
		}
	}
	
	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment1 extends Fragment {
		/**
		 * The fragment argument representing the section number for this
		 * fragment.
		 */
		private static final String ARG_SECTION_NUMBER = "section_number";
		private SimpleCursorAdapter dataAdapter;
		Spinner spinner;
		Button sendEmailButton;
		TextView peoplenumTextView;
//		EditText classNameEditText;
		AutoCompleteTextView myAutoCompleteTextView;
		EditText dateEditText;
		EditText cardIdEditText;
		ListView listView;
		Button selectPeopleButton;
		ArrayList<String> _options = new ArrayList<String>();
		boolean[] _selections=null;
		ArrayList<String> tempLateArray ;
		Button showLatePeopleButton;
		InputMethodManager imm;
		 
		
		/**
		 * Returns a new instance of this fragment for the given section number.
		 */
		public static PlaceholderFragment1 newInstance(int sectionNumber) {
			PlaceholderFragment1 fragment = new PlaceholderFragment1();
			Bundle args = new Bundle();
			args.putInt(ARG_SECTION_NUMBER, sectionNumber);
			fragment.setArguments(args);
			return fragment;
		}

		public PlaceholderFragment1() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_index1,
					container, false);
			return rootView;
		}

		@Override
		public void onAttach(Activity activity) {
			super.onAttach(activity);
			((Index) activity).onSectionAttached(getArguments().getInt(
					ARG_SECTION_NUMBER));
		}
		
		@Override
		public void onActivityCreated(Bundle savedInstanceState) {
			super.onActivityCreated(savedInstanceState);
			imm =(InputMethodManager)mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
			listView = (ListView) this.getView().findViewById(R.id.listViewclass);
			spinner = (Spinner)this.getView().findViewById(R.id.checkinOroutSpinner);
			String[] lunch = {"簽到", "簽退"};
			ArrayAdapter<String> lunchList = new ArrayAdapter<String>(mContext,R.layout.myspinner, lunch);
			spinner.setAdapter(lunchList);
			lunchList.setDropDownViewResource(R.layout.myspinner);
			sendEmailButton = ( Button ) this.getView().findViewById( R.id.sendEmailButton );
			sendEmailButton.setOnClickListener( 
					new Button.OnClickListener() {
						@Override
						public void onClick(View arg0) {
							// TODO Auto-generated method stub
							//建立新Intent
							String data = dateEditText.getText().toString();
							String classname = myAutoCompleteTextView.getText().toString();
							Cursor cursor = mDbRW.rawQuery("Select * from " + DbCheckinTableSrt +" Where classname = '"+ classname +"' AND checkindate LIKE '%"+ data +"%'", null);
							cursor.moveToFirst();
							//ArrayList<String> checkin = new ArrayList<String>();
							String tempstr = "";
							for(int i=0; i<cursor.getCount(); i++) {
								//checkin.add(cursor.getString(2));
								tempstr+=(i+1)+ ".  " +cursor.getString(2)+ " 簽到時間:" +cursor.getString(3) + "  簽退時間:" +cursor.getString(4) +"\n";
								cursor.moveToNext();		
							}
							
							Intent mailIntent = new Intent(Intent.ACTION_SEND);
					        String[] tos={};
					        String[] ccs={};
					        mailIntent.putExtra(Intent.EXTRA_EMAIL, tos);
					        mailIntent.putExtra(Intent.EXTRA_CC, ccs);
					        mailIntent.putExtra(Intent.EXTRA_TEXT, tempstr);
					        mailIntent.putExtra(Intent.EXTRA_SUBJECT,data+" " +classname+ "簽到紀錄" );
					        mailIntent.setType("message/rfc822");
					        startActivity(Intent.createChooser ( mailIntent, "簽到紀錄") );
						}}
					);
			
			peoplenumTextView = (TextView) this.getView().findViewById(R.id.peopleNum);		
			//SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
			SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy-MM-dd");
			String date = sDateFormat.format(new java.util.Date());
			dateEditText = (EditText) this.getView().findViewById(R.id.dateeditText);
			dateEditText.setText(date);
			
			
			Cursor cursor = mDbRW.rawQuery("Select * from " + DbStaffsTableSrt, null);
			cursor.moveToFirst();
			for(int i=0; i<cursor.getCount(); i++) {
				_options.add(cursor.getString(2));
				cursor.moveToNext();
			}
			//
			final String[] nameArrayList = (String[]) _options.toArray(new String[0]);
			_selections =  new boolean[ nameArrayList.length ];
			
			selectPeopleButton = ( Button ) this.getView().findViewById( R.id.selectPeopleButton );
			selectPeopleButton.setOnClickListener( 
			new Button.OnClickListener() {
				@Override
				public void onClick(View v) {	
					alertDialog = new AlertDialog.Builder( mContext )
		        		.setTitle( "需到人員" )
		        		.setMultiChoiceItems( nameArrayList, _selections, new DialogInterface.OnMultiChoiceClickListener()
		        			{
				    			public void onClick( DialogInterface dialog, int clicked, boolean selected )
				    			{
				    				//Log.i( "ME", _options[ clicked ] + " selected: " + selected );
				    			}
		        			})
		        		.setPositiveButton( "OK", new DialogInterface.OnClickListener()
			        		{
				    			public void onClick( DialogInterface dialog, int clicked )
				    			{
				    				switch( clicked )
				    				{
				    					case DialogInterface.BUTTON_POSITIVE:
				    						printSelected(nameArrayList,_selections);
				    						break;
				    				}
				    			}
			        		})
		        	.show();
				}
			});
					
			displayListView();
		}
		
		private void displayListView() { 		
//			Cursor cursor = mDbRW.rawQuery("Select * from " + DbCheckinTableSrt, null);
			Cursor cursor = null;
			  // The desired columns to be bound
			  String[] columns = new String[] {
			    "classname",
			    "nickname",
			    "checkindate",
			    "checkoutdate"
			  };
			 
			  // the XML defined views which the data will be bound to
			  int[] to = new int[] { 
			    R.id.classname_check,
			    R.id.nickname_check,
			    R.id.checkindate_check,
			    R.id.checkoutdate_check,
			  };

			  // create the adapter using the cursor pointing to the desired data 
			  //as well as the layout information
			  dataAdapter = new SimpleCursorAdapter(
				mContext, R.layout.checklist, 
			    cursor, 
			    columns, 
			    to,
			    0);
			 
			  
			  // Assign adapter to ListView
			  listView.setAdapter(dataAdapter);		 		 
			  listView.setOnItemClickListener(new OnItemClickListener() {
				   @Override
				   public void onItemClick(AdapterView<?> listView, View view, 
				     int position, long id) {
				   // Get the cursor, positioned to the corresponding row in the result set
				   Cursor cursor = (Cursor) listView.getItemAtPosition(position);
				 
				   // Get the state's capital from this row in the database.
				   String countryCode = 
				    cursor.getString(cursor.getColumnIndexOrThrow("nickname"));
				   }
			  });
			 
			  final String[] namelist ={"常會","康輔意識"};
			  ArrayAdapter<String> qsadapter = new ArrayAdapter<String>(mContext,android.R.layout.simple_dropdown_item_1line,namelist);
			//建立AutoCompleteTextView
			        myAutoCompleteTextView = (AutoCompleteTextView) this.getView().findViewById(R.id.autoCompleteTextView);
			//把內容丟進去
			        myAutoCompleteTextView.setAdapter(qsadapter);
			//聚焦於第一行，不做這行也沒差
			        myAutoCompleteTextView.setThreshold(1);
			        
			        myAutoCompleteTextView.addTextChangedListener(new TextWatcher() {
						 
					  	public void afterTextChanged(Editable s) {
					  	}
				 
					  	public void beforeTextChanged(CharSequence s, int start, 
					     int count, int after) {
					  	}
				 
					  	public void onTextChanged(CharSequence s, int start, 
					     int before, int count) {
					  		dataAdapter.getFilter().filter(s.toString());
					  		updatePeopleNum();
					  	}
				  });
			        myAutoCompleteTextView.setOnEditorActionListener(
			        		new TextView.OnEditorActionListener() {   
			        		          
									@Override
									public boolean onEditorAction(TextView v,
											int actionId, KeyEvent event) {
										// TODO Auto-generated method stub 
										if (actionId == EditorInfo.IME_ACTION_SEND  
						                        || actionId == EditorInfo.IME_ACTION_DONE  
						                        || (event != null && KeyEvent.KEYCODE_ENTER == event.getKeyCode() && KeyEvent.ACTION_DOWN == event.getAction())) {  
											imm.hideSoftInputFromWindow(myAutoCompleteTextView.getWindowToken(), 0);
						                }  
        		                        return false; 
									}   
			        		});
//			        myAutoCompleteTextView.setOnItemClickListener(new OnItemClickListener(){ 
//			            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, 
//			                    long arg3) {
//			             
//			             //搜尋namelist，取得選項位置
//			               //從頭迴圈到尾，若內容等於所點項目則聚焦到該項
//			              //這裡可能有更好的搜尋法
//			             for(int i=0;i<namelist.length;i++){
//			              if(myAutoCompleteTextView.getText().toString().equals(namelist[i].toString())==true){
//			               
//			              }              
//			             }             
//			            } 
//			        });
			  
			  cardIdEditText = (EditText) this.getView().findViewById(R.id.idclasseditText);
			  cardIdEditText.addTextChangedListener(new TextWatcher() {
			 
				   public void afterTextChanged(Editable s) {
				   }
				 
				   public void beforeTextChanged(CharSequence s, int start, 
				     int count, int after) {
				   }
			 
				   public void onTextChanged(CharSequence s, int start, 
			     int before, int count) {
				   if(s.length()==10&&spinner.getSelectedItem().toString().equals("簽到")){
					   String data = dateEditText.getText().toString();
					   String classname = myAutoCompleteTextView.getText().toString();
					   if(!classname.equals("")){
						   Cursor cursor = mDbRW.rawQuery("Select * from " + DbStaffsTableSrt + " Where cardnum = " + s.toString(), null);
							if(cursor.getCount()>0){
								cursor.moveToFirst();
								String nickname = cursor.getString(2);
								Cursor cursor1 = mDbRW.rawQuery("Select * from " + DbCheckinTableSrt +" Where classname = '"+ classname +"' AND checkindate LIKE '%"+ data +"%' AND nickname = '" + nickname + "'", null);
								if(cursor1.getCount()==0){																	
									ContentValues newRow2 = new ContentValues();
									newRow2.put("classname", classname);
									newRow2.put("nickname", nickname);
									SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
									String date = sDateFormat.format(new java.util.Date());
									newRow2.put("checkindate", date);
									mDbRW.insert(DbCheckinTableSrt, null, newRow2);
									Toast.makeText(mContext, "OK", Toast.LENGTH_SHORT)
									.show();									
									cardIdEditText.setText("");
									updatePeopleNum();
								}else{
									Toast.makeText(mContext, "已簽到", Toast.LENGTH_SHORT)
									.show();
									cardIdEditText.setText("");
								}
							}else{
								Toast.makeText(mContext, "卡號不存在", Toast.LENGTH_SHORT)
								.show();
								cardIdEditText.setText("");
							}					
					   }else{
						   myAutoCompleteTextView.setError("請輸入名稱");
						   cardIdEditText.setText("");
					   }
				   }
				   if(s.length()==10&&spinner.getSelectedItem().toString().equals("簽退")){
					   if(!myAutoCompleteTextView.getText().toString().equals("")){
						   Cursor cursor = mDbRW.rawQuery("Select * from " + DbStaffsTableSrt + " Where cardnum = " + s.toString(), null);					   
							if(cursor.getCount()>0){	
								cursor.moveToFirst();
								ContentValues cv = new ContentValues(); 
								SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
								String date = sDateFormat.format(new java.util.Date());
						        cv.put("checkoutdate", date);
						        //設定where
						        String where = "nickname" + "='" + cursor.getString(2)+"';";
						 
						        mDbRW.update(DbCheckinTableSrt, cv, where, null);
						        Toast.makeText(mContext, "OK", Toast.LENGTH_SHORT)
								.show();
								cardIdEditText.setText("");
							}else{
								Toast.makeText(mContext, "卡號不存在", Toast.LENGTH_SHORT)
								.show();
								cardIdEditText.setText("");
							}					
					   }else{
						   myAutoCompleteTextView.setError("請輸入名稱");
						   cardIdEditText.setText("");
					   }
				   }
				   
				   if(s.length()==9&&spinner.getSelectedItem().toString().equals("簽到")){
					   String data = dateEditText.getText().toString();
					   String classname = myAutoCompleteTextView.getText().toString();
					   if(!classname.equals("")){
						   Cursor cursor = mDbRW.rawQuery("Select * from " + DbStaffsTableSrt + " Where cardnum = " + s.toString(), null);
							if(cursor.getCount()>0){
								cursor.moveToFirst();
								String nickname = cursor.getString(2);
								Cursor cursor1 = mDbRW.rawQuery("Select * from " + DbCheckinTableSrt +" Where classname = '"+ classname +"' AND checkindate LIKE '%"+ data +"%' AND nickname = '" + nickname + "'", null);
								if(cursor1.getCount()==0){																	
									ContentValues newRow2 = new ContentValues();
									newRow2.put("classname", classname);
									newRow2.put("nickname", nickname);
									SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
									String date = sDateFormat.format(new java.util.Date());
									newRow2.put("checkindate", date);
									mDbRW.insert(DbCheckinTableSrt, null, newRow2);
									Toast.makeText(mContext, "OK", Toast.LENGTH_SHORT)
									.show();									
									cardIdEditText.setText("");
									updatePeopleNum();
								}else{
									Toast.makeText(mContext, "已簽到", Toast.LENGTH_SHORT)
									.show();
									cardIdEditText.setText("");
								}
							}else{
								Cursor cursor2 = mDbRW.rawQuery("Select * from " + DbStaffsTableSrt + " Where cardnum LIKE '" + s.toString() + "%'", null);
								if(cursor2.getCount()<1){
									Toast.makeText(mContext, "卡號不存在", Toast.LENGTH_SHORT)
									.show();
									cardIdEditText.setText("");
								}
							}					
					   }else{
						   myAutoCompleteTextView.setError("請輸入名稱");
						   cardIdEditText.setText("");
					   }
				   }
				   if(s.length()==9&&spinner.getSelectedItem().toString().equals("簽退")){
					   if(!myAutoCompleteTextView.getText().toString().equals("")){
						   Cursor cursor = mDbRW.rawQuery("Select * from " + DbStaffsTableSrt + " Where cardnum = " + s.toString(), null);					   
							if(cursor.getCount()>0){	
								cursor.moveToFirst();
								ContentValues cv = new ContentValues(); 
								SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
								String date = sDateFormat.format(new java.util.Date());
						        cv.put("checkoutdate", date);
						        //設定where
						        String where = "nickname" + "='" + cursor.getString(2)+"';";
						 
						        mDbRW.update(DbCheckinTableSrt, cv, where, null);
						        Toast.makeText(mContext, "OK", Toast.LENGTH_SHORT)
								.show();
								cardIdEditText.setText("");
							}else{
								Cursor cursor2 = mDbRW.rawQuery("Select * from " + DbStaffsTableSrt + " Where cardnum LIKE '" + s.toString() + "%'", null);
								if(cursor2.getCount()<1){
									Toast.makeText(mContext, "卡號不存在", Toast.LENGTH_SHORT)
									.show();
									cardIdEditText.setText("");
								}
							}					
					   }else{
						   myAutoCompleteTextView.setError("請輸入名稱");
						   cardIdEditText.setText("");
					   }
				   }
				   			   
			    dataAdapter.getFilter().filter(myAutoCompleteTextView.getText().toString());		    
			    
			   }
			  });
			   
			  dataAdapter.setFilterQueryProvider(new FilterQueryProvider() {
				@Override
				public Cursor runQuery(CharSequence constraint) {
					// TODO Auto-generated method stub
					try {
						return fetchCountriesByClassName(constraint.toString());
					} catch (SQLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					return null;
				}
			     });
			 
			 }
		
		public Cursor fetchCountriesByClassName(String inputText) throws SQLException {		  
			  Cursor mCursor = null;
			  if (inputText == null  ||  inputText.length () == 0)  {
				  mCursor = null;
			  }
			  else {
				  mCursor = mDbRW.rawQuery("Select * from " + DbCheckinTableSrt +" Where checkindate LIKE '%"+dateEditText.getText().toString()+"%' AND classname LIKE '%" + inputText + "%' ORDER BY _id desc", null);
				  
			  }
			  if (mCursor != null) {
				  mCursor.moveToFirst();
			  }
			  return mCursor;			 
		}
		
		void updatePeopleNum(){
			String data = dateEditText.getText().toString();
			String classname = myAutoCompleteTextView.getText().toString();
			Cursor mCursor = mDbRW.rawQuery("Select * from " + DbCheckinTableSrt +" Where checkindate LIKE '%"+data+"%' AND classname LIKE '%" + classname + "%'", null);
		    mCursor.moveToFirst();
		    int i = mCursor.getCount();
		    String istr = String.valueOf(i);
		    peoplenumTextView.setText("人數: "+istr+" 人");
		}
		
		protected void printSelected(String[] _options,boolean[] _selections){
			ArrayList<String> checkNameArray = new ArrayList<String>();
			for( int i = 0; i < _options.length; i++ ){
				if(_selections[i]){
					//Log.i( "ME", _options[ i ] );
					checkNameArray.add(_options[ i ]);
				}
			}
			isLate(checkNameArray);
		}
		
		void isLate(ArrayList<String> checkNameArray){
			tempLateArray = new ArrayList<String>();
			boolean b = false;
			String data = dateEditText.getText().toString();
			String classname = myAutoCompleteTextView.getText().toString();
			
			Cursor cursor = mDbRW.rawQuery("Select * from " + DbCheckinTableSrt +" Where classname = '"+ classname +"' AND checkindate LIKE '%"+ data +"%'", null);
			cursor.moveToFirst();
			ArrayList<String> checkin = new ArrayList<String>();
			for(int i=0; i<cursor.getCount(); i++) {
				checkin.add(cursor.getString(2));
				cursor.moveToNext();		//�N��в��ܤU�@�����
			}
			
			for(String str:checkNameArray){
				b = false;
				for(String str1:checkin){
					if(str.equals(str1)){
						b = true;
						break;
					}				
				}
				if(b){
					//tempArray.add(str);
					//Log.i( "ME", "have: "+str);
				}else {
					tempLateArray.add(str);
					//Log.i( "ME", "no have: "+str);
				}				
			}
			final String[] lateNameArray = (String[]) tempLateArray.toArray(new String[0]);
			alertDialog = new AlertDialog.Builder( mContext )
	    		.setTitle( "遲到人員" )
	    		.setItems(lateNameArray, new DialogInterface.OnClickListener() {    			
	                public void onClick(DialogInterface dialog, int which) {
	    
	                }   
	            })
	    		.setPositiveButton( "OK", new DialogInterface.OnClickListener()
	        		{
		    			public void onClick( DialogInterface dialog, int clicked )
		    			{
		    			}
	        		})
	        	.show();	
		}
	}
	
	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment2 extends Fragment {
		/**
		 * The fragment argument representing the section number for this
		 * fragment.
		 */
		private static final String ARG_SECTION_NUMBER = "section_number";
		private SimpleCursorAdapter dataAdapter;
		ListView listView;
		PopupWindow mPopupWindow;
		ImageButton popupDelectButton;
		View popupView;
		
		/**
		 * Returns a new instance of this fragment for the given section number.
		 */
		public static PlaceholderFragment2 newInstance(int sectionNumber) {
			PlaceholderFragment2 fragment = new PlaceholderFragment2();
			Bundle args = new Bundle();
			args.putInt(ARG_SECTION_NUMBER, sectionNumber);
			fragment.setArguments(args);
			return fragment;
		}

		public PlaceholderFragment2() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_index2,
					container, false);			
			   popupView = inflater.inflate(R.layout.pop_identifyinfo, null);		   
		       
			return rootView;
		}

		@Override
		public void onAttach(Activity activity) {
			super.onAttach(activity);
			((Index) activity).onSectionAttached(getArguments().getInt(
					ARG_SECTION_NUMBER));
		}
		
		@Override
		public void onActivityCreated(Bundle savedInstanceState) {
			super.onActivityCreated(savedInstanceState);
			listView = (ListView) this.getView().findViewById(R.id.listViewtotal);
			displayListView();
		}
		
		private void displayListView() {
			 		
			Cursor cursor = mDbRW.rawQuery("Select * from " + DbStaffsTableSrt, null);		 
			  // The desired columns to be bound
			  String[] columns = new String[] {
			    "name",
			    "nickname",
			    "team"
			  };			 
			  // the XML defined views which the data will be bound to
			  int[] to = new int[] { 
			    R.id.name,
			    R.id.nickname,
			    R.id.team,
			  };
			  // create the adapter using the cursor pointing to the desired data 
			  //as well as the layout information
			  dataAdapter = new SimpleCursorAdapter(
				mContext, R.layout.staffslist, 
			    cursor, 
			    columns, 
			    to,
			    0);			  
			  // Assign adapter to ListView
			  listView.setAdapter(dataAdapter);
			 			 
			  listView.setOnItemClickListener(new OnItemClickListener() {
			   @Override
			   public void onItemClick(AdapterView<?> listView, View view, 
			     int position, long id) {
			   // Get the cursor, positioned to the corresponding row in the result set
				   final Cursor cursor = (Cursor) listView.getItemAtPosition(position);
				   final View tempview = view;
				   // Get the state's capital from this row in the database.
				   final String name = 
						   cursor.getString(cursor.getColumnIndexOrThrow("name"));
				   final String nickname = 
						   cursor.getString(cursor.getColumnIndexOrThrow("nickname"));
	
				   mPopupWindow = new PopupWindow(popupView, 200, 170, true);
			       mPopupWindow.setTouchable(true);
			       mPopupWindow.setOutsideTouchable(true);
			       mPopupWindow.setBackgroundDrawable(new BitmapDrawable(getResources(), (Bitmap) null));
				   mPopupWindow.showAsDropDown(view, 0, -170,Gravity.RIGHT);
				   popupDelectButton = (ImageButton) mPopupWindow.getContentView().findViewById(R.id.peopleDelectButton);
				   popupDelectButton.setOnClickListener(new Button.OnClickListener() {
						@Override
						public void onClick(View v) {
							// TODO Auto-generated method stub
							if(cursor.getCount()>0){
								 new AlertDialog.Builder(tempview
										.getContext())
										.setTitle("刪除")
										.setMessage("確定刪除"+name+"?" )
										.setNegativeButton("OK",
												new DialogInterface.OnClickListener() {
													public void onClick(
															DialogInterface dialog,
															int which) {
														mDbRW.delete(DbStaffsTableSrt, "name = '" + name+"' AND nickname= '" + nickname+"'", null);
														dataAdapter.getFilter().filter("");
														Toast.makeText(mContext, "移除成功", Toast.LENGTH_SHORT)
														.show();
													}
												})
										.setIcon(android.R.drawable.ic_dialog_alert)
										.show();						
							}else{
								Toast.makeText(mContext, "資料不存在", Toast.LENGTH_SHORT)
								.show();
							}
							mPopupWindow.dismiss();
						}
					});				
			   }			  
			  });
			 
			  EditText myFilter = (EditText) this.getView().findViewById(R.id.myFilter);
			  myFilter.addTextChangedListener(new TextWatcher() {
			 
			   public void afterTextChanged(Editable s) {
			   }
			 
			   public void beforeTextChanged(CharSequence s, int start, 
			     int count, int after) {
			   }
			 
			   public void onTextChanged(CharSequence s, int start, 
			     int before, int count) {
			    dataAdapter.getFilter().filter(s.toString());
			   }
			  });
			   
			  dataAdapter.setFilterQueryProvider(new FilterQueryProvider() {

				@Override
				public Cursor runQuery(CharSequence constraint) {
					// TODO Auto-generated method stub
					try {
						return fetchCountriesByName(constraint.toString());
					} catch (SQLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					return null;
				}
			     });		 
		}
		
		public Cursor fetchCountriesByName(String inputText) throws SQLException {			  
			  Cursor mCursor = null;
			  if (inputText == null  ||  inputText.length () == 0)  {
				  mCursor = mDbRW.rawQuery("Select * from " + DbStaffsTableSrt, null);		 
			  }
			  else {
				  mCursor = mDbRW.rawQuery("Select * from " + DbStaffsTableSrt +" Where name LIKE '%" + inputText + "%'", null);
			  }
			  if (mCursor != null) {
			   mCursor.moveToFirst();
			  }
			  return mCursor;		 
		}
		public Cursor fetchCountriesByNickname(String inputText) throws SQLException {			  
			  Cursor mCursor = null;
			  if (inputText == null  ||  inputText.length () == 0)  {
				  mCursor = mDbRW.rawQuery("Select * from " + DbStaffsTableSrt, null);		 
			  }
			  else {
				  mCursor = mDbRW.rawQuery("Select * from " + DbStaffsTableSrt +" Where nickname LIKE '%" + inputText + "%'", null);
			  }
			  if (mCursor != null) {
			   mCursor.moveToFirst();
			  }
			  return mCursor;		 
		}
	}
}

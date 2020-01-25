package com.example.kfacheckin;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

import sql.DbHelper;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

public class SettingActivity extends ActionBarActivity {

	private ActionBar  bar = null; 
	private static String DbName = "kfacheckin";
	private static SQLiteDatabase mDbRW;
	private static String DbTeamTableSrt = "team";
	private static String DbStaffsTableSrt = "staffs";
	private NfcAdapter mNfcAdapter = null;
	private PendingIntent mNfcPendingIntent = null;
	Spinner teamSpinner;
	ArrayList<String> mTeamList= new ArrayList<String>();
	ArrayAdapter<String> teamAdapter;
	EditText teamEditText;
	Button teamAddButton;
	Button teamRemoveButton;
	EditText peopleNameEditText;
	EditText peopleNicknameEditText;	
	Button peopleAddButton;
	Spinner cardAddTeamSpinner;
	Spinner cardAddNicknameSpinner;
	ArrayList<String> mcardAddNicknameList= new ArrayList<String>();
	ArrayAdapter<String> cardAddNicknameAdapter;
	EditText peopleCardnumEditText;
	Button cardAddButton;
	InputMethodManager imm;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_setting);
		
		checkNFCFunction();		
		initNFC();	
		imm =(InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
		DbHelper Dbhp = new DbHelper(getApplicationContext(), DbName, null, 1);	
		mDbRW = Dbhp.getReadableDatabase();
			
		teamSpinner = (Spinner)findViewById(R.id.teamspinner);
		teamEditText = (EditText)findViewById(R.id.teamAddeditText);
		teamAddButton = (Button)findViewById(R.id.teamAddbutton);
		teamAddButton.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(!teamEditText.getText().toString().equals("")){
					String teamname = teamEditText.getText().toString();
					Cursor cursor = mDbRW.rawQuery("Select * from " + DbTeamTableSrt + " where teamname = '" + teamname+"';", null);
					if(cursor.getCount()==0){
						ContentValues newRow = new ContentValues();
						newRow.put("teamname", teamname);			
						mDbRW.insert(DbTeamTableSrt, null, newRow);
						mTeamList.add(teamname);
						teamAdapter.notifyDataSetChanged();
						teamEditText.setText("");
						Toast.makeText(SettingActivity.this, "新增成功", Toast.LENGTH_SHORT)
						.show();
						
					}else{
						Toast.makeText(SettingActivity.this, "資料已存在", Toast.LENGTH_SHORT)
						.show();
					}
				}else{
					teamEditText.setError("null");
				}
				imm.hideSoftInputFromWindow(teamEditText.getWindowToken(), 0);
			}
		});
		teamRemoveButton = (Button)findViewById(R.id.teamRemovebutton);
		teamRemoveButton.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(!teamEditText.getText().toString().equals("")){
					final String teamname = teamEditText.getText().toString();
					Cursor cursor = mDbRW.rawQuery("Select * from " + DbTeamTableSrt + " where teamname = '" + teamname+"';", null);
					if(cursor.getCount()>0){
						AlertDialog adelect = new AlertDialog.Builder(v
								.getContext())
								.setTitle("刪除")
								.setMessage("確定刪除"+teamname+"?" )
								.setNegativeButton("OK",
										new DialogInterface.OnClickListener() {
											public void onClick(
													DialogInterface dialog,
													int which) {
												mDbRW.delete(DbTeamTableSrt, "teamname='" + teamname + "'", null);
												mTeamList.remove(teamname);
												teamAdapter.notifyDataSetChanged();
												teamEditText.setText("");
												Toast.makeText(SettingActivity.this, "移除成功", Toast.LENGTH_SHORT)
												.show();
											}
										})
								.setIcon(android.R.drawable.ic_dialog_alert)
								.show();	
						
					}else{
						Toast.makeText(SettingActivity.this, "資料不存在", Toast.LENGTH_SHORT)
						.show();
					}
				}else{
					teamEditText.setError("null");
				}
				imm.hideSoftInputFromWindow(teamEditText.getWindowToken(), 0);
			}
		});
		
		
		peopleNameEditText = (EditText)findViewById(R.id.peopleNameAddeditText);
		peopleNicknameEditText = (EditText)findViewById(R.id.peopleNicknameAddeditText);
		peopleAddButton = (Button)findViewById(R.id.peopleAddbutton);
		peopleAddButton.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				String name = peopleNameEditText.getText().toString();
				String nickname = peopleNicknameEditText.getText().toString();
				String team = teamSpinner.getSelectedItem().toString();
				if(!(name.equals("")||nickname.equals(""))){					
					Cursor cursor = mDbRW.rawQuery("Select * from " + DbStaffsTableSrt + " where name = '" + name+"' AND nickname= '" + nickname+"';", null);
					if(cursor.getCount()==0){
						ContentValues newRow = new ContentValues();
						newRow.put("name", name);
						newRow.put("nickname", nickname);
						newRow.put("team", team);
						mDbRW.insert(DbStaffsTableSrt, null, newRow);						
						peopleNameEditText.setText("");
						peopleNicknameEditText.setText("");
						Toast.makeText(SettingActivity.this, "新增成功", Toast.LENGTH_SHORT)
						.show();
					}else{
						Toast.makeText(SettingActivity.this, "資料已存在", Toast.LENGTH_SHORT)
						.show();
					}
				}else{
					Toast.makeText(SettingActivity.this, "有部分資訊尚未輸入", Toast.LENGTH_SHORT)
					.show();
				}
				imm.hideSoftInputFromWindow(teamEditText.getWindowToken(), 0);
			}
		});
			
		cardAddNicknameSpinner = (Spinner)findViewById(R.id.cardnumAddNicknamespinner);
		peopleCardnumEditText = (EditText)findViewById(R.id.cardnumAddeditText);
		peopleCardnumEditText.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				// TODO Auto-generated method stub	
				
			}
			@Override
			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub
				String text = s.toString();
			     int len = s.toString().length();
			     if (len == 1 && text.equals("0"))
			     {
			    	 s.clear();
			    	 peopleCardnumEditText.setError("0");		    	 
			     }
			}
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO Auto-generated method stub
				
			}
		});
		cardAddTeamSpinner = (Spinner)findViewById(R.id.cardnumAddTeamspinner);	
		cardAddTeamSpinner.setOnItemSelectedListener(new OnItemSelectedListener(){
			public void onItemSelected(AdapterView<?> parent, View v, int position,long id){
	            //Ū��Ĥ@�ӤU�Կ��O��ܲĴX��
	        	mcardAddNicknameList.clear();
	            String pos = (String)cardAddTeamSpinner.getSelectedItem();
	            Cursor cursor = mDbRW.rawQuery("Select * from " + DbStaffsTableSrt + " where team ='"+ pos+"';", null);
	            if(cursor.getCount()>0){
		            cursor.moveToFirst();
		       		for(int i=0; i<cursor.getCount(); i++) {
		       			mcardAddNicknameList.add(cursor.getString(2));
		       			cursor.moveToNext();		//�N��в��ܤU�@�����
		       		}			
		       		cardAddNicknameAdapter = new ArrayAdapter<String>(SettingActivity.this,R.layout.myspinner, mcardAddNicknameList);
		       		cardAddNicknameSpinner.setAdapter(cardAddNicknameAdapter);
		       		cardAddNicknameAdapter.setDropDownViewResource(R.layout.myspinner);
	       		}else{
	       			cardAddNicknameAdapter = new ArrayAdapter<String>(SettingActivity.this,R.layout.myspinner, mcardAddNicknameList);
	       			cardAddNicknameSpinner.setAdapter(cardAddNicknameAdapter);
	       			cardAddNicknameAdapter.setDropDownViewResource(R.layout.myspinner);
	       		}
	            }         
	        public void onNothingSelected(AdapterView<?> arg0){
	    
	        }
	    });
		cardAddButton = (Button)findViewById(R.id.cardnumAddbutton);
		cardAddButton.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				String cardnum = peopleCardnumEditText.getText().toString();
				String nickname = (String)cardAddNicknameSpinner.getSelectedItem();
				if(!(cardnum.equals("")||nickname.equals(""))){					
					Cursor cursor = mDbRW.rawQuery("Select * from " + DbStaffsTableSrt + " where nickname= '" + nickname+"';", null);
					Cursor cursor1 = mDbRW.rawQuery("Select * from " + DbStaffsTableSrt + " where cardnum= '" + cardnum+"';", null);
					if(cursor.getCount()>0 && cursor1.getCount()==0){
						ContentValues cv = new ContentValues(); 						
				        cv.put("cardnum", cardnum);			 
				        String where = "nickname" + "='" + nickname+"';";			 
				        mDbRW.update(DbStaffsTableSrt, cv, where, null);
				        peopleCardnumEditText.setText("");
						Toast.makeText(SettingActivity.this, "成功", Toast.LENGTH_SHORT)
						.show();
					}else{
						Toast.makeText(SettingActivity.this, "資料已存在", Toast.LENGTH_SHORT)
						.show();
						peopleCardnumEditText.setText("");
					}
				}else{
					Toast.makeText(SettingActivity.this, "有部分資訊尚未輸入", Toast.LENGTH_SHORT)
					.show();
					peopleCardnumEditText.setText("");
				}
				imm.hideSoftInputFromWindow(teamEditText.getWindowToken(), 0);
			}
		});
		
		showteam();
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
						peopleCardnumEditText.setText(new String(payload));
					}
				}
			}
		}else {
			this.finish();
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
	//算卡號
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
	
	 //新增卡號讓spinner列表
	void showteam(){
		Cursor cursor = mDbRW.rawQuery("Select * from " + DbTeamTableSrt, null);		
		if(cursor.getCount()>0){
			cursor.moveToFirst();
			for(int i=0; i<cursor.getCount(); i++) {
				mTeamList.add(cursor.getString(1));
				cursor.moveToNext();
			}			
			teamAdapter = new ArrayAdapter<String>(this,R.layout.myspinner, mTeamList);
			teamSpinner.setAdapter(teamAdapter);
			cardAddTeamSpinner.setAdapter(teamAdapter);
			teamAdapter.setDropDownViewResource(R.layout.myspinner);
		}else{
			teamAdapter = new ArrayAdapter<String>(this,R.layout.myspinner, mTeamList);
			teamSpinner.setAdapter(teamAdapter);
			cardAddTeamSpinner.setAdapter(teamAdapter);
			teamAdapter.setDropDownViewResource(R.layout.myspinner);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.setting, menu);
		bar = getActionBar();  //���ActionBar����H�A�q�o�Ӥ�k�]�i��action bar�Oactivity���@���ݩ�
        bar.setDisplayHomeAsUpEnabled(true);  //��ܪ�^���b�Y�A�åi�q�LonOptionsItemSelected()�i���ť�A��귽ID��android.R.id.home�C 
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		if(id == android.R.id.home) {
			 Intent i = new Intent(this,Index.class); 
			 i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); 
		     startActivity(i);
		     finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}

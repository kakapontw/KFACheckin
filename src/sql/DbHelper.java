package sql;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class DbHelper extends SQLiteOpenHelper {

	public String sCreateTableCommand;
	private static String DbStaffsTableSrt = "staffs";
	private static String DbCheckinTableSrt = "checkin";
	private static String DbTeamTableSrt = "team";
	String[] teamStringArray = {"行政體系","活動服務隊","社團服務隊","課程服務隊","行政服務隊","一般會員","康聯之友"};

	public DbHelper(Context context, String name, CursorFactory factory,
			int version) {
		super(context, name, factory, version);
		// TODO Auto-generated constructor stub

		sCreateTableCommand="";
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub	
		db.execSQL("CREATE TABLE " + DbStaffsTableSrt + "("+
				"_id INTEGER PRIMARY KEY,"+
				"name TEXT NOT NULL,"+
				"nickname TEXT NOT NULL,"+
				"cardnum TEXT,"+
				"team TEXT NOT NULL);");
		
		db.execSQL("CREATE TABLE " + DbCheckinTableSrt + "("+
				"_id INTEGER PRIMARY KEY,"+
				"classname TEXT NOT NULL,"+
				"nickname TEXT NOT NULL,"+
				"checkindate TEXT,"+
				"checkoutdate TEXT);");
		
		db.execSQL("CREATE TABLE " + DbTeamTableSrt + "("+
				"_id INTEGER PRIMARY KEY,"+
				"teamname TEXT NOT NULL);");
		
		for(int i=0;i<teamStringArray.length;i++){
			ContentValues newRow = new ContentValues();
			newRow.put("teamname", teamStringArray[i]);			
			db.insert(DbTeamTableSrt, null, newRow);
		}
		ContentValues newRow = new ContentValues();
		newRow.put("name", "卡卡胖");
		newRow.put("nickname", "卡卡");
		newRow.put("team", "行政體系");
		db.insert(DbStaffsTableSrt, null, newRow);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVer, int newVer) {
		// TODO Auto-generated method stub

	}

}

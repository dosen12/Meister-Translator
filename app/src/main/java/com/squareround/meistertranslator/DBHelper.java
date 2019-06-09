package com.squareround.meistertranslator;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.SparseArray;

import java.util.ArrayList;

public class DBHelper extends SQLiteOpenHelper {

    // DBHelper 생성자로 관리할 DB 이름과 버전 정보를 받음
    public DBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    // DB를 새로 생성할 때 호출되는 함수
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE TR1 (IND1 INTEGER PRIMARY KEY AUTOINCREMENT,"+" SQ int, Moviename String , Synkstart int, Contents String, Synkfinal int);");
        // 새로운 테이블 생성
        // 싱크 db.execSQL("CREATE TABLE KOREANSUBTITE ( PRIMARY KEY, , )");영화 이름,싱크 시작 시간, 번역된 내용(변수 안정함), 싱크 끝나는 시간
    }

    // DB 업그레이드를 위해 버전이 변경될 때 호출되는 함수
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public void insert(int SQ, String Moviename,  int Synkstart, String Contents, Integer Synkfinal) {
        SQLiteDatabase db = getWritableDatabase(); //쓰기모드로 불러옴
        // 입력되어 출력된 값으로 행을(튜플을) 하나씩 추가
        db.execSQL( "INSERT INTO TR1 VALUES(null, " +
                "'" + SQ + "', '" + Moviename + "', '" + Synkstart + "','" + Contents + "' , '" + Synkfinal + "')");
        db.close();
    }

    /*public void update(int SQQ, String MoviEname, int SynkStart, String ConTents, int SynkFinal) {
        SQLiteDatabase db = getWritableDatabase();

        db.execSQL("UPDATE TR SET update  SQ = '" + SQQ + "', update Moviename = '" + MoviEname + "',update Synkstart  = '" + SynkStart + "',update Contents = '" + ConTents + "',update Synkfinal = '" + SynkFinal + "')");
        db.close();// 자바 문법 사용하기
    }*/ // 필요성 없을듯

    public void delete(String Moviename) {
        SQLiteDatabase db = getWritableDatabase();
        // 입력한 항목과 일치하는 행의 가격 정보 수정
        db.execSQL("DELETE FROM TR1 WHERE Moviename = '"+ Moviename+"';");
        db.close();
    }

    public String getResult(/*String Contents*/) {
        // 읽기가 가능하게 DB 열기
        SQLiteDatabase db = getReadableDatabase();
        String result = "";

        Cursor cursor = db.rawQuery("SELECT * FROM TR1", null);
        while (cursor.moveToNext()) {
            result += cursor.getString(0)
                    + " : "
                    + cursor.getInt(1)
                    + " "
                    + cursor.getString(2)
                    + " "
                    + cursor.getInt(3)
                    + " "
                    + cursor.getString(4)
                    + " "
                    + cursor.getInt(5)
                    + "\n";

        }
        return result;
    }
    public void resetdb(){
        SQLiteDatabase db = getReadableDatabase();
        db.execSQL("DROP TABLE TR1");
        db.execSQL("CREATE TABLE TR1 ( IND1 INTEGER PRIMARY KEY AUTOINCREMENT ,SQ int, Moviename String , Synkstart int, Contents String, Synkfinal int)");
        db.close();
    }





    public SparseArray< String > loadData( String uri ) {
        SQLiteDatabase database = getReadableDatabase();
        Cursor cursor = database.rawQuery( "SELECT Synkstart, Contents, Synkfinal FROM TR1 WHERE Moviename = ?", new String[] { uri } );
        SparseArray< String > syncMap = new SparseArray<>();

        while( cursor.moveToNext() ) {
            if( !cursor.getString( 1 ).isEmpty() ) {
                syncMap.put( cursor.getInt( 0 ), cursor.getString( 1 ) );
            }
        }

        return syncMap;
    }

    public boolean searchMoviename( String uri ) {
        SQLiteDatabase database = getReadableDatabase();
        Cursor cursor = database.rawQuery( "SELECT Moviename FROM TR1 WHERE Moviename = ?", new String[] { uri } );

        return cursor.getCount() > 0;
    }

    public int getSQ( String uri ) {
        SQLiteDatabase database = getReadableDatabase();
        Cursor cursor = database.rawQuery( "SELECT SQ FROM TR1 WHERE Moviename = ?", new String[] { uri } );

        if( cursor.moveToNext() ) {
            return cursor.getInt( 0 );
        } else {
            return getNewSQ();
        }
    }

    public int getNewSQ() {
        SQLiteDatabase database = getReadableDatabase();
        Cursor cursor = database.rawQuery( "SELECT SQ FROM TR1 GROUP BY SQ", null );
        ArrayList< Integer > results = new ArrayList<>();
        int newSQ = 1;
        boolean min = true;

        while( cursor.moveToNext() ) {
            results.add( cursor.getInt( 0 ) );
        }
        while( min ) {
            min = false;
            for( int result : results ) {
                if( result == newSQ ) {
                    newSQ++;
                    min = true;
                    break;
                }
            }
        }

        return newSQ;
    }

}





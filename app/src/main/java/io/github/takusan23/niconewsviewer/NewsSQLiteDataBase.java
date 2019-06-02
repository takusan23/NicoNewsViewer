package io.github.takusan23.niconewsviewer;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class NewsSQLiteDataBase extends SQLiteOpenHelper {
    // データーベースのバージョン
    private static final int DATABASE_VERSION = 1;

    // データーベース名
    private static final String DATABASE_NAME = "News.db";
    private static final String TABLE_NAME = "newsdb";
    private static final String MEMO = "memo";
    private static final String SETTING = "setting";
    private static final String HTML = "html";
    private static final String CREATOR = "creator";
    private static final String LINK = "link";
    private static final String CATEGORY = "category";
    private static final String TITLE = "title";
    private static final String _ID = "_id";


    // , を付け忘れるとエラー
    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    _ID + " INTEGER PRIMARY KEY," +
                    MEMO + " TEXT ," +
                    TITLE + " TEXT ," +
                    CATEGORY + " TEXT ," +
                    CREATOR + " TEXT ," +
                    LINK + " TEXT ," +
                    HTML + " TEXT" +
                    ")";

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + TABLE_NAME;

    public NewsSQLiteDataBase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {

        // テーブル作成
        // SQLiteファイルがなければSQLiteファイルが作成される
        db.execSQL(
                SQL_CREATE_ENTRIES
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // アップデートの判別
        db.execSQL(
                SQL_DELETE_ENTRIES
        );
        onCreate(db);
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
}

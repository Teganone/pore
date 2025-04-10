package com.example.lab1;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.content.UriMatcher;
import android.content.ContentUris;
import android.database.sqlite.*;
import android.database.SQLException;


public class MyClass3 extends ContentProvider {

    DBHelper mDbHelper = null;
    SQLiteDatabase db = null;
    private static final UriMatcher mMatcher;

    static{
        mMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        mMatcher.addURI(Constant.AUTOHORITY,Constant.TABLE_NAME, Constant.ITEM);
        mMatcher.addURI(Constant.AUTOHORITY, Constant.TABLE_NAME+"/#", Constant.ITEM_ID);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Implement this to handle requests to delete one or more rows.
        return 0;
    }

    @Override
    public String getType(Uri uri) {
        switch (mMatcher.match(uri)) {
            case Constant.ITEM:
                return Constant.CONTENT_TYPE;
            case Constant.ITEM_ID:
                return Constant.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalArgumentException("Unknown URI"+uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        long rowId;
        if (mMatcher.match(uri)!=Constant.ITEM){
            throw new IllegalArgumentException("Unknown URI"+uri);
        }
        rowId = db.insert(Constant.TABLE_NAME,null,values);
        if (rowId > 0){
            Uri noteUri=ContentUris.withAppendedId(Constant.CONTENT_URI, rowId);
            getContext().getContentResolver().notifyChange(noteUri, null);
            return noteUri;
        }
        throw new SQLException("Failed to insert row into " + uri);
    }

    @Override
    public boolean onCreate() {

        mDbHelper = new DBHelper(getContext());

        db = mDbHelper.getReadableDatabase();

        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        Cursor c;
        switch (mMatcher.match(uri)) {
            case 1:
                c = this.db.query(Constant.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case 2:
                c = this.db.query(Constant.TABLE_NAME, projection, "_id=" + uri.getLastPathSegment(), selectionArgs, null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI" + uri);
        }
        c.setNotificationUri(getContext().getContentResolver(), uri);
        return c;
    }



    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        return 0;
    }
}

class Constant {

    public static final String TABLE_NAME = "user";

    public static final String COLUMN_ID = "_id";

    public static final String COLUMN_NAME = "name";

    public static final String AUTOHORITY = "com.example.testprovider";

    public static final int ITEM = 1;

    public static final int ITEM_ID = 2;

    public static final String CONTENT_TYPE = "vnd.android.cursor.dir/user";

    public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/user";

    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTOHORITY + "/user");

}


class DBHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "finch.db";

    private static final int DATABASE_VERSION = 1;

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db)  throws SQLException {
        //创建表格
        db.execSQL("CREATE TABLE IF NOT EXISTS "+ Constant.TABLE_NAME + "("+ Constant.COLUMN_ID +" INTEGER PRIMARY KEY AUTOINCREMENT," + Constant.COLUMN_NAME +" VARCHAR NOT NULL);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)  throws SQLException {
        db.execSQL("DROP TABLE IF EXISTS "+ Constant.TABLE_NAME+";");
        onCreate(db);
    }

}
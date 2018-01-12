package com.efrobot.talkstory.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.efrobot.talkstory.bean.HistoryBean;
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import java.sql.SQLException;

/**
 * Created by zd on 2018/1/12.
 */
public class DbHelper extends OrmLiteSqliteOpenHelper {

    public final String TAG = this.getClass().getSimpleName();
    private static int version = 1;
    private static String DB_NAME = "paopao_english";

    public DbHelper(Context context) {
        super(context, DB_NAME, null, version);
    }

    @Override
    public synchronized SQLiteDatabase getWritableDatabase() {
        return super.getWritableDatabase();
    }

    @Override
    public synchronized SQLiteDatabase getReadableDatabase() {
        return super.getReadableDatabase();
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase, ConnectionSource connectionSource) {
        try {
            TableUtils.createTable(connectionSource, HistoryBean.class);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, ConnectionSource connectionSource, int i, int i1) {

    }
}

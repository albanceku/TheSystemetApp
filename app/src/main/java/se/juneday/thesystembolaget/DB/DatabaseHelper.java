package se.juneday.thesystembolaget.DB;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import se.juneday.thesystembolaget.domain.Product;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String LOG_TAG = DatabaseHelper.class.getSimpleName();
    public static final String DATABASE_NAME = "product.db";

    public static final String TABLE_FAVORITES = "favorites";
    public static final String TABLE_LATEST = "latest_search";

    public static final String COL_1_FAV = "Name";
    public static final String COL_2_FAV = "Price";
    public static final String COL_3_FAV = "Alcohol";
    public static final String COL_4_FAV = "Volume";
    public static final String COL_5_FAV = "Id";

    public static final String COL_1_LATEST = "Latest_search";
    public static final String COL_2_LATEST = "Id";

    
    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, 9);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_FAVORITES + " (" + COL_1_FAV +
                " TEXT," + COL_2_FAV + " REAL," + COL_3_FAV +
                " REAL," + COL_4_FAV + " INTEGER," + COL_5_FAV +
                " INTEGER PRIMARY KEY AUTOINCREMENT)");
        db.execSQL("CREATE TABLE " + TABLE_LATEST + " (" + COL_1_LATEST +
                " TEXT," + COL_2_LATEST + " INTEGER PRIMARY KEY AUTOINCREMENT)");

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_FAVORITES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_LATEST);
        onCreate(db);
    }

    public boolean insertDataFavorites(String name, double price, double alcohol, int volume){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_1_FAV, name);
        contentValues.put(COL_2_FAV, price);
        contentValues.put(COL_3_FAV, alcohol);
        contentValues.put(COL_4_FAV, volume);
        long result = db.insert(TABLE_FAVORITES, null, contentValues);

        if(result == -1){
            return false;
        }else{
            return true;

        }
    }

    public boolean insertDataLatest(String latest){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_1_LATEST, latest);
        long result = db.insert(TABLE_LATEST, null, contentValues);

        if(result == -1){
            return false;
        }else{
            return true;
        }
    }

    public Cursor getAllDataFavorites(){
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "SELECT * FROM " + TABLE_FAVORITES;
        Cursor result = db.rawQuery(query, null);
        return result;
    }

    public Cursor getAllDataLatest(){
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "SELECT * FROM " + TABLE_LATEST;
        Cursor result = db.rawQuery(query, null);
        return result;
    }

    public Integer deleteDataFavorites(Product product){
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_FAVORITES, "Id = ?", new String[] {Integer.toString(product.nr())});
    }

    public Integer deleteDataLatest(String string){
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_LATEST, "Latest_search = ?", new String[] {string});
    }
}

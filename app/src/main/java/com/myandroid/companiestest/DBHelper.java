package com.myandroid.companiestest;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.myandroid.companiestest.entity.Company;
import com.myandroid.companiestest.entity.CompanyAddress;

import java.util.ArrayList;
import java.util.List;

public class DBHelper extends SQLiteOpenHelper {

    private static final String LOG_TAG = "DBHelper";
    private static final String DATABASE_NAME = "companies";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_NAME_COMPANIES = "companies";
    public static final String COMPANIES_UID = "_id";
    public static final String COMPANIES_NAME = "name";
    public static final String COMPANIES_DESCRIPTION = "description";

    public static final String TABLE_NAME_ADDRESSES = "addresses";
    public static final String ADDRESSES_UID = "_id";
    public static final String ADDRESSES_ADDRESS = "address";
    public static final String ADDRESSES_ID_COMPANY = "id_company";
    public static final String ADDRESSES_LATITUDE = "coordinate_width";
    public static final String ADDRESSES_LONGITUDE = "coordinate_length";

    private static final String DATABASE_CREATE_COMPANIES = "CREATE TABLE `companies` (\n" +
            "\t`_id`\tINTEGER PRIMARY KEY AUTOINCREMENT,\n" +
            "\t`name`\tTEXT,\n" +
            "\t`description`\tTEXT\n" +
            ");";

    private static final String DATABASE_CREATE_ADDRESSES = "CREATE TABLE `addresses` (\n" +
            "\t`_id`\tINTEGER PRIMARY KEY AUTOINCREMENT,\n" +
            "\t`address`\tTEXT,\n" +
            "\t`id_company`\tINTEGER,\n" +
            "\t`coordinate_width`\tTEXT,\n" +
            "\t`coordinate_length`\tTEXT\n" +
            ");";

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DATABASE_CREATE_COMPANIES);
        db.execSQL(DATABASE_CREATE_ADDRESSES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_ADDRESSES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_COMPANIES);
        onCreate(db);
    }

    public long insertCompany(String name, String description) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COMPANIES_NAME, name);
        contentValues.put(COMPANIES_DESCRIPTION, description);
        return db.insert(TABLE_NAME_COMPANIES, null, contentValues);
    }

    public long insertCompanyAddress(long idCompany, String address, double coord_w, double coord_l) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(ADDRESSES_ADDRESS, address);
        contentValues.put(ADDRESSES_ID_COMPANY, idCompany);
        contentValues.put(ADDRESSES_LATITUDE, coord_w);
        contentValues.put(ADDRESSES_LONGITUDE, coord_l);
        return db.insert(TABLE_NAME_ADDRESSES, null, contentValues);
    }

    public Cursor getAllCompanies() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.query(TABLE_NAME_COMPANIES, null, null, null, null, null, null);
        return c;
    }

    public Company getCompanyById(int id) {
        Company company = new Company();
        SQLiteDatabase db = this.getReadableDatabase();
        String selection = this.COMPANIES_UID + "=" + id;
        Cursor c = db.query(TABLE_NAME_COMPANIES, null, selection, null, null, null, null);
        while (c.moveToNext()) {
            company.setName(c.getString(c.getColumnIndex(COMPANIES_NAME)));
            company.setDescription(c.getString(c.getColumnIndex(COMPANIES_DESCRIPTION)));
        }
        company.setAddresses(getAddressesById(id));
        c.close();
        Log.v(LOG_TAG, company.toString());
        return company;
    }

    public int deleteCompanyById(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME_ADDRESSES,
                ADDRESSES_ID_COMPANY + " = ? ",new String[]{Integer.toString(id)});
        return db.delete(TABLE_NAME_COMPANIES,
                COMPANIES_UID + " = ? ", new String[]{Integer.toString(id)});
    }

    private List<CompanyAddress> getAddressesById(int idCompany) {
        List<CompanyAddress> addresses = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String selection = this.ADDRESSES_ID_COMPANY + "=" + idCompany;
        Cursor c = db.query(TABLE_NAME_ADDRESSES, null, selection, null, null, null, null);
        while (c.moveToNext()) {
            String address = c.getString(c.getColumnIndex(ADDRESSES_ADDRESS));
            int id = c.getInt(c.getColumnIndex(ADDRESSES_UID));
            double latitude = c.getDouble(c.getColumnIndex(ADDRESSES_LATITUDE));
            double longitude = c.getDouble(c.getColumnIndex(ADDRESSES_LONGITUDE));
            addresses.add(new CompanyAddress(id, address, idCompany, latitude, longitude));
        }
        return addresses;
    }


}

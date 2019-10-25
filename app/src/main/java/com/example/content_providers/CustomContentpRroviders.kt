package com.example.content_providers

import android.content.*
import android.database.Cursor
import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.database.sqlite.SQLiteQueryBuilder
import android.net.Uri
import android.text.TextUtils

class CustomContentpRroviders :ContentProvider() {


    // fields for the mDatabase
    companion object {
        val ID: String = "id"
        val PROVIDER_NAME: String = "com.androidatc.provider"
        val NAME: String = "name"
        val NICK_NAME: String = "nickname"
        val CONTENT_URI: Uri = Uri.parse("content://$PROVIDER_NAME/nicknames")
    }

    // integer values used in content URI
    val NICKNAME: Int = 1
    val NICKNAME_ID: Int = 2

    // projection map for a query
    private val mNicknameMap = HashMap<String, String>()

    // maps content URI "patterns" to the integer values that were set above
    var mUriMatcher: UriMatcher? = null

    // mDatabase declarations
    private var mDatabase: SQLiteDatabase? = null
    val DATABASE_NAME = "NicknamesDirectory"
    val TABLE_NAME = "Nicknames"
    val DATABASE_VERSION = 1
    val CREATE_TABLE = " CREATE TABLE $TABLE_NAME (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL, nickname TEXT NOT NULL);"

    init {
        mUriMatcher = UriMatcher(UriMatcher.NO_MATCH)
        mUriMatcher?.addURI(PROVIDER_NAME, "nicknames", NICKNAME)
        mUriMatcher?.addURI(PROVIDER_NAME, "nicknames /#", NICKNAME_ID)
    }

    override fun onCreate(): Boolean {
        var context = context
        var mDbHelper = DBHelper(context!!)

        // permissions to be writable
        mDatabase = mDbHelper.writableDatabase
        return mDatabase != null
    }

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ): Cursor? {
        var queryBuilder = SQLiteQueryBuilder()
        // the TABLE_NAME to query on
        queryBuilder.tables = TABLE_NAME
        when (mUriMatcher?.match(uri)) {
            NICKNAME -> queryBuilder.setProjectionMap(mNicknameMap)
            NICKNAME_ID -> queryBuilder.appendWhere(ID + " =" + uri?.getLastPathSegment())
            else -> throw IllegalArgumentException("Unknown URI " + uri)
        }
        var sort_order = NAME


        if (!TextUtils.isEmpty(sortOrder)) {
            // No sorting-> sort on names by default
            sort_order = sortOrder.toString()
        }
        val cursor = queryBuilder.query(mDatabase,
            projection, selection, selectionArgs,
            null, null, sort_order)
        /**
         * register to watch a content URI for changes
         */
        cursor.setNotificationUri(context!!.contentResolver, uri)
        return cursor
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        val row = mDatabase?.insert(TABLE_NAME, "", values)
        // If record is added successfully
        if (row != null) {
            if (row > 0) {
                var newUri = ContentUris.withAppendedId(CONTENT_URI, row)
                context?.contentResolver?.notifyChange(newUri, null)
                return newUri
            }
        }
        throw SQLException("Fail to add a new record into" + uri)
    }

    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<out String>?): Int {
        var count = 0
        when (mUriMatcher?.match(uri)) {
            NICKNAME -> count = mDatabase?.update(TABLE_NAME, values, selection, selectionArgs)!!
            NICKNAME_ID -> {
                var whereClause = ""
                if (!TextUtils.isEmpty(selection)) {
                    whereClause = " AND($selection)"
                }
                count = mDatabase?.update(TABLE_NAME, values, "$ID = ${uri?.lastPathSegment} $whereClause", selectionArgs)!!
            }
            else -> throw IllegalArgumentException("Unsupported URI " + uri)
        }
        context?.contentResolver?.notifyChange(uri, null);
        return count;
    }


    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int {
        var count = 0
        when (mUriMatcher?.match(uri)) {
            NICKNAME -> count = mDatabase?.delete(TABLE_NAME, selection, selectionArgs)!!
            NICKNAME_ID -> {
                var whereClause = ""
                if (!TextUtils.isEmpty(selection)) {
                    whereClause = " AND($selection)"
                }
                count = mDatabase?.delete(TABLE_NAME, "$ID = ${uri?.lastPathSegment} $whereClause", selectionArgs)!!
            }
            else -> throw IllegalArgumentException("Unsupported URI " + uri);
        }
        context?.contentResolver?.notifyChange(uri, null);
        return count;
    }

    override fun getType(uri: Uri): String? {
        when (mUriMatcher?.match(uri)) {
            NICKNAME -> return "vnd.android.cursor.dir/vnd.example.nicknames"
            NICKNAME_ID -> return "vnd.android.cursor.item/vnd.example.nicknames"
            else -> throw IllegalArgumentException("Unsupported URI:" + uri)
        }
    }

    private inner class DBHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

        override fun onCreate(db: SQLiteDatabase?) {
            db?.execSQL(CREATE_TABLE);
        }

        override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
            db?.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
            onCreate(db)
        }
    }

}

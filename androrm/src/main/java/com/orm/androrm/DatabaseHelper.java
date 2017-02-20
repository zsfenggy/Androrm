/**
 * Copyright (c) 2010 Philipp Giese
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package com.orm.androrm;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.orm.androrm.migration.Migration;
import com.orm.androrm.migration.Migrator;

/**
 * Class to open up a database connection.
 *
 * @author Philipp Giese
 * @modified by Stefan
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    /**
     * Tag for logging.
     */
    private static final String TAG = "ANDRORM:DB:HELPER";
    /**
     * Version of the database.
     */
    private static final int DATABASE_VERSION = 1;
    private static String FOREIGN_KEY_CONSTRAINTS = "ON";
    /**
     * {@link Set} containing names of all tables, that were created by this
     * class.
     */
    private static Set<String> mTables;
    /**
     * {@link Set} containing all classes, that are handled by the ORM.
     */
    private static Set<Class<? extends Model>> mModels;

    // add by Stefan
    public OnDatabaseUpgradeListener onDatabaseUpgradeListener;

    public static void setForeignKeyConstraints(boolean on) {
        if (on) {
            FOREIGN_KEY_CONSTRAINTS = "ON";
        } else {
            FOREIGN_KEY_CONSTRAINTS = "OFF";
        }
    }

    /**
     * Get a {@link Set} of model classes, that are handled by the ORM.
     *
     * @return {@link Set} of model classes.
     */
    private static Set<Class<? extends Model>> getModels() {
        if (mModels == null) {
            mModels = new HashSet<Class<? extends Model>>();
        }

        return mModels;
    }

    /**
     * Get a {@link Set} of all tables, that were created by this class.
     *
     * @return {@link Set} of tablenames.
     */
    private static Set<String> getTables() {
        if (mTables == null) {
            mTables = new HashSet<String>();
        }

        return mTables;
    }

    public DatabaseHelper(Context context, String dbName) {
        super(context, dbName, null, DATABASE_VERSION);
    }

    public DatabaseHelper(Context context, String dbName, int dbVersion) {
        super(context, dbName, null, dbVersion);
    }

    /**
     * Drops all tables of the database.
     *
     * @param db {@link SQLiteDatabase}.
     */
    public void drop(SQLiteDatabase db) {
        db.execSQL("PRAGMA foreign_keys=OFF;");

        for (String table : getTables()) {
            db.execSQL("DROP TABLE IF EXISTS `" + table + "`;");
        }

        db.execSQL("PRAGMA foreign_keys=" + FOREIGN_KEY_CONSTRAINTS + ";");

        getTables().clear();
        getModels().clear();
    }

    public void renameTable(SQLiteDatabase db, String from, String to) {
        String sql = "INSERT INTO " + to + " SELECT * FROM " + from;

        db.execSQL(sql);

        getTables().add(to);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        for (Class<? extends Model> model : getModels()) {
            List<TableDefinition> tableDefinitions = DatabaseBuilder.getTableDefinitions(model);
            if(null!=tableDefinitions) {
                for (TableDefinition definition : tableDefinitions) {
                    db.execSQL(definition.toString());
                    getTables().add(definition.getTableName());
                }
            }
        }

		/*
         * After all models of the user have been registered with the db, we
		 * also need to register the migrations table, that androrm uses to keep
		 * track which migration have already been applied, and which still need
		 * to be.
		 */
        addMigrations(db);
    }

    /**
     * Creates the internal table, that is used in order to keep track of
     * migrations, that were defined be a user. The migrations table is not
     * added to the global list of tables in order to prevent it from being
     * delete, when the user attempts to drop the database.
     *
     * @param db {@link SQLiteDatabase} instance.
     */
    private void addMigrations(SQLiteDatabase db) {
        List<TableDefinition> tableDefinitions = DatabaseBuilder.getTableDefinitions(Migration.class);
        if(null!=tableDefinitions) {
            for (TableDefinition definition : tableDefinitions) {
                db.execSQL(definition.toString());
            }
        }
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);

        if (!db.isReadOnly()) {
            // Enable or disable foreign key constraints
            db.execSQL("PRAGMA foreign_keys=" + FOREIGN_KEY_CONSTRAINTS + ";");
        }
    }

    /**
     * Androrm won't make use of the onUpgrade method. Instead we are using our
     * own mechanism called {@link Migration}. In order to update a
     * {@link Model} use the {@link Migrator} class instead.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion
                + ", which will destroy all data.");

        // tow lines below are source code.
        // drop(db);
        // onCreate(db);

        // add DataBaseUpgradeListener by Stefan
        if (null != onDatabaseUpgradeListener) {
            onDatabaseUpgradeListener.doUpgrade(db, oldVersion, newVersion);
        }
    }

    /**
     * Registers all given models with the ORM and triggers
     * {@link DatabaseHelper#onCreate(SQLiteDatabase)} to create the database.
     *
     * @param db     {@link SQLiteDatabase Database} instance.
     * @param models {@link List} of classes inheriting from {@link Model}.
     */
    public void setModels(SQLiteDatabase db, Collection<Class<? extends Model>> models) {
        mModels = new HashSet<Class<? extends Model>>();
        mModels.addAll(models);

        onCreate(db);
    }

    /**
     * add by Stefan
     *
     * @author Administrator
     */
    public interface OnDatabaseUpgradeListener {
        boolean doUpgrade(SQLiteDatabase db, int oldVersion, int newVersion);
    }

}

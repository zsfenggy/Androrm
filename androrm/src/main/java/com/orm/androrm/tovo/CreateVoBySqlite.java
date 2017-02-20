package com.orm.androrm.tovo;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * add by Stefan
 *
 * @author Administrator
 */
public class CreateVoBySqlite {

    public static <T> T sql2VO(SQLiteDatabase db, String sql, Class<T> clazz) {
        Cursor c = db.rawQuery(sql, null);
        return (T) cursor2VO(c, clazz);
    }

    public static <T> T sql2VO(SQLiteDatabase db, String sql, String[] selectionArgs, Class<T> clazz) {
        Cursor c = db.rawQuery(sql, selectionArgs);
        return (T) cursor2VO(c, clazz);
    }

    public static <T> List<T> sql2VOList(SQLiteDatabase db, String sql, Class<T> clazz) {
        Cursor c = db.rawQuery(sql, null);
        return cursor2VOList(c, clazz);
    }

    public static <T> List<T> sql2VOList(SQLiteDatabase db, String sql, String[] selectionArgs, Class<T> clazz) {
        Cursor c = db.rawQuery(sql, selectionArgs);
        return cursor2VOList(c, clazz);
    }

    public static <T> T cursor2VO(Cursor c, Class<T> clazz) {
        if (c == null) {
            return null;
        }
        try {
            c.moveToNext();
            return setValues2Fields(c, clazz);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            c.close();
        }
    }

    public static <T> List<T> cursor2VOList(Cursor c, Class<T> clazz) {
        if (c == null) {
            return null;
        }
        List<T> list = new java.util.LinkedList<T>();
        try {
            while (c.moveToNext()) {
                T obj = setValues2Fields(c, clazz);
                list.add(obj);
            }
            return list;
        } catch (Exception e) {
            e.printStackTrace();
            e.printStackTrace();
            return null;
        } finally {
            c.close();
        }
    }

    public static <T> T setValues2Fields(Cursor c, Class<T> clazz) throws Exception {
        String[] columnNames = c.getColumnNames();
        T obj = clazz.newInstance();
        Field[] fields = clazz.getFields();
        Field[] arrayOfField1;
        int length = (arrayOfField1 = fields).length;
        for (int i = 0; i < length; i++) {
            Field _field = arrayOfField1[i];
            Class<?> typeClass = _field.getType();
            for (String columnName : columnNames) {
                if (columnName.equalsIgnoreCase(_field.getName())) {
                    typeClass = getBasicClass(typeClass);
                    boolean isBasicType = isBasicType(typeClass);
                    if (isBasicType) {
                        String _str = c.getString(c.getColumnIndex(columnName));
                        if (_str == null) {
                            break;
                        }
                        _field.setAccessible(true);
                        if (typeClass.equals(Boolean.class)) {
                            _field.set(obj, "1".equals(_str));
                        } else {
                            Constructor<?> cons = typeClass.getConstructor(String.class);
                            Object attribute = cons.newInstance(_str);
                            _field.set(obj, attribute);
                        }
                        break;
                    }
                    if (typeClass.getName().equals("[B")) {
                        byte[] bytes = c.getBlob(c.getColumnIndex(columnName));
                        _field.set(obj, bytes);
                    }
                }
            }
        }
        return obj;
    }

    private static boolean isBasicType(Class<?> typeClass) {
        return (typeClass.equals(Integer.class)) || (typeClass.equals(Long.class)) || (typeClass.equals(Float.class))
                || (typeClass.equals(Double.class)) || (typeClass.equals(Boolean.class))
                || (typeClass.equals(Byte.class)) || (typeClass.equals(Short.class))
                || (typeClass.equals(String.class));
    }

    public static Class<?> getBasicClass(Class<?> typeClass) {
        Class<?> _class = basicMap.get(typeClass);
        if (_class == null)
            _class = typeClass;
        return _class;
    }

    private static Map<Class<?>, Class<?>> basicMap = new HashMap<>();

    static {
        basicMap.put(Integer.TYPE, Integer.class);
        basicMap.put(Long.TYPE, Long.class);
        basicMap.put(Float.TYPE, Float.class);
        basicMap.put(Double.TYPE, Double.class);
        basicMap.put(Boolean.TYPE, Boolean.class);
        basicMap.put(Byte.TYPE, Byte.class);
        basicMap.put(Short.TYPE, Short.class);
    }

}

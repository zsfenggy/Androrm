package com.orm.androrm.tovo;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;

/**
 * add by Stefan
 * 
 * @author Administrator
 *
 */
public class SerializableInterface {

	public static byte[] serialize(Object obj) {
		try {
			ByteArrayOutputStream mem_out = new ByteArrayOutputStream();
			ObjectOutputStream out = new ObjectOutputStream(mem_out);

			out.writeObject(obj);

			byte[] bytes = mem_out.toByteArray();

			out.close();
			mem_out.close();

			return bytes;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static <T> Object deserialize(byte[] bytes, Class<T> clazz) {
		try {
			ByteArrayInputStream mem_in = new ByteArrayInputStream(bytes);
			ObjectInputStream in = new ObjectInputStream(mem_in);

			Object obj = in.readObject();

			in.close();
			mem_in.close();

			return obj;
		} catch (StreamCorruptedException e) {
			return null;
		} catch (ClassNotFoundException e) {
			return null;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
}

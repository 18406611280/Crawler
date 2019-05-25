package com.aft.utils.copy;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

@SuppressWarnings("unchecked")
public class MyCopyUtils {

	/**
	 * 深度克隆
	 * @param <T>
	 * @param t
	 * @return
	 * @throws Exception
	 */
	public static <T> T deepCopy(T t) throws Exception {
		T o = null;
		ObjectOutputStream oos = null;
		ObjectInputStream ois = null;
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			oos = new ObjectOutputStream(baos);
			oos.writeObject(t);
			oos.flush();
			oos.close();
			ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
			ois = new ObjectInputStream(bais);

			o = (T)ois.readObject();
			ois.close();
			return o;
		} catch (Exception e) {
			throw e;
		} finally {
			if(null != oos) oos = null;
			if(null != ois) ois = null;
		}
	}
}
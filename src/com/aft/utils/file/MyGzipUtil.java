package com.aft.utils.file;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class MyGzipUtil {
	
	private static final int BUFFER_SIZE = 20480;
	
	/**
	 * 压缩文件
	 * @param gzipPath
	 * @param gzipFileName
	 * @param srcFile
	 * @return
	 * @throws Exception
	 */
	public static File gzipFile(String gzipPath, String gzipFileName, File srcFile) throws Exception {
		File pathFile = new File(gzipPath);
		if(!pathFile.exists()) pathFile.mkdirs();
		
		byte[] buf = new byte[BUFFER_SIZE];
		GZIPOutputStream gzos = null;
		FileInputStream in = null;
		try {
			File gzipFile = new File(gzipPath, gzipFileName);
			gzos = new GZIPOutputStream(new FileOutputStream(gzipFile));
			in = new FileInputStream(srcFile);
			int len = 0;
			while((len = in.read(buf)) != -1) {
				gzos.write(buf, 0, len);
			}
			in.close();
			gzos.flush();
			gzos.close();
			return gzipFile;
		} catch(Exception e) {
			throw e;
		} finally {
			if(null != in) in = null;
			if(null != gzos) gzos = null;
		}
	}
	
	/**
	 * 返回zip压缩的二进制
	 * @param bytes
	 * 
	 * @throws Exception 
	 */
	public static byte[] gzipFile(byte[] bytes) throws Exception {
		GZIPOutputStream gos = null;
		ByteArrayOutputStream bao = null;
		try {
			bao = new ByteArrayOutputStream();
			gos = new GZIPOutputStream(bao);
			gos.write(bytes);
			gos.flush();
			gos.close();
			bytes = bao.toByteArray();
			bao.flush();
			bao.close();
			return bytes;
		} catch (Exception e) {
			throw e;
		} finally {
			if(null != bao) bao = null;
			if(null != gos) gos = null;
		}
	}
	
	/**
	 * 返回 内容二进制
	 * @param bytes
	 * @return
	 * @throws Exception
	 */
	public static byte[] getGzipContent(byte[] bytes) throws Exception {
		GZIPInputStream gis = null;
		ByteArrayOutputStream bao = null;
		try {
			gis = new GZIPInputStream(new ByteArrayInputStream(bytes));
			bao = new ByteArrayOutputStream();
			int len = 0;
			byte[] buf = new byte[102400];
			while((len = gis.read(buf)) != -1) {
				bao.write(buf, 0, len);
			}
			gis.close();
			bao.flush();
			bytes = bao.toByteArray();
			bao.close();
			return bytes;
		} catch(Exception e) {
			throw e;
		} finally {
			if(null != gis) gis = null;
			if(null != bao) bao = null;
		}
	}
}
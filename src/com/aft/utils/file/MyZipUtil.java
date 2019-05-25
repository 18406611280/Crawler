package com.aft.utils.file;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import com.aft.utils.MyStringUtil;

public class MyZipUtil {
	
	private static final int BUFFER_SIZE = 20480;
	
	/**
	 * 压缩
	 * @param path
	 * @param zipFileName
	 * @param files
	 * 
	 * @return 新建文件名
	 * @throws Exception 
	 */
	public static String zipFile(String path, String zipFileName, File[] files) throws Exception {
		File pathFile = new File(path);
		if(!pathFile.exists()) pathFile.mkdirs();
		
		byte[] buf = new byte[BUFFER_SIZE];
		ZipOutputStream zos = null;
		FileInputStream in = null;
		try {
			String zipFileFullName = MyStringUtil.getFilePath(path, zipFileName);
			zos = new ZipOutputStream(new FileOutputStream(zipFileFullName));
			for(File f : files) {
				in = new FileInputStream(f);
				zos.putNextEntry(new ZipEntry(f.getName()));
				int len;
				while((len = in.read(buf)) > 0) {
					zos.write(buf, 0, len);
				}
				zos.closeEntry();
				in.close();
			}
			zos.flush();
			zos.close();
			return zipFileFullName;
		} catch (Exception e) {
			throw e;
		} finally {
			if(null != in) in = null;
			if(null != zos) zos = null;
		}
	}
	
	/**
	 * 压缩
	 * @param path zip地址
	 * @param zipFileName zip文件名
	 * @param fileFullName 要压缩的文件全路径
	 * 
	 * @return 新建文件名
	 * @throws Exception 
	 */
	public static String zipFile(String path, String zipFileName, File file) throws Exception {
		return zipFile(path, zipFileName, new File[]{file});
	}
	
	/**
	 * 压缩
	 * @param path zip地址
	 * @param zipFileName zip文件名
	 * @param fileFullName 要压缩的文件全路径
	 * 
	 * @return 新建文件名
	 * @throws Exception 
	 */
	public static String zipFile(String path, String zipFileName, String fileFullName) throws Exception {
		return zipFile(path, zipFileName, new File(fileFullName));
	}
}
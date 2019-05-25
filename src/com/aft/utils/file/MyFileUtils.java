package com.aft.utils.file;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class MyFileUtils {
	
	/**
	 * 递归删除目录下的所有文件及子目录下所有文件
	 * 
	 * @param dir 将要删除的文件目录
	 * @return boolean Returns "true" if all deletions were successful. If a
	 *         deletion fails, the method stops attempting to delete and returns
	 *         "false".
	 */
	public static boolean deleteDir(File dir) {
        if(dir.isDirectory()) {
            String[] children = dir.list();
            for(int i=0; i<children.length; i++) {	// 递归删除目录中的子目录下
                boolean success = deleteDir(new File(dir, children[i]));
                if(!success) return false;
            }
        }
        return dir.delete();
    }
	
	/**
	 * 生成文件
	 * @param filePath
	 * @param fileName
	 * @param content
	 * 
	 * @throws Exception
	 */
	public static void createFile(String filePath, String fileName, String content) throws Exception {
		MyFileUtils.createFile(filePath, fileName, content, false);
	}
	
	
	/**
	 * 生成文件
	 * @param filePath
	 * @param fileName
	 * @param content
	 * @param append
	 * 
	 * @throws Exception
	 */
	public static void createFile(String filePath, String fileName, String content, boolean append) throws Exception {
		FileOutputStream fos = null;
		try {
			File file = new File(filePath);
			if(!file.exists()) file.mkdirs();
			fos = new FileOutputStream(new File(filePath, fileName), append);
			if(append) content += "\r";
			fos.write(content.getBytes("UTF-8"));
			fos.flush();
			fos.close();
		} catch (Exception e) {
			throw e;
		} finally {
			if(null != fos) fos = null;
		}
	}
	
	/**
	 * 生成文件
	 * @param path
	 * @param fileName
	 * @param inputStream
	 * 
	 * @throws Exception
	 */
	public static void createFile(File file, InputStream inputStream) throws Exception {
		FileOutputStream fos = null;
		try {
			if(!file.getParentFile().exists()) file.getParentFile().mkdirs();
			fos = new FileOutputStream(file);
			byte[] bytes = new byte[102400];
			int len = 0;
			while((len=inputStream.read(bytes, 0, bytes.length)) != -1) {
				fos.write(bytes, 0, len);
			}
			fos.flush();
			fos.close();
			inputStream.close();
		} catch (Exception e) {
			throw e;
		} finally {
			if(null != fos) fos = null;
			if(null != inputStream) inputStream = null;
		}
	}
	
	/**
	 * 返回 byte[]
	 * @param inputStream
	 * @param charset
	 * @return
	 * @throws Exception
	 */
	public static String getStringByInputStream(InputStream inputStream, String charset) throws Exception {
		return new String(MyFileUtils.getByteArrayByInputStream(inputStream), charset);
	}
	
	/**
	 * 返回 byte[]
	 * @param inputStream
	 * 
	 * @return
	 * @throws Exception
	 */
	public static byte[] getByteArrayByInputStream(InputStream inputStream) throws Exception {
		ByteArrayOutputStream baos = null;
		try {
			baos = new ByteArrayOutputStream();
			byte[] temps = new byte[20480];
			int len = 0;
			while((len=inputStream.read(temps, 0, temps.length)) != -1) {
				baos.write(temps, 0, len);
			}
			byte[] bytes = baos.toByteArray();
			baos.flush();
			baos.close();
			inputStream.close();
			return bytes;
		} catch (Exception e) {
			throw e;
		} finally {
			if(null != baos) baos = null;
			if(null != inputStream) inputStream = null;
		}
	}
	
	/**
	 * 获取file下的所有文件
	 * @param file
	 * @return
	 */
	public static List<File> allFiles(File file) {
		if(null == file || !file.exists()) return new ArrayList<File>();
		List<File> files = new ArrayList<File>();
		for(File f : file.listFiles()) {
			if(f.isDirectory()) files.addAll(MyFileUtils.allFiles(f));
			else files.add(f);
		}
		return files;
	}
	
	/**
	 * 删除 文件 如果文件夹空,删除文件夹
	 * @param file
	 * @param timeMillis
	 */
	public static void deleteFile(File file, long timeMillis) {
		if(!file.isDirectory()) {	// 不是目录,判断时间,符合就删除
			if(file.lastModified() <= timeMillis) file.delete();
			return ;
		}
		
		// 目录
		for(File f : file.listFiles()) {
			MyFileUtils.deleteFile(f, timeMillis);
		}
		
		if(0 == file.listFiles().length) file.delete();
	}
	
	/**
	 * 改名
	 * @param srcFile
	 * @param renameFile
	 */
	public static void rename(File srcFile, File renameFile) {
		if(renameFile.exists()) return ;
		if(!renameFile.getParentFile().exists()) renameFile.getParentFile().mkdirs();
		srcFile.renameTo(renameFile);
	}
	/**
	 * 返回 byte[]
	 * @param fullFileName
	 * 
	 * @return
	 * @throws Exception
	 */
	public static byte[] getByteArrayByFile(String fullFileName) throws Exception {
		return getByteArrayByInputStream(new FileInputStream(fullFileName)); 
	}
}
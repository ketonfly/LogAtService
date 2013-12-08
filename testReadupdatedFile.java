package test;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

public class testReadUpdatedFile {
	public static int Begin = 0;

	public void appendMethodA(String fileName, String content) {
		try {
			// 打开一个随机访问文件流，按读写方式
			RandomAccessFile randomFile = new RandomAccessFile(fileName, "rw");
			// 文件长度，字节数
			long fileLength = randomFile.length();
			// 将写文件指针移到文件尾。
			randomFile.seek(fileLength);
			randomFile.writeBytes(content);
			randomFile.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		String file = "/home/keton/new_workspace/LogAtService/src/logs/test.log";
		RandomAccessFile randomFile = null;
		testReadUpdatedFile trf = new testReadUpdatedFile();

		try {
			randomFile = new RandomAccessFile(file, "r");
			long fileLength;
			int i = 0;
			while (true) {
				
				try {
					fileLength = randomFile.length();
					if (fileLength > Begin) {
						randomFile.seek(Begin);
						byte[] bytes = new byte[10];
						int byteread = 0;
						while ((byteread = randomFile.read(bytes)) != -1) {
							Begin = Begin + byteread;
							System.out.write(bytes, 0, byteread);
							trf.appendMethodA(
									"/home/keton/new_workspace/LogAtService/src/logs/backup.log",
									new String(bytes));
						}
						try {
							trf.appendMethodA(
									"/home/keton/new_workspace/LogAtService/src/logs/backup.log",
									new String("the test number :"+i));
							Thread.sleep(5000);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}

				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				i++;
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (randomFile != null) {
				try {
					randomFile.close();
				} catch (IOException e1) {
				}
			}
		}

	}
}

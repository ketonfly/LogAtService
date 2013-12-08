package test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Comparator;
import java.util.TreeMap;

public class ReadFileFromTime {
	// TODO
	// 1. tMap.clean()
	// 获取的时候已经自动更新了
	// 2. 当只有一个文件的时候，如何操作
	//	已经包括了
	// 3. Begin失效，丢失部分相关数据
	//	增大 MaxIndex 减少极端情况

	String folder;
	Long LastReadedTime = (long) 0;
	Long nowLastModifyTime;
	int nextBegin = 0;

	// TreeMap<Long, File> tMap = null;

	// String lastupdatefile;

	public ReadFileFromTime(String folder) {
		this.folder = folder;
	}

	// 获取当前文件夹下面所有文件的名字
	public TreeMap<Long, File> getAllFiles() {
		File file = new File(folder);
		try {
			if (!file.exists()) {
				file.createNewFile();
				System.out.println("folder is not exist!");
				System.exit(0);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		File test[] = file.listFiles();
		TreeMap<Long, File> ftan = new TreeMap<Long, File>(
				new Comparator<Long>() {
					public int compare(Long l1, Long l2) {
						return l1.compareTo(l2);
					}
				});
		for (int i = 0; i < test.length; i++) {
			if (test[i].lastModified() >= this.LastReadedTime)
				ftan.put(new Long(test[i].lastModified()), test[i]);
		}
		return ftan;
	}

	// 记录当前读取文件的modify time
	public void setLastReadedTime(Long lrt) {
		this.LastReadedTime = lrt;
	}

	// 获得当前读取或者刚刚完成读取的文件的modify time
	public long getLastReadedTime() {
		return this.LastReadedTime;
	}

	// 获取当前问价夹下最老文件的modify time
	public long getLastModifyTime(TreeMap<Long, File> tMap) {
		return tMap.firstKey();
	}

	// /获取当前问价夹下最老文件的filename
	public File getLastModifyFile(TreeMap<Long, File> tMap) {
		return tMap.get(tMap.firstKey());
	}

	// /获取当前问价夹下第二老文件的modify time
	public long getSecondLastModifyTime(TreeMap<Long, File> tMap) {
		return tMap.ceilingKey(this.LastReadedTime + 1);
	}

	// 获取当前问价夹下第二老文件的名字
	public File getSecondLastModifyFile(TreeMap<Long, File> tMap) {
		return tMap.get(this.getSecondLastModifyTime(tMap));
	}

	public void ReadFile(Long lrt, int mybegin) {
		System.out.println("in readfile");
		int begin = mybegin;
		RandomAccessFile randomFile = null;
		String filename = this.getAllFiles().get(lrt).toString();
		System.out.println(filename);
		this.setLastReadedTime(lrt);
		TreeMap<Long, File> tMap = null;

		try {
			randomFile = new RandomAccessFile(filename, "r");
			String backupfile = "/home/keton/new_workspace/LogAtService/src/backup/backup.log";
			File file = new File("backupfile");
			if (!file.exists()) {
				try {
					file.createNewFile();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			long fileLength;

			try {
				fileLength = randomFile.length();
				System.out.println(fileLength);
				if (fileLength > begin) {
					randomFile.seek(begin);
					byte[] bytes = new byte[10];
					int byteread = 0;
					while ((byteread = randomFile.read(bytes)) != -1) {
						begin = begin + byteread;
						this.appendMethodA(backupfile, new String(bytes));
					}
				}
				tMap = this.getAllFiles();
				if (this.getSecondLastModifyTime(tMap) == tMap.lastKey()
						&& tMap.get(LastReadedTime) == null) {
					this.nextBegin = begin;
				} else {
					this.nextBegin = 0;
				}

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (randomFile != null) {
				try {
					System.out.println("in close");
					randomFile.close();
				} catch (IOException e1) {
				}
			}
		}

	}

	public void func() {
		System.out.println("func");
		TreeMap<Long, File> tMap = this.getAllFiles();
		this.setLastReadedTime(this.getLastModifyTime(tMap));

		ReadFile(this.LastReadedTime, 0);
		System.out.println("before while");
		while (true) {
			tMap = this.getAllFiles();
			ReadFile(this.getSecondLastModifyTime(tMap), this.nextBegin);
		}
	}

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

	public static void main(String args[]) {

		String folder = "/home/keton/new_workspace/LogAtService/src/logs";
		ReadFileFromTime rfft = new ReadFileFromTime(folder);
		TreeMap<Long, File> tMap = null;
		int  i = 0;
		while (true) {
			
			tMap = rfft.getAllFiles();
			System.out.println(tMap);
			if(i%2==0){
				rfft.ReadFile(tMap.firstKey(), rfft.nextBegin);
				System.out.println("first key "+ "nextBegin = "+ rfft.nextBegin);
			}
			else {
				rfft.ReadFile(tMap.lastKey(),rfft.nextBegin);
				System.out.println("last key "+ "nextBegin = "+ rfft.nextBegin);
			}
			i++;
			try {
				Thread.sleep(0);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		/*
		 * System.out.println(rfft.getLastModifyTime(rfft.getAllFiles()));
		 * System.out.println(rfft.getLastModifyFile(rfft.getAllFiles()));
		 * rfft.setLastModifyTime(rfft.getLastModifyTime(rfft.getAllFiles()));
		 * System.out.println(rfft.getSecondLastModifyTime(rfft.getAllFiles()));
		 * System.out.println(rfft.getSecondLastModifyFile(rfft.getAllFiles()));
		 * rfft.func();
		 */

	}
}

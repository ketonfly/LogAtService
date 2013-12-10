package test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.Comparator;
import java.util.Properties;
import java.util.TreeMap;

public class ReadFileFromTime {
	// TODO
	// 1. tMap.clean()
	// 获取的时候已经自动更新了
	// 2. 当只有一个文件的时候，如何操作
	// 已经包括了
	// 3. Begin失效，丢失部分相关数据
	// 增大 MaxIndex 减少极端情况
	
	// TODO new
	// 1. refreshtMap 缺少一个log还没有生成的时候的等待的情况
	public Properties props = new Properties();
	public FileOutputStream out;
	public String configname;
	public String filePath;
	public String offset;
	public String StringOfLastReadedTime;

	String folder;
	Long LastReadedTime = (long) 0;
	int nextBegin = 0;
	TreeMap<Long, File> tMap = null;
	

	// TreeMap<Long, File> tMap = null;

	// String lastupdatefile;

	public ReadFileFromTime(String folder,String configname) {
		this.folder = folder;
		filePath = configname + ".properties";
		File file = new File(filePath);
		InputStream input;

		try {
			if (!file.exists())
				file.createNewFile();
			input = new FileInputStream(file);
			props.load(input);

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
			out = new FileOutputStream(filePath);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public int getOffset(){
		offset = props.getProperty(""+"NextBegin", "0");
		return Integer.parseInt(offset);
	}
	public long getStringOfLastReadedTime(){
		StringOfLastReadedTime = props.getProperty(""+"LastReadedTime", "0");
		return Long.parseLong(StringOfLastReadedTime);
	}
	public void setStringOfLastReadedTime(long LastReadedTime){
		props.setProperty(""+"LastReadedTime", Long.toString(LastReadedTime));
	}
	public void setOffset(int NextBegin){
		props.setProperty(""+"NextBegin", Integer.toString(NextBegin));
	}
	

	// 获取当前文件夹下面所有文件的名字
	public TreeMap<Long, File> refreshtMap() {
		//System.out.println("in refresh");
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
		//缺少一个如果没有文件等待的情况
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

	// /获取当前问价夹下第二老文件的modify time
	public long getNextOfLastReadedTime(TreeMap<Long, File> tMap) {
		return tMap.ceilingKey(this.LastReadedTime + 1);
	}

	public long getNewestModifyTime(TreeMap<Long, File> tMap) {
		return tMap.lastKey();
	}

	// finish reading the file && refresh tMap and set NextBegin
	public void ReadFile(Long lrt, int mybegin) {
		int begin = mybegin;
		RandomAccessFile randomFile = null;
		String filename = this.tMap.get(lrt).toString();
		this.setLastReadedTime(lrt);

		String backupfile = "/home/keton/new_workspace/LogAtService/src/backup/backup.log";
		long fileLength;

		try {

			randomFile = new RandomAccessFile(filename, "r");

			File file = new File("backupfile");
			if (!file.exists()) {
				try {
					file.createNewFile();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			try {
				fileLength = randomFile.length();
				if (fileLength > begin) {
					randomFile.seek(begin);
					byte[] bytes = new byte[10];
					int byteread = 0;
					while ((byteread = randomFile.read(bytes)) != -1) {
						begin = begin + byteread;
						this.appendMethodA(backupfile, new String(bytes));
					}
				}
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				// 刷新tMap
				tMap = refreshtMap();
				// 如果tMap中找不到lrt对应的File
				if (tMap.get(lrt) == null) {
					if (this.getNextOfLastReadedTime(tMap) == tMap
							.lastKey()){
						this.nextBegin = begin;
						System.out.println("2");
					}
					else if (this.getLastReadedTime() < tMap.firstKey()){
						this.nextBegin = 0;
						System.out.println("1");
					}
						
				}
				// 如果tMap中能找到lrt对应的File
				else {
					if (this.getLastReadedTime() == tMap.lastKey()){
						this.nextBegin = begin;
						System.out.println("3");
					}
						
					else{
						this.nextBegin = 0;
						System.out.println("4");
					}
						
				}
				System.out.println("this.begin = "+this.nextBegin);
				System.out.println(this.getLastReadedTime());

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
					System.out.println(filename);
					randomFile.close();
				} catch (IOException e1) {
				}
			}
		}

	}

	// control the File to be read
	public void ControlOfReadFile() {
		tMap = this.refreshtMap();
		LastReadedTime = this.getStringOfLastReadedTime();
		nextBegin = this.getOffset();
		
		while (true) {
			if (tMap.get(LastReadedTime) != null
					&& this.getLastReadedTime() == tMap.lastKey()){
				this.setStringOfLastReadedTime(LastReadedTime);
				this.setOffset(nextBegin);
				try {
					this.props.store(this.out, filePath);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				break;
				//this.ReadFile(this.getNewestModifyTime(tMap), this.nextBegin);
			}
				
			else
				this.ReadFile(this.getNextOfLastReadedTime(tMap),
						this.nextBegin);
			/*try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}*/
		}
	}

	// 将字节数组写入文件中
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
		ReadFileFromTime rfft = new ReadFileFromTime(folder,"test");
		rfft.ControlOfReadFile();

		/*
		 * System.out.println(rfft.getLastModifyTime(rfft.refreshtMap()));
		 * System.out.println(rfft.getLastModifyFile(rfft.refreshtMap()));
		 * rfft.setLastModifyTime(rfft.getLastModifyTime(rfft.refreshtMap()));
		 * System.out.println(rfft.getNextOfLastReadedTime(rfft.refreshtMap()));
		 * System.out.println(rfft.getSecondLastModifyFile(rfft.refreshtMap()));
		 * rfft.func();
		 */

	}
}

package grab.importdata.fileOperate;

/**
 * 特定字符串操作方法
 * @author licong
 *
 */
public class StringOperate {

	/**
	 * 统计字符串中\的个数
	 * @param str
	 * @return
	 * @return
	 */
	public static int stringNumbers(String str)  
    {
		int num=0;
		for(int i =0;i<str.length();i++){
			if(str.charAt(i)=='\\')num++;
		}
		for(int i =0;i<str.length();i++){
			if(str.charAt(i)=='/')num++;
		}
		return num;
    }
	
	/**
	 * 获取该path的文件名， 
	 * @param path I:\新浪微博\2014-07-01\湖北省\武汉市.json
	 * hdfs://namenode:9000/input/sinaData/export201407/2014-07-01/上海市/上海市.json
	 * @return 武汉市.json
	 */
	public static String getFileName(String path){
		String fileName = path;
		if(fileName.contains("\\")){
			for(fileName.contains("\\");;){
				if(fileName.contains("\\")){
					fileName = fileName.substring(fileName.indexOf("\\")+1, fileName.length());
				}
				else break;
			}
		}
		else if(fileName.contains("/")){
			for(fileName.contains("/");;){
				if(fileName.contains("/")){
					fileName = fileName.substring(fileName.indexOf("/")+1, fileName.length());
				}
				else break;
			}	
		}
		return fileName;
	}
	
	/**
	 * 获取城市名
	 * @param filePath 文件路径 I:\新浪微博\2014-07-01\湖北省\武汉市.json
	 * hdfs://namenode:9000/input/sinaData/export201407/2014-07-01/上海市/上海市.json
	 * @return 武汉市
	 */
	public static String getCityName(String filePath){
		String fileName = filePath;
		if(fileName.contains("\\")){
			for(fileName.contains("\\");;){
				if(fileName.contains("\\")){
					fileName = fileName.substring(fileName.indexOf("\\")+1, fileName.length());
				}
				else{
					fileName = fileName.substring(0, fileName.indexOf("."));
					break;
				} 
			}
		}
		else if(fileName.contains("/")){
			for(fileName.contains("/");;){
				if(fileName.contains("/")){
					fileName = fileName.substring(fileName.indexOf("/")+1, fileName.length());
				}
				else{
					fileName = fileName.substring(0, fileName.indexOf("."));
					break;
				} 
			}
		}
		return fileName;
	}
	
	/**
	 * 获取省名
	 * @param filePath 文件路径 I:\新浪微博\2014-07-01\湖北省\武汉市.json
	 * hdfs://namenode:9000/input/sinaData/export201407/2014-07-01/上海市/上海市.json
	 * @return 湖北省
	 */
	public static String getProvinceName(String filePath){
		String fileName = filePath;
		if(fileName.contains("\\")){
			for(fileName.contains("\\");;){
				int count = stringNumbers(fileName);
				if(count>1){
					fileName = fileName.substring(fileName.indexOf("\\")+1, fileName.length());
				}
				else if(count == 1){
					fileName = fileName.substring(0, fileName.indexOf("\\"));
					break;
				}
				else break;
			}
		}
		else if(fileName.contains("/")){
			for(fileName.contains("/");;){
				int count = stringNumbers(fileName);
				if(count>1){
					fileName = fileName.substring(fileName.indexOf("/")+1, fileName.length());
				}
				else if(count == 1){
					fileName = fileName.substring(0, fileName.indexOf("/"));
					break;
				}
				else break;
			}
		}
		return fileName;
	}
	
	/**
	 * 获取日期
	 * @param filePath 文件路径 I:\新浪微博\2014-07-01\湖北省\武汉市.json
	 * hdfs://namenode:9000/input/sinaData/export201407/2014-07-01/上海市/上海市.json
	 * @return 20140701
	 */
	public static String getDate(String filePath){
		String fileName = filePath;
		if(fileName.contains("\\")){
			for(fileName.contains("\\");;){
				int count = stringNumbers(fileName);
				if(count>2){
					fileName = fileName.substring(fileName.indexOf("\\")+1, fileName.length());
				}
				else if(count == 2){
					fileName = fileName.substring(0, fileName.indexOf("\\"));
					break;
				}
				else break;
			}
		}
		else if(fileName.contains("/")){
			for(fileName.contains("/");;){
				int count = stringNumbers(fileName);
				if(count>2){
					fileName = fileName.substring(fileName.indexOf("/")+1, fileName.length());
				}
				else if(count == 2){
					fileName = fileName.substring(0, fileName.indexOf("/"));
					break;
				}
				else break;
			}
		}
		return fileName.substring(0, 4)+fileName.substring(5, 7)+fileName.substring(8, 10);
	}
	
	/**
	 * 获取Tue Jul 01 12:23:09 CST 2014 中的时间12
	 * @param created_at
	 * @return 12
	 */
	public static int getTime(String created_at){
		String tmpString = created_at.substring(0, created_at.indexOf(":"));
		String timeString = tmpString.substring(tmpString.length()-2, tmpString.length());
		return Integer.parseInt(timeString);
	}
	
	/**
	 * 十进制转32位二进制字符串
	 * @param i
	 * @return
	 */
	public static String putIntToString(int i){
		String s="";
		for(int j = 31;j >= 0;j--){
			if(((1 << j) & i) != 0){
				s += "1";
			}
			else
				s += "0";
		}
		return s;
	}
	
	/**
	 * 将二进制字符串中的一天赋值位1
	 * @param s 32位二进制字符串
	 * @param day 日期，几号
	 * @return 32位二进制字符串
	 */
	public static String setOne(String s,int day){
		char[] temp = s.toCharArray();
		temp[32-day] = '1';
		return String.valueOf(temp);
	}
}

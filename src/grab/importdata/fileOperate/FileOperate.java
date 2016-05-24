package grab.importdata.fileOperate;

import java.io.File;
import java.util.List;

/**
 * 文件相关操作方法
 * @author licong
 *
 */
public class FileOperate {
	 /**
     * 递归查找某目录下的某文件名路径集合
     * @param pathString
     * @param fileName
     * @param list 用于存储返回值的List
     * @return
     * @throws Exception
     */
    public static List<File> getFile(String pathString,String fileName,List<File> list) throws Exception{
		try
		{
			File f=new File(pathString);
			if(f.isDirectory())
			{
				File[] fList=f.listFiles();
				for(int j=0;j<fList.length;j++)
				{
					if(fList[j].isDirectory())
					{
						getFile(fList[j].getPath(),fileName,list); 
					}
				}
				for(int j=0;j<fList.length;j++)
				{
					if(fList[j].isFile())
					{
						if(fList[j].toString().contains(fileName)){
							list.add(fList[j]);
						}
					}

				}
			}
		}
		catch(Exception e)
		{
			System.out.println("Error： " + e);
		}
		return list;
	}
	
	/**
	 * 获取某路径下的所有文件路径
	 * @param path
	 * @param list 用于存储返回值的List
	 * @return	List <File>
	 */
	public static List<File> getFile(String path,List<File> list){
		File file=new File(path);
		File[] tempList = file.listFiles();
		for (int i = 0; i < tempList.length; i++) {
			if (tempList[i].isDirectory()) {
				getFile(tempList[i].toString(),list);
			}
			if(tempList[i].isFile()){
				list.add(tempList[i]);
			}
		}
		return list;
	}

	/**
	 * 递归查找某路径下的某路径名路径集合
	 * @param pathString
	 * @param dirName
	 * @param list 用于存储返回值的List
	 * @return 路径list
	 * @throws Exception
	 */
	public static List<File> getDirectory(String pathString,String dirName,List<File> list) throws Exception{
		try
		{
			File f=new File(pathString);
			if(f.isDirectory())
			{
				File[] fList=f.listFiles();
				for(int j=0;j<fList.length;j++)
				{
					if(fList[j].isDirectory())
					{
						if(fList[j].toString().contains(dirName))
							list.add(fList[j]);
						getDirectory(fList[j].getPath(),dirName,list); 
					}
				}
			}
		}
		catch(Exception e)
		{
			System.out.println("Error： " + e);
		}
		return list;
	}
	
	/**
	 * 获取某路径下的所有文件夹路径
	 * @param path
	 * @param list 用于存储返回值的List
	 * @return	List <File>
	 */
	public static List<File> getFileDirectory(String path,List<File> list){
		File file=new File(path);
		File[] tempList = file.listFiles();
		for (int i = 0; i < tempList.length; i++) {
			if (tempList[i].isDirectory()) {
				list.add(tempList[i]);
				getFileDirectory(tempList[i].toString(),list);
			}
		}
		return list;
	}
}

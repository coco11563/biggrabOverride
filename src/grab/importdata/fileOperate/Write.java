package grab.importdata.fileOperate;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;

/**
 * 写文件方法
 * @author licong
 *
 */
public class Write {

	/**
	 * 写入json文件
	 * @param path
	 * @param json
	 * @param fileName
	 */
	public static void writeJson(String path,Object json,String fileName){
        BufferedWriter writer = null;
        File file = new File(path);
        if(!file.exists()){
            file.mkdirs();
        }
        file = new File(path + fileName + ".json");
        if(!file.exists()){
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } 
        try {
            writer = new BufferedWriter(new FileWriter(file));
            writer.write(json.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                if(writer != null){
                    writer.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
	
	/**
	 * 写入txt
	 * @param path
	 * @param txtName
	 * @param content
	 * @throws IOException
	 */
	public static void writerText(String path,String txtName,String content) throws IOException {
        File dirFile = new File(path);
        if (!dirFile.exists()) {
            dirFile.mkdirs();
        }
        File txtFile = new File(path+txtName+".txt");
        if (!txtFile.exists()) {
        	txtFile.createNewFile();
        }
        FileOutputStream fos = new FileOutputStream(path+txtName+".txt",true);//true��ʾ���ļ�ĩβ׷��  
        fos.write(content.getBytes());  
        fos.close();
	}
}

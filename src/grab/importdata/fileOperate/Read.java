package grab.importdata.fileOperate;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import json.JSONArray;
import json.JSONException;
import json.JSONObject;

/**
 * 读文件方法
 * @author licong
 *
 */
public class Read {

	/**
	 * 读取我们的json数据
	 * @param path
	 * @return
	 * @throws IOException
	 * @throws JSONException
	 */
	public static JSONArray read_jsonFile ( String path ) throws IOException,JSONException
	{
		JSONArray json_array = new JSONArray ( ) ;
		File file = new File ( path ) ;
		BufferedReader reader = new BufferedReader ( new FileReader ( file ) ) ;
		String tempString = null ;
		while ( ( tempString = reader.readLine ( ) ) != null )
		{
			json_array.put ( new JSONObject ( tempString ) ) ;
			}
		reader.close ( ) ;
		return json_array ;
		}
	
	/**
	 * 读取文件
	 * @param path
	 * @return
	 * @throws JSONException 
	 */
	public static String readJson(String path) throws JSONException{
		 //从给定位置获取文件
        File file = new File(path);
        BufferedReader reader = null;
        //返回值,使用StringBuffer
        StringBuffer data = new StringBuffer();
        //
        try {
            reader = new BufferedReader(new FileReader(file));
            //每次读取文件的缓存
            String temp = null;
            while((temp = reader.readLine()) != null){
                data.append(temp);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            //关闭文件流
            if (reader != null){
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return data.toString();
    }
	
	/**
	 * 读取文件
	 * @param path
	 * @return
	 * @throws IOException
	 */
//	public static String readFile(String path) throws IOException{
//		FileInputStream inputStream = null;
//		Scanner sc = null;
//		String json=null;
//		List<String> tempList = new ArrayList<String>();
//		int i=0,j=0;
//		try {
//		    inputStream = new FileInputStream(path);
//		    sc = new Scanner(inputStream, "UTF-8");
//		    while (sc.hasNextLine()) {
//		        String line = sc.nextLine();
//		        json = json +line;
//		        i++;
//		        if(i==100){
//		        	j++;
//		        	System.out.println(j);
//		        	i=0;
//		        	tempList.add(json);
//		        	json =null;
//		        	}
//		    }
//		    // note that Scanner suppresses exceptions
//		    if (sc.ioException() != null) {
//		        throw sc.ioException();
//		    }
//		} finally {
//		    if (inputStream != null) {
//		        inputStream.close();
//		    }
//		    if (sc != null) {
//		        sc.close();
//		    }
//		}
//		if(json!=null){
//			tempList.add(json);
//			}
//		String fileString ="";
//		for(int m=0;m<tempList.size();m++){
//			fileString=fileString+tempList.get(m);
//			System.out.println(m);
//		}
//		return fileString;
//	}
}

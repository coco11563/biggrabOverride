package method;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import json.JSONArray;
import json.JSONException;
import json.JSONObject;
public class readJSONFile {
	
	/**
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
		reader.close( );
		return json_array;
	}
	
	/**
	 * ����ȡ����json�ļ����´洢��write_path·���¡�����Ϊ�人��.json
	 * 
	 * @param array
	 * @throws JSONException
	 * @throws Exception
	 */
	public static void storFiles(JSONArray array) throws JSONException, Exception
	{
		String 				write_path				=		"" ;
		JSONObject 		write_json_object 	= 		null ;
		File 					write_file				=		null ;
		FileWriter 			write_fw					=		null ;
		int count = 0;
		for(int i=0;i<array.length();i++)
		{
			write_json_object 	= 	array.getJSONObject ( i );
			write_path 				=  	"D:\\sinadata" ;		//�洢·��
			//write_path 				=  	"C:\\Users\\xgxy003win8\\Pictures" ;
			write_file 				= 		new File ( write_path );
			if ( !write_file.isFile ( ) ) write_file.mkdirs  ( );
			write_fw 				= 		new FileWriter(write_path + "\\" + "�人��.json",true);
			write_fw.write( ( write_json_object.toString ( ) + "\r\n") );
			count++;
			System.out.println(count);
			write_fw.close();
			write_fw = null;
		}
	}
	public static void storlistFiles(List<String> array) throws JSONException, Exception
	{
		String 				write_path				=		"" ;
		File 					write_file				=		null ;
		FileWriter 			write_fw					=		null ;
		int count=0;
		for(int i=0;i<array.size();i++)
		{
			String write_json_object 	= 	array.get(i);
			write_path 				=  	"E:\\data" ;		//�洢·��
			//write_path 				=  	"C:\\Users\\xgxy003win8\\Pictures" ;
			write_file 				= 		new File ( write_path );
			if ( !write_file.isFile ( ) ) write_file.mkdirs  ( );
			write_fw 				= 		new FileWriter(write_path + "\\" + "����ǩ��.json",true);
			write_fw.write( ( write_json_object.toString ( ) + "\r\n") );
			count++;
			System.out.println(count);
			write_fw.close();
			write_fw = null;
		}
	}
	
	
	/**
	 *  ѭ����ȡĳ·���������人�е����ݣ������ܡ�
	 * @param file
	 * @throws Exception
	 */
	
	public static void readFile(File file) throws Exception
	{
		
		if(file!=null)
		{
			if(file.isDirectory())
			{
				File f[]= file.listFiles();
				if(f!=null)
				{
					for(int i=0;i<f.length;i++)
					{
						readFile(f[i]);
					}
				}
			}else
			{
				if(file.getName().equals("�人��.json"))
				{
					System.out.println(file);
					JSONArray json_array = readJSONFile.read_jsonFile (file.toString());
					storFiles(json_array);
				}	
			}
		}
	}
	


	/**
	 * ȥ��poiid���ظ���id
	 * @param list
	 * @return
	 */
	public static List<String> dupliDel(List<String> list){
		int count =0;
		List<String> templist = new ArrayList<String>();
		Iterator<String> it=list.iterator(); 
		while(it.hasNext()){  
			String a=it.next();  
			if(templist.contains(a)){  
				it.remove();  
				count ++;
			}else{  
				templist.add(a);  
			}  
		}
		System.out.println("����"+count+"���ظ���¼��"); 
		return templist;
	}
	
	
	/**
	 *  ѭ����ȡĳ·���������人�е�ǩ�����ݣ�����ȡid  picurl
	 * @param file
	 * @throws Exception
	 */
	static List<List<String>> id_pic_list =new ArrayList<List<String>>();
	static int pic_sum = 0;
	static List<String> data_array =new ArrayList<String>();
	public static List<List<String>> readFilePic(File file) throws Exception{
		//public static List<String> readFilePic(File file) throws Exception{	
		if(file!=null)
		{
			if(file.isDirectory())
			{
				File f[]= file.listFiles();
				if(f!=null)
				{
					for(int i=0;i<f.length;i++)
					{
						readFilePic(f[i]);
					}
				}
			}else
			{
				if(file.getName().equals("�人��.json"))
				{
					System.out.println(file);
					JSONArray json_array = readJSONFile.read_jsonFile (file.toString());
					//��ȡǩ�������е������
					for(int i = 0;i<json_array.length();i++){
						JSONObject data = json_array.getJSONObject(i);
						String geo = data.getString ( "geo" );
						JSONObject geoObject = new JSONObject ( geo );
						String coordinates = geoObject.getString ( "coordinates" );
						String locationString[] = coordinates.split ( "," );
						Double lat = Double.valueOf ( locationString [ 0 ].substring ( 1,
								locationString [ 0 ].length ( ) ) );
						Double lon= Double.valueOf ( locationString [ 1 ].substring ( 0,
								locationString [ 1 ].length ( ) - 1 ) );
						
							if(json_array.getJSONObject(i).has("annotations")){
							if(json_array.getJSONObject(i).getJSONArray("annotations").getJSONObject(0).has("place")){	
								pic_sum ++;
								List<String> id_pic= new ArrayList<String>();
								String id =String.valueOf(lon);
								String pic_url =String.valueOf(lat);
								id_pic.add(id);
								id_pic.add(pic_url);
								if(json_array.getJSONObject(i).getJSONArray("annotations").getJSONObject(0).getJSONObject("place").has("title")){
									String poiname = json_array.getJSONObject(i).getJSONArray("annotations").getJSONObject(0).getJSONObject("place").getString("title");
									id_pic.add(poiname);
									
								}
								String text = json_array.getJSONObject(i).getString("text");
								id_pic.add(text);
								id_pic_list.add(id_pic);	
							}
							}
							
					}
				}	
			}
		}
		return id_pic_list;
		//return data_array;
	}
	
	
	public static void main(String args[]) throws Exception{
		//��ȡǩ�������е�id��ͼƬurl���ݡ�
		
		String write_path = "";
		File 					write_file				=		null ;
		FileWriter 			write_fw					=		null ;
		String checkfileName="E:\\data";	//��ȡ�ƶ��ļ����µ������人��.json �ļ�
		File file1 = new File(checkfileName);
		List<List<String>> pic_list = readFilePic(file1);
		System.out.println(pic_sum);
		for(int i =0 ;i<pic_list.size();i++){
			String id = pic_list.get(i).get(0);
			String pic_url = pic_list.get(i).get(1);
			String poiname = pic_list.get(i).get(2);
			String text = pic_list.get(i).get(3);
			write_path 					=  	"E:\\data" ;		//�洢·��
			write_file 					= 		new File ( write_path );
			if ( !write_file.isFile ( ) ) write_file.mkdirs  ( );
			//write_fw 				= 		new FileWriter(write_path + "/" + "id_picurl.txt",true);
			write_fw 				= 		new FileWriter(write_path + "/" + "��������.txt",true);
			write_fw.write(id + " " +  pic_url +" "+poiname+" "+text + "\r\n");
			write_fw.close();
			write_fw = null;
		}
		
	}

}

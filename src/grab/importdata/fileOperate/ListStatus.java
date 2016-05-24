/*package grab.importdata.fileOperate;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.FileUtil;
import org.apache.hadoop.fs.Path;

public class ListStatus {

	*//**
	 * 锟斤拷取HDFS某路锟斤拷锟斤拷目录
	 * @param args
	 * @return
	 * @throws IOException
	 *//*
	public static Path[] getHDFSFileList(String[] args) throws IOException{
		String uri = args[0];
		Configuration conf = new Configuration();
		FileSystem fs = FileSystem.get(URI.create(uri),conf);
		Path[] paths = new Path[args.length];
		for(int i =0;i<args.length;i++){
			paths[i] = new Path(args[i]);
		}
		FileStatus[] status = fs.listStatus(paths);
		Path[] listPath = FileUtil.stat2Paths(status);
		for(Path a : listPath){
			System.out.println(a);
		}
		return listPath;
	}
	
	
	
	public static void main(String[] args) throws Exception{
//		List<Path> pathList = new ArrayList<Path>();
//		getAllHDFSFileList("hdfs://192.168.1.177:9000/input/sinaData/2014-07-01",pathList);
//		for(Path a : pathList)System.out.println(a);
		
//		String area = Read.readJson("C:\\Users\\licong\\Desktop\\sina\\out\\getServicesSuportArea.json");
//		JSONArray areaArray = new JSONArray(area);
//		int m=0;
//		for(int i =0;i<areaArray.length();i++){
//			JSONObject province = areaArray.getJSONObject(i);
//			JSONArray city = province.getJSONArray("c");
//			m=m+city.length();
//		}
//		System.out.println(m);
//		
	}
}
*/
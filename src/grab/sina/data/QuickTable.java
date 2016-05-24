package grab.sina.data;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;


/**
 * 
 * 快表技术,为城市归类制作的快表（采用最大循环队列的结构）
 * 
 * @author Lee
 *
 */

class Item
{
	
	public Item ( String pro, String city, double lat, double lon )
	{
		s_pro		= 		pro ;
		
		s_city			=		city ; 
		
		s_lat			=		lat ;
		
		s_lon			=		lon ;
	}
	
	public 		String		s_pro		= 		"" ;
	
	public 		String		s_city			=		"" ; 
	
	public 		double		s_lat			=		0 ;
	
	public 		double		s_lon			=		0 ;
}

public class QuickTable
{
	
	static		private 		int			max_length		=		300 ;
	
	static		private		int			rear					=		-1 ;
	
	static		private		int			nItem				=		0 ;
	
	static		private		Item[]		quick_table		=		new Item[max_length] ;
	
	static		private		double		geo_error			=		0.00025 ;	
	
	
	
	/**
	 * 
	 * 添加快表记录，快表被填满后，下一个添加的元素将最老的数据替换。
	 * 
	 * @param object
	 * 
	 */
	static public 	void 	put( String pro, String city, double lat, double lon )
	{
		 if(rear == max_length-1)
	          rear = -1; 
		 quick_table[++rear] = new Item(pro,city,lat,lon) ;
		 
		 if(nItem < max_length)
			 nItem++;
	}
	
	/**
	 * 
	 * 搜索整个快表，根据坐标匹配误差范围内的城市名
	 * 
	 * @return
	 * 
	 */
	static public	String[] search( double lat, double lon )
	{
		int 	cur 	= 		0 ;
		while( cur< nItem - 1 )
		{
			if( finded( quick_table[cur].s_lat, quick_table[cur].s_lon, lat, lon ) )
			{
				String[] pro_city 		= 		new String[2];
				pro_city[0]		=		quick_table[cur].s_pro ;
				pro_city[1]		=		quick_table[cur].s_city ;
				return pro_city ;
			}
			cur++ ;	
		}
		return null;	
	}
	
	/**
	 * 
	 * 判断数据是否在误差允许范围内
	 * 
	 * @param lat
	 * 
	 * @param lon
	 * 
	 * @param t_lat
	 * 
	 * @param t_lon
	 * 
	 * @return
	 * 
	 */
	static private boolean finded(double lat, double lon, double t_lat, double t_lon)
	{
		if( Math.abs( lat - t_lat ) <= geo_error && Math.abs( lat - t_lat ) <= geo_error )
			return true ;
		else
			return false ;
	}
	
	static public int getLength()
	{
		return nItem ;
	}
	
	
	
////////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * 测试快表效率的函数，为了改进geo_error和max_length这两个参数而设计
	 * @throws IOException 
	 */
	static public void test_Performance ( String bz_path, String cs_path )
			throws IOException
	{
		File bz_file = new File ( bz_path );
		File cs_file = new File ( cs_path );
		File [ ] bz_pro_files = bz_file.listFiles ( );
		File [ ] cs_pro_files = cs_file.listFiles ( );
		int sum = 0;
		int error = 0;
		for ( int i = 0; i < bz_pro_files.length; i++ )
		{
			error += test_error_tree( bz_pro_files [ i ], cs_pro_files[i] );
			sum += test_sum_tree(bz_pro_files [ i ]);
		}
		
		System.out.println(error+":"+sum);
		
		MathContext mc = new MathContext(5, RoundingMode.HALF_DOWN);
        BigDecimal a=new BigDecimal(error);
        BigDecimal b=new BigDecimal(sum);
        System.out.println(a.divide(b,mc));   
	}
	
	/*
	 * 通过递归得到某一路径下所有的目录及其文件
	 */
	private static int test_error_tree ( File f1, File f2) throws IOException
	{
		int sum_error = 0;
		File [ ] t1 = f1.listFiles ( );
		File [ ] t2 = f2.listFiles ( );
		for ( int i = 0; i < t1.length; i++ )
		{
			int ll = test_numLine ( t1 [ i ] ) - test_numLine(t2[i]);
			sum_error += Math.abs ( ll );
			System.out.println(ll);
		}
		return sum_error ;
	}

	private static int test_sum_tree ( File f1) throws IOException
	{
		int sum_error = 0;
		File [ ] t1 = f1.listFiles ( );
		for ( int i = 0; i < t1.length; i++ )
		{
			sum_error += test_numLine ( t1 [ i ] ) ;
		}
		return sum_error ;
	}

	/*
	 * 计算文件行数
	 */
	private static int test_numLine ( File f ) throws IOException
	{
		int count = 0;
		InputStream input = new FileInputStream ( f );
		BufferedReader b = new BufferedReader ( new InputStreamReader ( input ) );
		String value = b.readLine ( );
		if ( value != null )
			while ( value != null )
			{
				count++;
				value = b.readLine ( );
			}
		b.close ( );
		input.close ( );
		return count;
	}
	 
	static public void main(String[] args) throws IOException
	{
		test_Performance("D://2//60796_bz","D://2//60796_qt_0.0005_40") ;
	}
}

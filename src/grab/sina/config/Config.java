
package grab.sina.config;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;


public class Config
{

	public static void main(String[] args)
	{
        String filepath = "/home/biggrab/config/config.properties";
        System.out.println(filepath);
         
        Config config = new Config(filepath);
         
        config.loadConfig();
        config.setPropertie("test2", "You are ready");
        config.saveConfig();
	}
	
	private String		path;
	private Properties	config;

	public Config ( String path )
	{
		this.path = path;
	}

	public boolean loadConfig ( )
	{
		config = new Properties ( );
		FileInputStream fis = null;
		boolean result = false;
		try
		{
			fis = new FileInputStream ( path );
			config.load ( fis );
			result = true;
		}
		catch ( Exception e )
		{
			e.printStackTrace ( );
		}
		finally
		{
			if ( fis != null )
			{
				try
				{
					fis.close ( );
				}
				catch ( Exception e )
				{
					e.printStackTrace ( );
				}
			}
		}
		return result;
	}

	public void saveConfig ( )
	{
		FileOutputStream fos = null;
		try
		{
			fos = new FileOutputStream ( path );
			config.store ( fos, "" );
		}
		catch ( Exception e )
		{
			e.printStackTrace ( );
		}
		finally
		{
			if ( fos != null )
			{
				try
				{
					fos.close ( );
				}
				catch ( Exception e )
				{
					e.printStackTrace ( );
				}
			}
		}
	}

	public void setPropertie ( String key, String value )
	{
		config.setProperty ( key, value );
	}

	public String getProperty ( String key )
	{
		return config.getProperty ( key );
	}

	public String getPath ( )
	{
		return path;
	}

	public void setPath ( String path )
	{
		this.path = path;
	}
	
}

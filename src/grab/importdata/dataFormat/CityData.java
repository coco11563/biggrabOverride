package grab.importdata.dataFormat;

public class CityData {
	private String cityNum;
	private int count;
	
	public CityData(String cityNum,int count){
		this.cityNum=cityNum;
		this.count=count;
	}
	
	public String getCityNum() {
		return cityNum;
	}
	public void setCityNum(String cityNum) {
		this.cityNum = cityNum;
	}
	public int getCount() {
		return count;
	}
	public void setCount(int count) {
		this.count = count;
	}
}

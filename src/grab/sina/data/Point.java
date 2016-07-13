package grab.sina.data;
/**
 * 
 * @author Shaow
 * 目的是创建一个存储单次抓取的链表
 * @param lat 存储此次抓取的纬度
 * @param lng 存储此次抓取的经度
 * @param pointType 存储抓取点的类型（是否上次抓到）
 * @param numbers 存储这个点上累计获取的数量
 * @param lastgrab 存储这个点上本次抓取的数量（或者说是上次）
 * 
 */
public class Point {
				private int num = 0;
				private int lastgrab = 0;
				private int pointType = 0;
				private double lat ;
				private double lng ;
				/**
				 * 
				 * @param lat
				 * @param lng
				 * @param pointType
				 * @param lastgrab
				 * @param num
				 */
				public void set(double lat,double lng , int pointType , int lastgrab , int num)
				{
					this.lastgrab = lastgrab;
					this.lat = lat;
					this.lng = lng;
					this.num = num;
					this.pointType = pointType;
				}
				public int getPointType() {
					return pointType;
				}
				public void setPointType(int pointType) {
					this.pointType = pointType;
				}
				public double getLat() {
					return lat;
				}
				public void setLat(double lat) {
					this.lat = lat;
				}
				public double getLng() {
					return lng;
				}
				public void setLng(double lng) {
					this.lng = lng;
				}
				public int getNum() {
					return num;
				}
				public void setNum(int num) {
					this.num = num;
				}
				public int getLastgrab() {
					return lastgrab;
				}
				public void setLastgrab(int lastgrab) {
					this.lastgrab = lastgrab;
				}
				
				
}

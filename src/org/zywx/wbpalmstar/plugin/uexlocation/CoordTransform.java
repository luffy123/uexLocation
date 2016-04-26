/*
 * Copyright (c) 2015.  The AppCan Open Source Project.
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *
 */
package org.zywx.wbpalmstar.plugin.uexlocation;

public class CoordTransform {
    private static final double xPi = 3.14159265358979324 * 3000.0 / 180.0;
    private static final double a = 6378245.0;
    private static final double ee = 0.00669342162296594323;

    private static boolean outOfChina(double lon, double lat) {
        if (lon < 72.004 || lon > 137.8347) {
            return true;
        }
        if (lat < 0.8293 || lat > 55.8271) {
            return true;
        }
        return false;
    }
    private static double transformLat(double x, double y) {
        double ret = -100.0 + 2.0 * x + 3.0 * y + 0.2 * y * y + 0.1 * x * y + 0.2 * Math.sqrt(Math.abs(x));
        ret += (20.0 * Math.sin(6.0 * x * Math.PI) + 20.0 * Math.sin(2.0 * x * Math.PI)) * 2.0 / 3.0;
        ret += (20.0 * Math.sin(y * Math.PI) + 40.0 * Math.sin(y / 3.0 * Math.PI)) * 2.0 / 3.0;
        ret += (160.0 * Math.sin(y / 12.0 * Math.PI) + 320 * Math.sin(y * Math.PI / 30.0)) * 2.0 / 3.0;
        return ret;
    }
    
    private static double transformLon(double x, double y) {
        double ret = 300.0 + x + 2.0 * y + 0.1 * x * x + 0.1 * x * y + 0.1 *  Math.sqrt( Math.abs(x));
        ret += (20.0 *  Math.sin(6.0 * x * Math.PI) + 20.0 *  Math.sin(2.0 * x * Math.PI)) * 2.0 / 3.0;
        ret += (20.0 *  Math.sin(x * Math.PI) + 40.0 *  Math.sin(x / 3.0 * Math.PI)) * 2.0 / 3.0;
        ret += (150.0 *  Math.sin(x / 12.0 * Math.PI) + 300.0 *  Math.sin(x / 30.0 * Math.PI)) * 2.0 / 3.0;
        return ret;
    }

    /**
     * 地球坐标 转 火星坐标
     * @param wgLat
     * @param wgLon
     * @return
     */
    public static double[] WGS84ToGCJ02( double wgLon, double wgLat) {
        double [] result = new double[2];
        if (outOfChina(wgLon, wgLat)) {
            result[0] = wgLon;
            result[1] = wgLat;
            return result;
        }
        double dLat = transformLat(wgLon - 105.0, wgLat - 35.0);
        double dLon = transformLon(wgLon - 105.0, wgLat - 35.0);
        double radLat = wgLat / 180.0 * Math.PI;
        double magic = Math.sin(radLat);
        magic = 1 - ee * magic * magic;
        double sqrtMagic = Math.sqrt(magic);
        dLat = (dLat * 180.0) / ((a * (1 - ee)) / (magic * sqrtMagic) * Math.PI);
        dLon = (dLon * 180.0) / (a / sqrtMagic * Math.cos(radLat) * Math.PI);
        result[0] =  wgLon + dLon;
        result[1] = wgLat + dLat;
        return result;
    }
    /**
     * 火星坐标 转 地球坐标
     * @param wgLon
     * @param wgLat
     * @return
     */
    public static double[] GCJ02ToWGS84(double wgLon, double wgLat) {
        double [] result = new double[2];
        if (outOfChina(wgLon, wgLat)) {
            result[0] = wgLon;
            result[1] = wgLat;
            return result;
        }
        double dLat = transformLat(wgLon - 105.0, wgLat - 35.0);
        double dLon = transformLon(wgLon - 105.0, wgLat - 35.0);
        double radLat = wgLat / 180.0 * Math.PI;
        double magic = Math.sin(radLat);
        magic = 1 - ee * magic * magic;
        double sqrtMagic = Math.sqrt(magic);
        dLat = (dLat * 180.0) / ((a * (1 - ee)) / (magic * sqrtMagic) * Math.PI);
        dLon = (dLon * 180.0) / (a / sqrtMagic * Math.cos(radLat) * Math.PI);
        result[0] = wgLon * 2 - (wgLon + dLon);
        result[1] = wgLat * 2 - (wgLat + dLat);
        return result;
    }

    /**
     * 火星坐标转百度坐标
     * @param lng
     *          经度
     * @param lat
     *          纬度
     * @return
     */
    public static double []  GCJ02ToBD09(double lng, double lat) {
        double z = Math.sqrt(lng * lng + lat * lat) + 0.00002 * Math.sin(lat * xPi);
        double theta = Math.atan2(lat, lng) + 0.000003 * Math.cos(lng * xPi);
        double [] result = new double[2];
        result[0] = z * Math.cos(theta) + 0.0065;
        result[1] = z * Math.sin(theta) + 0.006;
        return result;
    }

    /**
     * 百度 转 火星
     * @param lng
     * @param lat
     * @return
     */
    public static double [] BD09ToGCJ02(double lng, double lat) {
        double x = lng - 0.0065;
        double y = lat - 0.006;
        double z = Math.sqrt(x * x + y * y) - 0.00002 * Math.sin(y * xPi);
        double theta = Math.atan2(y, x) - 0.000003 * Math.cos(x * xPi);
        double [] result = new double[2];
        result[0] = z * Math.cos(theta);
        result[1] =  z * Math.sin(theta);
        return result;
    }

    /**
     * 地球 转 百度
     * @param lng
     * @param lat
     * @return
     */
    public static double [] WGS84ToBD09(double lng, double lat) {
        double GCJ02 [] = WGS84ToGCJ02(lng, lat);
        return GCJ02ToBD09(GCJ02[0], GCJ02[1]);
    }

    /**
     * 百度 转 地球
     * @param lng
     * @param lat
     * @return
     */
    public static double [] BD09ToWGS84(double lng, double lat) {
        double GCJ02 [] = BD09ToGCJ02(lng, lat);
        return GCJ02ToWGS84(GCJ02[0], GCJ02[1]);
    }
}

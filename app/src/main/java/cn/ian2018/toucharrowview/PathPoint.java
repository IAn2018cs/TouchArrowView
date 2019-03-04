package cn.ian2018.toucharrowview;

import android.graphics.Path;
import android.graphics.PointF;

import java.util.List;

/**
 * Description:
 * Author:chenshuai
 * E-mail:chenshuai@amberweather.com
 * Date:2019/3/4
 */
public class PathPoint {
    public Path path;
    public List<PointF> points;

    public PathPoint(Path path, List<PointF> points) {
        this.path = path;
        this.points = points;
    }
}

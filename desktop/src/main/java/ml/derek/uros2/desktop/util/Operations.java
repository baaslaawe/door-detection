package ml.derek.uros2.desktop.util;

import ml.derek.uros2.desktop.Detection;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.*;

public class Operations
{
    public static int POINT_MERGE_MAX_DISTANCE = 100;


    public static List<Point> mergePoints(Mat unmerged)
    {
        return mergePoints(Convert.pointList(unmerged));
    }

    public static void mergeAllPoints(List<Point> unmerged)
    {
        for(int x = 0; x < 1000; x++)
        {
            unmerged = mergePoints(unmerged);
        }
    }

    public static List<Point> mergePoints(List<Point> unmerged)
    {
        List<Point> merged = new ArrayList<>();

        for(Point p1 : unmerged)
        {
            Point closest = null;
            for (Point p2 : unmerged)
            {
                if (!p1.equals(p2))
                {
                   if(closest == null)
                       closest = p2;
                    else
                   {
                       if(p1.dot(p2) < p1.dot(closest))
                           closest = p2;
                   }
                }
            }

            if(p1.dot(closest) <  POINT_MERGE_MAX_DISTANCE)
            {
                merged.add(merge(p1, closest));
            }
            else
                merged.add(p1);
        }

        return merged;
    }

    public static List<MatOfPoint> trim(List<MatOfPoint> untrimmed, int sides)
    {
        List<MatOfPoint> trimmed = new ArrayList<>();
        for(MatOfPoint poly : untrimmed)
        {
            if(poly.height() == sides)
                trimmed.add(poly);
        }

        return trimmed;
    }

    public static List<Line> joinPoints(Point[] points)
    {
        double range = 4;
        List<Line> lines = new ArrayList<>();
        for(Point p1 : points)
            for(Point p2 : points)
            {
                if(!p1.equals(p2))
                {
                    Line l = new Line(p1, p2);
                    boolean cancel = false;

                    if(l.angle() > 90 - range && l.angle() < 90 + range)
                        lines.add(l);
                    else if(l.angle() > 180 - range && l.angle() < 180 + range)
                        lines.add(l);
                }
            }



        return lines;
    }

    public static List<Point> cluster(List<Point> points)
    {
        return null;
    }

    public static void writeStringToFile(String string, String filename)
    {
        try
        {
            PrintWriter out = new PrintWriter(filename);
            out.write(string);
            out.close();
        } catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }


    }

    public static MatOfFloat range(int start, int stop)
    {
        float[] range = new float[stop];
        for(int i = start, p = 0; i < stop; i++, p++)
        {
            range[p] = i;
        }

        return new MatOfFloat(range);
    }


    public static List<Line> trim(List<Line> unfiltered, List<Point> points, double radius)
    {
        // We need to get 4 lines that represent the door
        // We will get the two longest vertical lines and
        // the two longest horizontal lines
        Collections.sort(unfiltered, new Comparator<Line>()
        {
            @Override
            public int compare(Line l1, Line l2)
            {
                return l1.length() > l2.length() ? -1 : 1;
            }
        });

        TreeMap<Integer, Line> intersections = new TreeMap<>();
        for(Line line : unfiltered)
        {
            int count = 0;
            for(Point point : points)
            {
                if(!(new Rect(line.p1, line.p2).contains(point)))
                    continue;

                if(line.distance(point) < radius)
                    count++;
            }

            intersections.put(count, line);
        }

        List<Line> lines = new ArrayList<>();
        int count = 0;
        for(Line line : intersections.values())
        {
            if (count < 2)
            {
                lines.add(line);
                count++;
            } else
            {
                if(count >= 4)
                    return lines;

                if(!line.isVertical())
                {
                    lines.add(line);
                    count++;
                }
            }
        }

        return lines;
    }


    public static Point merge(Point p1, Point p2)
    {
        return new Point((p1.x + p2.x) / 2, (p1.y + p2.y) / 2);
    }
}

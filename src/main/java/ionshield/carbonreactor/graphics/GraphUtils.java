package ionshield.carbonreactor.graphics;

import java.awt.*;
import java.math.BigDecimal;
import java.util.ArrayList;

public abstract class GraphUtils {
    
    public static void drawLine(Line l, Graphics g, Color lineColor) {
        Line line = l;//new Line(getScale() * (int)Math.round(getWidth() * (l.x1 / 100.0) / getScale()), (int) Math.round(getHeight() * (1 - (l.y1 / 100.0)) / getScale()), getScale() * (int)Math.round(getWidth() * (l.x2 / 100.0) / getScale()), getScale() * (int)Math.round(getHeight() * (1 - (l.y2 / 100.0)) / getScale()));
        
        int dx = line.x2 - line.x1;
        int dy = line.y2 - line.y1;
        boolean inv = Math.abs(dy) >= Math.abs(dx);
        
        getPoints(dx, dy, true).forEach(p -> {
            float a = (float)Math.max(Math.min(p.a, 1.0), 0.0);
            //System.out.println("X:" + p.x + " Y:" + p.y + " A:" + a);
            //if (!altAlpha) {
                g.setColor(new Color(lineColor.getRed(), lineColor.getGreen(), lineColor.getBlue(), (int)Math.min(255, Math.max(0, Math.round(Math.pow(a, 1 / 1.6) * 255)))));
            //}
            //else {
                //g.setColor(new Color((int) Math.floor(lineColor.getRed() * a), (int) Math.floor(lineColor.getGreen() * a), (int) Math.floor(lineColor.getBlue() * a)));
            //}
            if (inv) {
                g.fillRect(line.x1 + p.y * (int)Math.signum(dx), line.y1 + p.x * (int)Math.signum(dy), 1, 1);
            }
            else {
                g.fillRect(line.x1 + p.x * (int)Math.signum(dx), line.y1 + p.y * (int)Math.signum(dy), 1, 1);
            }
        });
    }
    
    private static java.util.List<Point> getPoints(int dx, int dy, boolean aa) {
        dx = Math.abs(dx);
        dy = Math.abs(dy);
        if (dy > dx) {
            return getPoints(dy, dx, aa);
        }
        java.util.List<Point> list = new ArrayList<>(dx);
        list.add(new Point(0, 0));
        
        int x = 0;
        int y = 0;
        int d = 2*dy - dx;
        
        for (x = 1; x <= dx; x++) {
            if (d >= 0) {
                if (aa) {
                    //System.out.println("X:" + x + " Y:" + y + " D:" + d + " DY/DX:" + (double)dy/dx + " A1:" + (((double)dy/dx) * x - y) + " A2:" + ((y + 1) - ((double)dy/dx) * x));
                    list.add(new Point(x, y + 1, ((double)dy/dx) * x - y));
                    list.add(new Point(x, y, (y + 1) - ((double)dy/dx) * x));
                }
                else {
                    list.add(new Point(x, y + 1));
                }
                y++;
                d += 2*(dy - dx);
            }
            else {
                if (aa) {
                    //System.out.println("X:" + x + " Y:" + y + " D:" + d + " DY/DX:" + (double)dy/dx + " A1:" + (((double)dy/dx) * x - y) + " A2:" + ((y + 1) - ((double)dy/dx) * x));
                    list.add(new Point(x, y + 1, ((double)dy/dx) * x - y));
                    list.add(new Point(x, y, (y + 1) - ((double)dy/dx) * x));
                }
                else {
                    list.add(new Point(x, y));
                }
                d += 2*dy;
            }
            //list.add(new Point(x, y));
        }
        
        return list;
    }
    
    public static String roundDouble(double d, int precision, int maxLength, boolean truncate) {
        if (d == 0) return "0";
        if (Double.isNaN(d)) return "NaN";
        if (Double.isInfinite(d)) return (d < 0 ? "-" : "") + "Infinity".substring(0, Math.min(maxLength, 8));
        String val = BigDecimal.valueOf(d).setScale(precision, BigDecimal.ROUND_HALF_UP).toString();
        String[] parts = val.split("\\.");
        if (parts.length < 2) {
            parts = new String[] {parts[0], "0"};
        }
        int exponent = 0;
        while (parts[0].length() > (maxLength + (d > 0 ? 0 : 1))) {
            String v = parts[0].substring(parts[0].length() - 1);
            parts[0] = parts[0].substring(0, parts[0].length() - 1);
            parts[1] = v + parts[1];
            exponent++;
        }
        if (parts[1].length() > precision) {
            boolean valueOverZero = false;
            for (int i = precision; i < parts[1].length(); i++) {
                if (parts[1].charAt(i) != '0') {
                    valueOverZero = true;
                    break;
                }
            }
            parts[1] = String.valueOf(Integer.parseInt(parts[1].substring(0, precision)) + (valueOverZero ? 1 : 0));
        }
        if (parts[1].length() > 0 && truncate) {
            int i = parts[1].length() - 1;
            while (i >= 0 && parts[1].charAt(i) == '0') {
                i--;
            }
            parts[1] = parts[1].substring(0, i + 1);
        }
        val = parts[0] + (parts[1].length() > 0 ? "." + parts[1] : "") + (exponent != 0 ? "E" + exponent : "");
        return val;
    }
    
    public static class Line {
        
        public int x1;
        public int y1;
        public int x2;
        public int y2;
        
        public Line() {}
        
        public Line(int x1, int y1, int x2, int y2) {
        
            this.x1 = x1;
            this.y1 = y1;
            this.x2 = x2;
            this.y2 = y2;
        }
    }
    
    public static class Point {
        public int x;
        public int y;
        public double a = 1;
        
        public Point() {}
        public Point(int x, int y) {
        
            this.x = x;
            this.y = y;
        }
        public Point(int x, int y, double a) {
            
            this.x = x;
            this.y = y;
            this.a = a;
        }
    }
}

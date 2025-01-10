/*
CFT - an interactive programmable shell for automation 
Copyright (C) 2020-2025 Roar Foshaug

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, version 3 of the License.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <https://www.gnu.org/licenses/>
*/

package rf.configtool.main.runtime.lib.dd;

// chatgpt.com

public class LineSegmentIntersection {

    // Method to check if two line segments intersect
    public static boolean doSegmentsIntersect(double ax1, double ay1, double ax2, double ay2,
                                              double cx1, double cy1, double cx2, double cy2) {
        // Find the four orientations
        int o1 = orientation(ax1, ay1, ax2, ay2, cx1, cy1);
        int o2 = orientation(ax1, ay1, ax2, ay2, cx2, cy2);
        int o3 = orientation(cx1, cy1, cx2, cy2, ax1, ay1);
        int o4 = orientation(cx1, cy1, cx2, cy2, ax2, ay2);

        // General case: Lines intersect if the orientations differ
        if (o1 != o2 && o3 != o4) {
            return true;
        }

        // Special cases: Check collinear points
        if (o1 == 0 && onSegment(ax1, ay1, cx1, cy1, ax2, ay2)) return true;
        if (o2 == 0 && onSegment(ax1, ay1, cx2, cy2, ax2, ay2)) return true;
        if (o3 == 0 && onSegment(cx1, cy1, ax1, ay1, cx2, cy2)) return true;
        if (o4 == 0 && onSegment(cx1, cy1, ax2, ay2, cx2, cy2)) return true;

        // Otherwise, the segments do not intersect
        return false;
    }

    // Helper function to find the orientation of the ordered triplet (p, q, r)
    // 0 -> collinear, 1 -> clockwise, 2 -> counterclockwise
    private static int orientation(double px, double py, double qx, double qy, double rx, double ry) {
        double val = (qy - py) * (rx - qx) - (qx - px) * (ry - qy);
        if (Math.abs(val) < 1e-9) return 0; // collinear
        return (val > 0) ? 1 : 2; // clockwise or counterclockwise
    }

    // Helper function to check if point (px, py) lies on the segment (q1x, q1y) to (q2x, q2y)
    private static boolean onSegment(double q1x, double q1y, double px, double py, double q2x, double q2y) {
        return px >= Math.min(q1x, q2x) && px <= Math.max(q1x, q2x) &&
               py >= Math.min(q1y, q2y) && py <= Math.max(q1y, q2y);
    }

    public void main(String[] args) {
        // Example: Line segment 1 (A to B) and segment 2 (C to D)
        double ax1 = 1, ay1 = 1, ax2 = 4, ay2 = 4; // Segment AB
        double cx1 = 1, cy1 = 4, cx2 = 4, cy2 = 1; // Segment CD

        if (doSegmentsIntersect(ax1, ay1, ax2, ay2, cx1, cy1, cx2, cy2)) {
            System.out.println("The line segments intersect.");
        } else {
            System.out.println("The line segments do not intersect.");
        }
    }
}

/*

How the Code Works:
Orientation:

The orientation method calculates the relative orientation of three points 
(
𝑃
,
𝑄
,
𝑅
)
(P,Q,R).
0
0: Collinear points.
1
1: Clockwise.
2
2: Counterclockwise.
General Case:

Two line segments 
𝐴
𝐵
AB and 
𝐶
𝐷
CD intersect if the orientation of 
𝐶
C and 
𝐷
D relative to 
𝐴
𝐵
AB differ and vice versa for 
𝐴
𝐵
AB relative to 
𝐶
𝐷
CD.
Special Cases:

Handles scenarios where points are collinear, ensuring intersection occurs within segment bounds using the onSegment method.
Efficient Checks:

The function returns true as soon as an intersection is detected, minimizing unnecessary computation.
Example:
Input:

Line segment 
𝐴
𝐵
AB: 
(
1
,
1
)
(1,1) to 
(
4
,
4
)
(4,4)
Line segment 
𝐶
𝐷
CD: 
(
1
,
4
)
(1,4) to 
(
4
,
1
)
(4,1)
Output:

arduino
Copy code
The line segments intersect.
This code ensures intersections are calculated strictly within the finite segments 
𝐴
𝐵
AB and 
𝐶
𝐷
CD. Let me know if you need further explanations or enhancements!



 * 
 */

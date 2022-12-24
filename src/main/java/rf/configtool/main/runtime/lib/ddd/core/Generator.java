/*
CFT - an interactive programmable shell for automation 
Copyright (C) 2020-2023 Roar Foshaug

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

package rf.configtool.main.runtime.lib.ddd.core;


/**
* This is a utility class containing commonly needed features for classes
* that generate shapes and forms. It is well suited for subclassing,
* making the methods directly available. 
*/

public class Generator {
    
//  public static final int NO_DETAIL=0;
//  public static final int LOW_DETAIL=1;
//  public static final int MEDIUM_DETAIL=2;
//  public static final int FULL_DETAIL=3;
//  
//  /**
//  * This variable is set in the constructor to point at an object
//  * capable of handling the triangles that will be generated.
//  */
//  private TriangleReceiver triDest;
//  
//  /**
//  * This variable is set in the constructor and indicates a "degradataion-
//  * factor" (if larger than 1) to the detail level of objects that use distance-from-eye
//  * to adjust detail level. Setting qualityDistanceFactor to 0.0 means that all such
//  * objects will be rendered in full detail.
//  */
//  private double qualityDistanceFactor=1.0;
//  
//  /** 
//  * Constructor. The TriangleReceiver parameter indicated where to send generated
//  * triangles. The qualityDistanceFactor is a factor that is multiplied with
//  * calculated distance when using distance to decide the quality of objects, for
//  * example number of segments in a circle, detail of brushes etc. Larger value
//  * gives lower quality - and faster updates.
//  */
//  public Generator (TriangleReceiver triDest, double qualityDistanceFactor) {
//      this.triDest=triDest;
//      this.qualityDistanceFactor=qualityDistanceFactor;
//  }
//
//  /**
//  * Calls update() on the triangle-receiver, indicating that it should repaint part
//  * of the image
//  */
//  public void update() {
//      triDest.update();
//  }
//  
//  /**
//  * Get the TriangleReceiver object
//  */
//  public TriangleReceiver getTriangleReceiver () {
//      return triDest;
//  }
//  
//  /**
//  * Deliver a triangle to the TriangleReceiver, simply by calling the tri() method
//  * on it. Convenience method only.
//  */
//  public void tri (Triangle t) {
//      triDest.tri(t);
//  }
//
//  /**
//  * This method calculates a recommended number of segments for rotation 
//  * objects. The Ref object is the local system, the maxDistanceOffset 
//  * is used if the center of the rotation object may move, for example
//  * for cylinders.
//  */
//  protected int calcRotationSegmentCount (Ref ref, 
//      double maxDistanceOffset, double rotationRadius)
//  {
//      // Note that maxDistanceOffset and rotationRadius are both given in the local
//      // system identified by ref. Its units may differ from the global system, so 
//      // we must multiply by the length of the longest unit vector in the system to get
//      // a correct value.
//      double biggestAxisValue=ref.getUnitVectorMaxLength();
//      maxDistanceOffset *= biggestAxisValue;
//      rotationRadius *= biggestAxisValue;
//      
//      double nearestPossiblePart=
//          ref.getPos().length() - maxDistanceOffset - rotationRadius;
//      double distFactor=
//          nearestPossiblePart * qualityDistanceFactor / (rotationRadius * 2.0);
//      
//      // if (distFactor > 2000) return 4;
//      if (distFactor > 1000) return 6;
//      if (distFactor > 500) return 12;
//      if (distFactor > 100) return 18;
//      
//      if (distFactor > 50) return 36;
//      if (distFactor > 25) return 72;
//      if (distFactor > 5) return 120;
//      
//      return 180;
//  }
//  
//  /* Helper method just as calcRotationSegment count - may be used to decide the
//  * level of brush detail level. Returns NO_DETAIL, LOW_DETAIL, MEDIUM_DETAIL or
//  * FULL_DETAIL as defined in this class. The maxDistanceOffset should give the
//  * maximum offset within the local system, measured from some center.
//  */
//  protected int calcDetailLevel (Ref ref, double maxDistanceOffset) {
//      double biggestAxisValue=ref.getUnitVectorMaxLength();
//      maxDistanceOffset *= biggestAxisValue;
//      double nearestPossiblePart=
//          ref.getPos().length() - maxDistanceOffset;
//      double distFactor=
//          nearestPossiblePart * qualityDistanceFactor / (maxDistanceOffset * 2);
//      // distFactor is now the relationship between nearest possible part of object and its
//      // maximum span (maxDistanceOffset times two, since it measures from the center only)
//      if (distFactor > 500) return NO_DETAIL;
//      if (distFactor > 250) return LOW_DETAIL;
//      if (distFactor > 100) return MEDIUM_DETAIL;
//      return FULL_DETAIL;     
//  }
//  
//  /**
//  * Performs the notVisible() call on the triangle receiver object. This method should
//  * be called for composite objects before processing starts, and if it returns false, 
//  * then that object should be aborted, as it will not be visible in the viewer anyway.
//  */
//  public boolean objectNotVisible (Bounds3d bounds) {
//      return triDest.notVisible(bounds);
//  }
//  
//  /**
//  * Create new brush object - supplies the triDest value so that triangles
//  * generated by the brush are given to the correct object for further processing.
//  */
//  public Brush createBrush() {
//      return new Brush(triDest);
//  }
//  
//  /**
//  * Creates an ordinary brush that must be moved forward (along the x axis) to
//  * produce a result. This is a circular brush that draws a tube when pushed
//  * straight ahead. May also draw the famaous torus if swept around a circle.
//  */
//  public Brush createCircularBrush (double radius, int segments, Color color) {
//      Brush b=new Brush(triDest);
//      b.setAttributes(color);
//      Vector3d vec=new Vector3d(0,0,radius);
//      b.addPoint(vec);
//
//      for (int i=1; i<=segments; i++) {
//          Vector3d rotVec=vec.rotateFractX( ((double) i) / ((double) segments) );
//          b.addSegment(rotVec);
//      }
//      return b;
//  }
//  
//  
//  /** This method is an example of a forward brush, that is, one that operates in
//  * the xz-plane rather than the yz-plane. This one creates a half-sphere, and is suitable
//  * for generating a sphere very easily: only rotate the current ref and apply penDown
//  * on the angles, and you have a ball.
//  */
//  public Brush createForwardHalfSphereBrush (double radius, int segments, Color color) {
//      Brush b=new Brush(triDest);
//      b.setAttributes(color);
//      Vector3d vec=new Vector3d(0,0,radius);
//      b.addPoint(vec);
//      
//      for (int i=1; i<=segments; i++) {
//              // i<=segments to generate "south-pole" point as well
//          double rotFract= ((double) i) / ((double) segments);
//          rotFract = rotFract / 2.0;      // since we only go half circle
//          Vector3d rotVec=vec.rotateFractY(rotFract);
//          b.addSegment(rotVec);
//      }
//      return b;
//
//  }
//      
//  /**
//  * Rotate forward brush through full circle, turning left. If, for example, the brush is
//  * a half-cicle, then a sphere will be created.
//  */
//  public void rotateForwardBrushLeft (Ref center, Brush b, int segments) {
//      for (int i=0; i<=segments; i++) {
//          b.penDown(center.turnLeftFraction(i,segments));
//      }
//  }
//  
//  public void rotateForwardBrushRight (Ref center, Brush b, int segments) {
//      for (int i=0; i<=segments; i++) {
//          b.penDown(center.turnRightFraction(i,segments));
//      }
//  }
//  /**
//  * Creates a sphere with the given radius out from center
//  */
//  public void sphere (Ref center, double radius, Color color) {
//      Bounds3d bounds=new Bounds3d(center, radius, radius, radius, radius, radius, radius);
//      if (triDest.notVisible(bounds)) return;
//      int segments=calcRotationSegmentCount(center, radius, radius) / 2 ; // half circle only
//      Brush b=createForwardHalfSphereBrush (radius, segments, color);
//      b.setVisibleTrianglesOnly(false);
//      // Skal egentlig sette denne til true, men f�r da et hull rundt "sydpolen" som jeg
//      // ikke skj�nner noe av. Inntil videre kan det bare st� slik - kan fikse det senere en
//      // gang. Problemet skyldes n�dvendigvis at trianglene som genereres p� den siste
//      // sirkelen f�r punktene angitt i feil rekkef�lge. Hvorfor?????
//      rotateForwardBrushLeft(center, b, segments);
//  }
//
//  /**
//  * Creates a torus centered at the given ref, with inner radius and thickness as
//  * indicated. The torus is created by creating a circular brush and rotating it to
//  * the left.
//  */
//  public void torus (Ref center, double innerRadius, double thickness, Color color) { 
//      double outerRadius=innerRadius+thickness;
//      Bounds3d bounds=new Bounds3d(center, outerRadius, outerRadius, thickness/2, thickness/2,
//              outerRadius, outerRadius);
//      if (triDest.notVisible(bounds)) return;
//      
//      int brushSegments=calcRotationSegmentCount(center, innerRadius+thickness, thickness/2);
//      int ringSegments=calcRotationSegmentCount(center, innerRadius+thickness, innerRadius+thickness);
//      
//      Brush b=createCircularBrush(thickness/2, brushSegments, color);
//      b.setVisibleTrianglesOnly(true);
//      for (int i=0; i<=(ringSegments+1); i++) {
//          Ref brushPos=center.turnLeftFraction( ((double) i) / ((double) ringSegments) );
//          brushPos=brushPos.forward(innerRadius + thickness/2).turnLeftDeg(90);
//          b.penDown(brushPos);
//      }
//  }
//  
//  /** Creates a panel that is visible from the upper side */
//  public void panel (Ref ref, double right, double forward, Color col) {
//      Brush b=new Brush(triDest);
//      b.setVisibleTrianglesOnly(true);
//      b.setAttributes(col);
//      b.addRUPoint(0,0);
//      b.addRUSegment(right,0);
//      b.penDown(ref);
//      b.penDown(ref.forward(forward));
//  }
//  
//
//  /**
//  * Generates a box of given color, starting at given ref, stretching right, up and
//  * forward as given. 
//  */
//  public void box (Ref ref, double right, double up, double forward, Color col) {
//      // side walls
//      Brush b=new Brush(triDest);
//      b.setAttributes(col);
//      b.addRUPoint(0,0);
//      b.addRUSegment(0,up);
//      b.addRUPoint(right, up);
//      b.addRUSegment(right,0);
//      
//      b.penDown(ref);
//      b.penDown(ref.forward(forward));
//      
//      // ends, top, bottom
//      b=new Brush(triDest);
//      b.setAttributes(col);
//      b.addRUPoint(0,0);
//      b.addRUSegment(right,0);
//      
//      b.penDown(ref);
//      b.penDown(ref.up(up));
//      b.penDown(ref.up(up).forward(forward));
//      b.penDown(ref.forward(forward));
//      b.penDown(ref);
//  }
//  
//  /**
//  * Creates a cylinder centered around the x-axis, moving forward along x-axis for the length.
//  * Returns ref moved forward the given length. There are two radius values, the one at
//  * the start and the one at the other end. In addition wall-thickness is given as a Double
//  * object, since it is optional. If null, the cylinder will only be the outside
//  * shell. For both variants we have that only planes facing the camera are visible, so
//  * if one wants an inside, a wallthickness must be specified. 
//  */
//  public Ref cylinder (Ref ref, double outerRadius1, double outerRadius2, Double wallThickness,
//      double length, Color col)
//  {
//      double maxRadius=Math.max(outerRadius1, outerRadius2);
//      Bounds3d bounds=new Bounds3d(ref,
//          maxRadius, maxRadius,  // left, right
//          maxRadius, maxRadius, // up, down
//          length,  // forward
//          0.0   // backward
//          ); 
//      if (triDest.notVisible(bounds)) {
//          return ref.forward(length);
//      }
//      
//      int rotationSegments=calcRotationSegmentCount (ref, length, maxRadius);
//      Brush b=new Brush(triDest);
//      b.setVisibleTrianglesOnly(true);
//      b.setAttributes(col);
//      if (wallThickness==null) {
//          Ref bref=new Ref();
//          b.addPoint(bref.forward(length).right(outerRadius2));
//          b.addSegment(bref.right(outerRadius1));
//      } else {
//          double innerRadius1=outerRadius1-wallThickness.doubleValue();
//          double innerRadius2=outerRadius2-wallThickness.doubleValue();
//          Ref bref=new Ref();  // local ref to create the brush
//          b.addPoint(bref.right(innerRadius1));
//          b.addSegment(bref.forward(length).right(innerRadius2));
//          b.addSegment(bref.forward(length).right(outerRadius2));
//          b.addSegment(bref.right(outerRadius1));
//          b.addSegment(bref.right(innerRadius1));
//      }
//
//      b.penDown(ref);
//      for (int i=0; i<=rotationSegments; i++) {
//          b.penDown(ref.rollRightFraction(i,rotationSegments));
//      }
//      
//      return (ref.forward(length));
//  }
//

}

package rf.configtool.main.runtime.lib.ddd.core;

/**
* This interface represents anyone that is capable of processing the triangles
* that are generated. 
*/
public interface TriangleReceiver {

	/**
	* A triangle has been generated
	*/
	public void tri (Triangle t) ;

	/**
	* This method returns true if the object whose bounds are given as parameter
	* will definitely not be displayed in the viewer (if TriangleReceiver is a viewer),
	* or otherwise should not be calculated as triangles.
	* <p>
	* Composite objects at all levels can call this method with their own bounds, to 
	* effectively prune the world tree fo systems and subsystems.
	*/
	public boolean notVisible (rf.configtool.main.runtime.lib.ddd.core.Bounds3d bounds) ;
	

}

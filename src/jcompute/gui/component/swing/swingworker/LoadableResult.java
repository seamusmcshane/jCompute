package jcompute.gui.component.swing.swingworker;

/**
 * This interface can be used is used by Loadable processors to return the result of the operations.
 * <p>
 * The functioning of load is up to the callee.
 * The indexes allow the callee to create a mapping of what is needed to be loaded and the order.
 * <p>
 * Callers agree not to modify the index values or the order and should copy the index array if needed and
 * that result array order matches the index array order given to {@link Loadable}
 * 
 * @see LoadableTask
 * @see Loadable
 */
public interface LoadableResult
{
	/**
	 * An integer constant representing a failed load
	 */
	public static final int FAILED_MARK = -1;
	
	/**
	 * An integer constant representing a successful load
	 */
	public static final int SUCCEEDED_MARK = 0;
	
	/**
	 * The pass and fail status of each index
	 * 
	 * @return
	 * A int array of pass or fail marks in the same order as the array given to {@link Loadable}
	 * @see {@link LoadableResult#SUCCEEDED_MARK}
	 * @see {@link LoadableResult#FAILED_MARK}
	 * @see {@link LoadableResult#getTotalSuccesses()}
	 * @see {@link LoadableResult#getTotalFails()}
	 */
	public int[] getResult();
	
	/**
	 * Total calls of Loadable.load() that succeeded
	 * 
	 * @return
	 * @see {@link #getResult()}
	 * @see {@link #getTotalFails()}
	 */
	public int getTotalSuccesses();
	
	/**
	 * Total calls of Loadable.load() that failed
	 * 
	 * @return
	 * @see {@link #getResult()}
	 * @see {@link #getTotalSuccesses()}
	 */
	public int getTotalFails();
}

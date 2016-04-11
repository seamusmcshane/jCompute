package jcompute.gui.component.swing.swingworker;

/**
 * This interface is used as a generic method of loading with out exposing the actual detail of the loading method<br>
 * The functioning of load is up to the callee.<br>
 * The indexes allow the callee to create a mapping of what is needed to be loaded and the order.<br>
 * Callers agree not to modify the index values or the order and should copy the index array if needed.
 * <p>
 * {@link Loadable}s can delegate to other {@link Loadable}s via {@link #makeDelegate()}
 * Note however a {@link Loadable} can refuse (temporarily if busy or permanently) to act as delegate.<br>
 * Once a a loader becomes a delegate it must perform all delegation duties until {@link releaseDelegate} is called.<br>
 * The requester should still manage the index with the delegate performing the load.
 *
 * @author Seamus McShane
 * @version 1
 * @see LoadableResult
 * @see LoadableTask
 */
public interface Loadable
{
	/**
	 * The load call back with index.
	 *
	 * @param index
	 * @return
	 * True if all indexes loaded successfully.
	 * False if one ore more indexes failed to load.
	 */
	public boolean load(int index);
	
	/**
	 * The call back with the indexes that are to be loaded.
	 *
	 * @return
	 */
	public int[] getIndexes();
	
	/**
	 * Make the Loadable a delegate of the requester.
	 * 
	 * @param requester
	 * the requesting loadable.
	 * @return
	 * True if the delegate agrees. <br>
	 * False if the delegate does not.
	 * @see {@link #delegateLoad()}
	 * @see {@link #releaseDelegate()}
	 */
	public boolean makeDelegate(Loadable requester);
	
	/**
	 * Initiate a delegateLoad operation.
	 *
	 * @param requester
	 * the requesting loadable.
	 * @param
	 * info
	 * the delegate needs to perform the load operation. Typically this will be a filename, path or object name.
	 * @return
	 * True if the delegateLoad was successful.
	 * False if the delegateLoad failed.
	 * @see {@link #makeDelegate()}
	 * @see {@link #releaseDelegate()}
	 */
	public boolean delegateLoad(Loadable requester, String info);
	
	/**
	 * Release the loadable from delegation duties for the requester.
	 * 
	 * @see {@link #makeDelegate()}
	 * @see {@link #delegateLoad()}
	 */
	public void releaseDelegate(Loadable requester);
}
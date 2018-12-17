package mydatax.common.writer;

import mydatax.common.line.LineReceiver;
import mydatax.common.reader.Reader;
import mydatax.util.DataExchangeException;

/**
 * A kind of {@link Pluginable} which dump data to data destination(e.g mysql, HDFS).
 * 
 * @see {@link Pluginable}
 * @see {@link Reader}
 * 
 * */
public abstract class Writer{
	
	/**
	 * Initialize {@link Writer} before the Writer work.
	 * 
	 * @return
	 *			0 for OK, others for failure.
	 * 
	 * @throws	{@link	DataExchangeException}
	 * 					Method init failed, rerun DataX may resolve this problem, e.g. connect to database interrupted.
	 *
	 * */
	public abstract int init();
	
	/**
	 * Connect to destination DB(e.g mysql, HDFS)
	 * 
	 * @return
	 *			0 for OK, others for failure.
	 * 
	 * @throws	{@link	DataExchangeException}
	 * 			Method connect failed, rerun DataX may resolve this problem, e.g. connect to database interrupted.
	 * */
	public abstract int connect();
	
	/**
	 * Start to dump data into data destination.
	 * 
	 * @param	resultHandler	
	 * 			handler used by {@link Writer} to dump data from DataX engine(Usually in memory).
	 * 
	 * @return
	 *			0 for OK, others for failure.
	 * 
	 * @throws	{@link	DataExchangeException}
	 * 			Method startWrite failed, rerun DataX may resolve this problem, e.g. connect to database interrupted.
	 *
	 * */
	public abstract int startWrite(LineReceiver receiver);
	
	/**
	 * Commit transaction. A complement of method startDump.
	 * 
	 * @return
	 *			0 for OK, others for failure.
	 * 
	 * @throws	{@link	DataExchangeException}
	 * 					Method commit failed, rerun DataX may resolve this problem, e.g. connect to database interrupted.
	 * */
	public abstract int commit();
	
	
	/**
	 * Do some finish work
	 * 
	 * @return
	 *			0 for OK, others for failure.
	 * 
	 * @throws	{@link	DataExchangeException}
	 * 					Method finish failed, rerun DataX may resolve this problem, e.g. connect to database interrupted.
	 * */
	public abstract int finish();
}

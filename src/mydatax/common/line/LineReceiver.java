package mydatax.common.line;

import mydatax.common.Storage;
import mydatax.common.reader.Reader;
import mydatax.common.writer.Writer;

/**
 * DataX use {@link Storage} to help {@link Reader} and {@link Writer} to exchange data,
 * {@link Writer} uses {@link LineReceiver} to get data from {@link Storage}(Usually in memory).
 *  
 * @see LineSender
 * @see BufferedLineExchanger
 * 
 * */
public interface LineReceiver {
	
	/**
	 * Fetch the next {@link Line} from {@link Storage}.
	 * 
	 * @return	{@link Line}
	 * 			next {@link Line} in {@link Storage}, null if read to end.
	 * 
	 * */
	public Line getFromReader();
}

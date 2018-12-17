package mydatax.common.reader;

import java.lang.reflect.Method;

import org.apache.log4j.Logger;

import mydatax.common.line.LineSender;
import mydatax.plugin.PluginWorker;
import mydatax.util.DataExchangeException;
import mydatax.util.ExceptionTracker;
import mydatax.util.ExitStatus;

/**
 * {@link ReaderWorker} represents executor of {@link Reader} </br>
 * {@link Engine} use {@link ReaderWorker} to read data.</br>
 * 
 * */
public class ReaderWorker extends PluginWorker implements Runnable {
	private LineSender sender;

	private Method connect;

	private Method startRead;

	private Method finish;

	private static int globalIndex = 0;

	private static final Logger logger = Logger.getLogger(ReaderWorker.class);
	
	/**
	 * Constructor for {@link ReaderWorker}.
	 * 
	 * @param	pluginConf
	 * 			{@link PluginConf}
	 * 
	 * @param	myClass
	 * 			class of {@link Pluginable}
	 * 
	 * @throws		{@link DataExchangeException}
	 * 
	 * */
	public ReaderWorker(Class<?> myClass) {
		super(myClass);
		try {
			connect = myClass.getMethod("connect", new Class[] {});
			startRead = myClass
					.getMethod("startRead", new Class[] { Class
							.forName("mydatax.common.line.LineSender") });
			finish = myClass.getMethod("finish", new Class[] {});
		}  catch (Exception e) {
			logger.error(ExceptionTracker.trace(e));
			throw new DataExchangeException(e);
		}
		this.setMyIndex(globalIndex++);
	}


	/**
	 * Read data, main execute logic code of {@link Reader} <br>
	 * NOTE: When catches exception, {@link ReaderWorker} exit process immediately.
	 * 
	 * */
	@Override
	public void run() {
		try {
//			int iRetcode = (Integer) init.invoke(myObject, new Object[] {});
//			if (iRetcode != 0) {
//				logger.error("Reader initialize failed .");
//				System.exit(ExitStatus.FAILED.value());
//				return;
//			}
			int iRetcode = (Integer) connect.invoke(myObject,
					new Object[] {});
			if (iRetcode != 0) {
				logger.error("Reader connect to DB failed .");
				System.exit(ExitStatus.FAILED.value());
				return;
			}
			iRetcode = (Integer) startRead.invoke(myObject,
					new Object[] { this.sender });
			if (iRetcode != 0) {
				logger.error("Reader startRead failed .");
				System.exit(ExitStatus.FAILED.value());
				return;
			}
			iRetcode = (Integer) finish.invoke(myObject, new Object[] {});
			if (iRetcode != 0) {
				logger.error("Reader finish loading failed .");
				System.exit(ExitStatus.FAILED.value());
				return;
			}
		}  catch (Exception e) {
			logger.error(ExceptionTracker.trace(e));
			System.exit(ExitStatus.FAILED.value());
		}
	}
	
	public void setLineSender(LineSender sender) {
		this.sender = sender;
	}

}

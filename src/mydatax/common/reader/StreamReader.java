package mydatax.common.reader;


import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStreamReader;

import org.apache.log4j.Logger;

import mydatax.common.line.Line;
import mydatax.common.line.LineSender;
import mydatax.plugin.PluginStatus;
import mydatax.util.DataExchangeException;
import mydatax.util.ExceptionTracker;



/**
 * @author bazhen.csy
 *
 */
public class StreamReader extends Reader {	
	private char FIELD_SPLIT = '\t';
	
	private String ENCODING = "UTF-8";
	
	private String nullString = "";
	
	private static Logger logger = Logger.getLogger(StreamReader.class.getCanonicalName());
	
	@Override
	public int init() {		
		System.out.println("duck");
		return PluginStatus.SUCCESS.value();
	}


	@Override
	public int connect() {
		return 0;
	}

	private String changeNull(final String item) {
		if (nullString != null && nullString.equals(item)) {
			return null;
		}
		return item;
	}

	@Override
	public int startRead(LineSender resultWriter){
		int ret = PluginStatus.SUCCESS.value();
		
		int previous;
		String fetch;
		
		BufferedReader reader;
		try {
			reader = new BufferedReader(new FileReader("181.txt"));
			while ((fetch = reader.readLine()) != null) {
				previous = 0;
				Line line = resultWriter.createLine();
				for (int i = 0; i < fetch.length(); i++) {
					if (fetch.charAt(i) == this.FIELD_SPLIT) {
						line.addField(changeNull(fetch.substring(previous, i)));
						previous = i + 1;
					}
				}
				line.addField(fetch.substring(previous));
				resultWriter.sendToWriter(line);
			}
			reader.close();
			resultWriter.flush();
			System.out.println("--------------------------Fuck--------------------------Fuck--------------------------Fuck--------------------------");
		}  catch (Exception e) {
			logger.error(ExceptionTracker.trace(e));
			throw new DataExchangeException(e.getCause());
		}
		
		return ret;
	}


	@Override
	public int finish(){
		return PluginStatus.SUCCESS.value();
	}
}

/**
 * @Title: engine.java
 * @Package mydatax
 * @Description: TODO
 * Copyright: Copyright (c) 2017
 * 
 * @author chenjc5
 * @date 2018年12月6日 下午4:41:04
 * @version V1.0
 */

package mydatax;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import mydatax.common.Storage;
import mydatax.common.StorageFactory;
import mydatax.common.line.BufferedLineExchanger;
import mydatax.common.reader.ReaderWorker;
import mydatax.common.writer.WriterWorker;

/**
  * @ClassName: engine
  * @Description: TODO
  * @author chenjc5
  * @date 2018年12月6日 下午4:41:04
  *
  */

public class engine {
	
	private static final Logger logger = Logger.getLogger(engine.class);
	
	private static final int PERIOD = 10;
	
	private static final int MAX_CONCURRENCY = 64;

	public static void main(String[] args) throws Exception {
		engine e = new engine();
		e.start();
	}

	/**
	 * @throws ClassNotFoundException 
	 * @throws InterruptedException 
	 * @return 
	  * start(这里用一句话描述这个方法的作用)
	  * TODO(这里描述这个方法适用条件 – 可选)
	  * TODO(这里描述这个方法的执行流程 – 可选)
	  * TODO(这里描述这个方法的使用方法 – 可选)
	  * TODO(这里描述这个方法的注意事项 – 可选)
	  *
	  * @author chenjc5
	  * @date 2018年12月6日 下午5:05:24
	  * @Title: start
	  * @Description: TODO
	  * @param     设定文件
	  * @return void    返回类型
	  * @throws
	  */
	
	
	
	private int start() throws Exception {
		logger.info("DataX startups .");

		Storage storage = StorageFactory.product("mydatax.common.RAMStorage");
		storage.init("test", 1024, 65535, 1024);
		List storagelist = new ArrayList<>();
		storagelist.add(storage);
		
		NamedThreadPoolExecutor readerPool = initReaderPool(storagelist);
		NamedThreadPoolExecutor writerPool = initWriterPool(storagelist);

		logger.info("DataX starts to exchange data .");
		readerPool.shutdown();
		writerPool.shutdown();

		int sleepCnt = 0;
		int retcode = 0;

		while (true) {
			/* check reader finish? */
			boolean readerFinish = readerPool.isTerminated();
			if (readerFinish) {
				storage.setPushClosed(true);
			}

			boolean writerAllFinish = true;

			if(!writerPool.isTerminated()) {
				writerAllFinish = false;
			}
				
			if (readerFinish && writerAllFinish) {
//				logger.info("DataX Reader post work begins .");
//				readerPool.doPost();
//				logger.info("DataX Reader post work ends .");
//
//				logger.info("DataX Writers post work begins .");
//				writerPool.doPost();
//				logger.info("DataX Writers post work ends .");
//
//				logger.info("DataX job succeed .");
				break;
			} else if (!readerFinish && writerAllFinish) {
				logger.error("DataX Writers finished before reader finished.");
				logger.error("DataX job failed.");
				readerPool.shutdownNow();
				readerPool.awaitTermination(3, TimeUnit.SECONDS);
				break;
			}

			Thread.sleep(1000);
			sleepCnt++;

			if (sleepCnt % PERIOD == 0) {
				/* reader&writer count num of thread */
				StringBuilder sb = new StringBuilder();
				sb.append(String.format("ReaderPool %s: Active Threads %d .",
						readerPool.getName(), readerPool.getActiveCount()));
				logger.info(sb.toString());

				sb.setLength(0);
				sb.append(String.format(
						"WriterPool %s: Active Threads %d .",
						writerPool.getName(),
						writerPool.getActiveCount()));
				logger.info(sb.toString());
			}
		}

		long total = -1;
		boolean writePartlyFailed = false;
		
		String[] lineCounts = storage.info().split(":");
		long lineTx = Long.parseLong(lineCounts[1]);
		if (total != -1 && total != lineTx) {
			writePartlyFailed = true;
			logger.error("Writer partly failed, for " + total + "!="
					+ lineTx);
		}
		total = lineTx;
		
		return writePartlyFailed ? 200 : retcode;
	}

	/**
	  * initWriterPool(这里用一句话描述这个方法的作用)
	  * TODO(这里描述这个方法适用条件 – 可选)
	  * TODO(这里描述这个方法的执行流程 – 可选)
	  * TODO(这里描述这个方法的使用方法 – 可选)
	  * TODO(这里描述这个方法的注意事项 – 可选)
	  *
	  * @author chenjc5
	  * @date 2018年12月9日 下午1:10:57
	  * @Title: initWriterPool
	  * @Description: TODO
	  * @param @param jobConf
	  * @param @param storagePool
	  * @param @return    设定文件
	  * @return List<NamedThreadPoolExecutor>    返回类型
	  * @throws
	  */
	
	
	
	private NamedThreadPoolExecutor initWriterPool(List<Storage> storagePool) throws Exception{
//		JarLoader jarLoader = new JarLoader(
//				new String[] {"C:\\Users\\Spectator\\Desktop"});
//		Class<?> myClass = jarLoader.loadClass("mydatax.StreamWriter");
		Class<?> myClass = this.getClass().getClassLoader().loadClass("mydatax.common.writer.StreamWriter");

		WriterWorker writerWorkerForPreAndPost = new WriterWorker(myClass);
		writerWorkerForPreAndPost.init();

		logger.info("DataX Writer prepare work begins .");
		logger.info("DataX Writer prepare work ends .");

		int concurrency = 1;

		NamedThreadPoolExecutor writerPool = new NamedThreadPoolExecutor(
				"101", concurrency,
				concurrency, 1L, TimeUnit.SECONDS,
				new LinkedBlockingQueue<Runnable>());

		writerPool.setPostWorker(writerWorkerForPreAndPost);
		writerPool.setParam(null);

		writerPool.prestartAllCoreThreads();
		logger.info("DataX Writer starts to write data .");

		WriterWorker writerWorker = new WriterWorker(myClass);
		List storagelist = new ArrayList<>();
		storagelist.add(storagePool);
		writerWorker.setLineReceiver(new BufferedLineExchanger(storagePool.get(0), storagePool));
		writerPool.execute(writerWorker);
	
	
		return writerPool;
	}

	/**
	 * @throws ClassNotFoundException 
	  * initReaderPool(这里用一句话描述这个方法的作用)
	  * TODO(这里描述这个方法适用条件 – 可选)
	  * TODO(这里描述这个方法的执行流程 – 可选)
	  * TODO(这里描述这个方法的使用方法 – 可选)
	  * TODO(这里描述这个方法的注意事项 – 可选)
	  *
	  * @author chenjc5
	  * @date 2018年12月9日 下午1:10:52
	  * @Title: initReaderPool
	  * @Description: TODO
	  * @param @param jobConf
	  * @param @param storagePool
	  * @param @return    设定文件
	  * @return NamedThreadPoolExecutor    返回类型
	  * @throws
	  */
	
	
	
	private NamedThreadPoolExecutor initReaderPool(List<Storage> storagePool) throws ClassNotFoundException {
//		JarLoader jarLoader = new JarLoader(
//				new String[]{"C:\\Users\\test\\Desktop\\res.jar"});
//		Class<?> myClass = jarLoader.loadClass("mydatax.StreamReader");
		
		Class<?> myClass = this.getClass().getClassLoader().loadClass("mydatax.common.reader.StreamReader");

		ReaderWorker readerWorkerForPreAndPost = new ReaderWorker(myClass);
		readerWorkerForPreAndPost.init();

		logger.info("DataX Reader prepare work begins .");
		logger.info("DataX Reader prepare work ends .");

		int concurrency = 1;
		
		NamedThreadPoolExecutor readerPool = new NamedThreadPoolExecutor(
				"Reader", concurrency, concurrency, 1L, TimeUnit.SECONDS,
				new LinkedBlockingQueue<Runnable>());
		readerPool.prestartAllCoreThreads();

		logger.info("DataX Reader starts to read data .");
		
		ReaderWorker readerWorker = new ReaderWorker(myClass);
		readerWorker.setLineSender(new BufferedLineExchanger(storagePool.get(0), storagePool));
		readerPool.execute(readerWorker);

		return readerPool;
	}

}

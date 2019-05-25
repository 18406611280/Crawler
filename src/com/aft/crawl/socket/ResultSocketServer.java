package com.aft.crawl.socket;

import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.log4j.Logger;

import com.aft.crawl.bean.JobDetail;
import com.aft.crawl.bean.TimerJob;
import com.aft.crawl.crawler.CrawlerController;
import com.aft.crawl.result.ResultPost;
import com.aft.crawl.result.vo.CrawlResultBase;
import com.aft.crawl.socket.vo.SocketRequestVo;
import com.aft.crawl.socket.vo.SocketResponseVo;
import com.aft.crawl.thread.ThreadController;
import com.aft.utils.MyDefaultProp;
import com.aft.utils.jackson.MyJsonTransformUtil;

public class ResultSocketServer extends Thread {
	
	private final static Logger logger = Logger.getLogger(ResultSocketServer.class);
	
	private static ServerSocket server = null;
	
	public static void main(String args[]) throws Exception {
		new ResultSocketServer().start();
	}
	
	public ResultSocketServer() {
		this(MyDefaultProp.getSocketServerPort());
	}
	
	public ResultSocketServer(int port) {
		if(null != server) return ;
		try {
			server = new ServerSocket(port);
		} catch(Exception e) {
			logger.error("server socket open exception:\r", e);
		}
	}
	
	@Override
	public void run() {
		while(true) {
			try {
				Socket socket = server.accept();
				
				InputStreamReader isr = new InputStreamReader(socket.getInputStream(), "GBK");
				char[] chars = new char[20480];
				StringBuilder sbMsg = new StringBuilder();
				int len = 0;
				while((len = isr.read(chars, 0, chars.length)) != -1) {
					sbMsg.append(chars, 0, len);
					if(sbMsg.toString().endsWith(MyDefaultProp.getSocketEndStr())) break ;
				}
				socket.shutdownInput();
				
				SocketRequestVo requestVo = MyJsonTransformUtil.readValue(sbMsg.toString(), SocketRequestVo.class);
				logger.info("socket请求数据:" + sbMsg);
				
				String returnMsg = null;
				if(1 == requestVo.getType()) {	// 获取航线
					CopyOnWriteArrayList<TimerJob>  timerJobs = TimerJob.getTimerJobs();
					for(TimerJob timerJob : timerJobs) {
						SocketResponseVo responseVo = new SocketResponseVo(timerJob.getJobId(), timerJob.getCrawlMark());
						List<JobDetail> jobDetails = null;
						while(true) {
							jobDetails = JobDetail.remoteJobs(timerJob);
							if(null == jobDetails || jobDetails.isEmpty()) break ;
							responseVo.getJobDetails().addAll(jobDetails);
						}
						returnMsg = MyJsonTransformUtil.writeValue(requestVo);
					}
				} else {	// 保存结果
					TimerJob timerJob = TimerJob.getTimerJob(requestVo.getJobId());
					String threadMark = ThreadController.getMainThreadMark(timerJob);
					
					List<CrawlResultBase> crawlResults = requestVo.getResults();
					if(null == crawlResults) crawlResults = new ArrayList<CrawlResultBase>();
					else {
//						ResultPost.muMemberPrice(threadMark, timerJob, crawlResults);	// 处理东航会员价
						ResultPost.saveFileLogger(timerJob, crawlResults);				// 特殊处理, 别乱动...
						ResultPost.filterResult(timerJob, crawlResults);				// 排序,过滤
					}
					ResultPost.postResult(threadMark, requestVo.getJobDetail(), crawlResults);
					CrawlerController.releaseCrawler(timerJob.getJobId());
					returnMsg = "ok";
				}
				PrintWriter os = new PrintWriter(socket.getOutputStream());
				os.write(returnMsg);
				os.flush();
				socket.shutdownOutput();
				logger.info("socket返回数据:" + returnMsg);
			} catch(Exception e) {
				logger.error("server socket exception:\r", e);
			}
		}
	}
}
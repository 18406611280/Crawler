package com.aft.swing;

import java.awt.Component;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.WindowConstants;

import org.apache.log4j.Logger;

import com.aft.crawl.CrawlerInit;
import com.aft.crawl.SwitchTypeEnum;
import com.aft.crawl.bean.CrawlCommon;
import com.aft.crawl.bean.CrawlExt;
import com.aft.crawl.bean.CrawlOperator;
import com.aft.crawl.bean.JobDetail;
import com.aft.crawl.bean.TimerJob;
import com.aft.crawl.proxy.ProxyUtil;
import com.aft.crawl.socket.ResultSocketServer;
import com.aft.crawl.thread.ScheduledThreadPoolController;
import com.aft.crawl.thread.ThreadController;
import com.aft.logger.MyCrawlerLogger;
import com.aft.utils.MyDefaultProp;
import com.aft.utils.MyStringUtil;
import com.aft.utils.date.MyDateFormatUtils;
import com.aft.utils.file.MyFileUtils;
import com.aft.utils.file.MyGzipUtil;
import com.aft.utils.http.MyHttpClientUtil;
import com.aft.utils.thread.MyThreadUtils;

public class CrawlerWin {

	private final static Logger logger = Logger.getLogger(CrawlerWin.class);
	
	private final static int width = 900;
	
	private final static int height = 500;
	
	private final static JFrame jFrame = new JFrame();
	
	private final static Font font = new Font("宋体", Font.PLAIN, 14);
	
	private final static JButton propReloadBtn = new JButton("加载配置");
	
	private final static JButton crawlerStatusBtn = new JButton("运行信息");
	
	private final static JCheckBox loggerScrollCheckBox = new JCheckBox("日志滚动", true);
	
	private final static JPanel jPanel = new JPanel();
	
	private final static JTextArea loggerTextArea = new JTextArea();
	
	private final static JScrollPane textAreaScroller = new JScrollPane(loggerTextArea,
			JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
	
	// 是否已点击关闭
	private static boolean closing = false;
	
	// 日志输出单线程池
	private final static ExecutorService loggerThread = Executors.newSingleThreadExecutor();
	
	private static CrawlerWin crawlerWin;
	
	private CrawlerWin() {
		this.init();
		
		// 采集基础信息定时加载
		if(MyDefaultProp.getLocalTest()) {
			CrawlCommon.loadCommon();
			CrawlExt.loadCrawlExts();
		} else {
			this.propReload();			// 加载配置属性...
			this.operatorOnlineTimer();	// 操作者在线检测
			this.propReloadTimer();		// 采集基础信息定时加载
			
			
			this.loadTimerJobTimer();	// 加载任务列表数据
			
			
			this.tempFileTimer();		// 开启删除过期日志, 临时文件
			this.saveFileTimer();		// 发送保存文件定时
			this.adslTimer();			// 检测拨号
			
			if(MyDefaultProp.isSocketServerOpen()) new ResultSocketServer().start();	// 打开socket
		}
		
		CrawlerInit.initController();	// 初始化...
//		OrderBrowser.initOrderBrowser();
	}
	
	/**
	 * 获取单例
	 * @return
	 */
	public synchronized static CrawlerWin instance() {
		return Instance.crawlerWin;
	}
	
	public static class Instance{
		private static final CrawlerWin crawlerWin = new CrawlerWin();
	}
	
	private void init() {
		jFrame.setTitle("Crawler-" + MyDefaultProp.getOperatorName());
		jFrame.setResizable(false);
		jFrame.setSize(width, height);
		jFrame.setLocationRelativeTo(null);
		jFrame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				if(closing) {	// 第二次点击,弹出强制退出提示
					int type = JOptionPane.showConfirmDialog(jFrame, "确定要强制退出?部分已获取任务会丢失!", "退出提示", JOptionPane.YES_NO_OPTION);
					if(type != JOptionPane.YES_OPTION) return ;
					
					logger.info("强制退出!");
					ProxyUtil.updateAllProxyUsed();
					jFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
					System.exit(-1);
				}
				int type = JOptionPane.showConfirmDialog(jFrame, "确定要退出?", "退出提示", JOptionPane.YES_NO_OPTION);
				jFrame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
				if(type != JOptionPane.YES_OPTION) return ;
				
				closing = true;
				propReloadBtn.setEnabled(false);
				crawlerStatusBtn.setEnabled(false);
				loggerScrollCheckBox.setEnabled(false);
				
				ScheduledThreadPoolController.shutdown();
				
				logger.info("正在等待运行任务完成, 系统会自动关闭!");
				MyDialog.showDialog("正在等待运行任务完成, 系统会自动关闭!");
				new Thread(new Runnable() {
					@Override
					public void run() {
						if(!TimerJob.getTimerJobs().isEmpty()) {
							loop : while(true) {
								for(TimerJob timerJob : TimerJob.getTimerJobs()) {
									if(!ThreadController.crawlFinish(timerJob.getJobId(), timerJob.getPageType())) {
										MyThreadUtils.sleep(1000);
										continue loop;
									}
								}
								break ;
							}
							MyThreadUtils.sleep(3000);	// 停顿下,让主线程的先执行完...
						}
						logger.info("Crawler 退出...");
						System.exit(0);
					}
				}).start();
			}
		});

		propReloadBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				try {
					logger.info("CrawlerWin 点击[加载配置]按钮!");
					
					int type = JOptionPane.showConfirmDialog(jFrame, "确定加载配置?", "操作提示", JOptionPane.YES_NO_OPTION);
					if(type != JOptionPane.YES_OPTION) return ;
					
					boolean propReload = CrawlerWin.this.propReload();
					if(!propReload) return ;
					CrawlerWin.logger("获取基础配置信息成功!");
										
					ThreadController.reloadThread();
					CrawlerWin.logger("重新初始化线程信息成功!");
					
					logger.info("CrawlerWin 加载配置完成!");
					CrawlerWin.logger("CrawlerWin 加载配置完成!");
				} catch(Exception e) {
					logger.error("CrawlerWin 点击[加载配置-YES]按钮异常\r", e);
					CrawlerWin.logger("CrawlerWin 点击[加载配置-YES]按钮异常:" + e);
				}
			}
		});
		
		crawlerStatusBtn.setEnabled(false);
		crawlerStatusBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				try {
					CrawlerStatusDialog.show();
				} catch(Exception e) {
					logger.error("CrawlerWin 点击[查看运行状态]按钮异常\r", e);
					CrawlerWin.logger("CrawlerWin 点击[查看运行状态]按钮异常:" + e);
				}
			}
		});
		
		loggerTextArea.setFont(font);
		loggerTextArea.setEditable(false);
		
		GridBagLayout gridbag = new GridBagLayout();
		GridBagConstraints gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.fill = GridBagConstraints.BOTH;
		gridBagConstraints.insets = new Insets(2, 2, 2, 2);
		jFrame.setLayout(gridbag);
		
		gridBagConstraints.weightx = 1.0;
		this.addComponent(propReloadBtn, gridbag, gridBagConstraints);
		
		gridBagConstraints.weightx = 1.0;
		this.addComponent(crawlerStatusBtn, gridbag, gridBagConstraints);
		
		gridBagConstraints.weightx = 1.0;
		this.addComponent(loggerScrollCheckBox, gridbag, gridBagConstraints);
		
		gridBagConstraints.gridwidth = GridBagConstraints.REMAINDER;	// 最后一列
		gridBagConstraints.weightx = 100.0;	// 空列, 要来填充的
		this.addComponent(jPanel, gridbag, gridBagConstraints);
		
		gridBagConstraints.weighty = 1.0;
		this.addComponent(textAreaScroller, gridbag, gridBagConstraints);
		
		this.setComponentUnity();
		jFrame.setVisible(true);
		logger.info("CrawlerWin 启动!");
		CrawlerWin.logger("CrawlerWin 启动!");
	}
	
	/**
	 * 设置控件 统一字体
	 */
	private void setComponentUnity() {
		Component[] components = jFrame.getRootPane().getContentPane().getComponents();
		for(Component component : components) {
			component.setFont(font);
		}
	}
	
	/**
	 * 
	 * @param jComponent
	 * @param gridbag
	 * @param gridBagConstraints
	 */
	private void addComponent(JComponent jComponent, GridBagLayout gridbag, GridBagConstraints gridBagConstraints) {
		gridbag.setConstraints(jComponent, gridBagConstraints);
		jFrame.add(jComponent);
	}
	
	/**
	 * 是否已点击关闭
	 * @return
	 */
	public static boolean isClosing() {
		return CrawlerWin.closing;
	}
	
	/**
	 * 记录日志
	 * @param message
	 */
	public static void logger(final Object message) {
		loggerThread.execute(new Runnable() {
			@Override
			public void run() {
				try {
					if(!loggerScrollCheckBox.isSelected()) return ;
					if(loggerTextArea.getLineCount() >= 1000) loggerTextArea.replaceRange("", 0, loggerTextArea.getLineEndOffset(0));
					loggerTextArea.append(MyDateFormatUtils.SDF_YYYYMMDDHHMMSSSSS().format(new Date()) + "\t" + message + "\r\n");
					loggerTextArea.setCaretPosition(loggerTextArea.getText().length());
				} catch(Exception e) {
					logger.error("loggerTextArea 记录日志异常:\r", e);
				}
			}
		});
	}
	
	/**
	 * 删除过期日志, 临时文件
	 */
	private synchronized void tempFileTimer() {
		if(!MyDefaultProp.getBackupOpen()) return ;
		ScheduledThreadPoolController.schedule(new Runnable() {
			@Override
			public void run() {
				try {
					Calendar calendar = Calendar.getInstance();
					File tempFile = new File(MyDefaultProp.getBackupFilePath());
					if(!tempFile.exists()) return ;
					
					logger.info("删除临时文件开始:" + tempFile.getPath());
					long maxSize = 1024 * 1024 * 1024;
					for(File file : tempFile.listFiles()) {
						if(!file.isFile() || !file.getName().endsWith(".txt")) continue ;
						if(file.length() < maxSize) continue ;
						file.renameTo(new File(MyStringUtil.getFilePath(file.getParent(), file.getName() + "." + MyDateFormatUtils.SDF_YYYYMMDDHHMM_1().format(calendar.getTime()))));
					}
					
					calendar.add(Calendar.DATE, -7);
					if(tempFile.exists()) MyFileUtils.deleteFile(tempFile, calendar.getTimeInMillis());
					logger.info("删除临时文件结束:" + tempFile.getPath());
				} catch(Exception e) {
					logger.info("定时删除文件异常:\r", e);
				}
			}
		}, 1, TimeUnit.HOURS);
	}
	
	/**
	 * 检测拨号
	 */
	private synchronized void adslTimer() {
//		ScheduledThreadPoolController.schedule(new Runnable() {
//			@Override
//			public void run() {
//				if(StringUtils.isEmpty(CrawlOperator.getAdslTitle())) return ;
//				try {
//					String adslIp = MyADSLUtil.getPPPOEIP(CrawlOperator.getNetworkInterface());
//					logger.info("定时检测拨号当前ip:" + adslIp);
//					if(StringUtils.isNotEmpty(adslIp)) return ;
//					
//					logger.info("定时检测拨号已断开, 开始拨号...");
//					MyADSLUtil.changeIp(CrawlOperator.getAdslTitle(), CrawlOperator.getAdslUsername(), CrawlOperator.getAdslPassword(), CrawlOperator.getNetworkInterface());
//					logger.info("定时检测拨号已断开, 拨号成功...");
//				} catch(Exception e) {
//					logger.error("定时检测是否已拨号异常:\r", e);
//				}
//			}
//		}, 1, TimeUnit.MINUTES);
	}
	
	/**
	 * 操作者在线检测
	 */
	private synchronized void operatorOnlineTimer() {
		ScheduledThreadPoolController.schedule(new Runnable() {
			@Override
			public void run() {
				try {
					String result = MyHttpClientUtil.get(CrawlCommon.getProcessorOnlineUrl() + "?operatorName=" + MyDefaultProp.getOperatorName());
					logger.info("操作者在线检测返回:" + result);
				} catch(Exception e) {
					logger.error("操作者在线检测异常:\r", e);
				}
			}
		}, 1, TimeUnit.MINUTES);
	}
	
	/**
	 * 发送保存文件定时
	 */
	private synchronized void saveFileTimer() {
		ScheduledThreadPoolController.schedule(new Runnable() {
			@Override
			public void run() {
				if(!CrawlCommon.getSaveFile() || !CrawlCommon.getSaveOther()) return ;
				
				List<File> files = MyFileUtils.allFiles(MyCrawlerLogger.getSaveFilePath());
				if(null == files || files.isEmpty()) return ;
				for(File file : files) {
					try {
						if(!file.getName().endsWith(".csv")) continue ;
						
						String[] names = file.getName().split("\\.")[0].split("-");	// pageType_id_crawlMark
						if(3 != names.length) continue ;
						
						String switchType = names[0];
						int timerJobId = Integer.parseInt(names[1]);
						String crawlMark = names[2];
						String pageType = SwitchTypeEnum.valueOf(switchType).getPageType();
						
						boolean hasMore = JobDetail.hasMoreJobDetail(pageType, timerJobId, crawlMark);	// 是否还有远程任务
						if(hasMore) continue ;
						boolean localOk = ThreadController.crawlFinish(timerJobId, pageType);			// 本地是否处理完
						if(!localOk) continue ;
						
						logger.info("压缩发送保存文件开始:" + file.getPath());
						File saveZipFile = MyGzipUtil.gzipFile(file.getParent(), switchType + "-" + crawlMark + ".gz", file);	// pageType_crawlMark
						
						logger.info("发送保存gz文件开始:" + saveZipFile.getPath());
						Map<String, String> paramMap = new HashMap<String, String>();
						paramMap.put("type", switchType);
						paramMap.put("version", crawlMark);
						
						String postResult = null;
						if(CrawlCommon.getSaveOther()) {
							postResult = MyHttpClientUtil.postForm(CrawlCommon.getSaveOtherUrl(), "jarFile", saveZipFile.getPath(), saveZipFile.getName(), paramMap);
							logger.info("发送保存gz文件返回:" + postResult);
						}
						if(CrawlCommon.getSaveFile()) {
							postResult = MyHttpClientUtil.postForm(CrawlCommon.getSaveFileUrl(), "jarFile", saveZipFile.getPath(), saveZipFile.getName(), paramMap);
							logger.info("发送保存gz文件返回:" + postResult);
						}
						// {"saveHdfsResult":"true"}
						if("{\"saveHdfsResult\":\"true\"}".equals(postResult)) file.delete();
					} catch(Exception e) {
						logger.error("发送保存文件定时异常:\r", e);
					}
				}
			}
		}, 1, TimeUnit.MINUTES);
	}
	
	/**
	 * 采集基础信息定时加载
	 */
	private synchronized void propReloadTimer() {
		ScheduledThreadPoolController.schedule(new Runnable() {
			@Override
			public void run() {
				if(CrawlerWin.closing) return ;
				propReload();
			}
		}, 5, ("all_realtime".equalsIgnoreCase(MyDefaultProp.getOperatorName())? TimeUnit.SECONDS : TimeUnit.MINUTES));
	}

	/**
	 * 加载任务列表数据
	 */
	private synchronized void loadTimerJobTimer() {
		ScheduledThreadPoolController.schedule(new Runnable() {
			@Override
			public void run() {
				if(CrawlerWin.closing) return ;
				CrawlerWin.loadTimerJob();	// 加载任务列表数据
			}
		}, 1,("all_realtime".equalsIgnoreCase(MyDefaultProp.getOperatorName())? TimeUnit.SECONDS : TimeUnit.MINUTES));
	}
	
	/**
	 * 加载信息
	 * @return
	 */
	private synchronized boolean propReload() {
		CrawlCommon crawlCommon = CrawlCommon.loadCommon();
		if(null == crawlCommon) {
			logger.warn("获取基础信息失败...");
			CrawlerWin.logger("获取基础信息失败!");
			MyDialog.showDialogCloseByTime("获取基础信息失败,请重试点击加载配置,如多次失败,报警吧...", 60);
			return false;
		}
		CrawlerWin.logger("获取基础信息成功!");
		
		
		// 加载操作者信息
		CrawlOperator crawlOperator = CrawlOperator.loadOperator();
		if(null == crawlOperator) {
			logger.warn("获取操作者为空,请检查是否存在或已启用...");
			CrawlerWin.logger("获取操作者为空,请检查是否存在或已启用!");
			MyDialog.showDialogCloseByTime("获取操作者为空,请检查是否存在或已启用, 请重试点击加载配置,如多次失败,报警吧...", 60);
			return false;
		}
		CrawlerWin.logger("获取操作者信息成功!");
		
//		CrawlerWin.loadTimerJob();	// 加载任务列表数据
		
		// 加载基础配置数据
		boolean loadOk = CrawlExt.loadCrawlExts();
		if(!loadOk) {
			logger.warn("获取基础配置信息失败,请重试点击加载配置...");
			CrawlerWin.logger("获取基础配置信息失败,请重试点击加载配置!");
			MyDialog.showDialogCloseByTime("获取基础配置信息失败,请重试点击加载配置,如多次失败,报警吧...", 60);
			return false;
		}
		crawlerStatusBtn.setEnabled(true);
		return true;
	}
	
	/**
	 * 加载任务列表数据
	 */
	private static boolean loadTimerJob() {
		boolean loadOk = TimerJob.loadTimerJobs();
		if(!loadOk) {
			logger.warn("获取任务列表失败,请重试点击加载配置...");
			CrawlerWin.logger("获取任务列表失败,请重试点击加载配置!");
			MyDialog.showDialogCloseByTime("获取任务列表失败,请重试点击加载配置,如多次失败,报警吧...", 60);
		}
		return loadOk;
	}

	public static void main(String[] args) throws Exception {
		CrawlerWin.instance();
	}
}
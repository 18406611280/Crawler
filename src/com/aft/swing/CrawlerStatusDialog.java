package com.aft.swing;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Collections;
import java.util.Comparator;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import javax.swing.JDialog;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;

import org.apache.log4j.Logger;

import com.aft.crawl.CrawlerType;
import com.aft.crawl.bean.TimerJob;

public class CrawlerStatusDialog {
	
	private final static Logger logger = Logger.getLogger(CrawlerStatusDialog.class);

	private final static JDialog jDialog = new JDialog();
	
	private final static JTable jTable = new JTable();
	
	private final static DefaultTableModel tableModel = new DefaultTableModel() {
		private static final long serialVersionUID = 5026125140568782661L;
		@Override
		public boolean isCellEditable(int row, int column) {
			return false;
		}
	};
	
	private final static JScrollPane jScroller = new JScrollPane(jTable,
			JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
	
	static {
		jDialog.setTitle("运行状态");
		jDialog.setSize(700, 300);
		jDialog.setResizable(false);
		jDialog.setAlwaysOnTop(true);
		jDialog.setLocationRelativeTo(null);
		jDialog.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				jDialog.dispose();
			}
		});
		
		jTable.setModel(tableModel);
		String[] newIdentifiers = new String[] {
									"类型",
									"任务名",
									"Ip数",
									"Ip线程数"
								};
		tableModel.setColumnIdentifiers(newIdentifiers);
		TableColumnModel colmodel = jTable.getColumnModel();
		colmodel.getColumn(0).setPreferredWidth(300);
		colmodel.getColumn(1).setPreferredWidth(300);
		colmodel.getColumn(2).setPreferredWidth(40);
		colmodel.getColumn(3).setPreferredWidth(60);
		
		jScroller.setBounds(5, 5, 450, 290);
		jDialog.add(jScroller);
		
		jDialog.setVisible(true);
		jDialog.repaint();
		
		new Timer().schedule(new TimerTask() {
			@Override
			public void run() {
				try {
					if(jDialog.isVisible()) CrawlerStatusDialog.show();
				} catch(Exception e) {
					logger.error("显示采集信息异常:\r", e);
				}
			}
		}, 10 * 1000, 10 * 1000);
	}
	
	/**
	 * 显示
	 * @throws Exception 
	 */
	@SuppressWarnings("unchecked")
	public synchronized static void show() throws Exception {
		Vector<Vector<Object>> vectors = tableModel.getDataVector();
		vectors.clear();	// 先清空表内容
		for(TimerJob timerJob : TimerJob.getTimerJobs()) {
			CrawlerType crawlerType = CrawlerType.getCrawlerType(timerJob.getPageType());
			Vector<Object> vector = new Vector<Object>();
			vector.add(crawlerType.toStr());
			
			vector.add(timerJob.getJobName() + "_" + timerJob.getJobId());
			
			vector.add(timerJob.getIpAmount());
			vector.add(timerJob.getIpAmount() + " * " + timerJob.getOneIpThread());
			
			vectors.add(vector);
		}
		Collections.sort(vectors, new Comparator<Vector<Object>>() {
			@Override
			public int compare(Vector<Object> o1, Vector<Object> o2) {
				return o1.get(0).toString().compareTo(o2.get(0).toString());
			}
		});
		jTable.repaint();
		if(!jDialog.isVisible()) jDialog.setVisible(true);
	}
}
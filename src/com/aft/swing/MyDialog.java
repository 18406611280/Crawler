package com.aft.swing;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class MyDialog {

	private final JDialog dialog = new JDialog();
	
	private final JTextArea textArea = new JTextArea();
	
	private final JScrollPane scroller = new JScrollPane(textArea,
			JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
	
	private final JButton button = new JButton("确定");
	
	private int leftTime = 0;
	
	private final Timer timer = new Timer();

	private MyDialog(String message) {
		this(message, 0);
	}
	
	private MyDialog(String message, final int time) {
		dialog.setTitle("提示窗口");
		dialog.setSize(300, 150);
		dialog.setLocationRelativeTo(null);
		dialog.setAlwaysOnTop(true);
		dialog.setResizable(false);
		dialog.setLayout(null);
		dialog.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				timer.cancel();
				dialog.dispose();
			}
		});
		
		textArea.setText(message);
		textArea.setEditable(false);
		textArea.setLineWrap(true);
		
		scroller.setBounds(10, 10, 280, 70);
		if(time > 0) {
			button.setText("确定(" + time + ")");
			leftTime = time;
			timer.schedule(new TimerTask() {
				@Override
				public void run() {
					if(leftTime <= 0) {
						timer.cancel();
						if(dialog.isDisplayable()) dialog.dispose();
						return ;
					}
					leftTime --;
					button.setText("确定(" + leftTime + ")");
				}
			}, 1000, 1000);
		}
		button.setBounds(110, 90, 90, 30);
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				timer.cancel();
				dialog.dispose();
			}
		});
		dialog.add(button);
		dialog.add(scroller);
		
		dialog.setVisible(true);
		dialog.repaint();
	}
	
	/**
	 * 显示
	 * @param message
	 */
	public static void showDialog(String message) {
		new MyDialog(message);
	}
	
	/**
	 * 显示 time 时间后自动关闭
	 * @param message
	 * @param time 秒
	 */
	public static void showDialogCloseByTime(String message, int time) {
		new MyDialog(message, time);
	}
}
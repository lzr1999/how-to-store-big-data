package day1;
import java.io.File;

import javax.swing.filechooser.FileSystemView;

import net.contentobjects.jnotify.JNotify;
import net.contentobjects.jnotify.JNotifyAdapter;
import net.contentobjects.jnotify.JNotifyException;
 
public class EnhancedJNotifyAdapter extends JNotifyAdapter {
	FileSystemView fsv = FileSystemView.getFileSystemView();
	File home = fsv.getHomeDirectory();
	String savePath = home.getPath();
	/** �����ӵ�Ŀ¼ */
	String path = savePath;
	/** ��עĿ¼���¼� */
	int mask = JNotify.FILE_CREATED | JNotify.FILE_DELETED | JNotify.FILE_MODIFIED | JNotify.FILE_RENAMED;
	/** �Ƿ������Ŀ¼������������ */
	boolean watchSubtree = true;
	/** ��������Id */
	public int watchID;
 
	public static void main(String[] args) {
		new EnhancedJNotifyAdapter().beginWatch();
	}
 
	/**
	 * ��������ʱ�������ӳ���
	 * 
	 * @return
	 */
	public void beginWatch() {
		/** ��ӵ����Ӷ����� */
		try {
			this.watchID = JNotify.addWatch(path, mask, watchSubtree, this);
			System.err.println("Done!");
		} catch (JNotifyException e) {
			e.printStackTrace();
		}
		// ��ѭ�����߳�һֱִ�У�����һ���Ӻ����ִ�У���Ҫ��Ϊ�������߳�һֱִ��
		// ����ʱ��ͼ���ļ�������Ч���޹أ�����˵���Ǽ���Ŀ¼�ļ��ı�һ���Ӻ�ż�⵽����⼸����ʵʱ�ģ����ñ���ϵͳ�⣩
		while (true) {
			try {
				Thread.sleep(60000);
			} catch (InterruptedException e) {// ignore it
			}
		}
	}
 
	/**
	 * ������Ŀ¼��һ�����µ��ļ����������򼴴������¼�
	 * 
	 * @param wd
	 *            �����߳�id
	 * @param rootPath
	 *            ����Ŀ¼
	 * @param name
	 *            �ļ�����
	 */
	public void fileCreated(int wd, String rootPath, String name) {
		System.err.println("�ļ�������, ����λ��Ϊ�� " + rootPath + "/" + name);
	}
 
	public void fileRenamed(int wd, String rootPath, String oldName, String newName) {
		System.err.println("�ļ���������, ԭ�ļ���Ϊ��" + rootPath + "/" + oldName
				+ ", ���ļ���Ϊ��" + rootPath + "/" + newName);
	}
 
	public void fileModified(int wd, String rootPath, String name) {
		System.err.println("�ļ����ݱ��޸�, �ļ���Ϊ��" + rootPath + "/" + name);
	}
 
	public void fileDeleted(int wd, String rootPath, String name) {
		System.err.println("�ļ���ɾ��, ��ɾ�����ļ���Ϊ��" + rootPath + name);
	}
}
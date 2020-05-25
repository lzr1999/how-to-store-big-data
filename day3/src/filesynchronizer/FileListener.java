package filesynchronizer;
import java.io.File;
import org.apache.commons.io.monitor.FileAlterationListenerAdaptor;
import org.apache.commons.io.monitor.FileAlterationObserver;


public class FileListener extends FileAlterationListenerAdaptor{
	private FileSynchronizer fileSynchronizer;
	public FileListener(FileSynchronizer fileSynchronizer) {
		this.fileSynchronizer = fileSynchronizer;
	}

//	文件创建
	public void onFileCreate(File file) {
		System.out.println("[新建]:"+file.getAbsolutePath());
		if(file.length() > fileSynchronizer.getMaxSize())
			fileSynchronizer.multipartUpload(file, "", 0, 1, null);
		else {
			fileSynchronizer.simpleUpload(file);
		}
	}
	

//	   文件创建修改
	public void onFileChange(File file) {
		System.out.println("[修改]:" + file.getAbsolutePath());
		if(file.length() > fileSynchronizer.getMaxSize())
			fileSynchronizer.multipartUpload(file, "", 0, 1, null);
		else {
			fileSynchronizer.simpleUpload(file);
		}
	}

//	 文件删除
	public void onFileDelete(File file) {
		System.out.println("[删除]:" + file.getAbsolutePath());
		fileSynchronizer.deleteFile(file);
	}
	
//	目录创建
	public void onDirectoryCreate(File directory) {
		System.out.println("[新建]:" + directory.getAbsolutePath());
		
	}
	
//	目录修改
	public void onDirectoryChange(File directory) {
		System.out.println("[修改]:" + directory.getAbsolutePath());
	}

//	目录删除
	public void onDirectoryDelete(File directory) {
		System.out.println("[删除]:" + directory.getAbsolutePath());
	}
	
	public void onStart(FileAlterationObserver observer) {
	    // TODO Auto-generated method stub
	    super.onStart(observer);
	}
	
	public void onStop(FileAlterationObserver observer) {
	    // TODO Auto-generated method stub
	    super.onStop(observer);
	}

}

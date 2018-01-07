//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package soot.jimple.infoflow.android.resources;

import java.io.File;
import java.util.Enumeration;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public abstract class AbstractResourceParser {
	public AbstractResourceParser() {
	}

	protected void handleAndroidResourceFiles(String apk, Set<String> fileNameFilter, IResourceHandler handler) {
		File apkF = new File(apk);
		if(!apkF.exists()) {
			throw new RuntimeException("file \'" + apk + "\' does not exist!");
		} else {
			try {
				ZipFile e = null;

				try {
					e = new ZipFile(apkF);
					Enumeration entries = e.entries();

					while(entries.hasMoreElements()) {
						ZipEntry entry = (ZipEntry)entries.nextElement();
						String entryName = entry.getName();
						handler.handleResourceFile(entryName, fileNameFilter, e.getInputStream(entry));
					}
				} finally {
					if(e != null) {
						e.close();
					}

				}

			} catch (Exception var13) {
				System.err.println("Error when looking for XML resource files in apk " + apk + ": " + var13);
				var13.printStackTrace();
				if(var13 instanceof RuntimeException) {
					throw (RuntimeException)var13;
				} else {
					throw new RuntimeException(var13);
				}
			}
		}
	}
}

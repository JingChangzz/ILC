package ilc.Properties;

import org.apache.commons.configuration2.io.FileLocationStrategy;
import org.apache.commons.configuration2.io.FileLocator;
import org.apache.commons.configuration2.io.FileSystem;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;

/**
 * {@code FileLocationStrategy}的一个实现，在当前工作路径及其子路径下搜索文件
 */
public class WorkingDirectoryLocationStrategy implements FileLocationStrategy {
    @Override
    public URL locate(FileSystem fileSystem, FileLocator locator) {
        return StringUtils.isEmpty(locator.getFileName()) ? null
                : locateFromSubdirectories(new File(System.getProperty("user.dir")), locator.getFileName());
    }

    /**
     * 在子目录下搜索文件
     *
     * @param directory 搜索的目录
     * @param fileName  文件名
     * @return 找到资源的URL或未找到时为<b>null</b>
     */
    private URL locateFromSubdirectories(File directory, String fileName) {
        URL url = null;
        String[] extensions = {FilenameUtils.getExtension(fileName)};
        Collection<File> files = FileUtils.listFiles(directory, extensions, true);
        for (File file : files) {
            if (file.getName().equals(fileName)) {
                try {
                    url = file.toURI().toURL();
                } catch (MalformedURLException e) {
                }
                break;
            }
        }
        return url;
    }
}

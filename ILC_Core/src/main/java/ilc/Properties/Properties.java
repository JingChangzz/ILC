package ilc.Properties;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.FileBasedConfiguration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.convert.DefaultListDelimiterHandler;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.io.BasePathLocationStrategy;
import org.apache.commons.configuration2.io.CombinedLocationStrategy;
import org.apache.commons.configuration2.io.FileLocationStrategy;

import java.io.File;
import java.util.Arrays;
import java.util.List;

/**
 * 基础配置类
 */
public class Properties {
    /**
     * 配置信息构造器
     */
    private FileBasedConfigurationBuilder<FileBasedConfiguration> builder;

    /**
     * 配置文件
     */
    private File file;

    /**
     * 构造基础配置对象
     *
     * @param file 配置文件，可为指定绝对路径的文件，如果无法通过绝对路径加载则在当前工作路径及其子路径下搜索同名文件
     */
    public Properties(File file) {
        Parameters parameters = new Parameters();
        List<FileLocationStrategy> strategies = Arrays.asList(
                new BasePathLocationStrategy(),
                new WorkingDirectoryLocationStrategy());
        builder = new FileBasedConfigurationBuilder<FileBasedConfiguration>(PropertiesConfiguration.class)
                .configure(parameters.fileBased()
                        .setFile(file)
                        .setEncoding("UTF-8")
                        .setLocationStrategy(new CombinedLocationStrategy(strategies))
                        .setListDelimiterHandler(new DefaultListDelimiterHandler(',')));
    }

    /**
     * 获取配置信息
     *
     * @return 如果配置文件加载失败则返回空配置信息
     */
    public Configuration getConfiguration() {
        Configuration configuration;
        try {
            configuration = builder.getConfiguration();
        } catch (ConfigurationException e) {
            e.printStackTrace();
            configuration = new PropertiesConfiguration();
        }
        return configuration;
    }

    /**
     * 获取配置文件
     *
     * @return 配置文件，如果获取失败则返回null
     */
    public File getFile() {
        if (file == null) {
            getConfiguration();
            file = builder.getFileHandler().getFile();
        }
        return file;
    }
}

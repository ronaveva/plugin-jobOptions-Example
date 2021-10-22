package org.example.rundeck.plugin;

import com.dtolabs.rundeck.core.plugins.Plugin;
import com.dtolabs.rundeck.core.plugins.configuration.AcceptsServices;
import com.dtolabs.rundeck.core.plugins.configuration.Describable;
import com.dtolabs.rundeck.core.plugins.configuration.Description;
import com.dtolabs.rundeck.core.plugins.configuration.PropertyScope;
import com.dtolabs.rundeck.core.storage.StorageTree;
import com.dtolabs.rundeck.core.storage.keys.KeyStorageTree;
import com.dtolabs.rundeck.plugins.ServiceNameConstants;
import com.dtolabs.rundeck.plugins.descriptions.PluginDescription;
import com.dtolabs.rundeck.plugins.option.OptionValue;
import com.dtolabs.rundeck.plugins.option.OptionValuesPlugin;
import com.dtolabs.rundeck.plugins.util.DescriptionBuilder;
import com.dtolabs.rundeck.plugins.util.PropertyBuilder;
import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;
import org.rundeck.app.spi.Services;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Plugin(name = PluginOptionProvider.PROVIDER_NAME, service = ServiceNameConstants.OptionValues)
@PluginDescription(title = PluginOptionProvider.PROVIDER_TITLE, description = PluginOptionProvider.PROVIDER_DESCRIPTION)
public class PluginOptionProvider implements OptionValuesPlugin, Describable, AcceptsServices {

    public static final String PROVIDER_NAME = "option-provider-example";
    public static final String PROVIDER_TITLE = "Option Provider Example";
    public static final String PROVIDER_DESCRIPTION = "Option Provided with Key Storage Access";
    public static final String EXAMPLE_URL="url";
    public static final String EXAMPLE_PASSWORD_PATH="password-key-storage-path";

    private Services services;

    @Override
    public List<OptionValue> getOptionValues(Map config) {
        //Here it retrieves the value from the key storage
        String valueFromKeyStorage = PluginOptionProvider.getValueFromStorage(config, services);
        System.out.println("Value obtained from key storage: " + valueFromKeyStorage);
        //These are the properties value from the project configuration
        System.out.println("Original url value from project configuration: " + config.get(EXAMPLE_URL));
        System.out.println("Original Key storage key from project configuration: " + config.get(EXAMPLE_PASSWORD_PATH));
        //Here you already have the url and the token to perform the call to the url option provider
        String resultUrl = replaceUrlToken(config.get(EXAMPLE_URL).toString(), valueFromKeyStorage);
        System.out.println("Final url string with token replaced by key storage value: " + resultUrl);
        //These options are just an example to be shown when running the job
        List<OptionValue> options = new ArrayList<>();
        StandardOptionValue option = new StandardOptionValue("url 1", "1"+resultUrl);
        options.add(option);
        StandardOptionValue optionTwo = new StandardOptionValue("url 2", "2"+resultUrl);
        options.add(optionTwo);
        return options;
    }

    private String replaceUrlToken(String originalUrl, String valueFromKeyStorage) {
        String finalUrl = originalUrl.replace("${token}", valueFromKeyStorage);
        return finalUrl;
    }

    @Override
    public Description getDescription() {
        return DescriptionBuilder.builder()
                .name(PROVIDER_NAME)
                .title(PROVIDER_TITLE)
                .description(PROVIDER_DESCRIPTION)
                .property(PropertyBuilder.builder()
                        .string(EXAMPLE_URL)
                        .title("Server URL")
                        .description("Example server URL")
                        .required(false)
                        .scope(PropertyScope.Project)
                        .build()
                )
                .property(PropertyBuilder.builder()
                        .string(EXAMPLE_PASSWORD_PATH)
                        .title("password path")
                        .description("The account password. Must be a keystorage path")
                        .required(false)
                        .scope(PropertyScope.Project)
                        .build()
                )
                .build();
    }

    @Override
    public void setServices(Services services) {
        this.services = services;
    }

    public static String getValueFromStorage(Map<String, Object> properties, Services services){
        String passwordPath = (String)properties.get("password-key-storage-path");
        StorageTree storageTree = services.getService(KeyStorageTree.class);
        String password = PluginOptionProvider.getPrivateKeyStorageData(passwordPath, storageTree);
        return password;
    }

    public static String getPrivateKeyStorageData(String path, StorageTree storageTree) {
        if (null == path) {
            return null;
        }
        try {
            InputStream is =  storageTree.getResource(path)
                    .getContents()
                    .getInputStream();
            String result = CharStreams.toString(new InputStreamReader(
                    is, Charsets.UTF_8));
            return result;
        }catch (IOException e){
            e.printStackTrace();
        }
        return null;
    }

    class StandardOptionValue implements OptionValue {

        private String name;
        private String value;
        StandardOptionValue(String name, String value) {
            this.name = name;
            this.value = value;
        }
        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getValue() {
            return value;
        }
    }

}

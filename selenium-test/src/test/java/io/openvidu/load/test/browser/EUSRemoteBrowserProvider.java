package io.openvidu.load.test.browser;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import static java.lang.invoke.MethodHandles.lookup;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.slf4j.Logger;

import org.openqa.selenium.remote.RemoteWebDriver;

public class EUSRemoteBrowserProvider implements BrowserProvider {

    final static Logger log = getLogger(lookup().lookupClass());

    // AWS PARAMS

    public static String REGION = "eu-west-1";
    public static String BROWSERS_AMI_ID = "ami-0bfc646d9bb6ad37c";
    private static JsonObject AWS_CONFIG;
    protected static String EUS_URL;

    public EUSRemoteBrowserProvider() {
        EUS_URL = System.getenv("ET_EUS_API");
    }

    public void setUpAWSConfig() {

        // GET BROSER AMI (OR USE DEFAULT)

        String browsersAmiID = System.getenv("BROWSERS_AMI_ID");
        if (browsersAmiID != null && !"".equals(browsersAmiID)) {
            BROWSERS_AMI_ID = browsersAmiID;
        }

        // Aws Config
        String secretAccessKey = System.getenv("AWS_SECRET_ACCESS_KEY");
        String accessKeyId = System.getenv("AWS_ACCESS_KEY_ID");
        String sshUser = System.getenv("AWS_SSH_USER");
        String sshPrivateKey = System.getenv("AWS_SSH_PRIVATE_KEY");

        // Instances config
        String instanceType = System.getenv("AWS_INSTANCE_TYPE");
        String keyName = System.getenv("AWS_KEY_NAME");
        String securityGroups = System.getenv("AWS_SECURITY_GROUPS");
        String tagSpecifications = System.getenv("AWS_TAG_SPECIFICATIONS");
        int numInstances = 1;

        JsonParser parser = new JsonParser();
        AWS_CONFIG = new JsonObject();

        AWS_CONFIG.addProperty("region", REGION);
        AWS_CONFIG.addProperty("secretAccessKey", secretAccessKey);
        AWS_CONFIG.addProperty("accessKeyId", accessKeyId);
        AWS_CONFIG.addProperty("sshUser", sshUser);
        AWS_CONFIG.addProperty("sshPrivateKey", sshPrivateKey.replace("\\r\\n", System.lineSeparator()));

        // Instances Config

        JsonObject awsInstancesConfig = new JsonObject();
        awsInstancesConfig.addProperty("amiId", BROWSERS_AMI_ID);
        awsInstancesConfig.addProperty("instanceType", instanceType);
        awsInstancesConfig.addProperty("keyName", keyName);

        awsInstancesConfig.addProperty("numInstances", numInstances);
        JsonArray securityGroupsElement = parser.parse(securityGroups).getAsJsonArray();
        awsInstancesConfig.add("securityGroups", securityGroupsElement);

        JsonArray tagSpecificationsElement = parser.parse(tagSpecifications).getAsJsonArray();
        awsInstancesConfig.add("tagSpecifications", tagSpecificationsElement);
        AWS_CONFIG.add("awsInstancesConfig", awsInstancesConfig);

        log.info("AWS Config: {}", AWS_CONFIG);

    }

    @Override
    public Browser getBrowser(BrowserProperties properties) {
        Browser browser = null;
        DesiredCapabilities capabilities;

        ChromeOptions options = ChromeBrowser.generateFakeVideoChromeOptions("/opt/openvidu/fakevideo.y4m",
                "/opt/openvidu/fakeaudio.wav");
        options.addArguments("--window-size=1920,1200", "--ignore-certificate-errors");
        capabilities = DesiredCapabilities.chrome();
        capabilities.setAcceptInsecureCerts(true);
        capabilities.setCapability(ChromeOptions.CAPABILITY, options);
        capabilities.setCapability(CapabilityType.ACCEPT_SSL_CERTS, true);
        capabilities.setCapability(CapabilityType.ACCEPT_INSECURE_CERTS, true);
        // FOR REMOTE
        capabilities.setCapability("elastestTimeout", 3600);
        // AWS capabilities for browsers
        setUpAWSConfig();
        ObjectMapper mapper = new ObjectMapper();
        Map<String, String> awsConfigMap = null;
        try {
            awsConfigMap = mapper.readValue(AWS_CONFIG.toString(), Map.class);
        } catch (IOException e) {
            log.error("Error parsing AWS Config: "+e.getMessage());
        }finally{
            capabilities.setCapability("awsConfig", awsConfigMap);
        }

		WebDriver driver = this.getWebDriver(capabilities);
		browser = new ChromeBrowser(properties, driver);
		log.info("Using EUS Chrome web driver (AWS)");

		return browser;
	}

	@Override
	public List<Browser> getBrowsers(List<BrowserProperties> properties) {
		List<Browser> browsers = new ArrayList<>();
		Iterator<BrowserProperties> iterator = properties.iterator();

		while (iterator.hasNext()) {
			BrowserProperties props = iterator.next();
			ChromeBrowser chromeBrowser = (ChromeBrowser) this.getBrowser(props);
			browsers.add(chromeBrowser);
		}
		return browsers;
	}

	private WebDriver getWebDriver(DesiredCapabilities caps){
        WebDriver driver = null;
        String browserVersion = System.getProperty("browserVersion");
        if (browserVersion != null) {
            log.info("Browser Version: {}", browserVersion);
            caps.setVersion(browserVersion);
        }
		caps.setCapability("testName", "loadTest");
		try {
			driver = new RemoteWebDriver(new URL(EUS_URL), caps);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
        return driver;
    }

    @Override
    public void terminateInstances() {
        // Do nothing
		log.debug("EUSRemoteBrowserProvider use EUS. EUS terminate automatically all browser instances in AWS.");
    }

}
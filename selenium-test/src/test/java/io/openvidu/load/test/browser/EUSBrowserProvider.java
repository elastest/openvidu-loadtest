/*
 * (C) Copyright 2017-2018 OpenVidu (https://openvidu.io/)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package io.openvidu.load.test.browser;

import static java.lang.invoke.MethodHandles.lookup;
import static org.slf4j.LoggerFactory.getLogger;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.openqa.selenium.remote.RemoteWebDriver;

/**
 * Manages local browsers (web driver and browser in the same host as this test)
 *
 * @author Pablo Fuente (pablofuenteperez@gmail.com)
 */
public class EUSBrowserProvider implements BrowserProvider {

	final static Logger log = getLogger(lookup().lookupClass());

	protected static final Logger logger = LoggerFactory.getLogger(EUSBrowserProvider.class);

	protected static final String CHROME = "chrome";
	protected static final String FIREFOX = "firefox";

	protected static String browserType = CHROME;
	protected static String browserVersion;
	protected static String eusURL;

	public EUSBrowserProvider() {

        browserType = System.getProperty("browser");
        logger.info("Browser Type: {}", browserType);
        eusURL = System.getenv("ET_EUS_API");

	}

	@Override
	public Browser getBrowser(BrowserProperties properties) {
		Browser browser = null;
		DesiredCapabilities capabilities;

		ChromeOptions options = ChromeBrowser.generateFakeVideoChromeOptions("/opt/openvidu/fakevideo.y4m",
					"/opt/openvidu/fakeaudio.wav");
		options.addArguments("--window-size=1920,1200","--ignore-certificate-errors");
		capabilities = DesiredCapabilities.chrome();
		capabilities.setAcceptInsecureCerts(true);
		capabilities.setCapability(ChromeOptions.CAPABILITY, options);
		capabilities.setCapability(CapabilityType.ACCEPT_SSL_CERTS, true);
		capabilities.setCapability(CapabilityType.ACCEPT_INSECURE_CERTS, true);
		WebDriver driver = this.getWebDriver(capabilities);
		browser = new ChromeBrowser(properties, driver);
		log.info("Using EUS Chrome web driver");

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
			log.info("Using EUS Chrome web drivers (get multiple browsers)");
		}
		return browsers;
	}

	private WebDriver getWebDriver(DesiredCapabilities caps){
        WebDriver driver = null;
        browserVersion = System.getProperty("browserVersion");
		if (browserVersion != null) {
			logger.info("Browser Version: {}", browserVersion);
			caps.setVersion(browserVersion);
		}

		caps.setCapability("testName", "loadTest");
		try {
			driver = new RemoteWebDriver(new URL(eusURL), caps);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
        return driver;
    }

	@Override
	public void terminateInstances() {
		// Do nothing
		log.debug("EUSBrowserProvider does not terminate any instance");
	}

}

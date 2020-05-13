package io.mosip.orchestrator.report;

import org.apache.log4j.Logger;


/**
 * Reporter class act as util for additional report class or listeners
 * 
 * @author Vignesh
 *
 */
public class Reporter {

	private static final Logger REPORTLOG = Logger.getLogger(Reporter.class);

	public static String getAppDepolymentVersion() {
		/*MavenXpp3Reader reader = new MavenXpp3Reader();
		Model model = null;
		try {
			model = reader.read(new FileReader(MosipTestRunner.getGlobalResourcePath()+"/version.txt"));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			REPORTLOG.error("Exception in tagging the build number" + e.getMessage());
		}*/
		return "0.12.18";
	}
	
	public static String getAppEnvironment() {
		return System.getProperty("env.user");
	}

}

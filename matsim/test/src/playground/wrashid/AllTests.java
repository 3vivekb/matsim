package playground.wrashid;


import playground.wrashid.PHEV.Utility.EnergyConsumptionSamplesTest;
import playground.wrashid.deqsim.TestPDESStarter2_EquilPopulationPlans1Modified1;
import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTests {

	public static Test suite() {
		TestSuite suite = new TestSuite(
				"Test for playground.wrashid");
		suite.addTest(playground.wrashid.deqsim.AllPDES2Tests.suite());
		suite.addTest(playground.wrashid.PHEV.Utility.AllTests.suite());
		
		return suite;
	}
}

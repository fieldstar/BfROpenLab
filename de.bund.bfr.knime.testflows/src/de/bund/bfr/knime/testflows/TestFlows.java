package de.bund.bfr.knime.testflows;

import java.io.File;
import java.io.IOException;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.workflow.UnsupportedWorkflowVersionException;
import org.knime.core.util.LockFailedException;
import org.knime.testing.core.TestrunConfiguration;

import nl.esciencecenter.e3dchem.knime.testing.TestFlowRunner;

public class TestFlows {

	@Rule
	public ErrorCollector collector = new ErrorCollector();
	private TestFlowRunner runner;
	private String path;

	@Before
	public void setUp() {
		TestrunConfiguration runConfiguration = new TestrunConfiguration();

		runConfiguration.setCheckLogMessages(false);
		runConfiguration.setLoadSaveLoad(false);
		runner = new TestFlowRunner(collector, runConfiguration);
		path = TestFlows.class.getProtectionDomain().getCodeSource().getLocation().getPath();
	}

	@Test
	public void testAddressCreator() throws IOException, InvalidSettingsException, CanceledExecutionException,
			UnsupportedWorkflowVersionException, LockFailedException, InterruptedException {
		runner.runTestWorkflow(new File(path + "workflows/AddressCreator_Test"));
	}

	@Test
	public void testDiffFitting() throws IOException, InvalidSettingsException, CanceledExecutionException,
			UnsupportedWorkflowVersionException, LockFailedException, InterruptedException {
		runner.runTestWorkflow(new File(path + "workflows/DiffFitting_Test"));
	}

	@Test
	public void testFittingLOD() throws IOException, InvalidSettingsException, CanceledExecutionException,
			UnsupportedWorkflowVersionException, LockFailedException, InterruptedException {
		runner.runTestWorkflow(new File(path + "workflows/Fitting_LOD_Test"));
	}

	@Test
	public void testFitting() throws IOException, InvalidSettingsException, CanceledExecutionException,
			UnsupportedWorkflowVersionException, LockFailedException, InterruptedException {
		runner.runTestWorkflow(new File(path + "workflows/Fitting_Test"));
	}

	@Test
	public void testGeocoding() throws IOException, InvalidSettingsException, CanceledExecutionException,
			UnsupportedWorkflowVersionException, LockFailedException, InterruptedException {
		runner.runTestWorkflow(new File(path + "workflows/Geocoding_Test"));
	}

	@Test
	public void testGISCluster() throws IOException, InvalidSettingsException, CanceledExecutionException,
			UnsupportedWorkflowVersionException, LockFailedException, InterruptedException {
		runner.runTestWorkflow(new File(path + "workflows/GISCluster_Test"));
	}

	@Test
	public void testShapefileReader() throws IOException, InvalidSettingsException, CanceledExecutionException,
			UnsupportedWorkflowVersionException, LockFailedException, InterruptedException {
		runner.runTestWorkflow(new File(path + "workflows/ShapefileReader_Test"));
	}

	@Test
	public void testSupplyChainReader() throws IOException, InvalidSettingsException, CanceledExecutionException,
			UnsupportedWorkflowVersionException, LockFailedException, InterruptedException {
		runner.runTestWorkflow(new File(path + "workflows/SupplyChainReader_Test"));
	}

	@Test
	public void testSupplyChainReaderWithExtrafields() throws IOException, InvalidSettingsException,
			CanceledExecutionException, UnsupportedWorkflowVersionException, LockFailedException, InterruptedException {
		runner.runTestWorkflow(new File(path + "workflows/SupplyChainReader_Test_With_Extrafields"));
	}

	@Test
	public void testSupplyChainReaderLotBasedWithExtrafields() throws IOException, InvalidSettingsException,
			CanceledExecutionException, UnsupportedWorkflowVersionException, LockFailedException, InterruptedException {
		runner.runTestWorkflow(new File(path + "workflows/SupplyChainReader_Test_LotBased_With_Extrafields"));
	}

	@Test
	public void testSupplyChainReaderWithoutSerials() throws IOException, InvalidSettingsException,
			CanceledExecutionException, UnsupportedWorkflowVersionException, LockFailedException, InterruptedException {
		runner.runTestWorkflow(new File(path + "workflows/SupplyChainReader_Test_Without_Serials"));
	}

	@Test
	public void testTracingWithCCOnDeliveries() throws IOException, InvalidSettingsException,
			CanceledExecutionException, UnsupportedWorkflowVersionException, LockFailedException, InterruptedException {
		runner.runTestWorkflow(new File(path + "workflows/Tracing_Test_with_CC_on_Deliveries"));
	}

	@Test
	public void testTracingWithCollapseAndDifferentWeights() throws IOException, InvalidSettingsException,
			CanceledExecutionException, UnsupportedWorkflowVersionException, LockFailedException, InterruptedException {
		runner.runTestWorkflow(new File(path + "workflows/Tracing_Test_with_Collapse_and_Different_Weights"));
	}

	@Test
	public void testTracingWithJoinDeliveries() throws IOException, InvalidSettingsException,
			CanceledExecutionException, UnsupportedWorkflowVersionException, LockFailedException, InterruptedException {
		runner.runTestWorkflow(new File(path + "workflows/Tracing_Test_with_Join_Deliveries"));
	}

	@Test
	public void testTracingWithTemporalOrder() throws IOException, InvalidSettingsException, CanceledExecutionException,
			UnsupportedWorkflowVersionException, LockFailedException, InterruptedException {
		runner.runTestWorkflow(new File(path + "workflows/Tracing_Test_with_Temporal_Order"));
	}

	@Test
	public void testTracingWithoutTemporalOrder() throws IOException, InvalidSettingsException,
			CanceledExecutionException, UnsupportedWorkflowVersionException, LockFailedException, InterruptedException {
		runner.runTestWorkflow(new File(path + "workflows/Tracing_Test_without_Temporal_Order"));
	}

	@Test
	public void testTracingView() throws IOException, InvalidSettingsException, CanceledExecutionException,
			UnsupportedWorkflowVersionException, LockFailedException, InterruptedException {
		runner.runTestWorkflow(new File(path + "workflows/TracingView_Test"));
	}
}

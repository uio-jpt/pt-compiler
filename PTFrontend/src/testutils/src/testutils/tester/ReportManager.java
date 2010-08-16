package testutils.tester;

import java.util.LinkedList;
import java.util.List;

public class ReportManager {

	private List<SemanticReport> passedTests;
	private List<SemanticReport> failedTests;
	private List<SemanticReport> withPackages;
	private List<SimpleReport> compileReports;
	private List<SimpleReport> compileReportErrors;

	public ReportManager() {
		passedTests = new LinkedList<SemanticReport>();
		failedTests = new LinkedList<SemanticReport>();
		withPackages = new LinkedList<SemanticReport>();
		compileReports = new LinkedList<SimpleReport>();
		compileReportErrors = new LinkedList<SimpleReport>();
	}

	public void addReport(SemanticReport report) {
		if (report.hasPassed()) {
			passedTests.add(report);
			if (report.shouldBeOk && report.containsPTPackage)
				withPackages.add(report);
		} else
			failedTests.add(report);

	}

	public int getNumberOfPassedTests() {
		return passedTests.size() + compileReports.size();
	}

	public int getNumberOfTestsTotal() {
		return getNumberOfPassedTests() + getNumberOfFailedTests();
	}

	public int getNumberOfFailedTests() {
		return failedTests.size() + compileReportErrors.size();
	}

	public boolean allPassed() {
		return getNumberOfFailedTests() == 0;
	}

	public List<SemanticReport> getFailedReports() {
		return failedTests;
	}

	public List<SemanticReport> getCompilableTests() {
		return withPackages;
	}
	
	public List<SimpleReport> getFailedCompilations() {
		return compileReportErrors;
	}

	public void addReport(SimpleReport report) {
		if (report.actual)
			compileReports.add(report);
		else
			compileReportErrors.add(report);
	}
}

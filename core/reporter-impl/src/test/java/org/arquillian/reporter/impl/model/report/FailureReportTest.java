package org.arquillian.reporter.impl.model.report;

import java.util.List;

import org.arquillian.reporter.api.model.report.FailureReport;
import org.arquillian.reporter.api.model.report.Report;
import org.arquillian.reporter.impl.utils.Utils;
import org.junit.Test;

import static org.arquillian.reporter.impl.asserts.ReportAssert.assertThatReport;
import static org.arquillian.reporter.impl.utils.dummy.DummyStringKeys.FAILURE_REPORT_NAME;

/**
 * @author <a href="mailto:mjobanek@redhat.com">Matous Jobanek</a>
 */
public class FailureReportTest {

    @Test
    public void testAddNewReportToFailureReport() throws InstantiationException, IllegalAccessException {
        FailureReport failureReport = Utils.prepareReport(FailureReport.class, FAILURE_REPORT_NAME, 1, 5);

        // add a normal report - should be added into List of subReports
        Report basicReport = Utils.prepareReport(Report.class, "report", 5, 10);
        failureReport.addNewReport(basicReport);

        // verify
        assertThatReport(failureReport)
            .hasSubReportsEndingWith(basicReport)
            .hasNumberOfSubreports(5);

        // add failure report - should be added into List of subReports
        FailureReport failureToAdd = Utils.prepareReport(FailureReport.class, FAILURE_REPORT_NAME, 5, 10);
        failureReport.addNewReport(failureToAdd);

        // verify
        assertThatReport(failureReport)
            .hasName(FAILURE_REPORT_NAME)
            .hasGeneratedSubreportsAndEntries(1, 5)
            .hasNumberOfSubreports(6)
            .hasNumberOfEntries(4)
            .hasSubReportsEndingWith(basicReport, failureToAdd);
    }

    @Test
    public void testMergeReports() throws InstantiationException, IllegalAccessException {
        FailureReport mainFailureReport = Utils.prepareReport(FailureReport.class, FAILURE_REPORT_NAME, 1, 5);
        List<FailureReport> firstFailure = Utils.prepareSetOfReports(FailureReport.class, 5, "first", 1, 5);
        mainFailureReport.getSubReports().addAll(firstFailure);

        FailureReport failureToMerge = Utils.prepareReport(FailureReport.class, "to merge", 5, 10);
        List<FailureReport> secondFailure = Utils.prepareSetOfReports(FailureReport.class, 5, "second", 5, 10);
        failureToMerge.getSubReports().addAll(secondFailure);

        //merge
        mainFailureReport.merge(failureToMerge);

        // the report that has been merged is still same
        assertThatReport(failureToMerge)
            .hasSubReportsEndingWith(secondFailure.stream().toArray(FailureReport[]::new))
            .hasName("to merge")
            .hasGeneratedSubreportsAndEntries(5, 10)
            .hasNumberOfSubreports(10)
            .hasNumberOfEntries(5);

        // verify that the main report should contain all information
        assertThatReport(mainFailureReport)
            .hassSubReportsContaining(firstFailure.stream().toArray(FailureReport[]::new))
            .hasSubReportsEndingWith(secondFailure.stream().toArray(FailureReport[]::new))
            .hasName(FAILURE_REPORT_NAME)
            .hasGeneratedSubreportsAndEntries(1, 10)
            .hasNumberOfSubreports(19)
            .hasNumberOfEntries(9);
    }
}

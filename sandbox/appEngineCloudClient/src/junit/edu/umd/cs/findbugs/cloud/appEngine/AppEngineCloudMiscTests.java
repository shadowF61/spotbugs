package edu.umd.cs.findbugs.cloud.appEngine;

import edu.umd.cs.findbugs.BugDesignation;
import edu.umd.cs.findbugs.BugInstance;
import edu.umd.cs.findbugs.PropertyBundle;
import edu.umd.cs.findbugs.SortedBugCollection;
import edu.umd.cs.findbugs.cloud.CloudPlugin;
import edu.umd.cs.findbugs.cloud.appEngine.protobuf.AppEngineProtoUtil;
import edu.umd.cs.findbugs.cloud.appEngine.protobuf.ProtoClasses.Evaluation;
import edu.umd.cs.findbugs.cloud.appEngine.protobuf.ProtoClasses.Issue;
import edu.umd.cs.findbugs.cloud.username.AppEngineNameLookup;
import junit.framework.TestCase;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Executor;

import static edu.umd.cs.findbugs.cloud.appEngine.BugFilingHelper.processJiraDashboardUrl;
import static edu.umd.cs.findbugs.cloud.appEngine.protobuf.AppEngineProtoUtil.normalizeHash;
import static org.mockito.Mockito.when;

public class AppEngineCloudMiscTests extends AbstractAppEngineCloudTest {

    public void testGetLatestDesignationFromEachUser() throws Exception {
        MockAppEngineCloudClient cloud = createAppEngineCloudClient();
        AppEngineCloudNetworkClient spyNetworkClient = cloud.createSpyNetworkClient();
        when(spyNetworkClient.getIssueByHash("fad2")).thenReturn(Issue.newBuilder().addAllEvaluations(Arrays.asList(
                Evaluation.newBuilder().setWho("user1").setDesignation("MUST_FIX").setWhen(SAMPLE_DATE+100).build(),
                Evaluation.newBuilder().setWho("user2").setDesignation("I_WILL_FIX").setWhen(SAMPLE_DATE+200).build(),
                Evaluation.newBuilder().setWho("user1").setDesignation("NOT_A_BUG").setWhen(SAMPLE_DATE+300).build()
                )).build());
        List<BugDesignation> designations = newList(cloud.getLatestDesignationFromEachUser(foundIssue));
        assertEquals(2, designations.size());
        assertEquals("user2", designations.get(0).getUser());
        assertEquals("user1", designations.get(1).getUser());
        assertEquals("NOT_A_BUG", designations.get(1).getDesignationKey());
    }

    public void testJiraDashboardUrlProcessor() {
        assertEquals("http://jira.atlassian.com", processJiraDashboardUrl("  jira.atlassian.com    "));
        assertEquals("http://jira.atlassian.com", processJiraDashboardUrl("jira.atlassian.com"));
        assertEquals("http://jira.atlassian.com", processJiraDashboardUrl("http://jira.atlassian.com"));
        assertEquals("http://jira.atlassian.com", processJiraDashboardUrl("https://jira.atlassian.com"));
        assertEquals("http://jira.atlassian.com", processJiraDashboardUrl("https://jira.atlassian.com/secure"));
        assertEquals("http://jira.atlassian.com", processJiraDashboardUrl("https://jira.atlassian.com/secure/"));
        assertEquals("http://jira.atlassian.com", processJiraDashboardUrl("https://jira.atlassian.com/secure/Dashboard.jspa"));
        assertEquals("http://jira.atlassian.com", processJiraDashboardUrl("https://jira.atlassian.com/secure/Dashboard.jspa;sessionId=blah"));
        assertEquals("http://jira.atlassian.com", processJiraDashboardUrl("https://jira.atlassian.com/secure/Dashboard.jspa?blah"));
    }

	public static void testEncodeDecodeHash() {
		checkHashEncodeRoundtrip("9e107d9d372bb6826bd81d3542a419d6");
		checkHashEncodeRoundtrip("83ab7e45f39c7a7a84e5e63b95beeb5");
		checkHashEncodeRoundtrip("1fe8e2bc5f1cceae0bf5954e7b5e84ac");
		checkHashEncodeRoundtrip("6977735a4a0f8036778b223cd9f9c1f0");
		checkHashEncodeRoundtrip("9ba282b1a7b049fa3c5b068941c25977");
		checkHashEncodeRoundtrip("6606a054edd331799ed567b4efd539a6");
		checkHashEncodeRoundtrip("6f2130edc682a1cb0db9b709179593d9");
		checkHashEncodeRoundtrip("ffffffffffffffffffffffffffffffff");
		checkHashEncodeRoundtrip("0");
		checkHashEncodeRoundtrip("1");
	}

	public void testNormalizeHash() {
		assertEquals("0", normalizeHash("0"));
		assertEquals("0", normalizeHash("000000000"));
		assertEquals("1", normalizeHash("000000000000001"));
		assertEquals("fffffffffffffffffffffffffffffff", normalizeHash("0fffffffffffffffffffffffffffffff"));
	}

	public void testNormalizeHashMakesLowercase() {
		assertEquals("f", normalizeHash("F"));
		assertEquals("fffffffffffffffffffffffffffffff", normalizeHash("0FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF"));
		assertEquals("ffffffffffffffffffffffffffffffff", normalizeHash("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF"));
	}

    // =================================== end of tests ===========================================


	private static void checkHashEncodeRoundtrip(String hash) {
		assertEquals(hash, AppEngineProtoUtil.decodeHash(AppEngineProtoUtil.encodeHash(hash)));
	}
}
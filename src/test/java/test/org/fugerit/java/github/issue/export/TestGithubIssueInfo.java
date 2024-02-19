package test.org.fugerit.java.github.issue.export;

import java.io.IOException;
import java.util.Properties;

import org.fugerit.java.github.issue.export.GithubIssueInfo;
import org.junit.Assert;
import org.junit.Test;

public class TestGithubIssueInfo {

	@Test
	public void testCache() throws IOException {
		GithubIssueInfo info = new GithubIssueInfo( new Properties() );
		String issueId = "1";
		String field = "assigne";
		String value = "oneuser";
		info.addCacheEntry(issueId, field, value);
		Assert.assertEquals( value , info.getCacheEntry(issueId, field));
	}
	
}

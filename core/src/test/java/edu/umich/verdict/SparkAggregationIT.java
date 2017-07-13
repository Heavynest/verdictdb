package edu.umich.verdict;

import java.util.ArrayList;
import java.util.List;

import org.apache.spark.sql.DataFrame;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SQLContext;
import org.junit.BeforeClass;
import org.junit.runner.JUnitCore;
import org.junit.runner.Request;
import org.junit.runner.Result;
import org.junit.Test;
import org.junit.runner.notification.Failure;



import edu.umich.verdict.exceptions.VerdictException;

public class SparkAggregationIT extends AggregationIT {
	
	static SQLContext sqlContext;
	
	static VerdictSparkContext vc;
	
	public static void setSqlContext(SQLContext sqlContext) {
		SparkAggregationIT.sqlContext = sqlContext;
	}
	
	public static void run(SQLContext sqlContext) {
		setSqlContext(sqlContext);
		Request request = Request.method(edu.umich.verdict.SparkAggregationIT.class, "simpleAvg");
		JUnitCore jcore = new JUnitCore();
		Result result = jcore.run(request);

		if (result.getFailureCount() > 0) {
			List<Failure> failures = result.getFailures();
			for (Failure f : failures) {
				System.out.println(f.getTrace());
			}
		}
	}
	
	@BeforeClass
	public static void connect() throws VerdictException {
		vc = new VerdictSparkContext(sqlContext);
	}

	protected List<List<Object>> collectResult(DataFrame df) {
		List<List<Object>> result = new ArrayList<List<Object>>();
		List<Row> rows = df.collectAsList();
		for (Row row : rows) {
			int colCount = row.size();
			List<Object> arow = new ArrayList<Object>();
			for (int i = 0; i < colCount; i++) {
				arow.add(row.get(i));
			}
			result.add(arow);
		}
		return result;
	}
	
	@Override
	protected void testSimpleAggQuery(String sql) throws VerdictException {
		List<List<Object>> expected = collectResult(sqlContext.sql(sql));
		List<List<Object>> actual = collectResult(vc.executeSparkQuery(sql));
		printTestCase(sql, expected, actual);
		assertColsSimilar(expected, actual, 1, error);
	}
	
	@Test
	public void simpleAvg() throws VerdictException {
		String sql = "select avg(days_since_prior) from instacart1g.orders";
		testSimpleAggQuery(sql);
	}
}

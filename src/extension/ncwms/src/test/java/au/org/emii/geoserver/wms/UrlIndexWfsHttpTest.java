package au.org.emii.geoserver.wms;

import org.apache.commons.io.IOUtils;
import org.easymock.EasyMock;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static junit.framework.TestCase.assertEquals;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ URL.class })
public class UrlIndexWfsHttpTest {
    private UrlIndexWfsHttp mockWfsQuery(String query, String response) throws Exception {
        UrlIndexWfsHttp urlIndexWfsHttpPartialMock = EasyMock
                .createMockBuilder(UrlIndexWfsHttp.class)
                .addMockedMethod("wfsQuery")
                .createMock();

        EasyMock.expect(urlIndexWfsHttpPartialMock.wfsQuery(query))
                .andReturn(IOUtils.toInputStream(response, "UTF-8"));

        return urlIndexWfsHttpPartialMock;
    }

    @Test
    public void testGetUrlForTimestampWithUnspecifiedTime() throws Exception {
        String csvString = "FID,time\n";
        csvString += "acorn_hourly_avg_rot_qc_timeseries_url.fid-607eb861_1502c191468_-7d5a,/mnt/imos-t3/IMOS/opendap/ACORN/gridded_1h-avg-current-map_QC/ROT/2014/06/06/IMOS_ACORN_V_20140606T003000Z_ROT_FV01_1-hour-avg.nc\n";

        Map<String, String> urlSubstitutions = new HashMap<>();
        urlSubstitutions.put("/mnt/imos-t3/IMOS/opendap/", "http://some_url/");
        Ncwms.urlSubstitutions = urlSubstitutions;

        String expected = "/mnt/imos-t3/IMOS/opendap/ACORN/gridded_1h-avg-current-map_QC/ROT/2014/06/06/IMOS_ACORN_V_20140606T003000Z_ROT_FV01_1-hour-avg.nc";

        String expectedQuery =
                "typeName=acorn_hourly_avg_rot_qc_timeseries_url&SERVICE=WFS&outputFormat=csv" +
                        "&REQUEST=GetFeature&VERSION=1.0.0&PROPERTYNAME=file_url" +
                        "&maxFeatures=1&sortBy=time+D";

        UrlIndexWfsHttp urlIndexWFSHttpMockWfsQuery = mockWfsQuery(expectedQuery, csvString);
        EasyMock.replay(urlIndexWFSHttpMockWfsQuery);
        assertEquals(expected, urlIndexWFSHttpMockWfsQuery.getUrlForTimestamp(
                new LayerDescriptor("acorn_hourly_avg_rot_qc_timeseries_url/variable"), null));

        EasyMock.verify(urlIndexWFSHttpMockWfsQuery);
    }

    @Test
    public void testGetUrlForTimestampWithSpecifiedTime() throws Exception {
        String csvString = "FID,time\n";
        csvString += "acorn_hourly_avg_rot_qc_timeseries_url.fid-607eb861_1502c191468_-7d5a,/mnt/imos-t3/IMOS/opendap/ACORN/gridded_1h-avg-current-map_QC/ROT/2014/06/06/IMOS_ACORN_V_20140606T003000Z_ROT_FV01_1-hour-avg.nc\n";

        String time = "2014-06-06T00:30:00Z";

        String expected = "/mnt/imos-t3/IMOS/opendap/ACORN/gridded_1h-avg-current-map_QC/ROT/2014/06/06/IMOS_ACORN_V_20140606T003000Z_ROT_FV01_1-hour-avg.nc";
        String expectedCqlFilter = URLEncoder.encode(
                String.format("time = %s", time),
                StandardCharsets.UTF_8.name());

        String expectedQuery =
            "typeName=acorn_hourly_avg_rot_qc_timeseries_url&SERVICE=WFS&outputFormat=csv" +
                "&REQUEST=GetFeature&VERSION=1.0.0&PROPERTYNAME=file_url" +
                "&CQL_FILTER=" + expectedCqlFilter + "&maxFeatures=1";

        UrlIndexWfsHttp urlIndexWFSHttpMockWfsQuery = mockWfsQuery(expectedQuery, csvString);
        EasyMock.replay(urlIndexWFSHttpMockWfsQuery);
        assertEquals(expected, urlIndexWFSHttpMockWfsQuery.getUrlForTimestamp(
                new LayerDescriptor("acorn_hourly_avg_rot_qc_timeseries_url/variable"), "2014-06-06T00:30:00Z"));

        EasyMock.verify(urlIndexWFSHttpMockWfsQuery);
    }

    @Test
    public void testGetTimesForDay() throws Exception {
        String csvString = "FID,time\n";
        csvString += "acorn_hourly_avg_rot_qc_timeseries_url.fid-607eb861_1502bdb829d_-1c97,2010-03-10T08:30:00\n";
        csvString += "acorn_hourly_avg_rot_qc_timeseries_url.fid-607eb861_1502bdb829d_-1c96,2010-03-10T09:30:00\n";
        csvString += "acorn_hourly_avg_rot_qc_timeseries_url.fid-607eb861_1502bdb829d_-1c95,2010-03-10T10:30:00\n";

        List<String> expected = new ArrayList<String>() {{
            add("08:30:00.000Z");
            add("09:30:00.000Z");
            add("10:30:00.000Z");
        }};

        String dayStart = "2010-03-10T00:00:00.000Z";
        String dayEnd =  "2010-03-11T00:00:00.000Z";
        String expectedCqlFilter = URLEncoder.encode(
                String.format("time >= %s AND time < %s", dayStart, dayEnd),
                StandardCharsets.UTF_8.name());

        String expectedQuery =
                "typeName=acorn_hourly_avg_rot_qc_timeseries_url&SERVICE=WFS&outputFormat=csv" +
                        "&REQUEST=GetFeature&VERSION=1.0.0&PROPERTYNAME=time" +
                        "&CQL_FILTER=" + expectedCqlFilter;

        UrlIndexWfsHttp urlIndexWFSHttpMockWfsQuery = mockWfsQuery(expectedQuery, csvString);
        EasyMock.replay(urlIndexWFSHttpMockWfsQuery);
        assertEquals(expected.toString(), urlIndexWFSHttpMockWfsQuery.getTimesForDay(
                new LayerDescriptor("acorn_hourly_avg_rot_qc_timeseries_url/variable"), dayStart).toString());

        EasyMock.verify(urlIndexWFSHttpMockWfsQuery);
    }

    @Test
    public void testGetUniqueDates() throws Exception {
        String csvString = "FID,time\n";
        csvString += "acorn_hourly_avg_rot_qc_timeseries_url.fid-607eb861_1502bdb829d_-1c99,2010-02-23T06:30:00\n";
        csvString += "acorn_hourly_avg_rot_qc_timeseries_url.fid-607eb861_1502bdb829d_-1c98,2010-02-23T08:30:00\n";
        csvString += "acorn_hourly_avg_rot_qc_timeseries_url.fid-607eb861_1502bdb829d_-1c97,2010-03-09T08:30:00\n";
        csvString += "acorn_hourly_avg_rot_qc_timeseries_url.fid-607eb861_1502bdb829d_-1c96,2010-03-10T09:30:00\n";
        csvString += "acorn_hourly_avg_rot_qc_timeseries_url.fid-607eb861_1502bdb829d_-1c95,2010-03-10T10:30:00\n";
        csvString += "acorn_hourly_avg_rot_qc_timeseries_url.fid-607eb861_1502bdb829d_-1c94,2010-03-11T15:30:00\n";
        csvString += "acorn_hourly_avg_rot_qc_timeseries_url.fid-607eb861_1502bdb829d_-1c93,2011-01-22T18:30:00\n";
        csvString += "acorn_hourly_avg_rot_qc_timeseries_url.fid-607eb861_1502bdb829d_-1c92,2011-01-22T20:30:00\n";

        Map<String, Map<String, Set<String>>> expected =
                new HashMap<>();

        expected.put("2010", new HashMap<String, Set<String>>());
        expected.get("2010").put("1", new HashSet<String>());
        expected.get("2010").get("1").add("23");
        expected.get("2010").put("2", new HashSet<String>());
        expected.get("2010").get("2").add("9");
        expected.get("2010").get("2").add("10");
        expected.get("2010").get("2").add("11");

        expected.put("2011", new HashMap<String, Set<String>>());
        expected.get("2011").put("0", new HashSet<String>());
        expected.get("2011").get("0").add("22");

        String expectedQuery =
                "typeName=acorn_hourly_avg_rot_qc_timeseries_url&SERVICE=WFS&outputFormat=csv" +
                        "&REQUEST=GetFeature&VERSION=1.0.0&PROPERTYNAME=time";

        UrlIndexWfsHttp urlIndexWFSHttpMockWfsQuery = mockWfsQuery(expectedQuery, csvString);
        EasyMock.replay(urlIndexWFSHttpMockWfsQuery);
        assertEquals(expected.toString(), urlIndexWFSHttpMockWfsQuery.getUniqueDates(
                new LayerDescriptor("acorn_hourly_avg_rot_qc_timeseries_url/variable")).toString());

        EasyMock.verify(urlIndexWFSHttpMockWfsQuery);
    }
}

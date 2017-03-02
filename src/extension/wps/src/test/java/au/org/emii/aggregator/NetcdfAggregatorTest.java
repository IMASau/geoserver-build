package au.org.emii.aggregator;

import static au.org.emii.test.util.Resource.resourcePath;
import static au.org.emii.test.util.Assert.assertNetcdfFilesEqual;

import au.org.emii.aggregator.exception.AggregationException;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import ucar.nc2.time.CalendarDate;
import ucar.nc2.time.CalendarDateRange;
import ucar.unidata.geoloc.LatLonPointImmutable;
import ucar.unidata.geoloc.LatLonRect;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * NetcdfAggregator tests for different types of collections
 */
public class NetcdfAggregatorTest {
    private Path outputFile;

    @Before
    public void createOutputFile() throws IOException {
        outputFile = Files.createTempFile("output", "nc");
    }

    @Test
    public void testSpatialSubsetSingleFile() throws IOException, AggregationException {
        LatLonRect bbox = new LatLonRect(new LatLonPointImmutable(-33.0, 113.9), new LatLonPointImmutable(-32.0, 114.9));

        try (NetcdfAggregator netcdfAggregator = new NetcdfAggregator(
                outputFile, null, bbox, null, null, null, null
        )) {
            netcdfAggregator.add(resourcePath("au/org/emii/aggregator/acorn-1.nc"));
        }

        assertNetcdfFilesEqual(resourcePath("au/org/emii/aggregator/single-expected.nc"), outputFile);
    }

    @Test
    public void testSpatialSubsetMultipleFile() throws IOException, AggregationException {
        LatLonRect bbox = new LatLonRect(new LatLonPointImmutable(-33.0, 113.9), new LatLonPointImmutable(-32.0, 114.9));

        try (NetcdfAggregator netcdfAggregator = new NetcdfAggregator(
                outputFile, null, bbox, null, null, null, null
        )) {
            netcdfAggregator.add(resourcePath("au/org/emii/aggregator/acorn-1.nc"));
            netcdfAggregator.add(resourcePath("au/org/emii/aggregator/acorn-2.nc"));
        }

        assertNetcdfFilesEqual(resourcePath("au/org/emii/aggregator/multiple-expected.nc"), outputFile);
    }

    @Test
    public void testSpatialSubsetMultipleFileUnpack() throws IOException, AggregationException {
        LatLonRect bbox = new LatLonRect(new LatLonPointImmutable(-30.68, 97.82), new LatLonPointImmutable(-30.64, 97.86));

        try (NetcdfAggregator netcdfAggregator = new NetcdfAggregator(
            outputFile, null, bbox, null, null, null, null
        )) {
            netcdfAggregator.add(resourcePath("au/org/emii/aggregator/srs-1.nc"));
            netcdfAggregator.add(resourcePath("au/org/emii/aggregator/srs-2.nc"));
        }

        assertNetcdfFilesEqual(resourcePath("au/org/emii/aggregator/unpack-expected.nc"), outputFile);
    }

    @Test(expected = AggregationException.class)
    public void testSpatialSubsetNoData() throws IOException, AggregationException {
        LatLonRect bbox = new LatLonRect(new LatLonPointImmutable(-20.0, 113.9), new LatLonPointImmutable(-18.0, 114.9));

        try (NetcdfAggregator netcdfAggregator = new NetcdfAggregator(
            outputFile, null, bbox, null, null, null, null
        )) {
            netcdfAggregator.add(resourcePath("au/org/emii/aggregator/acorn-1.nc"));
            netcdfAggregator.add(resourcePath("au/org/emii/aggregator/acorn-2.nc"));
        }

        assertNetcdfFilesEqual(resourcePath("au/org/emii/aggregator/multiple-expected.nc"), outputFile);
    }

    @Test
    public void testPointSubsetInBbox() throws IOException, AggregationException {
        LatLonRect bbox = new LatLonRect(new LatLonPointImmutable(-32.8, 114.0), new LatLonPointImmutable(-32.8, 114.0));

        try (NetcdfAggregator netcdfAggregator = new NetcdfAggregator(
            outputFile, null, bbox, null, null, null, null
        )) {
            netcdfAggregator.add(resourcePath("au/org/emii/aggregator/acorn-1.nc"));
            netcdfAggregator.add(resourcePath("au/org/emii/aggregator/acorn-2.nc"));
        }

        assertNetcdfFilesEqual(resourcePath("au/org/emii/aggregator/point-subset-inside.nc"), outputFile);
    }

    @Test
    public void testPointSubsetOutsideBbox() throws IOException, AggregationException {
        LatLonRect bbox = new LatLonRect(new LatLonPointImmutable(-20.0, 113.0), new LatLonPointImmutable(-20.0, 113.0));

        try (NetcdfAggregator netcdfAggregator = new NetcdfAggregator(
            outputFile, null, bbox, null, null, null, null
        )) {
            netcdfAggregator.add(resourcePath("au/org/emii/aggregator/acorn-1.nc"));
            netcdfAggregator.add(resourcePath("au/org/emii/aggregator/acorn-2.nc"));
        }

        assertNetcdfFilesEqual(resourcePath("au/org/emii/aggregator/point-subset-outside.nc"), outputFile);
    }

    @Test
    public void testTemporalSubset() throws IOException, AggregationException {
        CalendarDateRange dateRange = CalendarDateRange.of(
            CalendarDate.parseISOformat("gregorian", "2009-05-07"), CalendarDate.parseISOformat("gregorian", "2009-05-22"));

        try (NetcdfAggregator netcdfAggregator = new NetcdfAggregator(
            outputFile, null, null, null, dateRange, null, null
        )) {
            netcdfAggregator.add(resourcePath("au/org/emii/aggregator/cars.nc"));
        }

        assertNetcdfFilesEqual(resourcePath("au/org/emii/aggregator/temporal-expected.nc"), outputFile);
    }

    @Test
    public void testLatLonProjectionSubset() throws IOException, AggregationException {
        LatLonRect bbox = new LatLonRect(new LatLonPointImmutable(-31.0, 113.0), new LatLonPointImmutable(-30.0, 114.0));

        try (NetcdfAggregator netcdfAggregator = new NetcdfAggregator(
            outputFile, null, bbox, null, null, null, null
        )) {
            netcdfAggregator.add(resourcePath("au/org/emii/aggregator/projection-1.nc"));
            netcdfAggregator.add(resourcePath("au/org/emii/aggregator/projection-2.nc"));
        }

        assertNetcdfFilesEqual(resourcePath("au/org/emii/aggregator/projection-expected.nc"), outputFile);
    }

    @Test
    public void testLatLonProjectionPointSubsetWithin() throws IOException, AggregationException {
        LatLonRect bbox = new LatLonRect(new LatLonPointImmutable(-31.0, 113.0), new LatLonPointImmutable(-31.0, 113.0));

        try (NetcdfAggregator netcdfAggregator = new NetcdfAggregator(
            outputFile, null, bbox, null, null, null, null
        )) {
            netcdfAggregator.add(resourcePath("au/org/emii/aggregator/projection-1.nc"));
            netcdfAggregator.add(resourcePath("au/org/emii/aggregator/projection-2.nc"));
        }

        assertNetcdfFilesEqual(resourcePath("au/org/emii/aggregator/projection-point-within-expected.nc"), outputFile);
    }

    @Test
    public void testLatLonProjectionPointSubsetOutside() throws IOException, AggregationException {
        LatLonRect bbox = new LatLonRect(new LatLonPointImmutable(-10.0, 114.0), new LatLonPointImmutable(-10.0, 114.0));

        try (NetcdfAggregator netcdfAggregator = new NetcdfAggregator(
            outputFile, null, bbox, null, null, null, null
        )) {
            netcdfAggregator.add(resourcePath("au/org/emii/aggregator/projection-1.nc"));
            netcdfAggregator.add(resourcePath("au/org/emii/aggregator/projection-2.nc"));
        }

        assertNetcdfFilesEqual(resourcePath("au/org/emii/aggregator/projection-point-outside-expected.nc"), outputFile);
    }

    @Test(expected = AggregationException.class)
    public void testLatLonProjectionSpatialSubsetNoData() throws IOException, AggregationException {
        LatLonRect bbox = new LatLonRect(new LatLonPointImmutable(-10.0, 114.0), new LatLonPointImmutable(-12.0, 114.0));

        try (NetcdfAggregator netcdfAggregator = new NetcdfAggregator(
            outputFile, null, bbox, null, null, null, null
        )) {
            netcdfAggregator.add(resourcePath("au/org/emii/aggregator/projection-1.nc"));
            netcdfAggregator.add(resourcePath("au/org/emii/aggregator/projection-2.nc"));
        }

        assertNetcdfFilesEqual(resourcePath("au/org/emii/aggregator/projection-point-outside-expected.nc"), outputFile);
    }

    @After
    public void deleteOutputFile() throws IOException {
        Files.deleteIfExists(outputFile);
    }

}
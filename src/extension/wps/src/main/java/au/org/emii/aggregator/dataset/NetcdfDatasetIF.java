package au.org.emii.aggregator.dataset;

import au.org.emii.aggregator.coordsystem.LatLonCoords;
import au.org.emii.aggregator.coordsystem.TimeAxis;
import au.org.emii.aggregator.exception.AggregationException;
import au.org.emii.aggregator.variable.NetcdfVariable;
import ucar.ma2.Range;
import ucar.nc2.Attribute;
import ucar.nc2.Dimension;
import ucar.nc2.time.CalendarDateRange;
import ucar.unidata.geoloc.LatLonRect;

import java.io.IOException;
import java.util.List;

/**
 * Created by craigj on 17/02/17.
 */
public interface NetcdfDatasetIF {
    List<Attribute> getGlobalAttributes();

    List<Dimension> getDimensions();

    List<NetcdfVariable> getVariables();

    NetcdfVariable findVariable(String fullName);

    LatLonRect getBbox();

    TimeAxis getTimeAxis();

    boolean hasVerticalAxis();

    NetcdfVariable getVerticalAxis();

    LatLonCoords getLatLonCoords();

    NetcdfDatasetIF subset(CalendarDateRange timeRange, Range verticalSubset,
                           LatLonRect bbox) throws AggregationException;
}

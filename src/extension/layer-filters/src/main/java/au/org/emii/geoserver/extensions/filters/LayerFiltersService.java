/*
 * Copyright 2014 IMOS
 *
 * The AODN/IMOS Portal is distributed under the terms of the GNU General Public License
 *
 */

package au.org.emii.geoserver.extensions.filters;

import au.org.emii.geoserver.extensions.filters.layer.data.DataDirectory;
import au.org.emii.geoserver.extensions.filters.layer.data.Filter;
import au.org.emii.geoserver.extensions.filters.layer.data.FiltersDocument;
import au.org.emii.geoserver.extensions.filters.layer.data.io.FilterConfigurationFile;
import au.org.emii.geoserver.extensions.filters.layer.data.io.PossibleValuesReader;
import au.org.emii.geoserver.extensions.filters.layer.data.ValuesDocument;

import org.geoserver.catalog.Catalog;
import org.geoserver.catalog.DataStoreInfo;
import org.geoserver.catalog.LayerInfo;
import org.geoserver.platform.ServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.naming.NamingException;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.util.List;
import java.util.Set;

public class LayerFiltersService {

    private Catalog catalog;
    private List<String> uniqueValuesAllowedRegex = null;

    @Autowired
    private ServletContext context;

    public LayerFiltersService() {}

    public void setCatalog(Catalog catalog) {
        this.catalog = catalog;
    }

    public Catalog getCatalog() {
        return catalog;
    }

    public void setUniqueValuesAllowedRegex(List<String> uniqueValuesAllowedRegex) {
        this.uniqueValuesAllowedRegex = uniqueValuesAllowedRegex;
    }

    public List<String> getUniqueValuesAllowedRegex() {
        return uniqueValuesAllowedRegex;
    }

    private boolean uniqueValuesAllowed(String workspace, String layer, String propertyName) {
        if (uniqueValuesAllowedRegex == null)
            return true;

        String fullLayerName = String.format("%s:%s/%s", workspace, layer, propertyName);
        for (final String regex : getUniqueValuesAllowedRegex()) {
            if (fullLayerName.matches(regex)) {
                return true;
            }
        }
        return false;
    }

    public void enabledFilters(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException
    {
        String workspace = request.getParameter("workspace");
        String layer = request.getParameter("layer");

        try {
            respondWithDocument(response, getEnabledFiltersDocument(workspace, layer));
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void uniqueValues(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException
    {
        String workspace = request.getParameter("workspace");
        String layer = request.getParameter("layer");
        String propertyName = request.getParameter("propertyName");

        if (! uniqueValuesAllowed(workspace, layer, propertyName)) {
            throw new RuntimeException(String.format("uniqueValues not allowed for '%s:%s/%s'",
                workspace, layer, propertyName));
        }

        try {
            respondWithDocument(response, getUniqueValuesDocument(workspace, layer, propertyName));
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Document getEnabledFiltersDocument(String workspace, String layer)
        throws ParserConfigurationException, SAXException, IOException, NamingException, ServiceException
    {
        LayerInfo layerInfo = getLayerInfo(workspace, layer);

        if (layerInfo == null) {
            throw new ServiceException("Could not find layer " + workspace + ":" + layer);
        }

        FilterConfigurationFile file = new FilterConfigurationFile(getLayerDataDirectoryPath(layerInfo));
        List<Filter> filters = file.getFilters();

        return new FiltersDocument().build(filters);
    }

    private Document getUniqueValuesDocument(String workspace, String layer, String propertyName)
        throws Exception
    {
        LayerInfo layerInfo = getLayerInfo(workspace, layer);
        PossibleValuesReader possibleValuesReader = new PossibleValuesReader();
        Set values = possibleValuesReader.read(getDataStoreInfo(workspace, layer), layerInfo, propertyName);

        return new ValuesDocument().build(values);
    }

    private void respondWithDocument(HttpServletResponse response, Document document) throws TransformerException, IOException {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        DOMSource source = new DOMSource(document);
        StreamResult result = new StreamResult(response.getOutputStream());
        transformer.transform(source, result);
    }

    private LayerInfo getLayerInfo(String workspace, String layer) {
        return LayerInfoProperties.getLayer(getCatalog(), workspace, layer);
    }

    private DataStoreInfo getDataStoreInfo(String workspace, String layer) {
        return getCatalog().getDataStoreByName(workspace, getLayerInfo(workspace, layer).getResource().getStore().getName());
    }

    private String getLayerDataDirectoryPath(LayerInfo layerInfo) {
        return new DataDirectory(context).getLayerDataDirectoryPath(layerInfo);
    }
}

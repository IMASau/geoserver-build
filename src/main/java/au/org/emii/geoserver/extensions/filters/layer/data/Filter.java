/*
 * Copyright 2014 IMOS
 *
 * The AODN/IMOS Portal is distributed under the terms of the GNU General Public License
 *
 */

package au.org.emii.geoserver.extensions.filters.layer.data;

import org.apache.commons.lang.StringUtils;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

public class Filter implements Serializable {

    private String name;
    private String type;
    private String label;
    private Boolean enabled;
    private Boolean visualised;
    private Boolean excludedFromDownload;
    private Set<String> values;

    public Filter() {}

    public Filter(String name, String type) {
        this.name = name;
        this.type = type;
        this.enabled = Boolean.FALSE;
        this.visualised = Boolean.TRUE;
        this.excludedFromDownload = Boolean.FALSE;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getLabel() {
        if (label != null) {
            return label;
        }

        return getName() != null ? StringUtils.capitalize(getName().replaceAll("_", " ")) : null;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public Boolean isEnabled() {
        return getEnabled();
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public Boolean getVisualised() {
        return visualised;
    }

    public Boolean isVisualised() {
        return getVisualised();
    }

    public void setVisualised(Boolean visualised) {
        this.visualised = visualised;
    }

    public Boolean getExcludedFromDownload() {
        return excludedFromDownload;
    }

    public Boolean isExcludedFromDownload() {
        return getExcludedFromDownload();
    }

    public void setExcludedFromDownload(Boolean excludedFromDownload) {
        this.excludedFromDownload = excludedFromDownload;
    }

    public Set<String> getValues() {
        return values;
    }

    public void setValues(Set<String> values) {
        this.values = values;
    }

    public Filter merge(Filter other) {
        Filter right = coalesce(other, this);

        Filter filter = new Filter();
        filter.setName(coalesce(right.getName(), getName()));
        filter.setType(coalesce(right.getType(), getType()));
        filter.setLabel(coalesce(right.getLabel(), getLabel()));
        filter.setVisualised(coalesce(right.getVisualised(), getVisualised()));
        filter.setEnabled(coalesce(right.getEnabled(), getEnabled()));
        filter.setExcludedFromDownload(coalesce(right.getExcludedFromDownload(), getExcludedFromDownload()));

        return filter;
    }

    private <T> T coalesce(T a, T b) {
        return a == null ? b : a;
    }
}

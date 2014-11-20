/*
 * Copyright 2014 IMOS
 *
 * The AODN/IMOS Portal is distributed under the terms of the GNU General Public License
 *
 */

package au.org.emii.geoserver.extensions.filters.layer.data;

import org.apache.wicket.markup.html.form.CheckBox;

public class LayerDataPropertyCheckBox extends CheckBox {

    private String name;

    public LayerDataPropertyCheckBox(String id, String name) {
        this(id);
        this.name = name;
    }

    public LayerDataPropertyCheckBox(String id) {
        super(id);
    }

    @Override
    public String getInputName() {
        return name;
    }
}

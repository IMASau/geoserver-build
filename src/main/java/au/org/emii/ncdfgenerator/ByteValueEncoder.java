
package au.org.emii.ncdfgenerator;

import java.util.Map;

import ucar.ma2.Array;
import ucar.ma2.DataType;


class ByteValueEncoder implements IValueEncoder {
    private byte fill;

    ByteValueEncoder() {
        this.fill = 0x0;
    }

    public DataType targetType() {
        return DataType.BYTE;
    }

    public void prepare(Map<String, Object> attributes) throws NcdfGeneratorException {
        try {
            fill = (Byte) attributes.get("_FillValue");
        } catch(Exception e) {
            throw new NcdfGeneratorException("Expected _FillValue attribute to be Byte type");
        }
    }

    public void encode(Array array, int ima, Object value) throws NcdfGeneratorException {
        if(value == null) {
            array.setByte(ima, fill);
        } else if(value instanceof Byte) {
            array.setByte(ima, (Byte) value);
        } else if(value instanceof String && ((String)value).length() == 1) {
            // coerce string of length 1 to byte
            String s = (String) value;
            Byte ch = s.getBytes()[0];
            array.setByte(ima, ch);
        } else {
            throw new NcdfGeneratorException("Failed to convert type to byte");
        }
    }
}

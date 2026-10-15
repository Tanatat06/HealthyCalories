package healthycalories.persistence;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public final class CsvManager {
    private CsvManager() {
    }

    public static List<String> parseLine(String line) throws IOException {
        if (line == null) {
            throw new IllegalArgumentException("CSV line must not be null");
        }
        List<String> fields = new ArrayList<>();
        StringBuilder field = new StringBuilder();
        boolean quoted = false;
        for (int i = 0; i < line.length(); i++) {
            char current = line.charAt(i);
            if (quoted) {
                if (current == '"' && i + 1 < line.length()
                        && line.charAt(i + 1) == '"') {
                    field.append('"');
                    i++;
                } else if (current == '"') {
                    quoted = false;
                } else {
                    field.append(current);
                }
            } else if (current == ',') {
                fields.add(field.toString());
                field.setLength(0);
            } else if (current == '"' && field.length() == 0) {
                quoted = true;
            } else {
                field.append(current);
            }
        }
        if (quoted) {
            throw new IOException("CSV contains an unterminated quoted field");
        }
        fields.add(field.toString());
        return fields;
    }

    public static String formatLine(String... values) {
        if (values == null) {
            throw new IllegalArgumentException("CSV values must not be null");
        }
        StringBuilder line = new StringBuilder();
        for (int i = 0; i < values.length; i++) {
            if (i > 0) {
                line.append(',');
            }
            String value = values[i] == null ? "" : values[i];
            if (value.indexOf(',') >= 0 || value.indexOf('"') >= 0
                    || value.indexOf('\n') >= 0 || value.indexOf('\r') >= 0) {
                line.append('"').append(value.replace("\"", "\"\"")).append('"');
            } else {
                line.append(value);
            }
        }
        return line.toString();
    }
}

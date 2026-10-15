package main.java.healthycalories.model.contract;
public interface CsvConvertible {
    String[] toCsvRow();
    void fromCsvRow(String[] row);
}
package main.java.healthycalories.model.contract;
public interface CsvConvertible {
    public String toCsvRow();
    public void fromCsvRow(String csvRow);
}
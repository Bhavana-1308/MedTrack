package com.medreminder.interfaces;

/**
 * Interface implemented by domain models that can be exported to CSV reports
 * using File Handling services.
 */
public interface Exportable {

    /**
     * Returns the CSV header row for this record type.
     */
    String getCsvHeader();

    /**
     * Converts the implementing object into a single comma-separated line.
     */
    String toCsvRow();
}

package com.littlepay.exercise.csv.taps;

import com.littlepay.exercise.tap.TapType;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

import java.io.IOException;
import java.io.Reader;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

/** Reads taps from CSV with the header ID, DateTimeUTC, TapType, StopId, CompanyId, BusID, PAN **/
public class TapCsvReader {

    private static final CSVFormat FORMAT = CSVFormat.DEFAULT.builder()
            .setHeader()
            .setSkipHeaderRecord(true)
            .setIgnoreSurroundingSpaces(true)
            .setIgnoreEmptyLines(true)
            .build();

    /** Leaves the reader open; whoever opened it closes it **/
    public List<TapRow> read(Reader reader) throws IOException {
        List<TapRow> rows = new ArrayList<>();
        for (CSVRecord record : FORMAT.parse(reader)) {
            rows.add(toRow(record));
        }
        return rows;
    }

    private static TapRow toRow(CSVRecord record) {
        try {
            return new TapRow(
                    TapsCsvFieldFormatHelper.parseTimestamp(record.get("DateTimeUTC")),
                    TapType.valueOf(record.get("TapType")),
                    TapsCsvFieldFormatHelper.parseStopId(record.get("StopId")),
                    record.get("CompanyId"),
                    record.get("BusID"),
                    record.get("PAN"));
        } catch (DateTimeParseException | IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid tap on record " + record.getRecordNumber() + ": " + e.getMessage(), e);
        }
    }
}

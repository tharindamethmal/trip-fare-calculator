package com.littlepay.exercise.csv.trips;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.List;

/** Writes trips as CSV, one row per trip **/
public class TripCsvWriter {

    private static final CSVFormat FORMAT = CSVFormat.DEFAULT.builder()
            .setHeader("Started", "Finished", "DurationSecs", "FromStopId", "ToStopId", "ChargeAmount",
                    "CompanyId", "BusID", "PAN", "Status")
            .build();

    /** Leaves the writer open; whoever opened it closes it **/
    public void write(List<TripRow> trips, Writer writer) throws IOException {
        CSVPrinter printer = FORMAT.print(writer);
        for (TripRow trip : trips) {
            printer.printRecord(toColumns(trip));
        }
        printer.flush();
    }

    private static List<String> toColumns(TripRow trip) {
        return Arrays.asList(
                TripsCsvFieldFormatHelper.formatTimestamp(trip.started()),
                trip.finished() == null ? "" : TripsCsvFieldFormatHelper.formatTimestamp(trip.finished()),
                trip.durationSecs() == null ? "" : trip.durationSecs().toString(),
                TripsCsvFieldFormatHelper.formatStopId(trip.fromStopId()),
                trip.toStopId() == null ? "" : TripsCsvFieldFormatHelper.formatStopId(trip.toStopId()),
                "$" + trip.chargeAmount().toPlainString(),
                trip.companyId(),
                trip.busId(),
                trip.pan(),
                trip.status());
    }
}

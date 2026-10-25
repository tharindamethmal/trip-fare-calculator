# Trip Fare Calculator

## Prerequisites

- JDK 25
- Maven 3.9+

```sh
java -version
mvn -version
```

## Build

```sh
mvn clean package
```

## Run

```sh
java -jar target/trip-fare-calculator-1.0-SNAPSHOT.jar taps.csv
```

## Output file (Optional)

Writes a new `trips-<UTC timestamp>.csv` in the current folder on each run.
To choose the file instead (overwritten if it exists), pass it as a second argument:

```sh
java -jar target/trip-fare-calculator-1.0-SNAPSHOT.jar taps.csv trips.csv
```

## Test

```sh
mvn test
```

## Assumptions

- Fares apply in both directions
- One fare per stop pair; duplicates are rejected
- One currency per fares file; no currency conversion
- Taps are matched in time order, not file order
- A tap off with no tap on before it is an unmatched trip and is not charged
- No time limit between a tap on and its tap off; a tap off the next day still completes the trip
- All fares are defined in AUD (`src/main/resources/fares-aud.csv`)

## Glossary

| Term            | Meaning                                                  |
|-----------------|----------------------------------------------------------|
| Tap             | A card touched on (`ON`) or off (`OFF`) at a stop        |
| PAN             | Primary account number; identifies a credit card         |
| Trip            | Taps of one card matched into a journey                  |
| Completed trip  | Tap on and tap off at different stops                    |
| Incomplete trip | Tap on with no tap off                                   |
| Cancelled trip  | Tap on and tap off at the same stop                      |
| Unmatched trip  | Tap off with no tap on before it                         |
| Fare            | Published price between two stops, in either direction   |
| Max fare        | Highest fare from a stop; charged for an incomplete trip |
| Charge          | Amount one trip costs after the rules are applied        |
| Fare provider   | Source of fares and their currency, e.g. a CSV file      |

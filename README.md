# Spring Batch file-processing example

This application uses Spring Integration to watch an input directory for signal
files and launches a Spring Batch job for each matching data file. Spring Batch
startup auto-launch is disabled; jobs are launched by the file flow.

## Run

Start the application from the repository root so the relative directories and
report files use the repository root as their working directory:

```shell
./gradlew :adapter:bootRun
```

## Submit an input file

Place a text data file and a matching `.GO` signal file in `app-input`. The
signal filename must be the data filename with `.GO` appended. For example:

```text
app-input/
  customers.txt
  customers.txt.GO
```

The `.GO` file is a signal only; the batch reader processes `customers.txt`.
Each line in the data file is read as one `Person`. By default, data filenames
containing `app-data` are rejected and moved to `app-invalid`; other filenames
are processed.

The default flow is:

```mermaid
flowchart TD
    A["app-input: poll for *.GO every 4 seconds"] --> B{"Matching data file exists?"}
    B -- No --> C["Discard .GO signal"]
    B -- Yes --> D["Delete .GO signal and continue with data file"]
    D --> E{"Data filename contains configured invalid pattern?"}
    E -- Yes --> F["Move data file to app-invalid"]
    E -- No --> G["Launch SpringBatchJob with input.file.name and unique run.id"]
    G --> H["Read each line as a Person"]
    H --> I["Pass through processor"]
    I --> J["Writer sends GET request to batch.endpoint"]
    J --> K["afterJob writes Report-<filename>.json"]
    K --> L{"Job completed?"}
    L -- Yes --> M["Move data file to app-processed"]
    L -- No --> N["Move data file to app-failed"]
```

In short, the flow polls `app-input` for `*.GO` signal files, checks for the
matching data file, rejects filenames containing the configured invalid
filename pattern, and launches the batch job for accepted files. The job reads
each line as a `Person`, passes it through the processor, and the writer issues
a GET request to the configured endpoint. The `afterJob` listener writes a
`Report-<data-filename-without-extension>.json` report in the application
working directory before the input file is moved.

For example, processing `customers.txt` creates
`Report-customers.json`. Report files are separate from the processed/failed
input directories.

## Configuration

The defaults are in `config/application.yml`:

| Property | Default | Purpose |
| --- | --- | --- |
| `flow.inputDirectory` | `app-input` | Directory polled for signal and data files |
| `flow.pollingFrequency` | `4000` | Polling interval in milliseconds |
| `flow.invalidFilenamePattern` | `app-data` | Filename substring that causes an input file to be rejected |
| `flow.invalidDirectory` | `app-invalid` | Destination for rejected data files |
| `flow.processedDirectory` | `app-processed` | Destination for successfully processed files |
| `flow.failedDirectory` | `app-failed` | Destination for files whose jobs fail |
| `batch.jobName` | `SpringBatchJob` | Batch job name |
| `batch.stepName` | `StepOne` | Batch step name |
| `batch.chunkSize` | `1000` | Items written per chunk |
| `batch.endpoint` | `https://httpbin.org/get` | Endpoint called by the item writer |

These paths are relative to the application working directory unless configured
as absolute paths. Boot's `spring.batch.job.enabled` is `false`, so the job will
not start automatically without an input file.

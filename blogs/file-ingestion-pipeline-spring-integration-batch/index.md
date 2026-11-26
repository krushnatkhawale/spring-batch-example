---
title: "Automating File Ingestion and Lifecycle Management with Spring Integration and Spring Batch"
slug: "file-ingestion-pipeline-spring-integration-batch"
author: "Krushna Khawale"
date: "2026-11-27"
description: "How Spring Integration complements Spring Batch in the adapter module by managing signal-based file polling, pre-execution validation, JobLaunchingGateway triggers, and post-batch archiving."
tags: [Spring Integration, Spring Batch, File Processing, Enterprise Integration Patterns, Java]
version: "0.1.0"
commit: "2fd2d9b"
repoUrl: "https://github.com/krushnatkhawale/blogify"
problem: |
  Spring Batch excels at chunk-oriented data processing and transaction checkpoints, but it is strictly an execution engine. It cannot natively detect when an upstream producer has finished writing a file to disk, validate filenames prior to database record creation, or move files to archive and quarantine folders based on job results. Relying on basic cron jobs or custom file watchers introduces race conditions on partially written files, pollutes JobRepository tables with failed executions from invalid files, and leaves files stranded in input folders.
insight: |
  Spring Integration solves file orchestration by wrapping Spring Batch in an automated ingestion pipeline. Using Spring Integration File adapters and Enterprise Integration Patterns (EIP), the adapter module implements: a two-phase signal file (.GO) handshake to guarantee write completion, pre-launch filtering to isolate invalid files in app-invalid/ before touching the database, a JobLaunchingGateway to dispatch dynamic JobParameters, and a status-based router that moves finished files to app-processed/ or failed files to app-failed/.
diagram: |
  flowchart TD
      subgraph Inbound ["1. File Polling & Signal Handshake"]
          A["filePoller: Poll app-input for *.GO"] --> B{"NoInputFileSignalFileFilter:<br/>Companion data file exists?"}
          B -- Yes --> C["SignalToInputFileTransformer:<br/>Delete .GO & forward data File"]
          B -- No --> B_Drop["Delete orphan .GO file"]
      end

      subgraph Validation ["2. Pre-Execution Validation"]
          C --> D{"FileValidator:<br/>Filename matches valid pattern?"}
          D -- Invalid --> E["filteredFileProcessor:<br/>Move file to app-invalid/"]
      end

      subgraph Launch ["3. Job Launching Gateway"]
          D -- Valid --> F["JobRequestHelper:<br/>Build JobLaunchRequest (file path + run.id)"]
          F --> G["batch-int:job-launching-gateway"]
          G --> H["Spring Batch Engine:<br/>Execute Chunk Step (Reader -> Processor -> Writer)"]
          H --> I["JobListener:<br/>Generate JSON Report & return JobExecution"]
      end

      subgraph Lifecycle ["4. Post-Execution Status Routing"]
          I --> J{"JobStatusBasedRouter:<br/>Inspect JobExecution.getStatus()"}
          J -- COMPLETED --> K["completedJobProcessor:<br/>Move file to app-processed/"]
          J -- FAILED --> L["unCompletedJobProcessor:<br/>Move file to app-failed/"]
      end
takeaways:
  - "Eliminate partial-read race conditions: Polling for companion signal files (*.GO) ensures large files are completely written before the batch job starts."
  - "Filter corrupt files before batch submission: Discarding invalid files prior to launching avoids creating unnecessary JobExecution and StepExecution records in the database."
  - "Bridge file flows to batch jobs seamlessly: Use JobLaunchingGateway and JobRequestHelper to dynamically inject file paths and unique run timestamps into JobParameters."
  - "Automate post-batch file movement: Route files based on JobExecution status to processed or failed directories without custom shell scripts or manual steps."
---

## Why Pair Spring Integration with Spring Batch?

Spring Batch is the standard for chunk-oriented batch processing in Java. It excels at:
- Chunk-based commits (`ItemReader`, `ItemProcessor`, `ItemWriter`).
- Skip, retry, and fault tolerance policies.
- Execution tracking and checkpoint restartability via `JobRepository`.

However, Spring Batch is **passive**: it requires an external trigger and assumes that the input file is already present, valid, fully written, and located at a given path. It does not handle:
1. **File readiness**: Knowing whether a producer has finished uploading a file over FTP or a network share.
2. **Pre-flight validation**: Checking filename conventions before spinning up database transactions and `JobExecution` audit logs.
3. **File lifecycle management**: Moving successful files to an archive directory or isolating failed files into a quarantine directory.

Rather than writing ad-hoc shell scripts or error-prone `@Scheduled` methods, the `adapter` module uses **Spring Integration** to build a robust file ingestion and lifecycle pipeline around Spring Batch.

---

## 1. Safe File Polling with a Two-Phase Signal Handshake (`.GO`)

Reading a file while it is still being written causes silent data truncation, parsing exceptions, or corrupted records.

To prevent this, the `adapter` module adopts a **signal file handshake**:
1. The upstream producer writes `data-file-100.txt`.
2. Once write operations are complete, the producer creates an empty signal file: `data-file-100.txt.GO`.
3. Spring Integration's inbound channel adapter polls **only** for `*.GO` files:

```xml
<!-- adapter/src/main/resources/integration-context.xml -->
<int-file:inbound-channel-adapter id="filePoller"
                                  channel="signalFileChannel"
                                  directory="file:${flow.inputDirectory:app-input}"
                                  prevent-duplicates="false"
                                  filename-pattern="*.GO">
    <int:poller fixed-rate="${flow.pollingFrequency:4000}"/>
</int-file:inbound-channel-adapter>
```

When a `.GO` signal arrives, the flow checks whether the actual companion data file is present:

```java
// adapter/src/main/java/play/with/integration/flow/filter/NoInputFileSignalFileFilter.java
public class NoInputFileSignalFileFilter {
    public boolean filter(File signalFile) throws IOException {
        String signalFilePath = signalFile.getAbsolutePath();
        String inputFilePath = signalFilePath.substring(0, signalFilePath.indexOf(".GO"));
        File inputFile = new File(inputFilePath);
        if (inputFile.exists()) {
            return true;
        } else {
            forceDelete(signalFile);
            return false;
        }
    }
}
```

Once confirmed, `SignalToInputFileTransformer` deletes the `.GO` signal file and forwards the actual data `File` down the message channel:

```java
// adapter/src/main/java/play/with/integration/flow/transformer/SignalToInputFileTransformer.java
public class SignalToInputFileTransformer {
    public File transform(File signalFile) throws IOException {
        String signalFilePath = signalFile.getAbsolutePath();
        String inputFilePath = signalFilePath.substring(0, signalFilePath.indexOf(".GO"));
        forceDelete(signalFile);
        return new File(inputFilePath);
    }
}
```

This guarantees that Spring Batch never attempts to process an incomplete file.

---

## 2. Pre-Execution Validation: Keeping `JobRepository` Clean

If a file has an invalid naming convention or format, launching a Spring Batch job creates immediate `FAILED` entries in `BATCH_JOB_EXECUTION` and `BATCH_STEP_EXECUTION` tables. This pollutes operational monitoring and inflates failure counts.

Spring Integration places a filter right before batch submission:

```xml
<!-- adapter/src/main/resources/integration-context.xml -->
<int:filter input-channel="inboundFileChannel" 
            output-channel="filteredFilesChannel"
            discard-channel="rejectedFilesChannel">
    <bean class="play.with.integration.flow.filter.FileValidator">
        <property name="invalidFilenamePattern" value="${flow.invalidFilenamePattern:app-data}"/>
    </bean>
</int:filter>

<int-file:outbound-channel-adapter id="filteredFileProcessor" 
                                   channel="rejectedFilesChannel"
                                   delete-source-files="true"
                                   directory="file:${flow.invalidDirectory:app-invalid}"/>
```

Invalid files are discarded directly into `app-invalid/`. Because the batch engine is never invoked for bad files, the database execution history remains pristine.

---

## 3. The `JobLaunchingGateway`: Bridging Messages to Batch Jobs

Once a file is validated, it must be converted into a batch job trigger.

In Spring Integration, `JobRequestHelper` transforms the inbound `File` into a `JobLaunchRequest`, dynamically injecting parameters:
- `input.file.name`: the absolute path to the data file, consumed by Spring Batch's `@StepScope` reader.
- `run.id`: a current timestamp ensuring each job run has unique parameters for restartability.

```java
// adapter/src/main/java/play/with/integration/flow/config/JobRequestHelper.java
public class JobRequestHelper {
    private Job job;
    private String fileParameterName;

    @Transformer
    public JobLaunchRequest toRequest(File inputFile) {
        JobParametersBuilder jobParametersBuilder = new JobParametersBuilder();
        jobParametersBuilder.addString(fileParameterName, inputFile.getAbsolutePath());
        jobParametersBuilder.addLong("run.id", System.currentTimeMillis());
        return new JobLaunchRequest(job, jobParametersBuilder.toJobParameters());
    }
}
```

The `JobLaunchRequest` is dispatched to the `<batch-int:job-launching-gateway>`:

```xml
<!-- adapter/src/main/resources/integration-context.xml -->
<batch-int:job-launching-gateway request-channel="jobRequestChannel" 
                                reply-channel="jobReplyChannel"/>
```

The gateway handles the handoff to the Spring Batch `JobLauncher`, runs the chunk pipeline, and outputs the resulting `JobExecution` message payload onto `jobReplyChannel`.

---

## 4. Post-Execution Routing & File Lifecycle Management

When the batch job completes or encounters an error, the input file still sits in `app-input/`. What should happen to it?

Rather than coupling file movement logic inside Spring Batch listeners or writers, the adapter flow uses a **Status-Based Router**:

```java
// adapter/src/main/java/play/with/integration/flow/router/JobStatusBasedRouter.java
public class JobStatusBasedRouter {
    public String transform(JobExecution jobExecution) {
        if (jobExecution.getStatus() == BatchStatus.COMPLETED) {
            return "completedJobChannel";
        }
        return "unCompletedJobChannel";
    }
}
```

Each destination channel transforms the `JobExecution` back into the original file reference and invokes dedicated outbound file adapters:

```xml
<!-- adapter/src/main/resources/integration-context.xml -->
<int:transformer input-channel="completedJobChannel" output-channel="completedFileChannel">
    <bean class="play.with.integration.flow.transformer.JobToFileTransformer"/>
</int:transformer>

<int:transformer input-channel="unCompletedJobChannel" output-channel="unCompletedFileChannel">
    <bean class="play.with.integration.flow.transformer.JobToFileTransformer"/>
</int:transformer>

<int-file:outbound-channel-adapter id="completedJobProcessor" 
                                   channel="completedFileChannel"
                                   delete-source-files="true"
                                   directory="file:${flow.processedDirectory:app-processed}"/>

<int-file:outbound-channel-adapter id="unCompletedJobProcessor" 
                                   channel="unCompletedFileChannel"
                                   delete-source-files="true"
                                   directory="file:${flow.failedDirectory:app-failed}"/>
```

With `delete-source-files="true"`:
- **Completed jobs**: The data file is moved to `app-processed/`.
- **Failed jobs**: The data file is moved to `app-failed/` for operational inspection.

---

## Conclusion

By combining Spring Integration and Spring Batch in the `adapter` module, each framework focuses on what it does best:
- **Spring Integration** manages the file system: polling directories, validating file handshakes, filtering bad inputs, and archiving files post-execution.
- **Spring Batch** manages data processing: streaming records, executing chunk-based REST calls, committing transactions, and recording state checkpoints.

This clean separation of concerns creates a resilient, production-ready file processing pipeline.

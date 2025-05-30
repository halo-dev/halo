import groovy.json.JsonSlurper
import org.gradle.api.DefaultTask
import org.gradle.api.artifacts.repositories.PasswordCredentials
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction

import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

import static java.net.http.HttpRequest.BodyPublishers.ofFile
import static java.net.http.HttpRequest.BodyPublishers.ofString
import static java.nio.charset.StandardCharsets.UTF_8

/**
 * Task to upload a bundle to Sonatype Central Repository.
 * @author JohnNiang
 */
abstract class UploadBundleTask extends DefaultTask {

    @InputFile
    abstract RegularFileProperty getBundleFile();

    @Input
    @Optional
    abstract Property<PublishingType> getPublishingType();

    @Input
    abstract Property<PasswordCredentials> getCredentials();

    UploadBundleTask() {
        getPublishingType().convention(PublishingType.AUTOMATIC)
    }

    @TaskAction
    void upload() {
        if (project.version.toString().endsWith("-SNAPSHOT")) {
            logger.lifecycle("Detected SNAPSHOT version, uploading to maven-snapshots repository...")
            uploadSnapshotBundle()
            logger.lifecycle("Snapshot bundle uploaded successfully.")
            return
        }
        def deploymentId = uploadReleaseBundle()
        def maxWait = Duration.ofMinutes(12)
        def pollingInterval = Duration.ofSeconds(10)
        def maxRetry = (int) (maxWait.toMillis() / pollingInterval.toMillis())
        def retry = 0
        logger.lifecycle("Preparing to check deployment state for deployment ID: ${deploymentId}, max retries: ${maxRetry}, polling interval: ${pollingInterval.toSeconds()} seconds")
        while (retry++ <= maxRetry) {
            def state = checkDeploymentState(deploymentId)
            if (state == null) {
                throw new RuntimeException("Failed to check deployment state for deployment ID: ${deploymentId}")
            }
            if (state == DeploymentState.PUBLISHED) {
                println("Bundle published successfully with deployment ID: ${deploymentId}")
                break
            }
            if (state == DeploymentState.VALIDATED) {
                println("Bundle validated successfully with deployment ID: ${deploymentId}")
                break
            }
            if (state == DeploymentState.FAILED) {
                throw new RuntimeException("Bundle deployment failed with deployment ID: ${deploymentId}")
            }
            if (state == DeploymentState.VALIDATING) {
                logger.lifecycle('Bundle is being validated, please wait...')
            } else if (state == DeploymentState.PENDING) {
                logger.lifecycle('Bundle is pending, please wait...')
            }
            println("Deployment state: ${state}, retrying(${retry}) in ${pollingInterval.toSeconds()} seconds...")
            sleep(pollingInterval.toMillis())
        }
    }

    DeploymentState checkDeploymentState(String deploymentId) {
        createHttpClient().withCloseable { client ->
            def endpoint = "https://central.sonatype.com/api/v1/publisher/status?id=${deploymentId}"

            def request = HttpRequest.newBuilder(URI.create(endpoint))
                    .header("Authorization", bearerAuthorizationHeader)
                    .POST(HttpRequest.BodyPublishers.noBody())
                    .build()
            def response = client.send(request, HttpResponse.BodyHandlers.ofString(UTF_8))
            if (response.statusCode() != 200) {
                throw new RuntimeException("Failed to check deployment status: status: ${response.statusCode()} - body: ${response.body()}")
            }
            logger.debug("Response body: ${response.body()}")
            def status = new JsonSlurper().parseText(response.body())
            logger.debug("Deployment status: ${status}")
            return status.properties.deploymentState as DeploymentState
        }
    }

    void uploadSnapshotBundle() {
        logger.lifecycle('Starting to upload snapshot bundle: {}', bundleFile.asFile.get())
        createHttpClient().withCloseable { client ->
            new ZipInputStream(bundleFile.asFile.get().newInputStream()).withCloseable { zis ->
                ZipEntry entry
                while ((entry = zis.nextEntry) != null) {
                    if (entry.directory) {
                        continue
                    }
                    def relativePath = entry.name
                    def endpoint = "https://central.sonatype.com/repository/maven-snapshots/$relativePath"
                    def request = HttpRequest.newBuilder(URI.create(endpoint))
                            .header("Authorization", basicAuthorizationHeader)
                            .header("Content-Type", "application/octet-stream")
                            .PUT(HttpRequest.BodyPublishers.ofByteArray(zis.readAllBytes()))
                            .build()
                    def response = client.send(request, HttpResponse.BodyHandlers.ofString(UTF_8))
                    logger.debug('Response status code: {}, body: {}', response.statusCode(), response.body())
                    if (response.statusCode() != 200 && response.statusCode() != 201) {
                        throw new RuntimeException("Failed to upload snapshot bundle: status: ${response.statusCode()} - entry: ${relativePath} - body : ${response.body()}")
                    }
                    logger.lifecycle('Uploaded snapshot entry: {}, status: {}', relativePath, response.statusCode())
                }
            }
        }
    }

    String uploadReleaseBundle() {
        logger.lifecycle('Starting to upload release bundle: {}', bundleFile.asFile.get())
        createHttpClient().withCloseable { client ->
            // See https://www.w3.org/Protocols/rfc1341/7_2_Multipart.html for more
            def boundary = "------haloformboundary${UUID.randomUUID().toString().replace('-', '')}"
            def crlf = "\r\n"
            def delimiter = "${crlf}--${boundary}"
            def endpoint = "https://central.sonatype.com/api/v1/publisher/upload?publishingType=${publishingType}"

            def publishers = new ArrayList<HttpRequest.BodyPublisher>()
            publishers.add(ofString("${delimiter}${crlf}"))
            publishers.add(ofString("""\
Content-Disposition: form-data; name="bundle"; filename="${bundleFile.get().asFile.name}"\
${crlf}\
"""))
            publishers.add(ofString("Content-Type: application/octet-stream${crlf}${crlf}"))
            publishers.add(ofFile(bundleFile.get().asFile.toPath()))
            publishers.add(ofString("${delimiter}--${crlf}"))

            def request = HttpRequest.newBuilder(URI.create(endpoint))
                    .header("Authorization", bearerAuthorizationHeader)
                    .header("Content-Type", "multipart/form-data; boundary=${boundary}")
                    .POST(HttpRequest.BodyPublishers.concat(publishers.toArray(HttpRequest.BodyPublisher[]::new)))
                    .build()
            def response = client.send(request, HttpResponse.BodyHandlers.ofString(UTF_8))
            if (logger.debugEnabled) {
                logger.debug('Response status code: {}, body: {}', response.statusCode(), response.body())
            }
            if (response.statusCode() != 201 && response.statusCode() != 200) {
                throw new RuntimeException("Failed to upload bundle: status: ${response.statusCode()} - body: ${response.body()}")
            }
            def deploymentId = response.body().trim()
            logger.lifecycle('Uploaded release bundle successfully, deployment ID: {}', deploymentId)
            return deploymentId
        }
    }

    @Internal
    String getBearerAuthorizationHeader() {
        def encoded = Base64.encoder.encodeToString("${credentials.get().username}:${credentials.get().password}".getBytes(UTF_8))
        return "Bearer ${encoded}"
    }

    @Internal
    String getBasicAuthorizationHeader() {
        def encoded = Base64.encoder.encodeToString("${credentials.get().username}:${credentials.get().password}".getBytes(UTF_8))
        return "Basic ${encoded}"
    }

    static HttpClient createHttpClient() {
        return HttpClient.newBuilder()
                .connectTimeout(Duration.ofMinutes(1))
                .build()
    }

    enum PublishingType {
        AUTOMATIC,
        USER_MANAGED,
        ;
    }

    enum DeploymentState {
        PENDING,
        VALIDATING,
        VALIDATED,
        PUBLISHING,
        PUBLISHED,
        FAILED,
        ;
    }

}

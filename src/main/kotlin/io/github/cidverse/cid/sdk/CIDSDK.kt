@file:Suppress("MaxLineLength", "LongParameterList")
package io.github.cidverse.cid.sdk

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.github.cidverse.cid.sdk.domain.ArtifactFile
import io.github.cidverse.cid.sdk.domain.CIDError
import io.github.cidverse.cid.sdk.domain.CommandExecution
import io.github.cidverse.cid.sdk.domain.CommandExecutionResult
import io.github.cidverse.cid.sdk.domain.ConfigCurrent
import io.github.cidverse.cid.sdk.domain.LogMessage
import io.github.cidverse.cid.sdk.domain.ProjectModule
import io.github.cidverse.cid.sdk.domain.VCSCommit
import io.github.cidverse.cid.sdk.domain.VCSRelease
import io.github.cidverse.cid.sdk.domain.VCSTag
import io.github.cidverse.cid.sdk.util.extensionWithDot
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.newsclub.net.unix.AFSocketFactory
import org.newsclub.net.unix.AFUNIXSocketAddress
import java.io.File
import java.io.IOException
import java.net.Proxy
import java.net.SocketAddress
import java.time.Duration
import java.util.UUID
import kotlin.properties.Delegates

/**
 * The CID SDK Client
 */
@Suppress("TooManyFunctions")
open class CIDSDK(
    private var socket: String? = null,
    private var endpoint: String? = null,
    private var secret: String? = null
) {
    companion object {
        const val connectionTimeout = 30L
        val jsonMediaType = "application/json; charset=utf-8".toMediaType()
        val objectMapper = jacksonObjectMapper()
    }

    private var httpClient : OkHttpClient by Delegates.notNull()

    init {
        objectMapper.registerModule(JavaTimeModule())
		objectMapper.propertyNamingStrategy = PropertyNamingStrategies.SNAKE_CASE

        // detect from environment if empty
        if (socket == null) {
            socket = System.getenv("CID_API_SOCKET")
        }
        if (endpoint == null) {
            endpoint = System.getenv("CID_API_ADDR")
        }
        if (secret == null) {
            secret = System.getenv("CID_API_SECRET")
        }
        require(endpoint != null || socket != null) {
            "auto-detection of CID API failed and no endpoint provided to the SDK"
        }

        // init http client
        var httpClientBuilder = OkHttpClient.Builder()
            .callTimeout(Duration.ofMinutes(connectionTimeout))
			.readTimeout(Duration.ofMinutes(connectionTimeout))
            .proxy(Proxy.NO_PROXY)
        if (socket != null) {
            val socketAddress: SocketAddress = AFUNIXSocketAddress.of(File(socket!!))
            httpClientBuilder = httpClientBuilder
                .socketFactory(AFSocketFactory.FixedAddressSocketFactory(socketAddress))
        }

        // auth interceptor
        httpClient = httpClientBuilder
            .addInterceptor(Interceptor { chain ->
                val original = chain.request()
                val request = original.newBuilder()
                    .header("Authorization", "Bearer $secret")
                    .build()
                chain.proceed(request)
            })
            .build()
    }

    private fun getBaseUrl(): String {
        if (socket != null) {
            return "http://localhost"
        }
        if (endpoint != null) {
            return endpoint as String
        }

        throw IOException("invalid configuration")
    }

	/**
	 * checks makes a simple health check to see if the api is reachable
	 */
    open fun check(): Boolean {
        val request = Request.Builder()
            .url(getBaseUrl()+"/health")
            .build();

        httpClient.newCall(request).execute().use { response ->
            return response.isSuccessful
        }
    }

	/**
	 * logs a message at log level with context
	 *
	 * @param level the log level (debug, info, warn, error)
	 * @param message free text
	 * @param context optional key value map of additional properties
	 */
    open fun log(level: String = "info", message: String, context: Map<String, Any> = mapOf()) {
		val reqBody = LogMessage(level = level, message = message, context = context)
		val request = Request.Builder()
			.url(getBaseUrl()+"/log")
			.post(objectMapper.writeValueAsString(reqBody).toRequestBody(jsonMediaType))
			.build();

		httpClient.newCall(request).execute().use { response ->
			handleErrorResponse(response)
		}
	}

	/**
	 * the configuration
	 */
    open fun config(): ConfigCurrent {
        val request = Request.Builder()
            .url(getBaseUrl()+"/config/current")
            .build();

        httpClient.newCall(request).execute().use { response ->
            handleErrorResponse(response)
            return objectMapper.readValue(response.body!!.string())
        }
    }

	/**
	 * returns the full environment (normalized ci env + whitelisted variables)
	 */
    open fun env(): Map<String, String> {
		val request = Request.Builder()
			.url(getBaseUrl()+"/env")
			.build();

		httpClient.newCall(request).execute().use { response ->
            handleErrorResponse(response)
			return objectMapper.readValue(response.body!!.string())
		}
	}

	/**
	 * lists all project modules
	 */
    open fun modules(): List<ProjectModule> {
		val request = Request.Builder()
			.url(getBaseUrl()+"/module")
			.build();

		httpClient.newCall(request).execute().use { response ->
            handleErrorResponse(response)
			return objectMapper.readValue(response.body!!.string())
		}
	}

	/**
	 * returns the current module, only available when running module-scoped actions
	 */
	open fun module(): ProjectModule {
		val request = Request.Builder()
			.url(getBaseUrl()+"/module/current")
			.build();

		httpClient.newCall(request).execute().use { response ->
            handleErrorResponse(response)
			return objectMapper.readValue(response.body!!.string())
		}
	}

	/**
	 * queries the vcs repository
	 */
    open fun vcsCommits(from: String = "", to: String = "", changes: Boolean = false, limit: Int = 0): List<VCSCommit> {
        val request = Request.Builder()
            .url(getBaseUrl()+"/vcs/commit?from=$from&$to=$to&limit=$limit&changes=$changes")
            .build();

        httpClient.newCall(request).execute().use { response ->
            handleErrorResponse(response)
            return objectMapper.readValue(response.body!!.string())
        }
    }

	/**
	 * retrieves information about a vcs commit by hash
	 *
	 * @param
	 */
    open fun vcsCommitByHash(hash: String, changes: Boolean = false): VCSCommit {
        val request = Request.Builder()
            .url(getBaseUrl()+"/vcs/commit/$hash?changes=$changes")
            .build();

        httpClient.newCall(request).execute().use { response ->
            handleErrorResponse(response)
            return objectMapper.readValue(response.body!!.string())
        }
    }

    open fun vcsTags(): List<VCSTag> {
        val request = Request.Builder()
            .url(getBaseUrl()+"/vcs/tag")
            .build();

        httpClient.newCall(request).execute().use { response ->
            handleErrorResponse(response)
            return objectMapper.readValue(response.body!!.string())
        }
    }

    open fun vcsReleases(type: String?): List<VCSRelease> {
        val request = Request.Builder()
            .url(getBaseUrl()+"/vcs/release?type=$type")
            .build();

        httpClient.newCall(request).execute().use { response ->
            handleErrorResponse(response)
            return objectMapper.readValue(response.body!!.string())
        }
    }

    open fun executeCommand(
        command: String,
        captureOutput: Boolean = false,
        workDir: String? = null,
        env: Map<String, String>? = mapOf(),
        ports: List<Int> = listOf(),
        constraint: String = "",
    ): CommandExecutionResult {
        val reqBody = CommandExecution(command = command, captureOutput = captureOutput, workDir = workDir, env = env, ports = ports, constraint = constraint)
        val request = Request.Builder()
            .url(getBaseUrl()+"/command")
            .post(objectMapper.writeValueAsString(reqBody).toRequestBody(jsonMediaType))
            .build()

        httpClient.newCall(request).execute().use { response ->
            handleErrorResponse(response)
            return objectMapper.readValue(response.body!!.string())
        }
    }

	/**
	 * returns the file content
	 */
    open fun fileRead(path: String): String {
        return File(path).readText(Charsets.UTF_8)
    }

	/**
	 * lists all files in the directory
	 */
    open fun fileList(directory: String, extensions: List<String>? = null): List<File> {
		val files = mutableListOf<File>()

        File(directory).walk().forEach {
			if (extensions != null) {
				if (extensions.contains(it.extensionWithDot())) {
					files.add(it)
				}
			} else {
				files.add(it)
			}
        }

		return files
    }

	/**
	 * copies a file or directory
	 *
	 * @param source source dir or file
	 * @param target target dir or file
	 */
    open fun fileCopy(source: String, target: String) {
		File(source).copyRecursively(target = File(target), overwrite = true)
    }

	/**
	 * deletes the file
	 *
	 * @param path file path
	 */
    open fun fileDelete(path: String): Boolean {
        return File(path).delete()
    }

	/**
	 * writes the content to the filesystem
	 *
	 * @param path file path
	 * @param content file content
	 */
    open fun fileWrite(path: String, content: String) {
		File(path).writeText(content)
    }

    /**
     * Retrieves a list of artifacts
     *
     * @param module
     * @param type
     * @param name
     * @param format
     * @param formatVersion
     * @return a list of {@link ArtifactFile} objects representing the artifacts
     * @throws IOException if there is an error connecting to the server or parsing the response
     */
    open fun artifactList(
        module: String = "",
        type: String = "",
        name: String = "",
        format: String = "",
        formatVersion: String = "",
    ): List<ArtifactFile> {
        val request = Request.Builder()
            .url(getBaseUrl()+"/artifact?module=$module&type=$type&name=$name&format=$format&format_version=$formatVersion")
            .get()
            .build()

        httpClient.newCall(request).execute().use { response ->
            handleErrorResponse(response)
            return objectMapper.readValue(response.body!!.string())
        }
    }

    /**
     * Uploads an artifact
     *
     * @param file
     * @param module
     * @param type
     * @param format
     * @param formatVersion
     * @throws IOException if there is an error connecting to the server or parsing the response
     */
    open fun artifactUpload(
        file: String,
        module: String = "root",
        type: String,
        format: String = "",
        formatVersion: String = ""
    ) {
        val formBody: RequestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart(
                "file",
                file.substringAfterLast("/"),
                File(file).asRequestBody("application/octet-stream".toMediaTypeOrNull())
            )
            .addFormDataPart("type", type)
            .addFormDataPart("module", module)
            .addFormDataPart("format", format)
            .addFormDataPart("format_version", formatVersion)
            .build()
        val request = Request.Builder()
            .url(getBaseUrl()+"/artifact")
            .post(formBody)
            .build()

        httpClient.newCall(request).execute().use { response ->
            handleErrorResponse(response)
        }
    }

    /**
     * Downloads an artifact
     *
     * @param id
     * @param file
     * @param module
     * @param type
     * @return a list of {@link ArtifactFile} objects representing the artifacts
     * @throws IOException if there is an error connecting to the server or parsing the response
     */
    open fun artifactDownload(
        file: String,
        id: String = "",
        module: String = "root",
        type: String,
        targetFile: String
    ) {
        val request = Request.Builder()
            .url(getBaseUrl()+"/artifact/download?id=$id&name=$file&module=$module&type=$type")
            .get()
            .build()

        httpClient.newCall(request).execute().use { response ->
            handleErrorResponse(response)

            // write to filesystem
            val inputStream = response.body?.byteStream()
            val outputFile = File(targetFile)
            inputStream?.use { input ->
                outputFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
        }
    }

	/**
	 * returns a random uuid
	 */
    open fun uuid(): String {
        return UUID.randomUUID().toString()
    }

    private fun handleErrorResponse(response: Response) {
        if (!response.isSuccessful) {
            val content = response.body?.string()
            if ((content != null) && content.startsWith("{")) {
                val err: CIDError = objectMapper.readValue(content)
                throw err
            }

            throw IOException("Unexpected error $response")
        }
    }
}

package io.github.cidverse.cid.sdk

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.datatype.jsr310.JSR310Module
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.github.cidverse.cid.sdk.domain.CIDError
import io.github.cidverse.cid.sdk.domain.CommandExecution
import io.github.cidverse.cid.sdk.domain.CommandExecutionResult
import io.github.cidverse.cid.sdk.domain.ProjectInfo
import io.github.cidverse.cid.sdk.domain.ProjectModule
import io.github.cidverse.cid.sdk.domain.VCSCommit
import io.github.cidverse.cid.sdk.domain.VCSRelease
import io.github.cidverse.cid.sdk.domain.VCSTag
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.newsclub.net.unix.AFSocketFactory
import org.newsclub.net.unix.AFUNIXSocketAddress
import java.io.File
import java.io.IOException
import java.net.SocketAddress
import java.time.Duration
import kotlin.properties.Delegates

/**
 * The CID SDK Client
 */
class CIDSDK(
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

        // init http client
        var httpClientBuilder = OkHttpClient.Builder()
            .callTimeout(Duration.ofMinutes(connectionTimeout))
			.readTimeout(Duration.ofMinutes(connectionTimeout))
        if (socket != null) {
            val socketAddr: SocketAddress = AFUNIXSocketAddress.of(File(socket!!))
            httpClientBuilder = httpClientBuilder
                .socketFactory(AFSocketFactory.FixedAddressSocketFactory(socketAddr))
        } else if (endpoint != null) {
            // nothing?
        } else {
            throw IllegalArgumentException("auto-detection of CID API failed and no endpoint provided to the SDK")
        }

        // auth interceptor
        httpClient = httpClientBuilder
            .addInterceptor(Interceptor { chain ->
                val original = chain.request()
                val request = original.newBuilder()
                    .header("Authorization", "Bearer $secret")
                    .method(original.method, original.body)
                    .build()
                chain.proceed(request)
            })
            .build()
    }

    private fun getBaseUrl(): String {
        if (socket != null) {
            return "http:"
        }
        if (endpoint != null) {
            return endpoint as String
        }

        throw IOException("invalid configuration")
    }

    fun check() {
        val request = Request.Builder()
            .url(getBaseUrl()+"/health")
            .build();

        httpClient.newCall(request).execute().use { response ->
            handleErrorResponse(response)
            println("healthcheck ok" + (response.body?.string() ?: ""))
        }
    }

	fun project(): ProjectInfo {
		val request = Request.Builder()
			.url(getBaseUrl()+"/project")
			.build();

		httpClient.newCall(request).execute().use { response ->
            handleErrorResponse(response)
			return objectMapper.readValue(response.body!!.string())
		}
	}

	fun env(): Map<String, String> {
		val request = Request.Builder()
			.url(getBaseUrl()+"/env")
			.build();

		httpClient.newCall(request).execute().use { response ->
            handleErrorResponse(response)
			return objectMapper.readValue(response.body!!.string())
		}
	}

	fun modules(): List<ProjectModule> {
		val request = Request.Builder()
			.url(getBaseUrl()+"/module")
			.build();

		httpClient.newCall(request).execute().use { response ->
            handleErrorResponse(response)
			return objectMapper.readValue(response.body!!.string())
		}
	}

	fun currentModule(): ProjectModule {
		val request = Request.Builder()
			.url(getBaseUrl()+"/module/current")
			.build();

		httpClient.newCall(request).execute().use { response ->
            handleErrorResponse(response)
			return objectMapper.readValue(response.body!!.string())
		}
	}

    fun vcsCommits(changes: Boolean = false, limit: Int = 0): List<VCSCommit> {
        val request = Request.Builder()
            .url(getBaseUrl()+"/vcs/commit?limit=$limit&changes=$changes")
            .build();

        httpClient.newCall(request).execute().use { response ->
            handleErrorResponse(response)
            return objectMapper.readValue(response.body!!.string())
        }
    }

    fun vcsCommitByHash(hash: String, changes: Boolean = false): VCSCommit {
        val request = Request.Builder()
            .url(getBaseUrl()+"/vcs/commit/$hash?changes=$changes")
            .build();

        httpClient.newCall(request).execute().use { response ->
            handleErrorResponse(response)
            return objectMapper.readValue(response.body!!.string())
        }
    }

    fun vcsTags(): List<VCSTag> {
        val request = Request.Builder()
            .url(getBaseUrl()+"/vcs/tag")
            .build();

        httpClient.newCall(request).execute().use { response ->
            handleErrorResponse(response)
            return objectMapper.readValue(response.body!!.string())
        }
    }

    fun vcsReleases(type: String?): List<VCSRelease> {
        val request = Request.Builder()
            .url(getBaseUrl()+"/vcs/release?type="+type)
            .build();

        httpClient.newCall(request).execute().use { response ->
            handleErrorResponse(response)
            return objectMapper.readValue(response.body!!.string())
        }
    }

    fun executeCommand(workDir: String? = null, command: String, captureOutput: Boolean): CommandExecutionResult {
        val reqBody = CommandExecution(workDir = workDir, command = command, captureOutput = captureOutput)
        val request = Request.Builder()
            .url(getBaseUrl()+"/command")
            .post(objectMapper.writeValueAsString(reqBody).toRequestBody(jsonMediaType))
            .build()

        httpClient.newCall(request).execute().use { response ->
            handleErrorResponse(response)
            return objectMapper.readValue(response.body!!.string())
        }
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

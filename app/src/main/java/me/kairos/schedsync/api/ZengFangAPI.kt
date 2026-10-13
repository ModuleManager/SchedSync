package me.kairos.schedsync.api

import android.util.Base64
import android.util.Log
import com.google.gson.JsonParser
import io.ktor.client.*
import io.ktor.client.engine.android.*
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.cookies.*
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.kairos.schedsync.data.json.class_json.ClassInfo
import org.jsoup.Jsoup
import java.math.BigInteger
import java.security.KeyFactory
import java.security.spec.RSAPublicKeySpec
import javax.crypto.Cipher

/**
 * 正方教务系统 API 客户端。
 *
 * 负责处理与正方教务网的网络交互，包括登录认证、RSA密码加密、CSRF令牌提取、
 * 验证码刷新以及课表和班级数据的拉取。
 */
object ZengFangAPI {
    private const val TAG = "ZhengfangClient"
    private const val BASEURL = "http://syjw.wsyu.edu.cn"

    private const val LOGIN_URL = "$BASEURL/xtgl/login_slogin.html"
    private const val LOGOUT_URL = "$BASEURL/xtgl/login_logout.html"
    private const val PUBLIC_KEY_URL = "$BASEURL/xtgl/login_getPublicKey.html"
    private const val KAPTCHA_URL = "$BASEURL/kaptcha"

    @Volatile
    private var csrfToken: String = ""

    @Volatile
    private var isLoggedInState: Boolean = false

    private val cookieStorage = AcceptAllCookiesStorage()

    private val client = HttpClient(Android) {
        install(HttpCookies) {
            storage = cookieStorage
        }
        install(HttpTimeout) {
            requestTimeoutMillis = 15000
            connectTimeoutMillis = 10000
            socketTimeoutMillis = 15000
        }
        followRedirects = true
        defaultRequest {
            header(
                HttpHeaders.UserAgent,
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
            )
            header(
                HttpHeaders.Accept,
                "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8"
            )
            header(HttpHeaders.AcceptLanguage, "zh-CN,zh;q=0.9")
        }
    }

    /**
     * 获取当前是否处于登录状态。
     *
     * @return 若已成功登录教务系统且 Session 有效则返回 true，否则返回 false
     */
    fun checkIsLoggedIn(): Boolean = isLoggedInState

    /**
     * 执行正方教务系统登录流程。
     *
     * @param sid 用户学号
     * @param password 明文密码
     * @param captcha 验证码文本（可选）
     * @return [LoginResult] 登录状态结果对象
     */
    suspend fun login(
        sid: String,
        password: String,
        captcha: String? = null
    ): LoginResult = withContext(Dispatchers.IO) {
        try {
            val loginHtmlResponse = client.get(LOGIN_URL)

            val finalUrl = loginHtmlResponse.request.url.toString()
            if (finalUrl.contains("index") || finalUrl.contains("main")) {
                Log.i(TAG, "已处于登录状态 (URL: $finalUrl)")
                isLoggedInState = true
                return@withContext LoginResult.Success
            }

            val html = loginHtmlResponse.bodyAsText()
            if (html.contains("index_initMenu.html") || html.contains("个人信息") || html.contains("注销")) {
                Log.i(TAG, "已处于登录状态 (Content)")
                isLoggedInState = true
                return@withContext LoginResult.Success
            }

            val doc = Jsoup.parse(html)
            csrfToken = doc.select("#csrftoken").attr("value")

            if (csrfToken.isEmpty()) {
                Log.e(TAG, "获取 csrfToken 失败")
                return@withContext LoginResult.Failure("获取安全令牌失败，请重试")
            }

            val needsCaptcha = doc.select("input#yzm").isNotEmpty()

            if (needsCaptcha && captcha.isNullOrBlank()) {
                val captchaResponse = client.get(KAPTCHA_URL)
                val captchaBytes = captchaResponse.readRawBytes()
                return@withContext LoginResult.CaptchaRequired(captchaBytes)
            }

            val keyResponse = client.get(PUBLIC_KEY_URL) {
                header("X-Requested-With", "XMLHttpRequest")
                header(HttpHeaders.Accept, "application/json, text/javascript, */*; q=0.01")
            }

            val keyJsonStr = keyResponse.bodyAsText()
            val jsonElement = try {
                JsonParser.parseString(keyJsonStr)
            } catch (e: Exception) {
                Log.e(TAG, "公钥解析异常", e)
                return@withContext LoginResult.Failure("服务器响应格式异常")
            }

            if (!jsonElement.isJsonObject) {
                return@withContext LoginResult.Failure("登录状态异常")
            }

            val keyObj = jsonElement.asJsonObject
            val modulus = keyObj.get("modulus")?.asString ?: ""
            val exponent = keyObj.get("exponent")?.asString ?: ""

            if (modulus.isBlank() || exponent.isBlank()) {
                return@withContext LoginResult.Failure("获取加密公钥失败，请重试")
            }

            val encryptedPassword = encryptPassword(password, modulus, exponent)

            val response = client.submitForm(
                url = LOGIN_URL,
                formParameters = parameters {
                    append("csrftoken", csrfToken)
                    append("yhm", sid)
                    append("mm", encryptedPassword)
                    if (!captcha.isNullOrBlank()) {
                        append("yzm", captcha)
                    }
                }
            )

            val loginResHtml = response.bodyAsText()
            val loginDoc = Jsoup.parse(loginResHtml)
            val tips = loginDoc.select("p#tips").text()

            if (tips.isNotEmpty()) {
                Log.e(TAG, "登录失败: $tips")
                return@withContext LoginResult.Failure(tips)
            }

            Log.i(TAG, "登录成功！")
            isLoggedInState = true
            return@withContext LoginResult.Success

        } catch (e: Exception) {
            Log.e(TAG, "登录异常", e)
            return@withContext LoginResult.Failure("网络连接异常，请检查网络设置或稍后重试")
        }
    }

    /**
     * 刷新并获取新的图形验证码字节数组。
     *
     * @return 验证码图片的字节数组，若网络请求失败则返回 null
     */
    suspend fun refreshCaptcha(): ByteArray? = withContext(Dispatchers.IO) {
        try {
            val captchaResponse = client.get(KAPTCHA_URL)
            captchaResponse.readRawBytes()
        } catch (e: Exception) {
            Log.e(TAG, "刷新验证码失败", e)
            null
        }
    }

    /**
     * 获取特定班级的课表 JSON 响应数据。
     *
     * @param year 学年 (例如: 2026)
     * @param term 学期 (1 或 2)
     * @param selectClass 目标班级信息对象
     * @return 班级课表 JSON 文本，请求失败或 Session 失效时返回 null
     */
    suspend fun getSchedule(year: Int, term: Int, selectClass: ClassInfo): String? =
        withContext(Dispatchers.IO) {
            val mappedTerm = term * term * 3
            val url = "$BASEURL/kbdy/bjkbdy_cxBjKb.html?gnmkdm=N214505"

            try {
                val response = client.submitForm(
                    url = url,
                    formParameters = parameters {
                        append("xnm", year.toString())
                        append("xqm", mappedTerm.toString())
                        append("njdm_id", year.toString())
                        append("zyh_id", selectClass.zyhId)
                        append("bh_id", selectClass.bhId)
                        append("tjkbzdm", "1")
                        append("tjkbzxsdm", "0")
                    }
                ) {
                    header("X-Requested-With", "XMLHttpRequest")
                    header(
                        HttpHeaders.Referrer,
                        "$BASEURL/kbdy/bjkbdy_cxBjkbdyIndex.html?gnmkdm=N214505&layout=default"
                    )
                }

                val jsonStr = response.bodyAsText()
                val finalUrl = response.request.url.toString()

                if (finalUrl.contains("login") || jsonStr.contains("<title>用户登录</title>")) {
                    Log.w(TAG, "Session 失效，返回登录页: $finalUrl")
                    isLoggedInState = false
                    null
                } else {
                    jsonStr
                }
            } catch (e: Exception) {
                Log.e(TAG, "获取班级课表异常", e)
                null
            }
        }

    /**
     * 获取当前登录用户的个人课表 JSON 响应数据。
     *
     * @param year 学年 (例如: 2026)
     * @param term 学期 (1 或 2)
     * @return 个人课表 JSON 文本，请求失败或 Session 失效时返回 null
     */
    suspend fun getPersonalSchedule(year: Int, term: Int): String? = withContext(Dispatchers.IO) {
        val mappedTerm = term * term * 3
        val url = "$BASEURL/kbcx/xskbcx_cxXsgrkb.html?gnmkdm=N2151"

        try {
            val response = client.submitForm(
                url = url,
                formParameters = parameters {
                    append("xnm", year.toString())
                    append("xqm", mappedTerm.toString())
                    append("kzlx", "ck")
                }
            ) {
                header("X-Requested-With", "XMLHttpRequest")
                header(
                    HttpHeaders.Referrer,
                    "$BASEURL/kbcx/xskbcx_cxXskbIndex.html?gnmkdm=N2151&layout=default"
                )
            }

            val jsonStr = response.bodyAsText()
            val finalUrl = response.request.url.toString()

            if (finalUrl.contains("login") || jsonStr.contains("<title>用户登录</title>")) {
                Log.w(TAG, "Session 失效，返回登录页: $finalUrl")
                isLoggedInState = false
                null
            } else {
                jsonStr
            }
        } catch (e: Exception) {
            Log.e(TAG, "获取个人课表异常", e)
            null
        }
    }

    /**
     * 获取指定年级的班级列表 JSON 响应数据。
     *
     * @param year 年级/学年 (例如: 2026)
     * @return 班级列表 JSON 字符串，请求失败或 Session 失效时返回 null
     */
    suspend fun getInformation(year: Int): String? = withContext(Dispatchers.IO) {
        val url = "$BASEURL/xtgl/comm_cxBjdmList.html"

        try {
            val response: HttpResponse = client.get(url) {
                parameter("njdm_id", year.toString())
                parameter("gnmkdm", "N214505")
                header("X-Requested-With", "XMLHttpRequest")
                header(
                    HttpHeaders.Referrer,
                    "$BASEURL/kbdy/bjkbdy_cxBjkbdyIndex.html?gnmkdm=N214505&layout=default"
                )
            }

            val jsonStr = response.bodyAsText()
            val finalUrl = response.request.url.toString()

            if (finalUrl.contains("login") || jsonStr.contains("<title>用户登录</title>")) {
                Log.w(TAG, "Session 失效，返回登录页: $finalUrl")
                isLoggedInState = false
                null
            } else {
                jsonStr
            }
        } catch (e: Exception) {
            Log.e(TAG, "获取班级信息异常", e)
            null
        }
    }

    /**
     * 发送退出登录请求并注销会话。
     */
    suspend fun logout() = withContext(Dispatchers.IO) {
        if (!isLoggedInState) {
            Log.i(TAG, "未处于登录状态，无需发送退出登录请求")
            return@withContext
        }
        try {
            client.get(LOGOUT_URL)
        } catch (e: Exception) {
            Log.e(TAG, "退出登录请求失败", e)
        } finally {
            csrfToken = ""
            isLoggedInState = false
        }
    }

    /**
     * 使用正方教务系统提供的 RSA 公钥模数和指数加密明文密码。
     *
     * @param password 明文密码
     * @param modulusB64 Base64 编码的 RSA 模数
     * @param exponentB64 Base64 编码的 RSA 指数
     * @return Base64 编码的密文密码字符串
     */
    private fun encryptPassword(password: String, modulusB64: String, exponentB64: String): String {
        val cleanModulus = modulusB64.replace("\n", "").replace("\r", "").trim()
        val cleanExponent = exponentB64.replace("\n", "").replace("\r", "").trim()

        val modulusBytes = Base64.decode(cleanModulus, Base64.DEFAULT)
        val exponentBytes = Base64.decode(cleanExponent, Base64.DEFAULT)

        val modulus = BigInteger(1, modulusBytes)
        val exponent = BigInteger(1, exponentBytes)

        val spec = RSAPublicKeySpec(modulus, exponent)
        val factory = KeyFactory.getInstance("RSA")
        val pubKey = factory.generatePublic(spec)

        val cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")
        cipher.init(Cipher.ENCRYPT_MODE, pubKey)
        val encrypted = cipher.doFinal(password.toByteArray(Charsets.UTF_8))

        return Base64.encodeToString(encrypted, Base64.NO_WRAP)
    }
}

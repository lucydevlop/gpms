package io.glnt.gpms.common.utils

//import me.kuku.miraiplugin.data.QQLogin
//import me.kuku.miraiplugin.qqLogins
//import net.mamoe.mirai.Bot
//import net.mamoe.mirai.message.data.*
import java.net.URLEncoder
import kotlin.random.Random

object BotUtils {
    fun shortUrl(url: String): String{
        val newUrl = if (url.startsWith("http")) url
        else "http://$url"
        return if (url.contains("iheit.com") || url.contains("kuku.me") || url.contains("workers.dev")) {
            val response = OkHttpClientUtils.get("https://uxy.me/api.php?url=${URLEncoder.encode(newUrl, "utf-8")}")
            val jsonObject = OkHttpClientUtils.getJson(response)
            val shortUrl = jsonObject.getString("shorturl")
            shortUrl ?: "生成失败！！！"
        }else {
            val response = OkHttpClientUtils.get("https://api.kuku.me/tool/shorturl?url=${URLEncoder.encode(newUrl, "utf-8")}")
            OkHttpClientUtils.getStr(response)
        }
    }

    fun randomStr(len: Int): String{
        val str = "abcdefghijklmnopqrstuvwxyz0123456789"
        val sb = StringBuilder()
        for (i in (0 until len))
            sb.append(str[Random.nextInt(0, str.length)])
        return sb.toString()
    }

    fun randomNum(len: Int): String{
        val sb = StringBuilder()
        for (i in (0 until len)){
            sb.append(Random.nextInt(10))
        }
        return sb.toString()
    }

    fun regex(regex: String, text: String): String? {
        val r = Regex(regex)
        val find = r.find(text)
        return find?.value
    }

    fun regex(first: String, last: String , text: String): String? {
        val regex = "(?<=$first).*?(?=$last)"
        return this.regex(regex, text)
    }

    fun convertBool(str: String): Boolean{
        val list = listOf("开", "启", "打开")
        list.forEach {
            if (str in it) return true
        }
        return false
    }

//    fun messageToJsonArray(messageChain: MessageChain): JSONArray {
//        val jsonArray = JSONArray()
//        messageChain.forEachContent {
//            val jsonObject = JSONObject()
//            when (it){
//                is LightApp -> {
//                    jsonObject["type"] = "json"
//                    jsonObject["content"] = it.content
//                }
//                is ServiceMessage -> {
//                    jsonObject["type"] = "xml"
//                    jsonObject["serviceId"] = it.serviceId
//                    jsonObject["content"] = it.content
//                }
//                is FlashImage -> {
//                    jsonObject["type"] = "flashImage"
//                    jsonObject["content"] = it.image.imageId
//                }
//                is Image -> {
//                    jsonObject["type"] = "image"
//                    jsonObject["content"] = it.imageId
//                }
//                is Voice -> {
//                    jsonObject["type"] = "voice"
//                    jsonObject["content"] = it.url
//                }
//                is Face -> {
//                    jsonObject["type"] = "face"
//                    jsonObject["content"] = it.id
//                }
//                is PlainText -> {
//                    jsonObject["type"] = "text"
//                    jsonObject["content"] = it.content
//                }
//                is At -> {
//                    jsonObject["type"] = "at"
//                    jsonObject["content"] = it.target
//                }
//                else -> return@forEachContent
//            }
//            jsonArray.add(jsonObject)
//        }
//        return jsonArray
//    }
//
//    suspend fun jsonArrayToMessage(jsonArray: JSONArray, group: net.mamoe.mirai.contact.Group): MessageChain {
//        val empty = EmptyMessageChain
//        var message: MessageChain = empty
//        for (i in jsonArray.indices){
//            val jsonObject = jsonArray.getJSONObject(i)
//            val content = jsonObject.getString("content")
//            message = when (jsonObject.getString("type")){
//                "json" -> message.plus(LightApp(content))
//                "xml" -> {
//                    message.plus(ServiceMessage(jsonObject.getInteger("serviceId"), content))
//                }
//                "flashImage" -> message.plus(Image(content).flash())
//                "image" -> message.plus(Image(content))
//                "voice" -> message.plus(group.uploadVoice(OkHttpClientUtils.getStream(content)))
//                "face" -> message.plus(Face(content.toInt()))
//                "text" -> message.plus(PlainText(content))
//                "at" -> message.plus(At(group.members[content.toLong()]))
//                else -> continue
//            }
//        }
//        return message
//    }
//
//    fun toQQLogin(bot: Bot): QQLogin {
//        var sKey = ""
//        var superKey = ""
//        var psKey = ""
//        var groupPsKey = ""
//        for (field in bot::class.java.superclass.declaredFields) {
//            if (field.name == "client") {
//                field.isAccessible = true
//                val client = field[bot]
//                for (cf in field.type.declaredFields) {
//                    if (cf.name == "wLoginSigInfo") {
//                        cf.isAccessible = true
//                        val lsi = cf[client]
//                        val lsiJS = JSON.toJSONString(lsi)
//                        val lsiJO = JSON.parseObject(lsiJS)
//                        sKey = String(Base64.getDecoder().decode(lsiJO.getJSONObject("sKey").getString("data")))
//                        superKey = String(Base64.getDecoder().decode(lsiJO.getString("superKey")))
//
//                        val psKeys = lsiJO.getJSONObject("psKeyMap")
//
//                        for (k in psKeys.keys) {
//                            val value = String(
//                                Base64.getDecoder().decode(psKeys.getJSONObject(k).getString("data"))
//                                    ?: continue)
//                            if (k == "qzone.qq.com"){
//                                psKey = value
//                            }
//                            if (k == "qun.qq.com"){
//                                groupPsKey = value
//                            }
//                        }
//                    }
//                }
//            }
//        }
//        return QQLogin(bot.id, 0, "", sKey, psKey, groupPsKey, superKey, QQUtils.getToken(superKey).toString(), "", true)
//    }

}

fun StringBuilder.removeSuffixLine() = this.removeSuffix("\n")
//fun Bot.getQQLogin() = qqLogins[this.id]!!
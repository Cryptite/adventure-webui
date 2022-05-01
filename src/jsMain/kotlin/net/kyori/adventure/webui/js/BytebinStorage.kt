package net.kyori.adventure.webui.js

import kotlinx.browser.window
import kotlinx.serialization.encodeToString
import net.kyori.adventure.webui.Serializers
import net.kyori.adventure.webui.URL_API
import net.kyori.adventure.webui.URL_MINI_SHORTEN
import net.kyori.adventure.webui.tryDecodeFromString
import net.kyori.adventure.webui.websocket.Combined
import net.kyori.adventure.webui.websocket.Placeholders
import org.w3c.dom.HTMLTextAreaElement
import org.w3c.dom.WebSocket
import org.w3c.dom.url.URLSearchParams
import org.w3c.fetch.Headers
import org.w3c.fetch.RequestInit
import kotlin.js.Promise
import kotlin.js.json

private const val BYTEBIN_INSTANCE: String = "https://bytebin.lucko.me"

/** Since it's sorta undecided whether to use client- or server-sided bytebin integration, here's just a toggle
 * for it. When using server-side integration, the server essentially just acts pretty much like a direct proxy
 * to bytebin, as there is no additional logic needed. The client could also do all of it on its own much more directly,
 * however there are also some downsides to that...
 */
private const val CLIENTSIDE_BYTEBIN: Boolean = true

public fun bytebinStore(payload: Combined): Promise<String?> {
    val request = if (CLIENTSIDE_BYTEBIN) {
        window.fetch(
            "$BYTEBIN_INSTANCE/post",
            RequestInit(
                method = "POST",
                headers = Headers(json("Content-Type" to "application/json; charset=UTF-8")),
                body = Serializers.json.encodeToString(payload)
            )
        ).then { response -> response.headers.get("Location") }
    } else {
        window.postPacket("$URL_API$URL_MINI_SHORTEN", payload)
            .then { response -> response.text() }
            .then { i -> i } // :I
    }

    return request
}

private fun bytebinLoad(code: String): Promise<Combined?> {
    // TODO(rymiel): handle the 404 case
    val request = if (CLIENTSIDE_BYTEBIN) {
        window.fetch(
            "$BYTEBIN_INSTANCE/$code",
            RequestInit(method = "GET")
        )
    } else {
        window.fetch(
            "$URL_API$URL_MINI_SHORTEN?code=$code",
            RequestInit(method = "GET")
        )
    }

    return request
        .then { response -> response.text() }
        .then { text -> Serializers.json.tryDecodeFromString(text) }
}

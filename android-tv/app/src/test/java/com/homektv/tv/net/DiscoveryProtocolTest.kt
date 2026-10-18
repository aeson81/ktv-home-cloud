package com.homektv.tv.net

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DiscoveryProtocolTest {
    @Test
    fun parsesValidDiscoveryResponseUsingPacketSourceAddress() {
        val payload = """{"service":"home-ktv","protocolVersion":1,"name":"客厅KTV","port":12345}"""
        assertEquals(
            DiscoveredServer("192.168.1.20:12345", "客厅KTV"),
            DiscoveryProtocol.parseResponse(payload, "192.168.1.20"),
        )
    }

    @Test
    fun rejectsWrongServiceVersionAndPort() {
        assertNull(DiscoveryProtocol.parseResponse("""{"service":"other","protocolVersion":1,"port":8080}""", "10.0.0.2"))
        assertNull(DiscoveryProtocol.parseResponse("""{"service":"home-ktv","protocolVersion":2,"port":8080}""", "10.0.0.2"))
        assertNull(DiscoveryProtocol.parseResponse("""{"service":"home-ktv","protocolVersion":1,"port":70000}""", "10.0.0.2"))
    }
}

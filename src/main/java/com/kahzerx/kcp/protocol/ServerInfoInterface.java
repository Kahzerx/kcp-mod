package com.kahzerx.kcp.protocol;

import com.kahzerx.kcp.protocol.Protocols;

public interface ServerInfoInterface {
    Protocols getProtocol();

    void setProtocol(Protocols protocol);
}

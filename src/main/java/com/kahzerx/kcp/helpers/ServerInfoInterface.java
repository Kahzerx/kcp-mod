package com.kahzerx.kcp.helpers;

import com.kahzerx.kcp.protocol.Protocols;

public interface ServerInfoInterface {
    Protocols getProtocol();

    void setProtocol(Protocols protocol);
}

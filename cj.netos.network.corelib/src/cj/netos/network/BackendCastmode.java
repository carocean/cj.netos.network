package cj.netos.network;

public enum BackendCastmode {
    unicast,
    multicast,
    selectcast,
    forbiddenBackendCastButAllowFrontendUnicast,
    forbiddenBackendCastButAllowFrontendMulticast,
    forbiddenBackendCastButAllowFrontendSelectcast,
}

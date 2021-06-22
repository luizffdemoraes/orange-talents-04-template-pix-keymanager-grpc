package br.com.zup.luiz.interceptor

import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.grpc.protobuf.StatusProto
import io.grpc.Metadata

interface ExceptionHandler<E: Exception> {

    fun handle(e: E): StatusWithDetails

    fun supports(e: Exception): Boolean

    data class StatusWithDetails(val status: Status, val metadata: Metadata = Metadata()){
        constructor(se: StatusRuntimeException): this(se.status, se.trailers ?: Metadata())
        constructor(sp: com.google.rpc.Status) : this(StatusProto.toStatusRuntimeException(sp))

        fun asRuntimeException(): StatusRuntimeException {
            return status.asRuntimeException()
        }
    }
}
package br.com.zup.luiz.remove

import br.com.zup.luiz.KeymanagerRemoveGrpcServiceGrpc
import br.com.zup.luiz.RemoveChavePixRequest
import br.com.zup.luiz.RemoveChavePixResponse
import br.com.zup.luiz.interceptor.ErrorHandler
import io.grpc.stub.StreamObserver
import javax.inject.Inject
import javax.inject.Singleton

@ErrorHandler
@Singleton
class RemoveChaveEndpoint(@Inject private val service : RemoveChavePixService)
    : KeymanagerRemoveGrpcServiceGrpc.KeymanagerRemoveGrpcServiceImplBase() {

    override fun remove(
        request: RemoveChavePixRequest,
        responseObserver: StreamObserver<RemoveChavePixResponse>
    ) {

        service.remove(request.pixId, request.clienteId)

        val chaveRemovida = RemoveChavePixResponse.newBuilder()
            .setClienteId(request.clienteId)
            .setPixId(request.pixId)
            .build()

        responseObserver.onNext(chaveRemovida)
        responseObserver.onCompleted()
    }
}
package br.com.zup.luiz.carrega

import br.com.zup.luiz.CarregaChavePixRequest
import br.com.zup.luiz.CarregaChavePixResponse
import br.com.zup.luiz.KeyManagerCarregaGrpcServiceGrpc
import br.com.zup.luiz.integration.bcb.BancoCentralClient
import br.com.zup.luiz.interceptor.ErrorHandler
import br.com.zup.luiz.pix.ChavePixRepository
import io.grpc.stub.StreamObserver
import javax.inject.Inject
import javax.inject.Singleton
import javax.validation.Validator

@ErrorHandler // 1
@Singleton
class CarregaChaveEndpoint(
    @Inject private val repository: ChavePixRepository, // 1
    @Inject private val bcbClient: BancoCentralClient, // 1
    @Inject private val validator: Validator,
) : KeyManagerCarregaGrpcServiceGrpc.KeyManagerCarregaGrpcServiceImplBase() { // 1

    // 9
    override fun carrega(
        request: CarregaChavePixRequest, // 1
        responseObserver: StreamObserver<CarregaChavePixResponse>, // 1
    ) {

        val filtro = request.toModel(validator) // 2
        val chaveInfo = filtro.filtra(repository = repository, bcbClient = bcbClient)

        responseObserver.onNext(CarregaChavePixResponseConverter().convert(chaveInfo)) // 1
        responseObserver.onCompleted()
    }
}
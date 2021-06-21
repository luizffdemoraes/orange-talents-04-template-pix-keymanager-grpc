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
    @Inject private val repository: ChavePixRepository,
    @Inject private val bcbClient: BancoCentralClient,
    @Inject private val validator: Validator,
) : KeyManagerCarregaGrpcServiceGrpc.KeyManagerCarregaGrpcServiceImplBase() {


    override fun carrega(
        request: CarregaChavePixRequest,
        responseObserver: StreamObserver<CarregaChavePixResponse>,
    ) {

        val filtro = request.toModel(validator)
        val chaveInfo = filtro.filtra(repository = repository, bcbClient = bcbClient)

        responseObserver.onNext(CarregaChavePixResponseConverter().convert(chaveInfo))
        responseObserver.onCompleted()
    }
}
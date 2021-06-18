package br.com.zup.luiz.registra

import br.com.zup.luiz.KeyManagerRegistraGrpcServiceGrpc
import br.com.zup.luiz.RegistraChavePixRequest
import br.com.zup.luiz.RegistraChavePixResponse
import io.grpc.stub.StreamObserver
import javax.inject.Inject
import javax.inject.Singleton

//@ErrorHandler //anotacao criada para fazer com que meu interceptor intercepte os metodos dessa classe
@Singleton
//injetamos nosso service, para realizar as validacoes da bean validation e salvar nossa chave
class RegistraChaveEndpoint(@Inject private val service: NovaChavePixService)
    : KeyManagerRegistraGrpcServiceGrpc.KeyManagerRegistraGrpcServiceImplBase() {


    override fun registra(
        request: RegistraChavePixRequest,
        responseObserver: StreamObserver<RegistraChavePixResponse>
    ) {

        //utilizamos extension function para gerar um DTO de uma nova chave pix
        val novaChave = request.toModel()

        //com o dto criado, podemos chamar nosso servico, que alem de transformar
        //na nossa entidade modelo, irá salvar nossa chave no banco
        //mas antes ele irá validar os campos
        val chaveCriada = service.registra(novaChave)

        responseObserver.onNext(RegistraChavePixResponse.newBuilder()
            .setClienteId(chaveCriada.clienteId.toString())
            .setPixId(chaveCriada.id.toString())
            .build())
        responseObserver.onCompleted()
    }
}
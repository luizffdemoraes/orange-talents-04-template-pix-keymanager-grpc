package br.com.zup.luiz.remove

import br.com.zup.luiz.KeyManagerRemoveGrpcServiceGrpc
import br.com.zup.luiz.RemoveChavePixRequest
import br.com.zup.luiz.TipoDeConta
import br.com.zup.luiz.integration.bcb.BancoCentralClient
import br.com.zup.luiz.integration.bcb.DeletePixKeyRequest
import br.com.zup.luiz.integration.bcb.DeletePixKeyResponse
import br.com.zup.luiz.pix.ChavePix
import br.com.zup.luiz.pix.ChavePixRepository
import br.com.zup.luiz.pix.ContaAssociada
import br.com.zup.luiz.pix.TipoDeChave
import io.grpc.ManagedChannel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Bean
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.http.HttpResponse
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import java.time.LocalDateTime
import java.util.*
import javax.inject.Inject

@MicronautTest(transactional = false)
internal class RemoveChaveEndpointTest(
    @Inject val repository: ChavePixRepository,
    @Inject val grpcClient: KeyManagerRemoveGrpcServiceGrpc.KeyManagerRemoveGrpcServiceBlockingStub,
) {
    @Inject
    lateinit var bcbClient: BancoCentralClient

    lateinit var CHAVE_EXISTENTE: ChavePix

    @BeforeEach
    fun setup() {
        CHAVE_EXISTENTE = repository.save(chave(
            tipoDeChave = TipoDeChave.EMAIL,
            chave = "rponte@gmail.com",
            clienteId = UUID.randomUUID()
        ))
    }

    @AfterEach
    fun cleanUp() {
        repository.deleteAll()
    }

    @Test
    fun `deve remover chave pix existente`() {
        // cenário
        `when`(bcbClient.delete("rponte@gmail.com", DeletePixKeyRequest("rponte@gmail.com")))
            .thenReturn(HttpResponse.ok(
                DeletePixKeyResponse(key = "rponte@gmail.com",
                participant = ContaAssociada.ITAU_UNIBANCO_ISPB,
                deletedAt = LocalDateTime.now())
            )
            )

        // ação
        val response = grpcClient.remove(RemoveChavePixRequest.newBuilder()
            .setPixId(CHAVE_EXISTENTE.id.toString())
            .setClienteId(CHAVE_EXISTENTE.clienteId.toString())
            .build())

        // validação
        with(response) {
            assertEquals(CHAVE_EXISTENTE.id.toString(), pixId)
            assertEquals(CHAVE_EXISTENTE.clienteId.toString(), clienteId)
        }
    }

    @Test
    fun `nao deve remover chave pix existente quando ocorrer algum erro no servico do BCB`() {
        // cenário
        `when`(bcbClient.delete("rponte@gmail.com", DeletePixKeyRequest("rponte@gmail.com")))
            .thenReturn(HttpResponse.unprocessableEntity())

        // ação
        val thrown = assertThrows<StatusRuntimeException> {
            grpcClient.remove(RemoveChavePixRequest.newBuilder()
                .setPixId(CHAVE_EXISTENTE.id.toString())
                .setClienteId(CHAVE_EXISTENTE.clienteId.toString())
                .build())
        }

        // validação
        with(thrown) {
            assertEquals(Status.FAILED_PRECONDITION.code, status.code)
            assertEquals("Erro ao remover chave Pix no Banco Central do Brasil (BCB)", status.description)
        }
    }


        @Test
    internal fun `nao deve remover chave pix quando nao existente`() {
        val pixIdNaoExistente = UUID.randomUUID().toString()

        // acao
        val ex = assertThrows<StatusRuntimeException> {
            grpcClient.remove(RemoveChavePixRequest.newBuilder()
                .setPixId(pixIdNaoExistente)
                .setClienteId(CHAVE_EXISTENTE.clienteId.toString())
                .build())
        }

        // validacao
        with(ex) {
            assertEquals(Status.NOT_FOUND.code, status.code)
            assertEquals("Chave pix não encontrada ou não pertence ao usuário", status.description)
        }

    }


        @Test
    fun `nao deve remover chave pix quando existente porem pertence a outro cliente`() {
        //cenario
        val chaveOutroCliente = repository.save(
            chave(
                tipoDeChave = TipoDeChave.CPF,
                chave = "34270951001",
                clienteId = UUID.randomUUID()
            )
        )

        //acao
        val thrown = assertThrows<StatusRuntimeException> {
            grpcClient.remove(
                RemoveChavePixRequest.newBuilder()
                    .setPixId(CHAVE_EXISTENTE.id.toString())
                    .setClienteId(chaveOutroCliente.clienteId.toString())
                    .build()
            )
        }

        //validacao
        with(thrown) {
            assertEquals(Status.NOT_FOUND.code,status.code)
            assertEquals("Chave pix não encontrada ou não pertence ao usuário",status.description)
        }
    }

    @Test
    fun `nao deve remover chave pix quando parametros invalidos`() {
        // ação
        val thrown = assertThrows<StatusRuntimeException> {
            grpcClient.remove(RemoveChavePixRequest.newBuilder().build())
        }

        // validação
        with(thrown) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals("Dados inválidos", status.description)
        }
    }

    @MockBean(BancoCentralClient::class)
    fun bcbClient(): BancoCentralClient? {
        return mock(BancoCentralClient::class.java)
    }

    @Factory
    class Clients  {
        @Bean
        fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel): KeyManagerRemoveGrpcServiceGrpc.KeyManagerRemoveGrpcServiceBlockingStub? {
            return KeyManagerRemoveGrpcServiceGrpc.newBlockingStub(channel)
        }
    }

    private fun chave(
        tipoDeChave: TipoDeChave,
        chave: String = UUID.randomUUID().toString(),
        clienteId: UUID = UUID.randomUUID(),
    ): ChavePix {
        return ChavePix(
            clienteId = clienteId,
            tipoDeChave = tipoDeChave,
            chave = chave,
            tipoDeConta = TipoDeConta.CONTA_CORRENTE,
            conta = ContaAssociada(
                instituicao = "UNIBANCO ITAU",
                nomeDoTitular = "Rafael Ponte",
                cpfDoTitular = "12345678900",
                agencia = "1218",
                numeroDaConta = "123456"
            )
        )
    }
}
package br.com.zup.luiz.lista


import br.com.zup.luiz.*
import br.com.zup.luiz.carrega.CarregaChaveEndpointTest
import br.com.zup.luiz.exceptions.violations
import br.com.zup.luiz.integration.bcb.*
import br.com.zup.luiz.pix.ChavePix

import br.com.zup.luiz.pix.ChavePixRepository
import br.com.zup.luiz.pix.ContaAssociada
import br.com.zup.luiz.pix.TipoDeChave
import org.junit.jupiter.api.Assertions.*
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
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers
import org.hamcrest.Matchers.containsInAnyOrder
import org.hamcrest.Matchers.hasSize
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import java.time.LocalDateTime
import java.util.*
import javax.inject.Inject

/**
 * TIP: Necessario desabilitar o controle transacional (transactional=false) pois o gRPC Server
 * roda numa thread separada, caso contrário não será possível preparar cenário dentro do método @Test
 */
@MicronautTest(transactional = false)
internal class ListaChavesEndpointTest(
    val repository: ChavePixRepository,
    val grpcClient: KeyManagerListaGrpcServiceGrpc.KeyManagerListaGrpcServiceBlockingStub,
) {
    @Inject
    lateinit var bcbClient: BancoCentralClient
    lateinit var chave1: ChavePix
    lateinit var chave2: ChavePix
    lateinit var chave3: ChavePix
    lateinit var listaDeChaves: List<ChavePix>

    companion object {
        val CLIENTE_ID = UUID.randomUUID()
    }

    /**
     * TIP: por padrão roda numa transação isolada
     */
    @BeforeEach
    fun setup() {
        chave1 = repository.save(chave(tipo = TipoDeChave.EMAIL, chave = "rafael.ponte@zup.com.br", clienteId = CLIENTE_ID))
        chave2 = repository.save(chave(tipo = TipoDeChave.CPF, chave = "41790066644", clienteId = CLIENTE_ID))
        chave3 = repository.save(chave(tipo = TipoDeChave.CELULAR, chave = "+55 9 88888888", clienteId = CLIENTE_ID))
        listaDeChaves = listOf(chave1, chave2, chave3)
    }



    /**
     * TIP: por padrão roda numa transação isolada
     */
    @AfterEach
    fun cleanUp() {
        repository.deleteAll()
    }

    @Test
    fun `deve listar todas as chaves do cliente`() {
        // cenário
        val clienteId = CLIENTE_ID.toString()

        // ação
        val response = grpcClient.lista(ListaChavesPixRequest.newBuilder()
            .setClienteId(clienteId)
            .build())

        // validação

        with(response.chavesList) {
            forEachIndexed{index,elem->
                assertEquals(this.get(index).chave,listaDeChaves.get(index).chave)
            }
        }

    }

    /**
     * XXX: será que precisamos disso dado que não existe branch na query?
     */
    @Test
    fun `nao deve listar as chaves do cliente quando cliente nao possuir chaves`() {
        // cenário
        val clienteSemChaves = UUID.randomUUID().toString()

        // ação
        val response = grpcClient.lista(ListaChavesPixRequest.newBuilder()
            .setClienteId(clienteSemChaves)
            .build())

        // validação
        assertEquals(0, response.chavesCount)
    }

    @Test
    fun `nao deve listar todas as chaves do cliente quando clienteId for invalido`() {
        // cenário
        val clienteIdInvalido = ""

        // ação
        val thrown = assertThrows<StatusRuntimeException> {
            grpcClient.lista(ListaChavesPixRequest.newBuilder()
                .setClienteId(clienteIdInvalido)
                .build())
        }

        // validação
        with(thrown) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals("Cliente ID não pode ser nulo ou vazio", status.description)
        }
    }

    @Factory
    class Clients  {
        @Bean
        fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel): KeyManagerListaGrpcServiceGrpc.KeyManagerListaGrpcServiceBlockingStub? {
            return KeyManagerListaGrpcServiceGrpc.newBlockingStub(channel)
        }
    }

    private fun chave(
        tipo: TipoDeChave,
        chave: String = UUID.randomUUID().toString(),
        clienteId: UUID = UUID.randomUUID(),
    ): ChavePix {
        return ChavePix(
            clienteId = clienteId,
            tipoDeChave = tipo,
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